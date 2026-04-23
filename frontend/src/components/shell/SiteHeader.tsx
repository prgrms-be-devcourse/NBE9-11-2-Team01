"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { m, AnimatePresence } from "framer-motion";
import { Bell, ChevronDown, LogOut, Menu, X, Settings } from "lucide-react";
import { useAuth } from "@/context/AuthContext";
import { SignalLogo } from "@/components/brand/SignalLogo";
import { Avatar } from "@/components/profile/Avatar";
import { apiGet } from "@/lib/api";
import type { Board } from "@/lib/types";

const nav = [
  { href: "/#community", label: "시그널 피드" },
] as const;

export function SiteHeader() {
  const { user, loading, logout } = useAuth();
  const [open, setOpen] = useState(false);
  const [boards, setBoards] = useState<Board[]>([]);

  // 관리자 권한 확인 (ADMIN일 때만 true)
  const isAdmin = user?.role === 'ADMIN';

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const r = await apiGet<Board[]>("/boards");
        if (!cancelled) setBoards(r.data ?? []);
      } catch {
        if (!cancelled) setBoards([]);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

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

        {/* 데스크탑 메인 네비게이션 */}
        <nav className="hidden items-center gap-7 md:flex">
          <Link
            href="/"
            className="text-sm font-medium text-neutral-600 transition hover:text-neutral-900"
          >
            홈
          </Link>
          <div className="group relative flex items-center">
            <Link
              href="/#boards"
              className="inline-flex items-center gap-0.5 text-sm font-medium text-neutral-600 transition hover:text-neutral-900"
            >
              게시판
              <ChevronDown
                className="h-3.5 w-3.5 opacity-50 transition group-hover:rotate-180"
                aria-hidden
              />
            </Link>
            <div
              className="pointer-events-none absolute left-0 top-full z-[60] pt-1.5 opacity-0 transition-opacity duration-150 group-hover:pointer-events-auto group-hover:opacity-100"
              role="navigation"
              aria-label="게시판 목록"
            >
              <div className="min-w-[220px] rounded-xl border border-neutral-200 bg-white py-1.5 shadow-lg">
                {boards.length === 0 ? (
                  <p className="px-4 py-3 text-xs text-neutral-500">
                    등록된 게시판이 없습니다.
                  </p>
                ) : (
                  boards.map((b) => (
                    <Link
                      key={b.id}
                      href={`/boards/${b.id}/posts`}
                      className="block px-4 py-2.5 text-sm text-neutral-700 transition hover:bg-neutral-50 hover:text-neutral-900"
                    >
                      {b.boardName}
                    </Link>
                  ))
                )}
              </div>
            </div>
          </div>
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

        {/* 우측 사용자 메뉴 및 아이콘 세트 */}
        <div className="hidden items-center gap-3 md:flex">
          {!loading && user ? (
            <>
              <div className="flex max-w-[200px] items-center gap-2 mr-2">
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
                className="text-sm font-medium text-neutral-600 transition hover:text-neutral-900"
              >
                마이페이지
              </Link>
              <button
                type="button"
                onClick={() => void logout()}
                className="text-sm font-medium text-neutral-600 transition hover:text-neutral-900"
              >
                로그아웃
              </button>

              {/* 아이콘 그룹 (구분선 추가) */}
              <div className="flex items-center gap-2 ml-2 pl-4 border-l border-gray-200">
                <Link
                  href="/notifications"
                  className="inline-flex h-10 w-10 items-center justify-center rounded-xl border border-gray-200 bg-white text-gray-700 transition hover:border-gray-300 hover:bg-gray-50 hover:text-black"
                  aria-label="알림"
                  title="알림"
                >
                  <Bell className="h-5 w-5" />
                </Link>

                {/* 관리자 버튼: 최우측 배치, 블랙 스타일 */}
                {isAdmin && (
                  <Link
                    href="/admin/boards"
                    className="inline-flex h-10 w-10 items-center justify-center rounded-xl border border-black bg-black text-white transition hover:bg-neutral-800 shadow-sm"
                    aria-label="관리자 페이지"
                    title="관리자 설정"
                  >
                    <Settings className="h-5 w-5" />
                  </Link>
                )}
              </div>
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

        {/* 모바일 햄버거 메뉴 버튼 */}
        <button
          type="button"
          className="inline-flex h-10 w-10 items-center justify-center rounded-xl border border-neutral-200 text-neutral-900 md:hidden"
          aria-label="메뉴"
          onClick={() => setOpen((v) => !v)}
        >
          {open ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
        </button>
      </div>

      {/* 모바일 메뉴 오버레이 */}
      <AnimatePresence>
        {open && (
          <m.div
            initial={{ height: 0, opacity: 0 }}
            animate={{ height: "auto", opacity: 1 }}
            exit={{ height: 0, opacity: 0 }}
            className="overflow-hidden border-t border-neutral-200 bg-white md:hidden"
          >
            <div className="flex flex-col gap-1 px-4 py-4">
              <Link
                href="/"
                className="rounded-xl px-3 py-2.5 text-neutral-700"
                onClick={() => setOpen(false)}
              >
                홈
              </Link>
              <Link
                href="/#boards"
                className="rounded-xl px-3 py-2.5 text-neutral-700"
                onClick={() => setOpen(false)}
              >
                게시판
              </Link>
              {boards.length > 0 && (
                <div className="ml-2 flex flex-col border-l border-neutral-200 pl-3">
                  {boards.map((b) => (
                    <Link
                      key={b.id}
                      href={`/boards/${b.id}/posts`}
                      className="rounded-lg py-1.5 text-sm text-neutral-600"
                      onClick={() => setOpen(false)}
                    >
                      {b.boardName}
                    </Link>
                  ))}
                </div>
              )}
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
                    className="rounded-xl px-3 py-2.5 text-neutral-700"
                    onClick={() => setOpen(false)}
                  >
                    마이페이지
                  </Link>
                  
                  {/* 모바일에서도 관리자 메뉴 노출 */}
                  {isAdmin && (
                    <Link
                      href="/admin/boards"
                      className="rounded-xl px-3 py-2.5 font-bold text-black bg-neutral-50 flex items-center gap-2"
                      onClick={() => setOpen(false)}
                    >
                      <Settings className="h-4 w-4" />
                      관리자 설정
                    </Link>
                  )}

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