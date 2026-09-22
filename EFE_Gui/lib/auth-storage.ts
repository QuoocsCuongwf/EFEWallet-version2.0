"use client"

// Token storage abstraction.
// In a real Electron build this can be swapped for secure main-process storage
// (e.g. electron-store / safeStorage) exposed via a preload bridge on window.efe.
const TOKEN_KEY = "efe_access_token"
const USER_KEY = "efe_user"

type StoredUser = {
  fullName: string
  email: string
  walletAddress?: string | null
}

function hasElectronBridge(): boolean {
  return typeof window !== "undefined" && typeof (window as any).efe?.getToken === "function"
}

export function getToken(): string | null {
  if (typeof window === "undefined") return null
  if (hasElectronBridge()) return (window as any).efe.getToken() ?? null
  return window.localStorage.getItem(TOKEN_KEY)
}

export function setToken(token: string): void {
  if (typeof window === "undefined") return
  if (hasElectronBridge()) {
    ;(window as any).efe.setToken(token)
    return
  }
  window.localStorage.setItem(TOKEN_KEY, token)
}

export function clearToken(): void {
  if (typeof window === "undefined") return
  if (hasElectronBridge()) {
    ;(window as any).efe.clearToken?.()
  }
  window.localStorage.removeItem(TOKEN_KEY)
  window.localStorage.removeItem(USER_KEY)
}

export function getUser(): StoredUser | null {
  if (typeof window === "undefined") return null
  const raw = window.localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as StoredUser
  } catch {
    return null
  }
}

export function setUser(user: StoredUser): void {
  if (typeof window === "undefined") return
  window.localStorage.setItem(USER_KEY, JSON.stringify(user))
}
