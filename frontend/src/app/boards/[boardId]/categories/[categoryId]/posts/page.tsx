/**
 * 카테고리별 게시글 목록 페이지
 */
"use client";

import Link from "next/link";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import { KeyboardEvent, useCallback, useEffect, useMemo, useState } from "react";

type Post = {
  id: number;
  title: string;
  author: string;
  profileImage: string | null;
  categoryId: number;
  categoryName: string;
  likeCount: number;
  createdAt: string;
};

type PostPage = {
  posts: Post[];
  currentPage: number;
  totalPages: number;
  totalElements: number;
  hasNext: boolean;
};

type Category = {
  id: number;
  name: string;
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
  if (Number.isNaN(date.getTime())) return value;

  return new Intl.DateTimeFormat("ko-KR", {
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
  }).format(date);
}

export default function CategoryPostListPage() {
  const params = useParams<{ boardId: string; categoryId: string }>();
  const boardId = params.boardId;
  const categoryId = params.categoryId;
  const router = useRouter();
  const searchParams = useSearchParams();

  const page = Math.max(1, Number(searchParams.get("page") ?? "1") || 1);
  const keyword = searchParams.get("keyword")?.trim() ?? "";
  const sort = searchParams.get("sort")?.trim() ?? "latest";

  const [searchInput, setSearchInput] = useState(keyword);
  const [categoryName, setCategoryName] = useState("");
  const [postPage, setPostPage] = useState<PostPage | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const updateQuery = useCallback(
    (next: { page?: number; keyword?: string; sort?: string }) => {
      const nextPage = next.page ?? page;
      const nextKeyword = next.keyword ?? keyword;

      const query = new URLSearchParams();
      query.set("page", String(Math.max(1, nextPage)));

      const nextSort = next.sort ?? sort;
      query.set("sort", nextSort);

      if (nextKeyword) {
        query.set("keyword", nextKeyword);
      }

      router.push(`/boards/${boardId}/categories/${categoryId}/posts?${query.toString()}`);
    },
    [boardId, categoryId, keyword, page, router],
  );

  const fetchCategory = useCallback(async () => {
    try {
      const res = await fetch(`${getApiBaseUrl()}/boards/${boardId}/categories`, {
        method: "GET",
        credentials: "include",
      });

      if (!res.ok) {
        return;
      }

      const json = (await res.json()) as ApiResponse<Category[]>;
      if (!json.success) {
        return;
      }

      const selected = json.data.find((category) => String(category.id) === categoryId);
      if (selected) {
        setCategoryName(selected.name);
      }
    } catch {
      // 카테고리명 조회 실패는 목록 렌더링에 영향이 없으므로 무시
    }
  }, [boardId, categoryId]);

  const fetchPosts = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const query = new URLSearchParams();
      query.set("page", String(page));
      query.set("categoryId", categoryId);
      if (keyword) {
        query.set("keyword", keyword);
      }

      query.set("sort", sort);

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

      if (!categoryName && json.data.posts.length > 0) {
        setCategoryName(json.data.posts[0].categoryName);
      }
    } catch (error) {
      setErrorMessage(error instanceof Error ? error.message : "알 수 없는 오류가 발생했습니다.");
      setPostPage(null);
    } finally {
      setIsLoading(false);
    }
  }, [boardId, categoryId, categoryName, keyword, page, sort]);

  useEffect(() => {
    setSearchInput(keyword);
  }, [keyword]);

  useEffect(() => {
    fetchCategory();
  }, [fetchCategory]);

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
              <button
                type="button"
                onClick={() => updateQuery({ page: 1, sort: "latest" })}
                className={`rounded-md px-3 py-1.5 text-sm font-semibold ${
                    sort === "latest" ? "bg-zinc-900 text-white" : "bg-zinc-100 text-zinc-600 hover:bg-zinc-200"
                }`}
                >
                    최신순
                </button>
                <button
                type="button"
                onClick={() => updateQuery({ page: 1, sort: "likes" })}
                className={`rounded-md px-3 py-1.5 text-sm font-semibold ${
                    sort === "likes" ? "bg-zinc-900 text-white" : "bg-zinc-100 text-zinc-600 hover:bg-zinc-200"
                }`}
                >
                    인기순
                </button>
            </div>
          </div>
        </header>

        <section className="grid gap-6 md:grid-cols-[220px_1fr]">
          <aside className="rounded-xl border border-zinc-200 bg-white p-4">
            <p className="mb-3 text-sm font-semibold text-zinc-700">카테고리</p>
            <div className="flex flex-wrap gap-2">
              <Link
                href={`/boards/${boardId}/posts`}
                className="rounded-full border border-zinc-300 px-3 py-1 text-xs text-zinc-600 hover:bg-zinc-100"
              >
                전체
              </Link>
              <button
                type="button"
                className="rounded-full border border-zinc-900 bg-zinc-900 px-3 py-1 text-xs text-white"
              >
                {categoryName || `카테고리 #${categoryId}`}
              </button>
            </div>
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
                  <Link href={`/posts/${post.id}`} className="flex items-center justify-between gap-4 px-3 py-4">
                    <div className="min-w-0">
                      <p className="truncate font-medium text-zinc-900">{post.title}</p>
                      <p className="mt-1 text-xs text-zinc-500">{post.categoryName}</p>
                    </div>
                    <div className="shrink-0 text-right text-xs text-zinc-500">
                        <div className="flex items-center justify-end gap-1.5">
                            {post.profileImage ? (
                            <img
                                src={post.profileImage}
                                alt={post.author}
                                className="h-5 w-5 rounded-full object-cover"
                            />
                            ) : (
                            <div className="flex h-5 w-5 items-center justify-center rounded-full bg-zinc-200 text-xs font-medium text-zinc-600">
                                {post.author.charAt(0)}
                            </div>
                            )}
                            <p>{post.author}</p>
                        </div>
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

            <Link
              href={`/posts/write?boardId=${boardId}&categoryId=${categoryId}`}
              className="rounded-md bg-zinc-900 px-5 py-2 text-sm font-semibold text-white hover:bg-zinc-700"
            >
              글쓰기
            </Link>
          </div>
        </div>
        </section>
      </main>
    </div>
  );
}
