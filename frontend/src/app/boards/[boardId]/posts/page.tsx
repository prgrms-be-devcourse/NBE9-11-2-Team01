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
  const sort = searchParams.get("sort") ?? "latest"; // sort 변수 추가

  const [searchInput, setSearchInput] = useState(keyword);
  const [postPage, setPostPage] = useState<PostPage | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [boardName, setBoardName] = useState("");

  const updateQuery = useCallback(
    (next: { page?: number; keyword?: string; categoryId?: string; sort?: string }) => {
      const nextPage = next.page ?? page;
      const nextKeyword = next.keyword !== undefined ? next.keyword : keyword;
      const nextCategoryId = next.categoryId !== undefined ? next.categoryId : categoryId;
      const nextSort = next.sort ?? sort;

      const query = new URLSearchParams();
      query.set("page", String(Math.max(1, nextPage)));

      if (nextKeyword) query.set("keyword", nextKeyword);
      if (nextCategoryId) query.set("categoryId", nextCategoryId);
      if (nextSort) query.set("sort", nextSort);

      router.push(`/boards/${boardId}/posts?${query.toString()}`);
    },
    [boardId, categoryId, keyword, page, router, sort]
  );

  const fetchPosts = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const query = new URLSearchParams();
      query.set("page", String(page - 1)); // API가 0-based index일 경우를 대비 (필요시 수정)
      if (keyword) query.set("keyword", keyword);
      if (categoryId) query.set("categoryId", categoryId);
      if (sort) query.set("sort", sort);

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
  }, [boardId, categoryId, keyword, page, sort]);

  useEffect(() => {
    setSearchInput(keyword);
  }, [keyword]);

  useEffect(() => {
    fetchPosts();
  }, [fetchPosts]);

  const pageNumbers = useMemo(() => {
    if (!postPage || postPage.totalPages <= 0) return [];

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
    <div className="min-h-screen bg-blue-50/40 px-4 py-8">
      <main className="mx-auto flex w-full max-w-6xl flex-col gap-5">
        <header className="rounded-2xl border border-gray-200 bg-white px-6 py-5 shadow-sm">
          <div className="mb-4">
            <p className="text-xs font-semibold uppercase tracking-widest text-gray-400">게시글 목록</p>
            <h1 className="mt-1 text-2xl font-bold text-gray-900">{boardName || `게시판 #${boardId}`}</h1>
          </div>
          <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
            <div className="flex items-center gap-2">
              <div className="flex h-10 items-center gap-2 rounded-xl border border-gray-200 bg-white px-4 shadow-inner">
                <svg className="h-4 w-4 shrink-0 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-4.35-4.35M17 11A6 6 0 1 1 5 11a6 6 0 0 1 12 0z" />
                </svg>
                <input
                  type="text"
                  value={searchInput}
                  onChange={(event) => setSearchInput(event.target.value)}
                  onKeyDown={onSearchEnter}
                  placeholder="제목 검색 (Enter)"
                  className="w-56 bg-transparent text-sm outline-none placeholder:text-gray-400"
                />
              </div>
              <button
                type="button"
                onClick={() => updateQuery({ page: 1, keyword: searchInput.trim() })}
                className="h-10 rounded-xl border border-black bg-black px-5 text-sm font-medium text-white transition-colors hover:bg-gray-800"
              >
                검색
              </button>
              {keyword && (
                <button
                  type="button"
                  onClick={() => updateQuery({ page: 1, keyword: "" })}
                  className="h-10 rounded-xl border border-gray-200 px-4 text-sm text-gray-500 transition-colors hover:bg-blue-50"
                >
                  검색 초기화
                </button>
              )}
            </div>

            <div className="flex items-center gap-1 rounded-xl border border-gray-200 bg-blue-50/70 p-1">
              <button
                type="button"
                onClick={() => updateQuery({ page: 1, sort: "latest" })}
                className={`rounded-xl px-4 py-1.5 text-sm font-medium transition-all ${
                  sort === "latest" ? "bg-white text-gray-900 shadow-sm" : "text-gray-500 hover:text-gray-700"
                }`}
              >
                최신순
              </button>
              <button
                type="button"
                onClick={() => updateQuery({ page: 1, sort: "likes" })}
                className={`rounded-xl px-4 py-1.5 text-sm font-medium transition-all ${
                  sort === "likes" ? "bg-white text-gray-900 shadow-sm" : "text-gray-500 hover:text-gray-700"
                }`}
              >
                인기순
              </button>
            </div>
          </div>
        </header>

        <section className="grid gap-6 md:grid-cols-[220px_1fr]">
          <aside className="rounded-xl border border-zinc-200 bg-white p-4 h-fit">
            <p className="mb-3 text-sm font-semibold text-zinc-700">카테고리</p>
            <div className="flex flex-col gap-1">
              <button
                type="button"
                onClick={() => updateQuery({ page: 1, categoryId: "" })}
                className={`rounded-xl px-3 py-2 text-left text-sm font-medium transition-colors ${
                  !categoryId ? "bg-black text-white" : "text-gray-600 hover:bg-blue-50"
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
                    className={`rounded-xl px-3 py-2 text-left text-sm font-medium transition-colors ${
                      active ? "bg-black text-white" : "text-gray-600 hover:bg-blue-50"
                    }`}
                  >
                    {category.name}
                  </button>
                );
              })}
            </div>
            <p className="mt-4 text-[10px] leading-relaxed text-zinc-400">* 현재 페이지 데이터 기준 카테고리 노출</p>
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
                {postPage?.posts.map((post) => (
                  <li key={post.id} className="group transition-colors hover:bg-blue-50/50">
                    <Link
                      href={`/boards/${boardId}/posts/${post.id}`}
                      className="flex items-center justify-between gap-4 px-3 py-4"
                    >
                      <div className="min-w-0">
                        <p className="truncate font-medium text-zinc-900 group-hover:text-blue-600">{post.title}</p>
                        <p className="mt-1 text-xs text-zinc-500">{post.categoryName}</p>
                      </div>
                      <div className="shrink-0 text-right text-xs text-zinc-500">
                        <p className="font-medium text-zinc-700">{post.author}</p>
                        <p>👍 {post.likeCount}</p>
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
                  onClick={() => updateQuery({ page: page - 1 })}
                  className="rounded-xl border border-gray-200 px-3 py-1.5 text-sm text-gray-600 transition-colors hover:bg-blue-50 disabled:cursor-not-allowed disabled:opacity-40"
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
                      className={`rounded-xl px-3 py-1.5 text-sm font-medium transition-colors ${
                        active ? "bg-black text-white" : "border border-gray-200 text-gray-600 hover:bg-blue-50"
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
                  className="rounded-xl border border-gray-200 px-3 py-1.5 text-sm text-gray-600 transition-colors hover:bg-blue-50 disabled:cursor-not-allowed disabled:opacity-40"
                >
                  다음
                </button>
              </div>

              <Link
                href={`/boards/${boardId}/write`}
                className="rounded-xl border border-black bg-black px-5 py-2 text-sm font-semibold text-white transition-colors hover:bg-gray-800"
              >
                ✏️ 글쓰기
              </Link>
            </div>
          </div>
        </section>
      </main>
    </div>
  );
}