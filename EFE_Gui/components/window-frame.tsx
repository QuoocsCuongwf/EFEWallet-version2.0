"use client"

import type { ReactNode } from "react"
import { ThemeToggle } from "@/components/theme-toggle"

export function WindowFrame({ children }: { children: ReactNode }) {
  return (
    <div className="relative flex min-h-screen items-center justify-center overflow-hidden bg-background p-4 md:p-8">
      {/* Ambient gradient backdrop */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -left-32 -top-32 h-96 w-96 rounded-full bg-chart-3/20 blur-3xl"
      />
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -bottom-32 -right-24 h-96 w-96 rounded-full bg-chart-2/20 blur-3xl"
      />
      <div className="relative flex w-full max-w-5xl flex-col overflow-hidden rounded-2xl border border-border bg-card shadow-2xl ring-1 ring-black/5">
        {/* Title bar (Electron-style) */}
        <div className="flex items-center gap-2 border-b border-border bg-muted/60 px-4 py-3">
          <span className="flex gap-2" aria-hidden="true">
            <span className="h-3 w-3 rounded-full bg-destructive/80" />
            <span className="h-3 w-3 rounded-full bg-chart-4/70" />
            <span className="h-3 w-3 rounded-full bg-chart-2/70" />
          </span>
          <p className="ml-2 text-sm font-medium text-muted-foreground">
            EFEWallet — Secure Desktop Wallet
          </p>
          <ThemeToggle className="ml-auto h-8 w-8 text-muted-foreground hover:text-foreground" />
        </div>
        <div className="min-h-[36rem]">{children}</div>
      </div>
    </div>
  )
}
