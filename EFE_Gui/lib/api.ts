"use client"

import axios, {
  type AxiosError,
  type AxiosInstance,
  type AxiosRequestConfig,
  type InternalAxiosRequestConfig,
} from "axios"
import { clearToken, getToken } from "./auth-storage"

// Base URL of the EFEWallet backend (API Gateway).
// Point NEXT_PUBLIC_API_BASE_URL at your real server.
const BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"

export const api: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 15000,
  headers: { "Content-Type": "application/json" },
})

// --- Request interceptor: attach JWT automatically ---
api.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getToken()
  if (token) {
    config.headers.set("Authorization", `Bearer ${token}`)
  }
  return config
})

// --- Response interceptor: surface auth failures ---
api.interceptors.response.use(
  (response) => response,
  (error: AxiosError) => {
    if (error.response?.status === 401) {
      clearToken()
    }
    return Promise.reject(error)
  },
)

function isRetryableError(error: unknown): boolean {
  if (!axios.isAxiosError(error)) return false
  // Network timeout, connection aborted, or no response (network down).
  if (error.code === "ECONNABORTED" || error.code === "ETIMEDOUT") return true
  if (!error.response) return true
  // Retry transient gateway errors only.
  const status = error.response.status
  return status === 502 || status === 503 || status === 504
}

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms))

/**
 * Request wrapper that automatically retries on timeout / transient network
 * failures. The SAME config object (including any Idempotency-Key header) is
 * reused on every attempt, so the backend never double-charges.
 */
export async function requestWithRetry<T>(
  config: AxiosRequestConfig,
  options: { retries?: number; backoffMs?: number } = {},
): Promise<T> {
  const { retries = 3, backoffMs = 800 } = options
  let attempt = 0
  let lastError: unknown

  while (attempt <= retries) {
    try {
      const response = await api.request<T>(config)
      return response.data
    } catch (error) {
      lastError = error
      if (attempt === retries || !isRetryableError(error)) break
      // Exponential backoff before retrying with the identical idempotency key.
      await sleep(backoffMs * Math.pow(2, attempt))
      attempt += 1
    }
  }

  throw lastError
}

export function getErrorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as { message?: string; error?: string } | undefined
    if (data?.message) return data.message
    if (data?.error) return data.error
    if (error.code === "ECONNABORTED") return "Yêu cầu quá thời gian chờ. Vui lòng thử lại."
    if (!error.response) return "Không thể kết nối tới máy chủ. Kiểm tra kết nối mạng."
    return `Lỗi máy chủ (${error.response.status}).`
  }
  return "Đã xảy ra lỗi không xác định."
}
