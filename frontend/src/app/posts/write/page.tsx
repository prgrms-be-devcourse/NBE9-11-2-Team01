"use client";

import { useRouter, useSearchParams } from "next/navigation";
import { FormEvent, useEffect, useState } from "react";
import { apiGet, apiPostJson } from "@/lib/api";

type ApiResponse<T> = { success: boolean; code: string | null; message: string | null; data: T; };
type Category = { id: number; name: string; boardId: number; };
type PostWriteResponse = { id: number; };

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
        setErrorMessage("게시판 정보가 없습니다.");
        setIsLoadingMeta(false);
        return;
      }
      try {
        // 로그인 체크 및 데이터 로드
        await apiGet("/users/me");
        const json = await apiGet<Category[]>(`/boards/${boardId}/categories`);

        if (json.data) {
          setCategories(json.data);
          if (lockedCategoryId) {
            const exists = json.data.some((c) => String(c.id) === lockedCategoryId);
            if (!exists) throw new Error("카테고리를 찾을 수 없습니다.");
            setCategoryId(lockedCategoryId);
          }
        }
      } catch (error) {
        if (error instanceof Error && error.message.includes("401")) {
          router.push(`/login?next=/posts/write?boardId=${boardId}`);
          return;
        }
        setErrorMessage(error instanceof Error ? error.message : "오류 발생");
      } finally {
        setIsLoadingMeta(false);
      }
    };
    void init();
  }, [boardId, lockedCategoryId, router]);

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    if (!title.trim() || !content.trim() || !boardId || !categoryId) {
      setErrorMessage("모든 필드를 입력해 주세요.");
      return;
    }
    setIsSubmitting(true);
    setErrorMessage("");
    try {
      const json = await apiPostJson<PostWriteResponse, any>("/posts", {
        title: title.trim(),
        content: content.trim(),
        boardId: Number(boardId),
        categoryId: Number(categoryId),
      });
      if (json.success && json.data) router.push(`/posts/${json.data.id}`);
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "작성 실패");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-blue-50/40 px-4 py-8">
      <main className="mx-auto w-full max-w-3xl">
        <header className="mb-6 rounded-2xl border border-gray-200 bg-white px-6 py-5 shadow-sm">
          <p className="text-xs font-semibold uppercase text-gray-400">게시글 작성</p>
          <h1 className="mt-1 text-2xl font-bold text-gray-900">새 게시글 등록</h1>
        </header>

        {errorMessage && (
          <div className="mb-4 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">{errorMessage}</div>
        )}

        <div className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
          <form className="flex flex-col gap-5" onSubmit={onSubmit}>
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold text-gray-700">카테고리</label>
              <select
                value={categoryId}
                onChange={(e) => setCategoryId(e.target.value)}
                disabled={isLoadingMeta || isSubmitting || Boolean(lockedCategoryId)}
                className="h-11 w-full rounded-xl border border-gray-200 bg-gray-50 px-3 text-sm focus:border-gray-400"
              >
                <option value="" hidden>카테고리 선택</option>
                {categories.map((c) => (<option key={c.id} value={c.id}>{c.name}</option>))}
              </select>
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold text-gray-700">제목</label>
              <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} disabled={isSubmitting} className="h-11 rounded-xl border border-gray-200 px-3 text-sm focus:border-blue-300" />
            </div>
            <div className="flex flex-col gap-1.5">
              <label className="text-sm font-semibold text-gray-700">내용</label>
              <textarea value={content} onChange={(e) => setContent(e.target.value)} disabled={isSubmitting} className="min-h-64 rounded-xl border border-gray-200 px-3 py-3 text-sm focus:border-blue-300" />
            </div>
            <div className="flex justify-end gap-2 border-t pt-4">
              <button type="button" onClick={() => router.back()} className="px-5 py-2.5 text-sm text-gray-600">취소</button>
              <button type="submit" disabled={isSubmitting} className="rounded-xl bg-black px-5 py-2.5 text-sm font-semibold text-white">
                {isSubmitting ? "등록 중..." : "✏️ 등록"}
              </button>
            </div>
          </form>
        </div>
      </main>
    </div>
  );
}