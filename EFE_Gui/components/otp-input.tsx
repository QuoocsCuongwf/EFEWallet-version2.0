"use client"

import { useRef, type ClipboardEvent, type KeyboardEvent } from "react"
import { cn } from "@/lib/utils"

type OtpInputProps = {
  value: string
  onChange: (value: string) => void
  length?: number
  disabled?: boolean
  autoFocus?: boolean
}

export function OtpInput({
  value,
  onChange,
  length = 6,
  disabled = false,
  autoFocus = true,
}: OtpInputProps) {
  const inputs = useRef<Array<HTMLInputElement | null>>([])

  const digits = Array.from({ length }, (_, i) => value[i] ?? "")

  const focusIndex = (index: number) => {
    const el = inputs.current[index]
    if (el) el.focus()
  }

  const setDigit = (index: number, digit: string) => {
    const next = digits.slice()
    next[index] = digit
    onChange(next.join("").slice(0, length))
  }

  const handleChange = (index: number, raw: string) => {
    const sanitized = raw.replace(/\D/g, "")
    if (!sanitized) {
      setDigit(index, "")
      return
    }
    // Support fast typing / multiple chars landing in one box.
    const chars = sanitized.split("")
    const next = digits.slice()
    let cursor = index
    for (const char of chars) {
      if (cursor >= length) break
      next[cursor] = char
      cursor += 1
    }
    onChange(next.join("").slice(0, length))
    focusIndex(Math.min(cursor, length - 1))
  }

  const handleKeyDown = (index: number, e: KeyboardEvent<HTMLInputElement>) => {
    if (e.key === "Backspace") {
      if (digits[index]) {
        setDigit(index, "")
      } else if (index > 0) {
        focusIndex(index - 1)
        setDigit(index - 1, "")
      }
    } else if (e.key === "ArrowLeft" && index > 0) {
      focusIndex(index - 1)
    } else if (e.key === "ArrowRight" && index < length - 1) {
      focusIndex(index + 1)
    }
  }

  const handlePaste = (e: ClipboardEvent<HTMLInputElement>) => {
    e.preventDefault()
    const pasted = e.clipboardData.getData("text").replace(/\D/g, "").slice(0, length)
    if (pasted) {
      onChange(pasted)
      focusIndex(Math.min(pasted.length, length - 1))
    }
  }

  return (
    <div className="flex items-center justify-between gap-2" role="group" aria-label="Mã OTP 6 số">
      {digits.map((digit, index) => (
        <input
          key={index}
          ref={(el) => {
            inputs.current[index] = el
          }}
          type="text"
          inputMode="numeric"
          autoComplete="one-time-code"
          maxLength={1}
          value={digit}
          disabled={disabled}
          autoFocus={autoFocus && index === 0}
          aria-label={`Chữ số thứ ${index + 1}`}
          onChange={(e) => handleChange(index, e.target.value)}
          onKeyDown={(e) => handleKeyDown(index, e)}
          onPaste={handlePaste}
          onFocus={(e) => e.target.select()}
          className={cn(
            "h-14 w-full rounded-xl border bg-card text-center text-2xl font-semibold tabular-nums text-foreground shadow-sm transition-all",
            "focus:border-primary focus:ring-2 focus:ring-primary/30 focus:outline-none",
            "disabled:cursor-not-allowed disabled:opacity-50",
            digit ? "border-primary/60" : "border-input",
          )}
        />
      ))}
    </div>
  )
}
