"use client";

import Link from "next/link";
import { useState } from "react";
import { m, AnimatePresence } from "framer-motion";
import { Bell, ChevronDown, LogOut, Menu, X } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { SignalLogo } from "@/components/brand/SignalLogo";
import { Avatar } from "@/components/profile/Avatar";

const nav = [
  { href: "/", label: "홈" },
  { href: "/#boards", label: "게시판" },
  { href: "/#community", label: "시그널 피드" },
];

export function SiteHeader() {
  const { user, loading, logout } = useAuth();
  const [open, setOpen] = useState(false);

  return (
    <m.header
      initial={{ y: -12, opacity: 0 }}
      animate={{ y: 0, opacity: 1 }}
      transition={{ duration: 0.4, ease: [0.22, 1, 0.36, 1] as const }}
      className="sticky top-0 z-50 border-b border-gray-200 bg-white/90 backdrop-blur-md"
    >
      <div className="mx-auto flex h-16 max-w-6xl items-center justify-between px-4 sm:px-6">
        <Link
          href="/"
          className="flex items-center gap-2.5 text-lg font-semibold tracking-tight text-neutral-900"
        >
          <SignalLogo
            size={36}
            className="h-9 w-9 shrink-0 rounded-xl border border-neutral-200 bg-white shadow-sm"
          />
          <span>합격시그널</span>
        </Link>

        <nav className="hidden items-center gap-7 md:flex">
          {nav.map((item) => (
            <Link
              key={item.href}
              href={item.href}
              className="text-sm font-medium text-neutral-600 transition hover:text-neutral-900"
            >
              {item.label}
            </Link>
          ))}
        </nav>

        <div className="hidden items-center gap-3 md:flex">
          {!loading && user ? (
            <>
              <div className="flex max-w-[200px] items-center gap-2">
                <Avatar
                  src={user.profileImage}
                  alt={user.nickname}
                  size={32}
                  className="h-8 w-8"
                />
                <span className="truncate text-sm font-medium text-neutral-800">
                  {user.nickname}
                </span>
              </div>
              <Link
                href="/mypage"
                className="inline-flex h-10 items-center rounded-xl border border-gray-200 bg-white px-4 text-sm font-medium text-black transition hover:bg-blue-50"
              >
                마이페이지
              </Link>
              <button
                type="button"
                onClick={() => void logout()}
                className="inline-flex h-10 items-center gap-1.5 rounded-xl border border-gray-200 px-4 text-sm text-gray-700 transition hover:border-gray-300 hover:text-black"
              >
                <LogOut className="h-4 w-4" />
                로그아웃
              </button>
              <Link
                href="/notifications"
                className="inline-flex h-10 w-10 items-center justify-center rounded-xl border border-gray-200 bg-white text-gray-700 transition hover:border-gray-300 hover:bg-blue-50 hover:text-black"
                aria-label="알림 페이지로 이동"
                title="알림"
              >
                <Bell className="h-5 w-5" />
              </Link>
            </>
          ) : (
            <>
              <Link
                href="/login"
                className="text-sm font-medium text-neutral-600 transition hover:text-neutral-900"
              >
                로그인
              </Link>
              <Link
                href="/signup"
                className="inline-flex h-10 items-center rounded-xl bg-black px-5 text-sm font-semibold text-white transition hover:bg-gray-800"
              >
                회원가입
              </Link>
            </>
          )}
        </div>

        <button
          type="button"
          className="inline-flex h-10 w-10 items-center justify-center rounded-xl border border-neutral-200 text-neutral-900 md:hidden"
          aria-label="메뉴"
          onClick={() => setOpen((v) => !v)}
        >
          {open ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
        </button>
      </div>

      <AnimatePresence>
        {open && (
          <m.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: "auto", opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            className="overflow-hidden border-t border-neutral-200 bg-white md:hidden"
          >
            <div className="flex flex-col gap-1 px-4 py-4">
              {nav.map((item) => (
                <Link
                  key={item.href}
                  href={item.href}
                  className="rounded-xl px-3 py-2.5 text-neutral-700"
                  onClick={() => setOpen(false)}
                >
                  {item.label}
                </Link>
              ))}
              <hr className="my-2 border-neutral-200" />
              {!loading && user ? (
                <>
                  <Link
                    href="/notifications"
                    className="rounded-xl px-3 py-2.5 text-neutral-700"
                    onClick={() => setOpen(false)}
                  >
                    알림
                  </Link>
                  <Link
                    href="/mypage"
                    className="rounded-xl px-3 py-2.5 font-medium text-neutral-900"
                    onClick={() => setOpen(false)}
                  >
                    마이페이지
                  </Link>
                  <button
                    type="button"
                    className="rounded-xl px-3 py-2.5 text-left text-neutral-600"
                    onClick={() => {
                      setOpen(false);
                      void logout();
                    }}
                  >
                    로그아웃
                  </button>
                </>
              ) : (
                <>
                  <Link
                    href="/login"
                    className="rounded-xl px-3 py-2.5 font-medium text-neutral-900"
                    onClick={() => setOpen(false)}
                  >
                    로그인
                  </Link>
                  <Link
                    href="/signup"
                    className="rounded-xl px-3 py-2.5 text-neutral-700"
                    onClick={() => setOpen(false)}
                  >
                    회원가입
                  </Link>
                </>
              )}
            </div>
          </m.div>
        )}
      </AnimatePresence>
    </m.header>
  );
}
