"use client"

import { ArrowDownLeft, ArrowLeft, ArrowUpRight, Loader2 } from "lucide-react"
import { Button } from "@/components/ui/button"
import type { TransactionDetailItem } from "@/lib/services"

type TransactionDetailScreenProps = {
  detail: TransactionDetailItem | null
  loading: boolean
  onBack: () => void
}

const currency = new Intl.NumberFormat("vi-VN", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
})

function formatMoney(amount: number, direction: "IN" | "OUT") {
  return `${direction === "IN" ? "+" : "-"}${currency.format(amount)}`
}

export function TransactionDetailScreen({
  detail,
  loading,
  onBack,
}: TransactionDetailScreenProps) {
  return (
    <div className="flex flex-col p-8 md:p-10">
      <button
        type="button"
        onClick={onBack}
        className="mb-6 inline-flex items-center gap-1.5 text-sm font-medium text-muted-foreground hover:text-foreground"
      >
        <ArrowLeft className="h-4 w-4" />
        Quay lại
      </button>

      {loading ? (
        <div className="mx-auto mt-16 flex items-center gap-3 text-sm text-muted-foreground">
          <Loader2 className="h-4 w-4 animate-spin" />
          Đang tải chi tiết giao dịch...
        </div>
      ) : !detail ? (
        <p className="mt-8 rounded-xl border border-dashed border-border p-6 text-center text-sm text-muted-foreground">
          Không tìm thấy thông tin giao dịch.
        </p>
      ) : (
        <div className="mx-auto w-full max-w-xl rounded-2xl border border-border bg-card p-6">
          <div className="flex items-center gap-3">
            <div className="flex h-10 w-10 items-center justify-center rounded-full bg-accent text-accent-foreground">
              {detail.direction === "IN" ? (
                <ArrowDownLeft className="h-4 w-4" />
              ) : (
                <ArrowUpRight className="h-4 w-4" />
              )}
            </div>
            <div>
              <p className="text-sm text-muted-foreground">Mã giao dịch</p>
              <p className="text-sm font-semibold text-foreground">{detail.id}</p>
            </div>
          </div>

          <div className="mt-6 space-y-3 text-sm">
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">Số tiền</span>
              <span
                className={`font-semibold tabular-nums ${
                  detail.direction === "IN" ? "text-emerald-600" : "text-destructive"
                }`}
              >
                {formatMoney(detail.amount, detail.direction)}
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">Trạng thái</span>
              <span className="font-medium text-foreground">{detail.status}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">Loại giao dịch</span>
              <span className="font-medium text-foreground">{detail.type}</span>
            </div>
            {detail.fee !== undefined && (
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">Phí</span>
                <span className="font-medium text-foreground">{currency.format(detail.fee)}</span>
              </div>
            )}
            {detail.currency && (
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">Đơn vị tiền</span>
                <span className="font-medium text-foreground">{detail.currency}</span>
              </div>
            )}
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">Ví đối tác</span>
              <span className="font-medium text-foreground">
                {detail.counterpartyWallet ?? "Không có"}
              </span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">Ví nguồn</span>
              <span className="font-medium text-foreground">{detail.fromWallet ?? "Không có"}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">Ví đích</span>
              <span className="font-medium text-foreground">{detail.toWallet ?? "Không có"}</span>
            </div>
            <div className="flex items-center justify-between">
              <span className="text-muted-foreground">Thời gian tạo</span>
              <span className="font-medium text-foreground">
                {new Date(detail.createdAt).toLocaleString("vi-VN")}
              </span>
            </div>
            {detail.updatedAt && (
              <div className="flex items-center justify-between">
                <span className="text-muted-foreground">Cập nhật lúc</span>
                <span className="font-medium text-foreground">
                  {new Date(detail.updatedAt).toLocaleString("vi-VN")}
                </span>
              </div>
            )}
            {detail.description && (
              <div className="rounded-xl bg-muted p-3">
                <p className="text-xs text-muted-foreground">Ghi chú</p>
                <p className="mt-1 text-sm text-foreground">{detail.description}</p>
              </div>
            )}
            {detail.externalTransactionId && (
              <div className="rounded-xl bg-muted p-3">
                <p className="text-xs text-muted-foreground">Mã giao dịch ngoài hệ thống</p>
                <p className="mt-1 text-sm text-foreground">{detail.externalTransactionId}</p>
              </div>
            )}
          </div>

          <Button onClick={onBack} variant="secondary" className="mt-6 w-full">
            Quay lại danh sách giao dịch
          </Button>
        </div>
      )}
    </div>
  )
}
