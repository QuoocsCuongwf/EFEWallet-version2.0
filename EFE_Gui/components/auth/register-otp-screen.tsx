"use client"

import { useCallback, useEffect, useRef, useState } from "react"
import { ArrowLeft, Loader2, MailCheck } from "lucide-react"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { OtpInput } from "@/components/otp-input"
import { getErrorMessage } from "@/lib/api"
import { requestOtp, verifyRegisterOtp } from "@/lib/services"

const RESEND_SECONDS = 60

type RegisterOtpScreenProps = {
  email: string
  onVerified: () => void
  onBack: () => void
}

export function RegisterOtpScreen({ email, onVerified, onBack }: RegisterOtpScreenProps) {
  const [otp, setOtp] = useState("")
  const [verifying, setVerifying] = useState(false)
  const [sending, setSending] = useState(false)
  const [cooldown, setCooldown] = useState(RESEND_SECONDS)
  const hasRequested = useRef(false)

  const sendOtp = useCallback(
    async (silent = false) => {
      setSending(true)
      try {
        // Step 3 of the flow: ask backend to dispatch the REGISTER OTP (Kafka -> mail/SMS).
        await requestOtp({ action: "REGISTER", email })
        setCooldown(RESEND_SECONDS)
        if (!silent) toast.success("Đã gửi mã OTP mới đến email của bạn.")
      } catch (error) {
        toast.error(getErrorMessage(error))
      } finally {
        setSending(false)
      }
    },
    [email],
  )

  // Trigger the OTP dispatch once when arriving on this screen.
  useEffect(() => {
    if (hasRequested.current) return
    hasRequested.current = true
    void sendOtp(true)
  }, [sendOtp])

  // Resend cooldown timer.
  useEffect(() => {
    if (cooldown <= 0) return
    const id = setInterval(() => setCooldown((c) => Math.max(0, c - 1)), 1000)
    return () => clearInterval(id)
  }, [cooldown])

  async function handleVerify() {
    if (verifying) return
    if (otp.length !== 6) {
      toast.error("Vui lòng nhập đủ 6 số.")
      return
    }
    setVerifying(true)
    try {
      // Step 4: backend pulls the Redis draft, validates OTP, persists to PostgreSQL.
      await verifyRegisterOtp({ email, otp })
      toast.success("Đăng ký thành công! Vui lòng đăng nhập.")
      onVerified()
    } catch (error) {
      toast.error(getErrorMessage(error))
      setOtp("")
    } finally {
      setVerifying(false)
    }
  }

  return (
    <div className="flex min-h-[36rem] flex-col items-center justify-center p-8 md:p-10">
      <div className="w-full max-w-md text-center">
        <button
          type="button"
          onClick={onBack}
          className="mb-6 inline-flex items-center gap-1.5 text-sm font-medium text-muted-foreground hover:text-foreground"
        >
          <ArrowLeft className="h-4 w-4" />
          Quay lại
        </button>

        <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-accent text-accent-foreground">
          <MailCheck className="h-8 w-8" aria-hidden="true" />
        </div>

        <h2 className="mt-6 text-2xl font-bold text-foreground">Xác minh email</h2>
        <p className="mt-2 text-pretty text-sm text-muted-foreground">
          Chúng tôi đã gửi mã gồm 6 chữ số đến{" "}
          <span className="font-semibold text-foreground">{email}</span>. Nhập mã để hoàn tất đăng ký.
        </p>

        <div className="mt-8">
          <OtpInput value={otp} onChange={setOtp} disabled={verifying} />
        </div>

        <Button
          onClick={handleVerify}
          disabled={verifying || otp.length !== 6}
          className="mt-6 h-11 w-full text-base"
        >
          {verifying ? (
            <>
              <Loader2 className="h-4 w-4 animate-spin" />
              Đang xác minh...
            </>
          ) : (
            "Hoàn tất đăng ký"
          )}
        </Button>

        <p className="mt-5 text-sm text-muted-foreground">
          Chưa nhận được mã?{" "}
          {cooldown > 0 ? (
            <span className="tabular-nums">Gửi lại sau {cooldown}s</span>
          ) : (
            <button
              type="button"
              onClick={() => sendOtp(false)}
              disabled={sending}
              className="font-semibold text-primary hover:underline disabled:opacity-50"
            >
              {sending ? "Đang gửi..." : "Gửi lại mã"}
            </button>
          )}
        </p>
      </div>
    </div>
  )
}
