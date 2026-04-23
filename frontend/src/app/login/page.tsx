"use client";

import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import { FormEvent, Suspense, useState } from "react";
import { m, AnimatePresence } from "framer-motion";
import { Loader2, X } from "lucide-react";
import { apiPostJson, apiPutJson } from "@/lib/api";
import { useAuth } from "@/context/AuthContext";
import { GlassPanel } from "@/components/shell/GlassPanel";

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function LoginForm() {
  const router = useRouter();
  const params = useSearchParams();
  const { refresh } = useAuth();
  const registered = params.get("registered");

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  const [findOpen, setFindOpen] = useState(false);
  const [findNick, setFindNick] = useState("");
  const [findResult, setFindResult] = useState("");
  const [findBusy, setFindBusy] = useState(false);

  const [resetOpen, setResetOpen] = useState(false);
  const [resetEmail, setResetEmail] = useState("");
  const [resetPw, setResetPw] = useState("");
  const [resetBusy, setResetBusy] = useState(false);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    setError("");
    if (!EMAIL_RE.test(email.trim())) {
      setError("올바른 이메일 형식이 아닙니다.");
      return;
    }
    if (password.length < 4) {
      setError("비밀번호는 최소 4자 이상입니다.");
      return;
    }
    setLoading(true);
    try {
      await apiPostJson<unknown, { email: string; password: string }>(
        "/api/auth/login",
        { email: email.trim(), password },
      );
      await refresh();
      router.push("/");
    } catch (err) {
      setError(err instanceof Error ? err.message : "로그인에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  }

  async function doFindId() {
    setFindResult("");
    if (!findNick.trim()) return;
    setFindBusy(true);
    try {
      const r = await apiPostJson<string, { nickname: string }>(
        "/api/auth/find-id",
        { nickname: findNick.trim() },
      );
      setFindResult(r.data ?? "");
    } catch (err) {
      setFindResult(
        err instanceof Error ? err.message : "아이디를 찾을 수 없습니다.",
      );
    } finally {
      setFindBusy(false);
    }
  }

  async function doReset() {
    setError("");
    if (!EMAIL_RE.test(resetEmail.trim()) || resetPw.length < 4) return;
    setResetBusy(true);
    try {
      await apiPutJson<unknown, { email: string; newPassword: string }>(
        "/api/auth/reset-password",
        { email: resetEmail.trim(), newPassword: resetPw },
      );
      setResetOpen(false);
      setResetEmail("");
      setResetPw("");
    } catch (err) {
      setError(err instanceof Error ? err.message : "재설정에 실패했습니다.");
    } finally {
      setResetBusy(false);
    }
  }

  const inputClass =
    "w-full rounded-xl border border-neutral-200 bg-white px-4 py-3 text-sm text-neutral-900 outline-none ring-neutral-900/10 placeholder:text-neutral-400 focus:ring-2";

  return (
    <>
      <div className="flex flex-1 items-center justify-center bg-white px-4 py-14 sm:px-6">
        <m.div
          initial={{ opacity: 0, scale: 0.98 }}
          animate={{ opacity: 1, scale: 1 }}
          transition={{ duration: 0.4 }}
          className="w-full max-w-md"
        >
          <GlassPanel className="p-8 sm:p-10">
            <h1 className="text-2xl font-bold text-neutral-900">로그인</h1>
            <p className="mt-2 text-sm text-neutral-600">
              이메일과 비밀번호로 입장하세요.
            </p>
            {registered && (
              <p className="mt-4 rounded-xl border border-neutral-200 bg-neutral-50 px-4 py-2 text-sm text-neutral-800">
                회원가입이 완료되었습니다. 로그인해 주세요.
              </p>
            )}

            <form onSubmit={handleSubmit} className="mt-8 space-y-5">
              <div>
                <label className="mb-1.5 block text-sm font-medium text-neutral-700">
                  아이디 (이메일)
                </label>
                <input
                  type="email"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  className={inputClass}
                  autoComplete="email"
                />
              </div>
              <div>
                <label className="mb-1.5 block text-sm font-medium text-neutral-700">
                  비밀번호
                </label>
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className={inputClass}
                  autoComplete="current-password"
                />
              </div>
              {error && (
                <p className="text-sm text-neutral-700">{error}</p>
              )}
              <div className="flex flex-col gap-3 sm:flex-row sm:flex-wrap">
                <button
                  type="submit"
                  disabled={loading}
                  className="inline-flex h-12 flex-1 items-center justify-center gap-2 rounded-xl border border-black bg-black px-6 text-sm font-semibold text-white disabled:opacity-50"
                >
                  {loading && <Loader2 className="h-4 w-4 animate-spin" />}
                  로그인
                </button>
                <button
                  type="button"
                  onClick={() => setFindOpen(true)}
                  className="h-12 rounded-xl border border-gray-200 bg-white px-6 text-sm font-medium text-gray-800 hover:bg-blue-50"
                >
                  아이디 찾기
                </button>
                <button
                  type="button"
                  onClick={() => setResetOpen(true)}
                  className="h-12 rounded-xl border border-gray-200 bg-white px-6 text-sm font-medium text-gray-800 hover:bg-blue-50"
                >
                  비밀번호 재설정
                </button>
              </div>
            </form>

            <p className="mt-8 text-center text-sm text-neutral-600">
              계정이 없으신가요?{" "}
              <Link
                href="/signup"
                className="font-medium text-neutral-900 underline-offset-4 hover:underline"
              >
                회원가입
              </Link>
            </p>
          </GlassPanel>
        </m.div>
      </div>

      <AnimatePresence>
        {findOpen && (
          <m.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-[100] flex items-center justify-center bg-black/40 p-4 backdrop-blur-sm"
            onClick={() => setFindOpen(false)}
          >
            <m.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="w-full max-w-md rounded-2xl border border-neutral-200 bg-white p-6 shadow-2xl"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="flex items-center justify-between">
                <h2 className="text-lg font-semibold text-neutral-900">
                  아이디 찾기
                </h2>
                <button
                  type="button"
                  aria-label="닫기"
                  onClick={() => setFindOpen(false)}
                  className="rounded-lg p-1 text-neutral-500 hover:bg-neutral-100 hover:text-neutral-900"
                >
                  <X className="h-5 w-5" />
                </button>
              </div>
              <p className="mt-2 text-sm text-neutral-600">
                가입 시 입력한 닉네임으로 이메일을 찾습니다.
              </p>
              <input
                value={findNick}
                onChange={(e) => setFindNick(e.target.value)}
                placeholder="닉네임"
                className={`${inputClass} mt-4`}
              />
              {findResult && (
                <p className="mt-3 rounded-lg border border-neutral-200 bg-neutral-50 px-3 py-2 text-sm text-neutral-800">
                  {findResult.includes("@")
                    ? `가입된 아이디: ${findResult}`
                    : findResult}
                </p>
              )}
              <button
                type="button"
                disabled={findBusy}
                onClick={() => void doFindId()}
                className="mt-4 flex w-full items-center justify-center gap-2 rounded-xl border border-black bg-black py-3 text-sm font-semibold text-white disabled:opacity-50"
              >
                {findBusy && <Loader2 className="h-4 w-4 animate-spin" />}
                확인
              </button>
            </m.div>
          </m.div>
        )}
      </AnimatePresence>

      <AnimatePresence>
        {resetOpen && (
          <m.div
            initial={{ opacity: 0 }}
            animate={{ opacity: 1 }}
            exit={{ opacity: 0 }}
            className="fixed inset-0 z-[100] flex items-center justify-center bg-black/40 p-4 backdrop-blur-sm"
            onClick={() => setResetOpen(false)}
          >
            <m.div
              initial={{ scale: 0.95, opacity: 0 }}
              animate={{ scale: 1, opacity: 1 }}
              exit={{ scale: 0.95, opacity: 0 }}
              className="w-full max-w-md rounded-2xl border border-neutral-200 bg-white p-6 shadow-2xl"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="flex items-center justify-between">
                <h2 className="text-lg font-semibold text-neutral-900">
                  비밀번호 재설정
                </h2>
                <button
                  type="button"
                  aria-label="닫기"
                  onClick={() => setResetOpen(false)}
                  className="rounded-lg p-1 text-neutral-500 hover:bg-neutral-100 hover:text-neutral-900"
                >
                  <X className="h-5 w-5" />
                </button>
              </div>
              <p className="mt-2 text-sm text-neutral-600">
                이메일과 새 비밀번호를 입력하세요.
              </p>
              <input
                type="email"
                value={resetEmail}
                onChange={(e) => setResetEmail(e.target.value)}
                placeholder="이메일"
                className={`${inputClass} mt-4`}
              />
              <input
                type="password"
                value={resetPw}
                onChange={(e) => setResetPw(e.target.value)}
                placeholder="새 비밀번호 (4자 이상)"
                className={`${inputClass} mt-3`}
              />
              <button
                type="button"
                disabled={resetBusy}
                onClick={() => void doReset()}
                className="mt-4 flex w-full items-center justify-center gap-2 rounded-xl border border-black bg-black py-3 text-sm font-semibold text-white disabled:opacity-50"
              >
                {resetBusy && <Loader2 className="h-4 w-4 animate-spin" />}
                재설정
              </button>
            </m.div>
          </m.div>
        )}
      </AnimatePresence>
    </>
  );
}

export default function LoginPage() {
  return (
    <Suspense fallback={null}>
      <LoginForm />
    </Suspense>
  );
}
