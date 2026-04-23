"use client";

import type { ReactNode } from "react";

export function GlassPanel({
  children,
  className = "",
}: {
  children: ReactNode;
  className?: string;
}) {
  return (
    <div
      className={`rounded-2xl border border-gray-200 bg-white/95 shadow-[0_8px_28px_rgba(15,23,42,0.06)] backdrop-blur-sm ${className}`}
    >
      {children}
    </div>
  );
}
