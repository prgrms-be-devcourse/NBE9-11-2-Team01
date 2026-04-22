"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useCallback, useRef, useState } from "react";
import { m } from "framer-motion";
import { Check, Loader2, X } from "lucide-react";
import { apiGet, apiPostJson } from "@/lib/api";
import { GlassPanel } from "@/components/shell/GlassPanel";
import { profileImageUrl } from "@/lib/profileImage";

const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const inputClass =
  "w-full rounded-xl border border-neutral-200 bg-white px-4 py-3 text-sm text-neutral-900 outline-none ring-neutral-900/10 placeholder:text-neutral-400 focus:ring-2";

export default function SignupPage() {
  const router = useRouter();
  const fileRef = useRef<HTMLInputElement>(null);
  const [email, setEmail] = useState("");
  const [nickname, setNickname] = useState("");
  const [password, setPassword] = useState("");
  const [confirm, setConfirm] = useState("");
  const [profilePreview, setProfilePreview] = useState<string | null>(null);
  const [profileDataUrl, setProfileDataUrl] = useState<string | null>(null);
  const [emailStatus, setEmailStatus] = useState<"idle" | "ok" | "bad" | "checking">(
    "idle",
  );
  const [msg, setMsg] = useState("");
  const [loading, setLoading] = useState(false);

  const onFile = useCallback((f: File | null) => {
    if (!f || !f.type.startsWith("image/")) return;
    if (f.size > 280_000) {
      setMsg("프로필 이미지는 약 280KB 이하로 올려 주세요.");
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      const url = reader.result as string;
      setProfilePreview(url);
      setProfileDataUrl(url);
    };
    reader.readAsDataURL(f);
  }, []);

  const checkEmail = async () => {
    setMsg("");
    if (!EMAIL_RE.test(email.trim())) {
      setMsg("올바른 이메일 형식인지 확인해 주세요.");
      setEmailStatus("bad");
      return;
    }
    setEmailStatus("checking");
    try {
      const r = await apiGet<boolean>(
        `/api/auth/check-email?email=${encodeURIComponent(email.trim())}`,
      );
      const ok = r.data === true;
      setEmailStatus(ok ? "ok" : "bad");
      if (!ok) setMsg("이미 사용 중이거나 사용할 수 없는 이메일입니다.");
    } catch (e) {
      setEmailStatus("bad");
      setMsg(e instanceof Error ? e.message : "중복 확인에 실패했습니다.");
    }
  };

  const submit = async (e: React.FormEvent) => {
    e.preventDefault();
    setMsg("");
    if (!EMAIL_RE.test(email.trim())) {
      setMsg("이메일(아이디)를 확인해 주세요.");
      return;
    }
    if (password.length < 8) {
      setMsg("비밀번호는 8자 이상이어야 합니다.");
      return;
    }
    if (password !== confirm) {
      setMsg("비밀번호 확인이 일치하지 않습니다.");
      return;
    }
    if (!nickname.trim()) {
      setMsg("닉네임을 입력해 주세요.");
      return;
    }
    setLoading(true);
    try {
      await apiPostJson<unknown, Record<string, unknown>>("/api/auth/signup", {
        email: email.trim(),
        password,
        nickname: nickname.trim(),
        profileImage: profileDataUrl ?? undefined,
      });
      router.push("/login?registered=1");
    } catch (err) {
      setMsg(err instanceof Error ? err.message : "회원가입에 실패했습니다.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex flex-1 items-center justify-center bg-white px-4 py-14 sm:px-6">
      <m.div
        initial={{ opacity: 0, y: 20 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.45 }}
        className="w-full max-w-4xl"
      >
        <GlassPanel className="overflow-hidden">
          <div className="grid gap-0 lg:grid-cols-[280px_1fr]">
            <div className="flex flex-col items-center border-b border-neutral-200 bg-neutral-50 p-8 lg:border-b-0 lg:border-r">
              <div
                className="relative flex h-40 w-40 cursor-pointer items-center justify-center overflow-hidden rounded-2xl border border-dashed border-neutral-300 bg-neutral-50 transition hover:border-neutral-500"
                onClick={() => fileRef.current?.click()}
                role="button"
                tabIndex={0}
                onKeyDown={(e) => e.key === "Enter" && fileRef.current?.click()}
              >
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  src={profileImageUrl(profilePreview)}
                  alt=""
                  className="h-full w-full object-cover"
                />
                <span className="pointer-events-none absolute inset-x-0 bottom-0 bg-gradient-to-t from-black/45 to-transparent py-2 text-center text-[10px] font-medium text-white">
                  사진 등록
                </span>
              </div>
              <input
                ref={fileRef}
                type="file"
                accept="image/*"
                className="hidden"
                onChange={(e) => onFile(e.target.files?.[0] ?? null)}
              />
              <p className="mt-4 text-center text-sm text-neutral-600">
                프로필 사진 등록
              </p>
              <p className="mt-1 text-center text-xs text-neutral-500">
                선택 · 작은 용량 권장
              </p>
            </div>

            <form onSubmit={submit} className="p-8">
              <h1 className="text-2xl font-bold text-neutral-900">회원가입</h1>
              <p className="mt-2 text-sm text-neutral-600">
                이메일이 로그인 아이디로 사용됩니다.
              </p>

              <div className="mt-8 space-y-5">
                <div>
                  <label className="mb-1.5 block text-sm font-medium text-neutral-700">
                    아이디 (이메일)
                  </label>
                  <div className="flex gap-2">
                    <input
                      type="email"
                      value={email}
                      onChange={(e) => {
                        setEmail(e.target.value);
                        setEmailStatus("idle");
                      }}
                      className={`min-w-0 flex-1 ${inputClass}`}
                      placeholder="you@example.com"
                      autoComplete="email"
                    />
                    <button
                      type="button"
                      onClick={() => void checkEmail()}
                      className="shrink-0 rounded-xl border border-neutral-300 bg-neutral-100 px-4 text-sm font-medium text-neutral-900 transition hover:bg-neutral-200"
                    >
                      CHECK
                    </button>
                  </div>
                  {emailStatus === "ok" && (
                    <p className="mt-1 flex items-center gap-1 text-xs text-neutral-700">
                      <Check className="h-3.5 w-3.5" /> 사용 가능한 이메일입니다.
                    </p>
                  )}
                  {emailStatus === "bad" && (
                    <p className="mt-1 flex items-center gap-1 text-xs text-neutral-600">
                      <X className="h-3.5 w-3.5" /> 확인이 필요합니다.
                    </p>
                  )}
                  {emailStatus === "checking" && (
                    <p className="mt-1 flex items-center gap-1 text-xs text-neutral-500">
                      <Loader2 className="h-3.5 w-3.5 animate-spin" /> 확인 중…
                    </p>
                  )}
                </div>

                <div>
                  <label className="mb-1.5 block text-sm font-medium text-neutral-700">
                    닉네임
                  </label>
                  <input
                    value={nickname}
                    onChange={(e) => setNickname(e.target.value)}
                    className={inputClass}
                    placeholder="커뮤니티에서 쓸 이름"
                    autoComplete="nickname"
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
                    placeholder="8자 이상"
                    autoComplete="new-password"
                  />
                </div>

                <div>
                  <label className="mb-1.5 block text-sm font-medium text-neutral-700">
                    비밀번호 확인
                  </label>
                  <input
                    type="password"
                    value={confirm}
                    onChange={(e) => setConfirm(e.target.value)}
                    className={inputClass}
                    placeholder="한 번 더 입력"
                    autoComplete="new-password"
                  />
                </div>
              </div>

              {msg && (
                <p className="mt-6 rounded-xl border border-neutral-200 bg-neutral-50 px-4 py-3 text-sm text-neutral-800">
                  {msg}
                </p>
              )}

              <div className="mt-8 flex flex-wrap gap-3">
                <button
                  type="submit"
                  disabled={loading}
                  className="inline-flex h-12 flex-1 items-center justify-center gap-2 rounded-full bg-neutral-900 px-6 text-sm font-semibold text-white shadow-md disabled:opacity-50 sm:flex-none"
                >
                  {loading && <Loader2 className="h-4 w-4 animate-spin" />}
                  회원가입
                </button>
                <Link
                  href="/login"
                  className="inline-flex h-12 items-center justify-center rounded-full border border-neutral-300 bg-white px-6 text-sm font-medium text-neutral-800 transition hover:bg-neutral-50"
                >
                  아이디 찾기
                </Link>
              </div>

              <p className="mt-6 text-center text-sm text-neutral-600">
                이미 계정이 있나요?{" "}
                <Link
                  href="/login"
                  className="font-medium text-neutral-900 underline-offset-4 hover:underline"
                >
                  로그인
                </Link>
              </p>
            </form>
          </div>
        </GlassPanel>
      </m.div>
    </div>
  );
}
