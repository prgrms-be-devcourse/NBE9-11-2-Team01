/**
 * 게시글 작성 페이지
 */
"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";

type ApiResponse<T> = {
  success: boolean;
  code: string | null;
  message: string | null;
  data: T;
};

type Category = {
  id: number;
  name: string;
  boardId: number;
};

type PostWriteResponse = {
  id: number;
};

type MyPageResponse = {
  email: string;
  nickname: string;
  profileImage: string;
  role: string;
};

function getApiBaseUrl() {
  return process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
}

export default function PostWritePage() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const boardId = searchParams.get("boardId")?.trim() ?? "";
  const lockedCategoryId = searchParams.get("categoryId")?.trim() ?? "";

  const [categories, setCategories] = useState<Category[]>([]);
  const [title, setTitle] = useState("");
  const [content, setContent] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [isLoadingMeta, setIsLoadingMeta] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    const init = async () => {
      if (!boardId) {
        setErrorMessage("게시판 정보가 없습니다. 게시판 목록에서 다시 접근해 주세요.");
        setIsLoadingMeta(false);
        return;
      }

    // 로그인 여부 확인
    const meRes = await fetch(`${getApiBaseUrl()}/api/users/me`, {
      method: "GET",
      credentials: "include",
    });
    if (!meRes.ok) {
      router.push(`/login?next=/posts/write?boardId=${boardId}`);
      return;
    }

    // 카테고리 조회
    setIsLoadingMeta(true);
    setErrorMessage("");

    try {
      const res = await fetch(`${getApiBaseUrl()}/boards/${boardId}/categories`, {
        method: "GET",
        credentials: "include",
      });

      if (!res.ok) {
        throw new Error(`카테고리 정보를 불러오지 못했습니다. (${res.status})`);
      }

      const json = (await res.json()) as ApiResponse<Category[]>;
      if (!json.success) {
        throw new Error(json.message ?? "카테고리 목록 조회에 실패했습니다.");
      }

      setCategories(json.data);
      if (lockedCategoryId) {
          const exists = json.data.some((category) => String(category.id) === lockedCategoryId);
          if (!exists) {
            throw new Error("선택한 카테고리를 찾을 수 없습니다. 카테고리를 다시 확인해 주세요.");
          }
          setCategoryId(lockedCategoryId);
        } else {
          setCategoryId("");
        }
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "카테고리 조회 중 오류가 발생했습니다.");
    } finally {
      setIsLoadingMeta(false);
    }
  };

  init();
}, [boardId, lockedCategoryId, router]);

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();

    if (!title.trim() || !content.trim() || !boardId || !categoryId) {
      setErrorMessage("제목, 내용, 카테고리를 모두 입력해 주세요.");
      return;
    }

    setIsSubmitting(true);
    setErrorMessage("");

    try {
      const res = await fetch(`${getApiBaseUrl()}/posts`, {
        method: "POST",
        credentials: "include",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          title: title.trim(),
          content: content.trim(),
          boardId: Number(boardId),
          categoryId: Number(categoryId),
        }),
      });

      if (!res.ok) {
        if (res.status === 401) throw new Error("로그인이 필요합니다. 로그인 후 다시 시도해 주세요.");
        throw new Error(`게시글 작성에 실패했습니다. (${res.status})`);
      }

      const json = (await res.json()) as ApiResponse<PostWriteResponse>;

      if (!json.success) {
        throw new Error(json.message ?? "게시글 작성에 실패했습니다.");
      }

      router.push(`/posts/${json.data.id}`);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "알 수 없는 오류가 발생했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 px-4 py-8">
      <main className="mx-auto w-full max-w-3xl">
        <header className="mb-6 rounded-2xl border border-gray-200 bg-white px-6 py-5 shadow-sm">
          <p className="text-xs font-semibold uppercase tracking-widest text-gray-400">게시글 작성</p>
          <h1 className="mt-1 text-2xl font-bold text-gray-900">새 게시글 등록</h1>
        </header>

        {errorMessage && (
          <div className="mb-4 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
            {errorMessage}
          </div>
        )}

        <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
          <form className="flex flex-col gap-5" onSubmit={onSubmit}>
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold text-gray-700">카테고리</label>
              <div className="relative">
                <select
                  value={categoryId}
                  onChange={(e) => setCategoryId(e.target.value)}
                  disabled={isLoadingMeta || isSubmitting || categories.length === 0 || Boolean(lockedCategoryId)}
                  className={`h-11 w-full appearance-none rounded-xl border border-gray-200 bg-gray-50 pl-3 pr-10 text-sm text-gray-900 outline-none focus:border-gray-400 disabled:cursor-not-allowed disabled:opacity-60 ${Boolean(lockedCategoryId) ? "appearance-none" : ""}`}
                >
                  {categories.length === 0 ? (
                    <option value="">선택 가능한 카테고리가 없습니다</option>
                  ) : (
                    <>
                      <option value="" hidden>카테고리를 선택해주세요</option>
                      {categories.map((category) => (
                        <option key={category.id} value={category.id}>
                          {category.name}
                        </option>
                      ))}
                    </>
                  )}
                </select>
                {!lockedCategoryId && (
                  <div className="pointer-events-none absolute inset-y-0 right-3 flex items-center">
                    <svg className="h-4 w-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                    </svg>
                  </div>
                )}
              </div>
              {lockedCategoryId && (
                <p className="text-xs text-gray-400">선택된 카테고리로만 작성할 수 있습니다.</p>
              )}
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold text-gray-700">제목</label>
              <input
                type="text"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                maxLength={100}
                placeholder="제목을 입력하세요"
                disabled={isLoadingMeta || isSubmitting}
                className="h-11 rounded-xl border border-gray-200 px-3 text-sm text-gray-900 outline-none placeholder:text-gray-400 focus:border-gray-400 disabled:bg-gray-50 disabled:opacity-60"
              />
            </div>

            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold text-gray-700">내용</label>
              <textarea
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="내용을 입력하세요"
                disabled={isLoadingMeta || isSubmitting}
                className="min-h-64 rounded-xl border border-gray-200 px-3 py-3 text-sm text-gray-900 outline-none placeholder:text-gray-400 focus:border-gray-400 disabled:bg-gray-50 disabled:opacity-60"
              />
            </div>

            <div className="flex items-center justify-end gap-2 border-t border-gray-100 pt-4">
              <button
                type="button"
                onClick={() => router.back()}
                disabled={isSubmitting}
                className="rounded-xl border border-gray-200 px-5 py-2.5 text-sm text-gray-600 transition-colors hover:bg-gray-100 disabled:cursor-not-allowed disabled:opacity-50"
              >
                취소
              </button>
              <button
                type="submit"
                disabled={isLoadingMeta || isSubmitting}
                className="rounded-xl bg-gray-900 px-5 py-2.5 text-sm font-semibold text-white transition-colors hover:bg-gray-700 disabled:cursor-not-allowed disabled:opacity-50"
              >
                {isSubmitting ? "등록 중..." : "✏️ 등록"}
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}