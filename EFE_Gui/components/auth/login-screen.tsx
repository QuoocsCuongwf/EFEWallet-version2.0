"use client"

import { useState, type FormEvent } from "react"
import { Eye, EyeOff, Loader2 } from "lucide-react"
import { toast } from "sonner"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { BrandPanel } from "@/components/brand-panel"
import { getErrorMessage } from "@/lib/api"
import { login, getProfile } from "@/lib/services"
import { setToken, setUser } from "@/lib/auth-storage"

type LoginScreenProps = {
  prefillEmail?: string
  onLoggedIn: (user: { fullName: string; email: string }) => void
  onGoToRegister: () => void
}

export function LoginScreen({ prefillEmail, onLoggedIn, onGoToRegister }: LoginScreenProps) {
  const [email, setEmail] = useState(prefillEmail ?? "")
  const [password, setPassword] = useState("")
  const [showPassword, setShowPassword] = useState(false)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(e: FormEvent) {
    e.preventDefault()
    if (loading) return
    setLoading(true)
    try {
      const data = await login({ email, password })
      setToken(data.token)
      // Fetch profile (first/last name) from server if available
      let fullName = email.split("@")[0]
      try {
        const profile = await getProfile()
        fullName = [profile.data.firstName, profile.data.lastName].filter(Boolean).join(" ") || fullName
      } catch {
        // ignore — fallback to email prefix
      }
      const user = { fullName, email }
      setUser(user)
      toast.success("Đăng nhập thành công")
      onLoggedIn(user)
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
          <h2 className="text-2xl font-bold text-foreground">Chào mừng trở lại</h2>
          <p className="mt-1 text-sm text-muted-foreground">
            Đăng nhập để tiếp tục với EFEWallet.
          </p>

          <form onSubmit={handleSubmit} className="mt-8 flex flex-col gap-4">
            <div className="flex flex-col gap-2">
              <Label htmlFor="login-email">Email</Label>
              <Input
                id="login-email"
                type="email"
                required
                autoComplete="email"
                placeholder="ban@email.com"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            </div>

            <div className="flex flex-col gap-2">
              <Label htmlFor="login-password">Mật khẩu</Label>
              <div className="relative">
                <Input
                  id="login-password"
                  type={showPassword ? "text" : "password"}
                  required
                  autoComplete="current-password"
                  placeholder="••••••••"
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
                  Đang đăng nhập...
                </>
              ) : (
                "Đăng nhập"
              )}
            </Button>
          </form>

          <p className="mt-6 text-center text-sm text-muted-foreground">
            Chưa có tài khoản?{" "}
            <button
              type="button"
              onClick={onGoToRegister}
              className="font-semibold text-primary hover:underline"
            >
              Đăng ký ngay
            </button>
          </p>
        </div>
      </div>
    </div>
  )
}
