"use client"

import { useState, type FormEvent } from "react"
import { Eye, EyeOff, Loader2 } from "lucide-react"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { BrandPanel } from "@/components/brand-panel"
import { getErrorMessage } from "@/lib/api"
import { registerDraft } from "@/lib/services"

type RegisterScreenProps = {
  onDraftSaved: (input: { fullName: string; email: string }) => void
  onGoToLogin: () => void
}

export function RegisterScreen({ onDraftSaved, onGoToLogin }: RegisterScreenProps) {
  const [fullName, setFullName] = useState("")
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (loading) return
    if (password.length < 6) {
      toast.error("Mật khẩu phải có ít nhất 6 ký tự.")
      return
    }
    setLoading(true)
    try {
      // Step 1: backend saves a "pending" draft user in Redis (TTL 5 min).
      await registerDraft({ fullName, email, password })
      toast.success("Đã lưu thông tin. Đang gửi mã OTP...")
      onDraftSaved({ fullName, email })
    } catch (error) {
      toast.error(getErrorMessage(error))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="grid md:grid-cols-[1.1fr_1fr]">
      <BrandPanel />
      <div className="flex flex-col justify-center p-8 md:p-10">
        <div className="mx-auto w-full max-w-sm">
          <h2 className="text-2xl font-bold text-foreground">Tạo tài khoản</h2>
          <p className="mt-1 text-sm text-muted-foreground">
            Bắt đầu hành trình tài chính số của bạn.
          </p>

          <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-4">
            <div className="flex flex-col gap-2">
              <Label htmlFor="reg-name">Họ và tên</Label>
              <Input
                id="reg-name"
                required
                autoComplete="name"
                placeholder="Nguyễn Văn A"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
              />
            </div>

            <div className="flex flex-col gap-2">
              <Label htmlFor="reg-email">Email</Label>
              <Input
                id="reg-email"
                type="email"
                required
                autoComplete="email"
                placeholder="ban@email.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>

            <div className="flex flex-col gap-2">
              <Label htmlFor="reg-password">Mật khẩu</Label>
              <div className="relative">
                <Input
                  id="reg-password"
                  type={showPassword ? "text" : "password"}
                  required
                  autoComplete="new-password"
                  placeholder="Tối thiểu 6 ký tự"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="pr-10"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword((v) => !v)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground"
                  aria-label={showPassword ? "Ẩn mật khẩu" : "Hiện mật khẩu"}
                >
                  {showPassword ? <EyeOff className="h-4 w-4" /> : <Eye className="h-4 w-4" />}
                </button>
              </div>
            </div>

            <Button type="submit" disabled={loading} className="mt-2 h-11 w-full text-base">
              {loading ? (
                <>
                  <Loader2 className="h-4 w-4 animate-spin" />
                  Đang xử lý...
                </>
              ) : (
                "Tiếp tục"
              )}
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-muted-foreground">
            Đã có tài khoản?{" "}
            <button
              type="button"
              onClick={onGoToLogin}
              className="font-semibold text-primary hover:underline"
            >
              Đăng nhập
            </button>
          </p>
        </div>
      </div>
    </div>
  )
}
