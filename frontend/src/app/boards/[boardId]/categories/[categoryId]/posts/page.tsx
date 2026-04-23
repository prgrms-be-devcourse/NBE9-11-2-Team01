/**
 * 카테고리별 게시글 목록 페이지
 */
"use client";

import Link from "next/link";
import { useParams, useRouter, useSearchParams } from "next/navigation";
import { KeyboardEvent, useCallback, useEffect, useMemo, useState } from "react";
import { profileImageUrl } from "@/lib/profileImage";

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
  const [categories, setCategories] = useState<Category[]>([]);

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
      if (!res.ok) return;
      const json = (await res.json()) as ApiResponse<Category[]>;
      if (!json.success) return;

      setCategories(json.data);

      const selected = json.data.find((category) => String(category.id) === categoryId);
      if (selected) setCategoryName(selected.name);
    } catch {
      // 무시
    }
  }, [boardId, categoryId]);

  const fetchPosts = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage("");

    try {
      const query = new URLSearchParams();
      query.set("page", String(page));
      if (keyword) {
        query.set("keyword", keyword);
      }

      query.set("sort", sort);

      const res = await fetch(`${getApiBaseUrl()}/boards/${boardId}/categories/${categoryId}/posts?${query.toString()}`, {
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
    <div className="min-h-screen bg-blue-50/40 px-4 py-8">
        <main className="mx-auto flex w-full max-w-6xl flex-col gap-5">

        <header className="rounded-2xl border border-gray-200 bg-white px-6 py-5 shadow-sm">
            <div className="mb-4">
            <p className="text-xs font-semibold uppercase tracking-widest text-gray-400">게시글 목록</p>
            <h1 className="mt-1 text-2xl font-bold text-gray-900">{categoryName || `카테고리 #${categoryId}`}</h1>
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
                    초기화
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

        <section className="grid gap-5 md:grid-cols-[200px_1fr]">

            <aside className="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
              <p className="mb-3 text-xs font-semibold uppercase tracking-widest text-gray-400">카테고리</p>
              <div className="flex flex-col gap-1">
                <Link
                  href={`/boards/${boardId}/posts`}
                  className="rounded-xl px-3 py-2 text-sm font-medium text-gray-600 transition-colors hover:bg-blue-50"
                >
                  전체
                </Link>
                {categories.map((category) => (
                  <Link
                    key={category.id}
                    href={`/boards/${boardId}/categories/${category.id}/posts`}
                    className={`rounded-xl px-3 py-2 text-sm font-medium transition-colors ${
                      String(category.id) === categoryId
                        ? "bg-black text-white"
                        : "text-gray-600 hover:bg-blue-50"
                    }`}
                  >
                    {category.name}
                  </Link>
                ))}
              </div>
            </aside>

            <div className="rounded-2xl border border-gray-200 bg-white shadow-sm">
            <div className="flex items-center justify-between border-b border-gray-100 px-5 py-3">
                <span className="text-sm text-gray-500">
                총 <span className="font-semibold text-gray-900">{postPage?.totalElements ?? 0}</span>개
                </span>
                <span className="text-sm text-gray-400">
                {postPage?.currentPage ?? page} / {postPage?.totalPages ?? 1} 페이지
                </span>
            </div>

            {errorMessage && (
                <div className="m-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
                {errorMessage}
                </div>
            )}

            {isLoading ? (
                <div className="py-20 text-center text-sm text-gray-400">불러오는 중...</div>
            ) : !hasPosts ? (
                <div className="py-20 text-center text-sm text-gray-400">게시글이 없습니다.</div>
            ) : (
                <ul className="divide-y divide-gray-100">
                {postPage.posts.map((post) => (
                    <li key={post.id} className="group transition-colors hover:bg-blue-50/50">
                    <Link href={`/posts/${post.id}`} className="flex items-center justify-between gap-4 px-5 py-4">
                        <div className="min-w-0 flex-1">
                        <p className="truncate font-semibold text-gray-900 transition-colors group-hover:text-gray-600">
                            {post.title}
                        </p>
                        <p className="mt-1 text-xs text-gray-400">{post.categoryName}</p>
                        </div>
                        <div className="shrink-0 text-right">
                        <div className="flex items-center justify-end gap-1.5">
                            <img
                            src={profileImageUrl(post.profileImage)}
                            alt={post.author}
                            className="h-6 w-6 rounded-full object-cover"
                            />
                            <p className="text-sm font-medium text-gray-700">{post.author}</p>
                        </div>
                        <div className="mt-1 flex items-center justify-end gap-2 text-xs text-gray-400">
                            <span className="flex items-center gap-1">
                            <span>❤️</span>
                            <span>좋아요 {post.likeCount}</span>
                            </span>
                            <span>·</span>
                            <span>{formatDate(post.createdAt)}</span>
                        </div>
                        </div>
                    </Link>
                    </li>
                ))}
                </ul>
            )}

            <div className="flex items-center justify-between border-t border-gray-100 px-5 py-4">
                <div className="flex items-center gap-1">
                <button
                    type="button"
                    disabled={!postPage || postPage.currentPage <= 1}
                    onClick={() => updateQuery({ page: Math.max(1, page - 1) })}
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
                href={`/posts/write?boardId=${boardId}&categoryId=${categoryId}`}
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
