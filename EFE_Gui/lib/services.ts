"use client"

import axios from "axios"
import { api, requestWithRetry } from "./api"
import { getUser } from "./auth-storage"

export type OtpAction = "REGISTER" | "TRANSFER"

type ApiResponse<T> = {
  success: boolean
  message?: string
  data: T
}

type PageResponse<T> = {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
  last: boolean
}

type LoginData = {
  token: string
  fullName?: string
  refreshToken?: string
  typeToken?: string
  expiresIn?: number
}

type WalletData = {
  walletAddress?: string
}

type BalanceData = {
  balance: number | string
}

type TransactionHistoryData = {
  id: string
  amount: number | string
  description?: string | null
  counterpartyWallet?: string | null
  direction: "IN" | "OUT"
  createdAt: string
}

export type TransactionHistoryItem = {
  id: string
  amount: number
  description?: string
  counterpartyWallet?: string
  direction: "IN" | "OUT"
  createdAt: string
}

type TransactionDetailData = {
  id: string
  type: string
  status: string
  amount: number | string
  fee?: number | string | null
  currency?: string | null
  description?: string | null
  fromWallet?: string | null
  toWallet?: string | null
  direction: "IN" | "OUT"
  counterpartyWallet?: string | null
  externalTransactionId?: string | null
  createdAt: string
  updatedAt?: string | null
}

export type TransactionDetailItem = {
  id: string
  type: string
  status: string
  amount: number
  fee?: number
  currency?: string
  description?: string
  fromWallet?: string
  toWallet?: string
  direction: "IN" | "OUT"
  counterpartyWallet?: string
  externalTransactionId?: string
  createdAt: string
  updatedAt?: string
}

function splitFullName(fullName: string) {
  const parts = fullName.trim().split(/\s+/).filter(Boolean)
  if (parts.length <= 1) {
    return { firstName: parts[0] ?? "", lastName: parts[0] ?? "" }
  }
  return {
    firstName: parts.slice(0, -1).join(" "),
    lastName: parts[parts.length - 1],
  }
}

// ---------------- Auth ----------------

export async function registerDraft(input: {
  fullName: string
  email: string
  password: string
}) {
  const { firstName, lastName } = splitFullName(input.fullName)
  const { data } = await api.post<ApiResponse<unknown>>("/api/v1/auth/register", {
    email: input.email,
    password: input.password,
    firstName,
    lastName,
  })
  return data.data
}

export async function requestOtp(input: { action: OtpAction; email?: string }) {
  const identifier = input.email ?? getUser()?.email
  if (!identifier) throw new Error("Không tìm thấy email để gửi OTP.")

  const { data } = await api.post<ApiResponse<void>>("/api/v1/auth/request-otp", {
    identifier,
    action: input.action,
  })
  return data.data
}

export async function verifyRegisterOtp(input: { email: string; otp: string }) {
  const { data } = await api.post<ApiResponse<void>>("/api/v1/auth/verify-otp", {
    identifier: input.email,
    action: "REGISTER",
    otpCode: input.otp,
  })
  return data.data
}

export async function login(input: { email: string; password: string }) {
  const { data } = await api.post<ApiResponse<LoginData>>(
    "/api/v1/auth/login",
    input,
  )
  return data.data
}

export async function getProfile() {
  const { data } = await api.get<ApiResponse<{ firstName?: string; lastName?: string; email?: string }>>("/api/v1/auth/me")
  return data.data
}

async function ensureWalletExists() {
  try {
    await api.get<ApiResponse<WalletData>>("/api/v1/wallet/")
  } catch (error) {
    if (!axios.isAxiosError(error) || error.response?.status !== 404) throw error
    await api.post<ApiResponse<WalletData>>("/api/v1/wallet/generation")
  }
}

export async function getWalletSummary(): Promise<{ walletAddress: string | null; balance: number }> {
  await ensureWalletExists()

  const { data: walletRes } = await api.get<ApiResponse<WalletData>>("/api/v1/wallet/")
  const { data: balanceRes } = await api.get<ApiResponse<BalanceData>>("/api/v1/wallet/balance")

  const numericBalance =
    typeof balanceRes.data.balance === "number"
      ? balanceRes.data.balance
      : Number(balanceRes.data.balance)

  if (Number.isNaN(numericBalance)) {
    throw new Error("Không đọc được số dư từ máy chủ.")
  }

  return {
    walletAddress: walletRes.data.walletAddress ?? null,
    balance: numericBalance,
  }
}

// ---------------- Wallet ----------------

export type TransferInput = {
  recipient: string
  amount: number
  note?: string
  otp: string
}

/**
 * Executes a money transfer. The idempotencyKey MUST be generated once on the
 * client (UUID v4) and kept fixed across retries so the backend can dedupe and
 * never debit the account twice.
 */
export async function transfer(input: TransferInput, idempotencyKey: string) {
  const identifier = getUser()?.email
  if (!identifier) throw new Error("Không tìm thấy email người dùng để xác thực giao dịch.")

  await api.post<ApiResponse<void>>("/api/v1/auth/verify-transfer-otp", {
    identifier,
    otpCode: input.otp,
    toWalletAddress: input.recipient,
    amount: input.amount,
  })

  const response = await requestWithRetry<ApiResponse<{ status: string; transactionId?: string }>>(
    {
      url: "/api/v1/wallet/transfer",
      method: "POST",
      headers: {
        "Idempotency-Key": idempotencyKey,
      },
      data: {
        toWalletAddress: input.recipient,
        amount: input.amount,
        description: input.note,
      },
    },
    { retries: 3 },
  )
  return response.data
}

export async function getTransactionHistory(
  input: { page?: number; size?: number } = {},
): Promise<TransactionHistoryItem[]> {
  const { page = 0, size = 20 } = input
  const { data } = await api.get<ApiResponse<PageResponse<TransactionHistoryData>>>(
    "/api/v1/wallet/transactions",
    {
      params: { page, size },
    },
  )

  return data.data.content.map((item) => {
    const amount = typeof item.amount === "number" ? item.amount : Number(item.amount)
    if (Number.isNaN(amount)) {
      throw new Error("Không đọc được số tiền giao dịch từ máy chủ.")
    }
    return {
      id: item.id,
      amount,
      description: item.description ?? undefined,
      counterpartyWallet: item.counterpartyWallet ?? undefined,
      direction: item.direction,
      createdAt: item.createdAt,
    }
  })
}

export async function getTransactionDetail(transactionId: string): Promise<TransactionDetailItem> {
  const { data } = await api.get<ApiResponse<TransactionDetailData>>(
    `/api/v1/wallet/transactions/${transactionId}`,
  )
  const item = data.data
  const amount = typeof item.amount === "number" ? item.amount : Number(item.amount)
  const feeRaw = item.fee
  const fee =
    feeRaw == null ? undefined : typeof feeRaw === "number" ? feeRaw : Number(feeRaw)
  if (Number.isNaN(amount) || (fee !== undefined && Number.isNaN(fee))) {
    throw new Error("Không đọc được chi tiết giao dịch từ máy chủ.")
  }

  return {
    id: item.id,
    type: item.type,
    status: item.status,
    amount,
    fee,
    currency: item.currency ?? undefined,
    description: item.description ?? undefined,
    fromWallet: item.fromWallet ?? undefined,
    toWallet: item.toWallet ?? undefined,
    direction: item.direction,
    counterpartyWallet: item.counterpartyWallet ?? undefined,
    externalTransactionId: item.externalTransactionId ?? undefined,
    createdAt: item.createdAt,
    updatedAt: item.updatedAt ?? undefined,
  }
}
