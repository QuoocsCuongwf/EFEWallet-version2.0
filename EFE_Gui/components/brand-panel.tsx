"use client"

import { ShieldCheck, Wallet, Zap } from "lucide-react"

const features = [
  {
    icon: ShieldCheck,
    title: "Bảo mật đa lớp",
    desc: "Xác thực OTP & JWT cho mọi giao dịch.",
  },
  {
    icon: Zap,
    title: "Chống trừ tiền 2 lần",
    desc: "Idempotency-Key đảm bảo an toàn tuyệt đối.",
  },
  {
    icon: Wallet,
    title: "Chuyển tiền tức thì",
    desc: "Trải nghiệm mượt mà trên desktop.",
  },
]

export function BrandPanel() {
  return (
    <div className="brand-gradient relative hidden flex-col justify-between overflow-hidden p-10 text-primary-foreground md:flex">
      <div className="relative z-10">
        <div className="flex items-center gap-3">
          <div className="flex h-11 w-11 items-center justify-center rounded-xl bg-white/15 backdrop-blur">
            <Wallet className="h-6 w-6" aria-hidden="true" />
          </div>
          <span className="text-2xl font-bold tracking-tight">EFEWallet</span>
        </div>
        <h1 className="mt-12 text-balance text-3xl font-bold leading-tight">
          Ví điện tử thế hệ mới cho cuộc sống số
        </h1>
        <p className="mt-4 max-w-sm text-pretty leading-relaxed text-primary-foreground/80">
          Quản lý tài chính an toàn với công nghệ bảo mật cấp ngân hàng, ngay trên máy tính của bạn.
        </p>
      </div>

      <ul className="relative z-10 mt-10 flex flex-col gap-5">
        {features.map((f) => (
          <li key={f.title} className="flex items-start gap-3">
            <div className="mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-lg bg-white/15 backdrop-blur">
              <f.icon className="h-5 w-5" aria-hidden="true" />
            </div>
            <div>
              <p className="font-semibold">{f.title}</p>
              <p className="text-sm text-primary-foreground/75">{f.desc}</p>
            </div>
          </li>
        ))}
      </ul>

      {/* Decorative glow */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -right-16 -top-16 h-64 w-64 rounded-full bg-white/15 blur-3xl"
      />
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -bottom-20 -left-10 h-56 w-56 rounded-full bg-white/10 blur-3xl"
      />
      {/* Subtle dot grid texture */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 opacity-20 [background-image:radial-gradient(circle_at_center,white_1px,transparent_1px)] [background-size:22px_22px]"
      />
    </div>
  )
}
