"use client"

import { useEffect, useState } from "react"
import { WindowFrame } from "@/components/window-frame"
import { LoginScreen } from "@/components/auth/login-screen"
import { RegisterScreen } from "@/components/auth/register-screen"
import { RegisterOtpScreen } from "@/components/auth/register-otp-screen"
import { Dashboard } from "@/components/wallet/dashboard"
import { TransactionDetailScreen } from "@/components/wallet/transaction-detail-screen"
import { TransferScreen } from "@/components/wallet/transfer-screen"
import { toast } from "sonner"
import { getErrorMessage } from "@/lib/api"
import { clearToken, getToken, getUser, setUser as persistUser } from "@/lib/auth-storage"
import {
  getTransactionDetail,
  getTransactionHistory,
  getWalletSummary,
  type TransactionDetailItem,
} from "@/lib/services"

type Screen =
  | "login"
  | "register"
  | "register-otp"
  | "dashboard"
  | "transfer"
  | "transaction-detail"

type User = { fullName: string; email: string; walletAddress?: string | null }

type Transaction = {
  id: string
  recipient: string
  amount: number
  note?: string
  at: string
  direction: "IN" | "OUT"
}

const INITIAL_BALANCE = 12_500_000

export function WalletApp() {
  const [screen, setScreen] = useState<Screen>("login")
  const [user, setCurrentUser] = useState<User | null>(null)
  const [pendingRegister, setPendingRegister] = useState<User | null>(null)
  const [balance, setBalance] = useState(INITIAL_BALANCE)
  const [transactions, setTransactions] = useState<Transaction[]>([])
  const [selectedTransactionDetail, setSelectedTransactionDetail] = useState<TransactionDetailItem | null>(null)
  const [loadingTransactionDetail, setLoadingTransactionDetail] = useState(false)

  function saveUser(nextUser: User) {
    setCurrentUser(nextUser)
    persistUser(nextUser)
  }

  async function loadWalletSummary(currentUser: User) {
    const summary = await getWalletSummary()
    const history = await getTransactionHistory()
    const updatedUser = { ...currentUser, walletAddress: summary.walletAddress }
    saveUser(updatedUser)
    setBalance(summary.balance)
    setTransactions(
      history.map((item) => ({
        id: item.id,
        recipient: item.counterpartyWallet ?? "Ví đối tác",
        amount: item.amount,
        note: item.description,
        at: new Date(item.createdAt).toLocaleString("vi-VN", {
          hour: "2-digit",
          minute: "2-digit",
          day: "2-digit",
          month: "2-digit",
        }),
        direction: item.direction,
      })),
    )
  }

  useEffect(() => {
    const token = getToken()
    const storedUser = getUser()
    if (!token || !storedUser) return
    setCurrentUser(storedUser)
    setScreen("dashboard")
    void loadWalletSummary(storedUser).catch((error) => {
      toast.error(getErrorMessage(error))
    })
  }, [])

  function handleLogout() {
    clearToken()
    setCurrentUser(null)
    setTransactions([])
    setBalance(INITIAL_BALANCE)
    setScreen("login")
  }

  function handleOpenTransactionDetail(transactionId: string) {
    setScreen("transaction-detail")
    setSelectedTransactionDetail(null)
    setLoadingTransactionDetail(true)
    void getTransactionDetail(transactionId)
      .then((detail) => {
        setSelectedTransactionDetail(detail)
      })
      .catch((error) => {
        toast.error(getErrorMessage(error))
        setScreen("dashboard")
      })
      .finally(() => {
        setLoadingTransactionDetail(false)
      })
  }

  return (
    <WindowFrame>
      {screen === "login" && (
        <LoginScreen
          prefillEmail={pendingRegister?.email}
          onLoggedIn={(u) => {
            saveUser(u)
            setScreen("dashboard")
            void loadWalletSummary(u).catch((error) => {
              toast.error(getErrorMessage(error))
            })
          }}
          onGoToRegister={() => setScreen("register")}
        />
      )}

      {screen === "register" && (
        <RegisterScreen
          onDraftSaved={(input) => {
            setPendingRegister(input)
            setScreen("register-otp")
          }}
          onGoToLogin={() => setScreen("login")}
        />
      )}

      {screen === "register-otp" && pendingRegister && (
        <RegisterOtpScreen
          email={pendingRegister.email}
          onVerified={() => setScreen("login")}
          onBack={() => setScreen("register")}
        />
      )}

      {screen === "dashboard" && user && (
        <Dashboard
          user={user}
          balance={balance}
          transactions={transactions}
          onTransfer={() => setScreen("transfer")}
          onViewTransaction={handleOpenTransactionDetail}
          onLogout={handleLogout}
        />
      )}

      {screen === "transfer" && user && (
        <TransferScreen
          balance={balance}
          onBack={() => setScreen("dashboard")}
          onCompleted={() => {
            setScreen("dashboard")
            void loadWalletSummary(user).catch((error) => {
              toast.error(getErrorMessage(error))
            })
          }}
        />
      )}

      {screen === "transaction-detail" && (
        <TransactionDetailScreen
          detail={selectedTransactionDetail}
          loading={loadingTransactionDetail}
          onBack={() => setScreen("dashboard")}
        />
      )}
    </WindowFrame>
  )
}
