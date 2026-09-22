"use client"

import { ArrowDownLeft, ArrowUpRight, LogOut, Send, Wallet } from "lucide-react"
import { Button } from "@/components/ui/button"

type DashboardProps = {
  user: { fullName: string; email: string; walletAddress?: string | null }
  balance: number
  transactions: Array<{
    id: string
    recipient: string
    amount: number
    note?: string
    at: string
    direction: "IN" | "OUT"
  }>
  onTransfer: () => void
  onViewTransaction: (id: string) => void
  onLogout: () => void
}

const currency = new Intl.NumberFormat("vi-VN", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
})

export function Dashboard({
  user,
  balance,
  transactions,
  onTransfer,
  onViewTransaction,
  onLogout,
}: DashboardProps) {
  const initials = user.fullName
    .split(" ")
    .map((p) => p[0])
    .slice(-2)
    .join("")
    .toUpperCase()

  return (
    <div className="flex flex-col p-8 md:p-10">
      {/* Header */}
      <header className="flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="brand-gradient flex h-11 w-11 items-center justify-center rounded-full text-sm font-bold text-primary-foreground">
            {initials || <Wallet className="h-5 w-5" />}
          </div>
          <div>
            <p className="text-sm text-muted-foreground">Xin chào,</p>
            <p className="font-semibold text-foreground">{user.fullName}</p>
          </div>
        </div>
        <Button variant="ghost" size="sm" onClick={onLogout} className="text-muted-foreground">
          <LogOut className="h-4 w-4" />
          Đăng xuất
        </Button>
      </header>

      {/* Balance card */}
      <div className="brand-gradient relative mt-8 overflow-hidden rounded-2xl p-7 text-primary-foreground shadow-lg">
        <div className="relative z-10">
          <p className="text-sm text-primary-foreground/80">Số dư khả dụng</p>
          <p className="mt-2 text-4xl font-bold tabular-nums">{currency.format(balance)}</p>
          <p className="mt-4 text-xs text-primary-foreground/70">
            {user.walletAddress ?? user.email}
          </p>
        </div>
        <Wallet
          className="pointer-events-none absolute -right-4 -top-4 h-32 w-32 text-white/10"
          aria-hidden="true"
        />
      </div>

      {/* Actions */}
      <div className="mt-6 grid grid-cols-2 gap-4">
        <Button onClick={onTransfer} className="h-12 text-base">
          <Send className="h-4 w-4" />
          Chuyển tiền
        </Button>
        <Button variant="secondary" className="h-12 text-base" disabled>
          <ArrowDownLeft className="h-4 w-4" />
          Nạp tiền
        </Button>
      </div>

      {/* Recent transactions */}
      <section className="mt-8">
        <h3 className="text-sm font-semibold text-foreground">Giao dịch gần đây</h3>
        {transactions.length === 0 ? (
          <p className="mt-4 rounded-xl border border-dashed border-border p-6 text-center text-sm text-muted-foreground">
            Chưa có giao dịch nào.
          </p>
        ) : (
          <ul className="mt-4 flex flex-col gap-2">
            {transactions.map((t) => (
              <li
                key={t.id}
                className="rounded-xl border border-border bg-card"
              >
                <button
                  type="button"
                  onClick={() => onViewTransaction(t.id)}
                  className="flex w-full items-center justify-between px-4 py-3 text-left transition-colors hover:bg-muted/40"
                >
                  <div className="flex items-center gap-3">
                    <div className="flex h-9 w-9 items-center justify-center rounded-full bg-accent text-accent-foreground">
                      {t.direction === "IN" ? (
                        <ArrowDownLeft className="h-4 w-4" />
                      ) : (
                        <ArrowUpRight className="h-4 w-4" />
                      )}
                    </div>
                    <div>
                      <p className="text-sm font-medium text-foreground">{t.recipient}</p>
                      <p className="text-xs text-muted-foreground">
                        {t.note || (t.direction === "IN" ? "Nhận tiền" : "Chuyển tiền")}
                      </p>
                    </div>
                  </div>
                  <div className="text-right">
                    <p
                      className={`text-sm font-semibold tabular-nums ${
                        t.direction === "IN" ? "text-emerald-600" : "text-destructive"
                      }`}
                    >
                      {t.direction === "IN" ? "+" : "-"}
                      {currency.format(t.amount)}
                    </p>
                    <p className="text-xs text-muted-foreground">{t.at}</p>
                  </div>
                </button>
              </li>
            ))}
          </ul>
        )}
      </section>
    </div>
  )
}
