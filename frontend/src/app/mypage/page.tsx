"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { useCallback, useEffect, useRef, useState } from "react";
import { m } from "framer-motion";
import { Loader2 } from "lucide-react";
import { apiDelete, apiGet, apiPutJson } from "@/lib/api";
import type { MyPageUser } from "@/lib/types";
import { useAuth } from "@/context/AuthContext";
import { GlassPanel } from "@/components/shell/GlassPanel";
import { profileImageUrl } from "@/lib/profileImage";

const inputClass =
  "w-full rounded-xl border border-neutral-200 bg-white px-4 py-3 text-sm text-neutral-900 outline-none ring-neutral-900/10 placeholder:text-neutral-400 focus:ring-2";

export default function MyPage() {
  const router = useRouter();
  const { refresh, logout } = useAuth();
  const fileRef = useRef<HTMLInputElement>(null);

  const [user, setUser] = useState<MyPageUser | null>(null);
  const [nickname, setNickname] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPw, setConfirmPw] = useState("");
  const [preview, setPreview] = useState<string | null>(null);
  const [pendingImageData, setPendingImageData] = useState<string | null>(null);
  const [msg, setMsg] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const load = useCallback(async () => {
    try {
      const r = await apiGet<MyPageUser>("/users/me");
      const u = r.data ?? null;
      setUser(u);
      if (u) {
        setNickname(u.nickname);
        setPreview(u.profileImage);
      }
    } catch {
      router.replace("/login");
    } finally {
      setLoading(false);
    }
  }, [router]);

  useEffect(() => {
    void load();
  }, [load]);

  const onFile = (f: File | null) => {
    if (!f || !f.type.startsWith("image/")) return;
    if (f.size > 280_000) {
      setMsg("이미지는 약 280KB 이하로 올려 주세요.");
      return;
    }
    const reader = new FileReader();
    reader.onload = () => {
      const url = reader.result as string;
      setPreview(url);
      setPendingImageData(url);
    };
    reader.readAsDataURL(f);
  };

  const save = async (e: React.FormEvent) => {
    e.preventDefault();
    setMsg("");
    if (!nickname.trim()) {
      setMsg("닉네임을 입력해 주세요.");
      return;
    }
    if (newPassword && newPassword !== confirmPw) {
      setMsg("비밀번호 확인이 일치하지 않습니다.");
      return;
    }
    if (newPassword && newPassword.length < 4) {
      setMsg("새 비밀번호는 4자 이상이어야 합니다.");
      return;
    }
    setSaving(true);
    try {
      if (pendingImageData) {
        await apiPutJson<unknown, { profileImage: string }>(
          "/users/me/profile-image",
          { profileImage: pendingImageData },
        );
        setPendingImageData(null);
      }
      await apiPutJson<unknown, { nickname: string; newPassword?: string }>(
        "/users/me/info",
        {
          nickname: nickname.trim(),
          ...(newPassword ? { newPassword } : {}),
        },
      );
      setNewPassword("");
      setConfirmPw("");
      setMsg("저장되었습니다.");
      await load();
      await refresh();
    } catch (err) {
      setMsg(err instanceof Error ? err.message : "저장에 실패했습니다.");
    } finally {
      setSaving(false);
    }
  };

  const withdraw = async () => {
    if (!confirm("정말 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.")) return;
    try {
      await apiDelete("/auth/withdraw");
      await logout();
      router.replace("/");
    } catch (err) {
      setMsg(err instanceof Error ? err.message : "탈퇴에 실패했습니다.");
    }
  };

  if (loading) {
    return (
      <div className="flex flex-1 items-center justify-center bg-white py-24">
        <Loader2 className="h-10 w-10 animate-spin text-neutral-400" />
      </div>
    );
  }

  if (!user) return null;

  return (
    <div className="flex flex-1 justify-center bg-white px-4 py-14 sm:px-6">
      <m.div
        initial={{ opacity: 0, y: 16 }}
        animate={{ opacity: 1, y: 0 }}
        className="w-full max-w-4xl"
      >
        <GlassPanel className="overflow-hidden">
          <div className="grid gap-0 lg:grid-cols-[280px_1fr]">
            <div className="flex flex-col items-center border-b border-neutral-200 bg-neutral-50 p-8 lg:border-b-0 lg:border-r">
              <button
                type="button"
                className="relative flex h-40 w-40 cursor-pointer items-center justify-center overflow-hidden rounded-2xl border border-dashed border-neutral-300 bg-white"
                onClick={() => fileRef.current?.click()}
              >
                {/* eslint-disable-next-line @next/next/no-img-element */}
                <img
                  src={profileImageUrl(preview)}
                  alt=""
                  className="h-full w-full object-cover"
                />
                <span className="absolute bottom-2 rounded-xl bg-black/75 px-3 py-1 text-xs text-white">
                  변경
                </span>
              </button>
              <input
                ref={fileRef}
                type="file"
                accept="image/*"
                className="hidden"
                onChange={(e) => onFile(e.target.files?.[0] ?? null)}
              />
              <p className="mt-4 text-center text-sm text-neutral-600">
                프로필 사진 수정
              </p>
              <p className="mt-1 text-center text-xs text-neutral-500">
                미등록 시 기본 이미지가 표시됩니다
              </p>
            </div>

            <form onSubmit={save} className="p-8">
              <h1 className="text-2xl font-bold text-neutral-900">마이페이지</h1>
              <p className="mt-2 text-sm text-neutral-600">
                가입 정보를 확인하고 수정할 수 있습니다.
              </p>

              <div className="mt-8 space-y-5">
                <div>
                  <label className="mb-1.5 block text-sm font-medium text-neutral-700">
                    아이디 (가입된 이메일)
                  </label>
                  <input
                    readOnly
                    value={user.email}
                    className="w-full cursor-not-allowed rounded-xl border border-neutral-200 bg-neutral-50 px-4 py-3 text-sm text-neutral-600"
                  />
                </div>
                <div>
                  <label className="mb-1.5 block text-sm font-medium text-neutral-700">
                    닉네임
                  </label>
                  <input
                    value={nickname}
                    onChange={(e) => setNickname(e.target.value)}
                    className={inputClass}
                  />
                </div>
                <div>
                  <label className="mb-1.5 block text-sm font-medium text-neutral-700">
                    새 비밀번호 (선택)
                  </label>
                  <input
                    type="password"
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    className={inputClass}
                    placeholder="변경 시에만 입력"
                    autoComplete="new-password"
                  />
                </div>
                <div>
                  <label className="mb-1.5 block text-sm font-medium text-neutral-700">
                    비밀번호 확인
                  </label>
                  <input
                    type="password"
                    value={confirmPw}
                    onChange={(e) => setConfirmPw(e.target.value)}
                    className={inputClass}
                    autoComplete="new-password"
                  />
                </div>
                <div>
                  <label className="mb-1.5 block text-sm font-medium text-neutral-700">
                    가입일자
                  </label>
                  <input
                    readOnly
                    value={user.createdAt ? user.createdAt.split(" ")[0] : "—"}
                    className="w-full cursor-not-allowed rounded-xl border border-neutral-200 bg-neutral-50 px-4 py-3 text-sm text-neutral-600"
                  />
                </div>
                <div>
                  <label className="mb-1.5 block text-sm font-medium text-neutral-700">
                    권한
                  </label>
                  <input
                    readOnly
                    value={user.role}
                    className="w-full cursor-not-allowed rounded-xl border border-neutral-200 bg-neutral-50 px-4 py-3 text-sm text-neutral-600"
                  />
                </div>
              </div>

              {msg && (
                <p
                  className={`mt-6 rounded-xl border px-4 py-3 text-sm ${
                    msg.includes("저장")
                      ? "border-neutral-200 bg-neutral-50 text-neutral-800"
                      : "border-neutral-300 bg-neutral-100 text-neutral-800"
                  }`}
                >
                  {msg}
                </p>
              )}

              <div className="mt-8 flex flex-wrap gap-3">
                <button
                  type="submit"
                  disabled={saving}
                  className="inline-flex h-12 flex-1 items-center justify-center gap-2 rounded-xl border border-black bg-black px-6 text-sm font-semibold text-white disabled:opacity-50 sm:flex-none"
                >
                  {saving && <Loader2 className="h-4 w-4 animate-spin" />}
                  정보 수정
                </button>
                <button
                  type="button"
                  onClick={() => void withdraw()}
                  className="h-12 rounded-xl border border-gray-200 bg-white px-6 text-sm font-medium text-gray-700 transition hover:bg-blue-50"
                >
                  회원 탈퇴
                </button>
                <Link
                  href="/"
                  className="inline-flex h-12 items-center rounded-xl border border-gray-200 bg-white px-6 text-sm text-gray-800 transition hover:bg-blue-50"
                >
                  홈으로
                </Link>
              </div>
            </form>
          </div>
        </GlassPanel>
      </m.div>
    </div>
  );
}
