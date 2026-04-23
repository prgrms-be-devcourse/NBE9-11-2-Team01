"use client";

import type { ReactNode } from "react";
import { LazyMotion, domAnimation } from "framer-motion";
import { MeshBackground } from "./MeshBackground";
import { SiteFooter } from "./SiteFooter";
import { SiteHeader } from "./SiteHeader";

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <LazyMotion features={domAnimation} strict>
      <div className="relative flex min-h-full flex-col text-neutral-900">
        <MeshBackground />
        <SiteHeader />
        <div className="relative z-10 flex flex-1 flex-col">{children}</div>
        <SiteFooter />
      </div>
    </LazyMotion>
  );
}
