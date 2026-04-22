/**
 * 게시글 목록 페이지
 */
"use client";

import Link from "next/link";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import { KeyboardEvent, useCallback, useEffect, useMemo, useState } from "react";

type Post = {
  id: number;
  title: string;
  author: string;
  categoryId: number;
  categoryName: string;
  likeCount: number;
  createdAt: string;
  modifiedAt: string;
};

type PostPage = {
  posts: Post[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  hasNext: boolean;
};

type ApiResponse<T> = {
  success: boolean;
  code: string | null;
  message: string | null;
  data: T;
};

const PAGE_GROUP_SIZE = 5;

function getApiBaseUrl() {
  return process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
}

function formatDate(value: string) {
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

export default function PostListPage() {
  const params = useParams<{ boardId: string }>();
  const boardId = params.boardId;
  const router = useRouter();
  const searchParams = useSearchParams();

  const page = Math.max(1, Number(searchParams.get("page") ?? "1") || 1);
  const keyword = searchParams.get("keyword")?.trim() ?? "";
  const categoryId = searchParams.get("categoryId")?.trim() ?? "";

  const [searchInput, setSearchInput] = useState(keyword);
  const [postPage, setPostPage] = useState<PostPage | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const updateQuery = useCallback(
    (next: { page?: number; keyword?: string; categoryId?: string }) => {
      const nextPage = next.page ?? page;
      const nextKeyword = next.keyword ?? keyword;
      const nextCategoryId = next.categoryId ?? categoryId;

      const query = new URLSearchParams();
      query.set("page", String(Math.max(1, nextPage)));

      if (nextKeyword) {
        query.set("keyword", nextKeyword);
      }

      if (nextCategoryId) {
        query.set("categoryId", nextCategoryId);
      }

      router.push(`/boards/${boardId}/posts?${query.toString()}`);
    },
    [boardId, categoryId, keyword, page, router],
  );

  const fetchPosts = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const query = new URLSearchParams();
      query.set("page", String(page));
      if (keyword) {
        query.set("keyword", keyword);
      }
      if (categoryId) {
        query.set("categoryId", categoryId);
      }

      const res = await fetch(`${getApiBaseUrl()}/boards/${boardId}/posts?${query.toString()}`, {
        method: "GET",
        credentials: "include",
      });

      if (!res.ok) {
        if (res.status === 401) {
          throw new Error("로그인이 필요합니다. 로그인 후 다시 시도해 주세요.");
        }
        throw new Error(`게시글을 불러오지 못했습니다. (${res.status})`);
      }

      const json = (await res.json()) as ApiResponse<PostPage>;

      if (!json.success) {
        throw new Error(json.message ?? "게시글 조회에 실패했습니다.");
      }

      setPostPage(json.data);
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("알 수 없는 오류가 발생했습니다.");
      }
      setPostPage(null);
    } finally {
      setIsLoading(false);
    }
  }, [boardId, categoryId, keyword, page]);

  useEffect(() => {
    setSearchInput(keyword);
  }, [keyword]);

  useEffect(() => {
    fetchPosts();
  }, [fetchPosts]);

  const pageNumbers = useMemo(() => {
    if (!postPage || postPage.totalPages <= 0) {
      return [] as number[];
    }

    const groupIndex = Math.floor((postPage.currentPage - 1) / PAGE_GROUP_SIZE);
    const start = groupIndex * PAGE_GROUP_SIZE + 1;
    const end = Math.min(postPage.totalPages, start + PAGE_GROUP_SIZE - 1);

    return Array.from({ length: end - start + 1 }, (_, idx) => start + idx);
  }, [postPage]);

  const categories = useMemo(() => {
    const source = postPage?.posts ?? [];
    const dedup = new Map<number, string>();

    source.forEach((post) => {
      if (!dedup.has(post.categoryId)) {
        dedup.set(post.categoryId, post.categoryName);
      }
    });

    return Array.from(dedup.entries()).map(([id, name]) => ({ id, name }));
  }, [postPage]);

  const hasPosts = !!postPage && postPage.posts.length > 0;

  const onSearchEnter = (event: KeyboardEvent<HTMLInputElement>) => {
    if (event.key === "Enter") {
      updateQuery({ page: 1, keyword: searchInput.trim() });
    }
  };

  return (
    <div className="min-h-screen bg-zinc-50 px-6 py-8">
      <main className="mx-auto flex w-full max-w-6xl flex-col gap-6">
        <header className="flex flex-col gap-3 rounded-xl border border-zinc-200 bg-white p-5">
          <p className="text-sm text-zinc-500">게시글 목록</p>
          <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
            <div className="flex items-center gap-2">
              <input
                type="text"
                value={searchInput}
                onChange={(event) => setSearchInput(event.target.value)}
                onKeyDown={onSearchEnter}
                placeholder="제목 검색 (Enter)"
                className="h-10 w-72 rounded-full border border-zinc-300 px-4 text-sm outline-none transition focus:border-zinc-500"
              />
              <button
                type="button"
                onClick={() => updateQuery({ page: 1, keyword: searchInput.trim() })}
                className="h-10 rounded-full border border-zinc-300 px-4 text-sm font-medium hover:bg-zinc-100"
              >
                검색
              </button>
              {keyword && (
                <button
                  type="button"
                  onClick={() => updateQuery({ page: 1, keyword: "" })}
                  className="h-10 rounded-full border border-zinc-200 px-4 text-sm text-zinc-500 hover:bg-zinc-100"
                >
                  검색 초기화
                </button>
              )}
            </div>

            <div className="flex items-center gap-2 text-sm">
              <button className="rounded-md bg-zinc-900 px-3 py-1.5 font-semibold text-white">
                최신순
              </button>
              <button className="cursor-not-allowed rounded-md bg-zinc-100 px-3 py-1.5 text-zinc-400">
                인기순(준비중)
              </button>
            </div>
          </div>
        </header>

        <section className="grid gap-6 md:grid-cols-[220px_1fr]">
          <aside className="rounded-xl border border-zinc-200 bg-white p-4">
            <p className="mb-3 text-sm font-semibold text-zinc-700">카테고리</p>
            <div className="flex flex-wrap gap-2">
              <button
                type="button"
                onClick={() => updateQuery({ page: 1, categoryId: "" })}
                className={`rounded-full border px-3 py-1 text-xs ${
                  !categoryId
                    ? "border-zinc-900 bg-zinc-900 text-white"
                    : "border-zinc-300 text-zinc-600 hover:bg-zinc-100"
                }`}
              >
                전체
              </button>
              {categories.map((category) => {
                const active = categoryId === String(category.id);
                return (
                  <button
                    key={category.id}
                    type="button"
                    onClick={() => updateQuery({ page: 1, categoryId: String(category.id) })}
                    className={`rounded-full border px-3 py-1 text-xs ${
                      active
                        ? "border-zinc-900 bg-zinc-900 text-white"
                        : "border-zinc-300 text-zinc-600 hover:bg-zinc-100"
                    }`}
                  >
                    {category.name}
                  </button>
                );
              })}
            </div>
            <p className="mt-3 text-xs text-zinc-400">* 현재 페이지 데이터 기준 카테고리 노출</p>
          </aside>

          <div className="rounded-xl border border-zinc-200 bg-white p-4">
            <div className="mb-4 flex items-center justify-between text-sm text-zinc-500">
              <span>총 {postPage?.totalElements ?? 0}개</span>
              <span>페이지 {postPage?.currentPage ?? page}</span>
            </div>

            {errorMessage && (
              <div className="mb-4 rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-600">
                {errorMessage}
              </div>
            )}

            {isLoading ? (
              <div className="py-12 text-center text-sm text-zinc-400">로딩 중...</div>
            ) : !hasPosts ? (
              <div className="py-12 text-center text-sm text-zinc-400">게시글이 없습니다.</div>
            ) : (
              <ul className="divide-y divide-zinc-200 border-y border-zinc-200">
                {postPage.posts.map((post) => (
                  <li key={post.id} className="transition hover:bg-zinc-50">
                    <Link
                      href={`/posts/${post.id}`}
                      className="flex items-center justify-between gap-4 px-3 py-4"
                    >
                      <div className="min-w-0">
                        <p className="truncate font-medium text-zinc-900">{post.title}</p>
                        <p className="mt-1 text-xs text-zinc-500">{post.categoryName}</p>
                      </div>
                      <div className="shrink-0 text-right text-xs text-zinc-500">
                        <p>{post.author}</p>
                        <p>좋아요 {post.likeCount}</p>
                        <p>{formatDate(post.createdAt)}</p>
                      </div>
                    </Link>
                  </li>
                ))}
              </ul>
            )}

            <div className="mt-6 flex items-center justify-between">
              <div className="flex items-center gap-2">
                <button
                  type="button"
                  disabled={!postPage || postPage.currentPage <= 1}
                  onClick={() => updateQuery({ page: Math.max(1, page - 1) })}
                  className="rounded border border-zinc-300 px-3 py-1 text-sm disabled:cursor-not-allowed disabled:opacity-40"
                >
                  이전
                </button>
                {pageNumbers.map((number) => {
                  const active = number === postPage?.currentPage;
                  return (
                    <button
                      key={number}
                      type="button"
                      onClick={() => updateQuery({ page: number })}
                      className={`rounded px-3 py-1 text-sm ${
                        active ? "bg-zinc-900 text-white" : "border border-zinc-300 text-zinc-700"
                      }`}
                    >
                      {number}
                    </button>
                  );
                })}
                <button
                  type="button"
                  disabled={!postPage || !postPage.hasNext}
                  onClick={() => updateQuery({ page: page + 1 })}
                  className="rounded border border-zinc-300 px-3 py-1 text-sm disabled:cursor-not-allowed disabled:opacity-40"
                >
                  다음
                </button>
              </div>

              <Link href="/posts/write" className="rounded-md bg-zinc-900 px-5 py-2 text-sm font-semibold text-white hover:bg-zinc-700">
                글쓰기
              </Link>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}
