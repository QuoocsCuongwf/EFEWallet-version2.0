"use client"

import { useState, type FormEvent } from "react"
import { v4 as uuidv4 } from "uuid"
import { ArrowLeft, KeyRound } from "lucide-react"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { TransferOtpModal } from "./transfer-otp-modal"

type TransferScreenProps = {
  balance: number
  onBack: () => void
  onCompleted: (tx: {
    id: string
    recipient: string
    amount: number
    note?: string
  }) => void
}

const currency = new Intl.NumberFormat("vi-VN", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
})

export function TransferScreen({ balance, onBack, onCompleted }: TransferScreenProps) {
  const [recipient, setRecipient] = useState("")
  const [amountText, setAmountText] = useState("")
  const [note, setNote] = useState("")
  const [modalOpen, setModalOpen] = useState(false)
  // Keep one stable key per transfer draft (recipient/amount/note).
  const [idempotencyKey, setIdempotencyKey] = useState("")
  const [draftFingerprint, setDraftFingerprint] = useState("")

  const amount = Number(amountText.replace(/\D/g, ""))

  function getDraftFingerprint() {
    return JSON.stringify({
      recipient: recipient.trim(),
      amount,
      note: note.trim(),
    })
  }

  function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (!recipient.trim()) {
      toast.error("Vui lòng nhập tài khoản người nhận.")
      return
    }
    if (!amount || amount <= 0) {
      toast.error("Số tiền không hợp lệ.")
      return
    }
    if (amount > balance) {
      toast.error("Số dư không đủ để thực hiện giao dịch.")
      return
    }
    const nextFingerprint = getDraftFingerprint()
    if (!idempotencyKey || draftFingerprint !== nextFingerprint) {
      setIdempotencyKey(uuidv4())
      setDraftFingerprint(nextFingerprint)
    }
    setModalOpen(true)
  }

  function handleSuccess(transactionId?: string) {
    setModalOpen(false)
    onCompleted({
      id: transactionId ?? idempotencyKey,
      recipient,
      amount,
      note: note.trim() || undefined,
    })
  }

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

      <div className="mx-auto w-full max-w-md">
        <h2 className="text-2xl font-bold text-foreground">Chuyển tiền</h2>
        <p className="mt-1 text-sm text-muted-foreground">
          Số dư khả dụng:{" "}
          <span className="font-semibold text-foreground tabular-nums">
            {currency.format(balance)}
          </span>
        </p>

        <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-5">
          <div className="flex flex-col gap-2">
            <Label htmlFor="recipient">Tài khoản người nhận</Label>
            <Input
              id="recipient"
              required
              placeholder="Số tài khoản hoặc email"
              value={recipient}
              onChange={(e) => setRecipient(e.target.value)}
            />
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="amount">Số tiền (VND)</Label>
            <Input
              id="amount"
              inputMode="numeric"
              required
              placeholder="0"
              value={amount ? amount.toLocaleString("vi-VN") : ""}
              onChange={(e) => setAmountText(e.target.value)}
              className="text-lg font-semibold tabular-nums"
            />
            <div className="flex flex-wrap gap-2">
              {[100000, 500000, 1000000, 5000000].map((preset) => (
                <button
                  key={preset}
                  type="button"
                  onClick={() => setAmountText(String(preset))}
                  className="rounded-full border border-border bg-muted px-3 py-1 text-xs font-medium text-muted-foreground transition-colors hover:border-primary hover:text-primary"
                >
                  {currency.format(preset)}
                </button>
              ))}
            </div>
          </div>

          <div className="flex flex-col gap-2">
            <Label htmlFor="note">Ghi chú</Label>
            <Input
              id="note"
              placeholder="Lời nhắn (tuỳ chọn)"
              value={note}
              onChange={(e) => setNote(e.target.value)}
            />
          </div>

          <Button type="submit" className="mt-2 h-12 w-full text-base">
            <KeyRound className="h-4 w-4" />
            Xác nhận chuyển tiền
          </Button>
          <p className="text-center text-xs text-muted-foreground">
            Tài khoản nguồn được xác định tự động từ phiên đăng nhập của bạn.
          </p>
        </form>
      </div>

      {modalOpen && (
        <TransferOtpModal
          open={modalOpen}
          idempotencyKey={idempotencyKey}
          details={{ recipient, amount, note: note.trim() || undefined }}
          onSuccess={handleSuccess}
          onClose={() => setModalOpen(false)}
        />
      )}
    </div>
  )
}
