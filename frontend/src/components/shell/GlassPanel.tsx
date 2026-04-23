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
      className={`rounded-3xl border border-neutral-200/80 bg-white/90 shadow-[0_4px_24px_rgba(0,0,0,0.06)] backdrop-blur-sm ${className}`}
    >
      {children}
    </div>
  );
}
