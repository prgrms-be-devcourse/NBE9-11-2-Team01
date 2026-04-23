/**
 * 게시글 상세 페이지
 */
"use client";

import Link from "next/link";
import { useParams, usePathname, useRouter } from "next/navigation";
import { FormEvent, useCallback, useEffect, useMemo, useState } from "react";
import { profileImageUrl } from "@/lib/profileImage";
import { MoreHorizontal } from "lucide-react";

type Comment = {
  id: number;
  content: string;
  author: string;
  profileImage: string | null;
  likeCount: number;
  createdAt: string;
  modifiedAt: string;
  replies?: Comment[];
};

type PostDetail = {
  id: number;
  boardId: number;
  boardName: string;
  categoryId: number;
  categoryName: string;
  title: string;
  content: string;
  author: string;
  profileImage: string | null;
  likeCount: number;
  isLiked: boolean;
  createdAt: string;
  modifiedAt: string;
  comments: Comment[];
  isOwner: boolean;
};

type MyPageResponse = {
  email: string;
  nickname: string;
  profileImage: string;
  role: string;
};

type ApiResponse<T> = {
  success: boolean;
  code: string | null;
  message: string | null;
  data: T;
};

type PostLikeResponse = {
  liked: boolean;
  likeCount: number;
};

type CommentLikeResponse = {
  commentId: number;
  likeCount: number;
  liked: boolean;
};

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

type CommentItemProps = {
  comment: Comment;
  depth?: number;
  canManage: (author: string) => boolean;
  getCommentLikeUi: (comment: Comment) => { liked: boolean; likeCount: number };
  onToggleCommentLike: (commentId: number) => void;
  editingCommentId: number | null;
  editingCommentContent: string;
  setEditingCommentContent: (value: string) => void;
  onStartCommentEdit: (comment: Comment) => void;
  onCancelCommentEdit: () => void;
  onSubmitCommentEdit: (commentId: number) => void;
  onDeleteComment: (commentId: number) => void;
};

function CommentItem({
  comment,
  depth = 0,
  canManage,
  getCommentLikeUi,
  onToggleCommentLike,
  editingCommentId,
  editingCommentContent,
  setEditingCommentContent,
  onStartCommentEdit,
  onCancelCommentEdit,
  onSubmitCommentEdit,
  onDeleteComment,
}: CommentItemProps) {
  const replies = comment.replies ?? [];
  const [isRepliesOpen, setIsRepliesOpen] = useState(false);
  const isEditing = editingCommentId === comment.id;
  const isOwner = canManage(comment.author);
  const likeUi = getCommentLikeUi(comment);
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  // 하위 CommentItem에 전달할 공통 props
  const commentItemProps = {
    canManage,
    getCommentLikeUi,
    onToggleCommentLike,
    editingCommentId,
    editingCommentContent,
    setEditingCommentContent,
    onStartCommentEdit,
    onCancelCommentEdit,
    onSubmitCommentEdit,
    onDeleteComment,
  };

  return (
    <li className={`rounded-2xl border border-gray-200 bg-white p-4 shadow-sm ${depth > 0 ? "ml-6" : ""}`}>
      <div className="flex items-start justify-between gap-3">
        <div className="min-w-0 flex-1">
          {/* 프로필 + 작성자 + 날짜 */}
          <div className="flex gap-2">
            <img
              src={profileImageUrl(comment.profileImage)}
              alt={comment.author}
              className="h-7 w-7 shrink-0 rounded-full object-cover"
            />
            <div className="min-w-0 flex-1">
              <p className="text-sm font-semibold text-gray-900">{comment.author}</p>
              <div className="flex items-center gap-1.5 text-xs text-gray-400">
                <span>{formatDate(comment.createdAt)}</span>
                {comment.createdAt !== comment.modifiedAt && (
                  <span>(수정됨)</span>
                )}
              </div>

              {isEditing ? (
                <div className="mt-2 flex gap-2">
                  <input
                    type="text"
                    value={editingCommentContent}
                    onChange={(event) => setEditingCommentContent(event.target.value)}
                    className="h-9 flex-1 rounded-xl border border-gray-200 px-3 text-sm outline-none focus:border-gray-400"
                  />
                  <button
                    type="button"
                    onClick={() => onSubmitCommentEdit(comment.id)}
                    className="rounded-xl bg-gray-900 px-3 text-xs text-white hover:bg-gray-700"
                  >
                    저장
                  </button>
                  <button
                    type="button"
                    onClick={onCancelCommentEdit}
                    className="rounded-xl border border-gray-200 px-3 text-xs text-gray-600 hover:bg-gray-100"
                  >
                    취소
                  </button>
                </div>
              ) : (
                <p className="mt-1.5 whitespace-pre-wrap text-sm leading-relaxed text-gray-700">{comment.content}</p>
              )}

              <div className="mt-2">
                <button
                  type="button"
                  onClick={() => onToggleCommentLike(comment.id)}
                  className={`flex items-center gap-1 rounded-full border px-2 py-0.5 text-xs transition-colors ${
                    likeUi.liked
                      ? "border-red-200 bg-red-50 text-red-500"
                      : "border-gray-200 text-gray-400 hover:border-gray-300 hover:text-gray-600"
                  }`}
                >
                  <span>{likeUi.liked ? "❤️" : "🤍"}</span>
                  <span>좋아요 {likeUi.likeCount}</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* 더보기 버튼 */}
        {isOwner && !isEditing && (
          <div className="relative">
            <button
              type="button"
              onClick={() => setIsMenuOpen((prev) => !prev)}
              className="rounded-full p-1.5 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
            >
              <MoreHorizontal className="h-4 w-4" />
            </button>
            {isMenuOpen && (
              <>
                <div className="fixed inset-0 z-10" onClick={() => setIsMenuOpen(false)} />
                <div className="absolute right-0 z-20 mt-1 w-24 overflow-hidden rounded-xl border border-gray-200 bg-white shadow-lg">
                  <button
                    type="button"
                    onClick={() => { onStartCommentEdit(comment); setIsMenuOpen(false); }}
                    className="w-full px-4 py-2 text-left text-sm text-gray-700 transition-colors hover:bg-gray-50"
                  >
                    수정
                  </button>
                  <button
                    type="button"
                    onClick={() => { onDeleteComment(comment.id); setIsMenuOpen(false); }}
                    className="w-full px-4 py-2 text-left text-sm text-red-500 transition-colors hover:bg-red-50"
                  >
                    삭제
                  </button>
                </div>
              </>
            )}
          </div>
        )}
      </div>

      {/* 답글 토글 */}
      {replies.length > 0 && (
        <button
          type="button"
          onClick={() => setIsRepliesOpen((prev) => !prev)}
          className="mt-3 flex items-center gap-1.5 text-sm text-gray-500 transition-colors hover:text-gray-700"
          >
          {isRepliesOpen ? (
            <>▲ 답글 접기</>
          ) : (
            <>▼ 답글 {replies.length}개 보기</>
          )}
        </button>
      )}

      {isRepliesOpen && replies.length > 0 && (
        <ul className="mt-3 space-y-2">
          {replies.map((reply) => (
            <CommentItem
              key={reply.id}
              comment={reply}
              depth={depth + 1}
              {...commentItemProps}
            />
          ))}
        </ul>
      )}
    </li>
  );
}

export default function PostDetailPage() {
  const params = useParams<{ postId: string }>();
  const pathname = usePathname();
  const router = useRouter();
  const postId = params.postId;

  const [post, setPost] = useState<PostDetail | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");
  const [isAuthRequired, setIsAuthRequired] = useState(false);

  const [myNickname, setMyNickname] = useState<string | null>(null);

  const [commentContent, setCommentContent] = useState("");
  const [isCommentSubmitting, setIsCommentSubmitting] = useState(false);

  const [isDeletingPost, setIsDeletingPost] = useState(false);
  const [isPostLikeLoading, setIsPostLikeLoading] = useState(false);
  const [postLiked, setPostLiked] = useState(false);

  const [isEditingPost, setIsEditingPost] = useState(false);
  const [editingTitle, setEditingTitle] = useState("");
  const [editingContent, setEditingContent] = useState("");
  const [isSavingPostEdit, setIsSavingPostEdit] = useState(false);
  const [isPostMenuOpen, setIsPostMenuOpen] = useState(false);
  const [isContentExpanded, setIsContentExpanded] = useState(false);
  const [visibleCount, setVisibleCount] = useState(5);

  const [editingCommentId, setEditingCommentId] = useState<number | null>(null);
  const [editingCommentContent, setEditingCommentContent] = useState("");
  const [commentLikeUi, setCommentLikeUi] = useState
    <Record<number, { liked: boolean; likeCount: number }>
  >({});

  const loginHref = useMemo(() => {
    const query = new URLSearchParams();
    query.set("next", pathname ?? `/posts/${postId}`);
    return `/login?${query.toString()}`;
  }, [pathname, postId]);

  const canManageComment = useCallback(
    (author: string) => {
      if (!myNickname) return false;
      return author === myNickname;
    },
    [myNickname],
  );

  const fetchMe = useCallback(async () => {
    try {
      const res = await fetch(`${getApiBaseUrl()}/users/me`, {
        method: "GET",
        credentials: "include",
      });
      if (!res.ok) {
        setMyNickname(null);
        return;
      }
      const json = (await res.json()) as ApiResponse<MyPageResponse>;
      if (json.success) {
        setMyNickname(json.data.nickname);
      }
    } catch {
      setMyNickname(null);
    }
  }, []);

  const fetchPost = useCallback(async () => {
    setIsLoading(true);
    setErrorMessage("");
    setIsAuthRequired(false);

    try {
      const res = await fetch(`${getApiBaseUrl()}/posts/${postId}`, {
        method: "GET",
        credentials: "include",
      });

      if (!res.ok) {
        if (res.status === 401) {
          setIsAuthRequired(true);
          throw new Error("게시글 상세는 로그인이 필요합니다. 로그인 후 다시 시도해 주세요.");
        }
        if (res.status === 403) throw new Error("이 게시글에 접근할 권한이 없습니다.");
        if (res.status === 404) throw new Error("게시글을 찾을 수 없습니다.");
        throw new Error(`게시글을 불러오지 못했습니다. (${res.status})`);
      }

      const json = (await res.json()) as ApiResponse<PostDetail>;
      if (!json.success) {
        throw new Error(json.message ?? "게시글 상세 조회에 실패했습니다.");
      }

      setPost(json.data);
      setEditingTitle(json.data.title);
      setEditingContent(json.data.content);
      setPostLiked(json.data.isLiked);
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("알 수 없는 오류가 발생했습니다.");
      }
      setPost(null);
    } finally {
      setIsLoading(false);
    }
  }, [postId]);

  useEffect(() => {
    fetchPost();
    fetchMe();
  }, [fetchMe, fetchPost]);

  async function handleTogglePostLike() {
    if (!post || isPostLikeLoading) return;
    setIsPostLikeLoading(true);
    setErrorMessage("");

    try {
      const res = await fetch(`${getApiBaseUrl()}/posts/${post.id}/likes`, {
        method: "POST",
        credentials: "include",
      });

      if (!res.ok) {
        if (res.status === 401) {
          setIsAuthRequired(true);
          throw new Error("로그인 후 좋아요를 누를 수 있습니다.");
        }
        throw new Error(`좋아요 처리에 실패했습니다. (${res.status})`);
      }

      const json = (await res.json()) as ApiResponse<PostLikeResponse>;
      if (!json.success) throw new Error(json.message ?? "좋아요 처리에 실패했습니다.");

      setPostLiked(json.data.liked);
      setPost((prev) => (prev ? { ...prev, likeCount: json.data.likeCount } : prev));
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("알 수 없는 오류가 발생했습니다.");
      }
    } finally {
      setIsPostLikeLoading(false);
    }
  }

  async function handleCreateComment(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const content = commentContent.trim();
    if (!content) {
      setErrorMessage("댓글 내용을 입력해 주세요.");
      return;
    }

    setIsCommentSubmitting(true);
    setErrorMessage("");

    try {
      const res = await fetch(`${getApiBaseUrl()}/posts/${postId}/comments`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ content, parentId: null }),
      });

      if (!res.ok) {
        if (res.status === 401) {
          setIsAuthRequired(true);
          throw new Error("로그인 후 댓글을 작성할 수 있습니다.");
        }
        throw new Error(`댓글 작성에 실패했습니다. (${res.status})`);
      }

      const json = (await res.json()) as ApiResponse<unknown>;
      if (!json.success) throw new Error(json.message ?? "댓글 작성에 실패했습니다.");

      setCommentContent("");
      await fetchPost();
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("알 수 없는 오류가 발생했습니다.");
      }
    } finally {
      setIsCommentSubmitting(false);
    }
  }

  async function handleToggleCommentLike(commentId: number) {
    setErrorMessage("");

    try {
      const res = await fetch(`${getApiBaseUrl()}/comments/${commentId}/likes`, {
        method: "POST",
        credentials: "include",
      });

      if (!res.ok) {
        if (res.status === 401) {
          setIsAuthRequired(true);
          throw new Error("로그인 후 댓글 좋아요를 누를 수 있습니다.");
        }
        throw new Error(`댓글 좋아요 처리에 실패했습니다. (${res.status})`);
      }

      const json = (await res.json()) as ApiResponse<CommentLikeResponse>;
      if (!json.success) throw new Error(json.message ?? "댓글 좋아요 처리에 실패했습니다.");

      setCommentLikeUi((prev) => ({
        ...prev,
        [commentId]: { liked: json.data.liked, likeCount: json.data.likeCount },
      }));
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("알 수 없는 오류가 발생했습니다.");
      }
    }
  }

  function getCommentLikeUi(comment: Comment) {
    return commentLikeUi[comment.id] ?? { liked: false, likeCount: comment.likeCount };
  }

  function startEditComment(comment: Comment) {
    setEditingCommentId(comment.id);
    setEditingCommentContent(comment.content);
  }

  function cancelEditComment() {
    setEditingCommentId(null);
    setEditingCommentContent("");
  }

  async function submitEditComment(commentId: number) {
    const content = editingCommentContent.trim();
    if (!content) {
      setErrorMessage("댓글 내용을 입력해 주세요.");
      return;
    }

    setErrorMessage("");

    try {
      const res = await fetch(`${getApiBaseUrl()}/comments/${commentId}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ content, parentId: null }),
      });

      if (!res.ok) {
        if (res.status === 401) {
          setIsAuthRequired(true);
          throw new Error("로그인 후 댓글을 수정할 수 있습니다.");
        }
        if (res.status === 403) throw new Error("본인 댓글만 수정할 수 있습니다.");
        throw new Error(`댓글 수정에 실패했습니다. (${res.status})`);
      }

      const json = (await res.json()) as ApiResponse<unknown>;
      if (!json.success) throw new Error(json.message ?? "댓글 수정에 실패했습니다.");

      cancelEditComment();
      await fetchPost();
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("알 수 없는 오류가 발생했습니다.");
      }
    }
  }

  async function handleDeleteComment(commentId: number) {
    const confirmed = window.confirm("댓글을 삭제하시겠습니까?");
    if (!confirmed) return;

    setErrorMessage("");

    try {
      const res = await fetch(`${getApiBaseUrl()}/comments/${commentId}`, {
        method: "DELETE",
        credentials: "include",
      });

      if (!res.ok) {
        if (res.status === 401) {
          setIsAuthRequired(true);
          throw new Error("로그인 후 댓글을 삭제할 수 있습니다.");
        }
        if (res.status === 403) throw new Error("본인 댓글만 삭제할 수 있습니다.");
        throw new Error(`댓글 삭제에 실패했습니다. (${res.status})`);
      }

      await fetchPost();
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("알 수 없는 오류가 발생했습니다.");
      }
    }
  }

  async function handleSavePostEdit() {
    if (!post) return;

    const title = editingTitle.trim();
    const content = editingContent.trim();
    if (title.length < 2 || content.length < 2) {
      setErrorMessage("제목/내용은 최소 2자 이상이어야 합니다.");
      return;
    }

    setIsSavingPostEdit(true);
    setErrorMessage("");

    try {
      const res = await fetch(`${getApiBaseUrl()}/posts/${post.id}`, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ title, content, categoryId: post.categoryId }),
      });

      if (!res.ok) {
        if (res.status === 401) {
          setIsAuthRequired(true);
          throw new Error("로그인 후 게시글을 수정할 수 있습니다.");
        }
        if (res.status === 403) throw new Error("작성자만 게시글을 수정할 수 있습니다.");
        throw new Error(`게시글 수정에 실패했습니다. (${res.status})`);
      }

      const json = (await res.json()) as ApiResponse<unknown>;
      if (!json.success) throw new Error(json.message ?? "게시글 수정에 실패했습니다.");

      setIsEditingPost(false);
      await fetchPost();
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("알 수 없는 오류가 발생했습니다.");
      }
    } finally {
      setIsSavingPostEdit(false);
    }
  }

  async function handleDeletePost() {
    if (!post) return;

    const confirmed = window.confirm("정말 삭제하시겠습니까?");
    if (!confirmed) return;

    setIsDeletingPost(true);
    setErrorMessage("");

    try {
      const res = await fetch(`${getApiBaseUrl()}/posts/${post.id}`, {
        method: "DELETE",
        credentials: "include",
      });

      if (!res.ok) {
        if (res.status === 401) {
          setIsAuthRequired(true);
          throw new Error("로그인 후 삭제할 수 있습니다.");
        }
        if (res.status === 403) throw new Error("작성자만 삭제할 수 있습니다.");
        throw new Error(`게시글 삭제에 실패했습니다. (${res.status})`);
      }

      router.push(`/boards/${post.boardId}/posts`);
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("알 수 없는 오류가 발생했습니다.");
      }
    } finally {
      setIsDeletingPost(false);
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 px-4 py-8">
      <main className="mx-auto flex w-full max-w-4xl flex-col gap-5">

        <header className="rounded-2xl border border-gray-200 bg-white px-6 py-5 shadow-sm">
          <p className="text-xs font-semibold uppercase tracking-widest text-gray-400">{post?.boardName ?? "게시판"}</p>
          <h1 className="mt-1 text-2xl font-bold text-gray-900">게시글 상세</h1>
        </header>

        {errorMessage && (
          <div className="rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-600">
            {errorMessage}
          </div>
        )}

        {isAuthRequired && (
          <section className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
            <p className="text-sm text-gray-700">로그인이 필요한 페이지입니다.</p>
            <div className="mt-4 flex gap-2">
              <Link
                href={loginHref}
                className="inline-flex rounded-xl bg-gray-900 px-4 py-2 text-sm font-medium text-white transition-colors hover:bg-gray-700"
              >
                로그인 하러 가기
              </Link>
              <button
                type="button"
                onClick={fetchPost}
                className="inline-flex rounded-xl border border-gray-200 px-4 py-2 text-sm text-gray-600 transition-colors hover:bg-gray-100"
              >
                다시 시도
              </button>
            </div>
          </section>
        )}

        {isLoading ? (
          <section className="rounded-2xl border border-gray-200 bg-white p-12 text-center text-sm text-gray-400 shadow-sm">
            불러오는 중...
          </section>
        ) : !post ? (
          !isAuthRequired && (
            <section className="rounded-2xl border border-gray-200 bg-white p-12 text-center text-sm text-gray-400 shadow-sm">
              게시글을 찾을 수 없습니다.
            </section>
          )
        ) : (
          <>
            <section className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
              <div className="mb-5 flex items-start justify-between gap-3">
                <div className="min-w-0 flex-1">
                  <span className="inline-flex rounded-full bg-gray-100 px-3 py-1 text-xs font-medium text-gray-600">
                    {post.categoryName}
                  </span>

                  {isEditingPost ? (
                    <div className="mt-3 space-y-3">
                      <input
                        type="text"
                        value={editingTitle}
                        onChange={(event) => setEditingTitle(event.target.value)}
                        className="h-10 w-full rounded-xl border border-gray-200 px-3 text-sm outline-none focus:border-gray-400"
                      />
                      <textarea
                        value={editingContent}
                        onChange={(event) => setEditingContent(event.target.value)}
                        rows={6}
                        className="w-full rounded-xl border border-gray-200 px-3 py-2 text-sm outline-none focus:border-gray-400"
                      />
                      <div className="flex gap-2">
                        <button
                          type="button"
                          onClick={handleSavePostEdit}
                          disabled={isSavingPostEdit}
                          className="rounded-xl bg-gray-900 px-4 py-2 text-xs font-medium text-white transition-colors hover:bg-gray-700 disabled:cursor-not-allowed disabled:opacity-60"
                        >
                          {isSavingPostEdit ? "저장 중..." : "저장"}
                        </button>
                        <button
                          type="button"
                          onClick={() => {
                            setIsEditingPost(false);
                            setEditingTitle(post.title);
                            setEditingContent(post.content);
                          }}
                          className="rounded-xl border border-gray-200 px-4 py-2 text-xs text-gray-600 transition-colors hover:bg-gray-100"
                        >
                          취소
                        </button>
                      </div>
                    </div>
                  ) : (
                    <>
                      <h2 className="mt-3 text-xl font-bold text-gray-900">{post.title}</h2>
                      <div className="mt-3 flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-gray-400">
                        <span className="flex items-center gap-1.5">
                          <img
                            src={profileImageUrl(post.profileImage)}
                            alt={post.author}
                            className="h-5 w-5 rounded-full object-cover"
                          />
                          <span className="font-medium text-gray-700">{post.author}</span>
                        </span>
                        <span>작성일 {formatDate(post.createdAt)}</span>
                        {post.createdAt !== post.modifiedAt && (
                          <span className="text-gray-400">(수정됨 {formatDate(post.modifiedAt)})</span>
                        )}
                      </div>
                    </>
                  )}
                </div>

                <div className="flex items-center gap-2">
                  <button
                    type="button"
                    onClick={handleTogglePostLike}
                    disabled={isPostLikeLoading}
                    className={`flex items-center gap-1.5 rounded-xl border px-3 py-1.5 text-sm font-medium transition-colors disabled:cursor-not-allowed disabled:opacity-60 ${
                      postLiked
                        ? "border-red-200 bg-red-50 text-red-500"
                        : "border-gray-200 text-gray-600 hover:bg-gray-100"
                    }`}
                  >
                    <span>{postLiked ? "❤️" : "🤍"}</span>
                    <span>좋아요 {post.likeCount}</span>
                  </button>

                  {post.isOwner && !isEditingPost && (
                    <div className="relative">
                      <button
                        type="button"
                        onClick={() => setIsPostMenuOpen((prev) => !prev)}
                        className="rounded-full p-1.5 text-gray-400 transition-colors hover:bg-gray-100 hover:text-gray-600"
                      >
                        <MoreHorizontal className="h-5 w-5" />
                      </button>
                      {isPostMenuOpen && (
                        <>
                          <div className="fixed inset-0 z-10" onClick={() => setIsPostMenuOpen(false)} />
                          <div className="absolute right-0 z-20 mt-1 w-24 overflow-hidden rounded-xl border border-gray-200 bg-white shadow-lg">
                            <button
                              type="button"
                              onClick={() => { setIsEditingPost(true); setIsPostMenuOpen(false); }}
                              className="w-full px-4 py-2 text-left text-sm text-gray-700 transition-colors hover:bg-gray-50"
                            >
                              수정
                            </button>
                            <button
                              type="button"
                              onClick={() => { handleDeletePost(); setIsPostMenuOpen(false); }}
                              disabled={isDeletingPost}
                              className="w-full px-4 py-2 text-left text-sm text-red-500 transition-colors hover:bg-red-50 disabled:cursor-not-allowed disabled:opacity-50"
                            >
                              {isDeletingPost ? "삭제 중..." : "삭제"}
                            </button>
                          </div>
                        </>
                      )}
                    </div>
                  )}
                </div>
              </div>

              {!isEditingPost && (
                <div className="border-t border-gray-100 pt-5">
                  <div className={`whitespace-pre-wrap text-sm leading-7 text-gray-700 ${
                    !isContentExpanded && post.content.length > 300 ? "max-h-48 overflow-hidden" : ""
                  }`}>
                    {post.content}
                  </div>
                  {post.content.length > 300 && (
                    <button
                      type="button"
                      onClick={() => setIsContentExpanded((prev) => !prev)}
                      className="mt-3 w-full rounded-xl border border-gray-200 py-2.5 text-sm text-gray-500 transition-colors hover:bg-gray-50"
                    >
                      {isContentExpanded ? "접기" : `더보기`}
                    </button>
                  )}
                </div>
              )}
            </section>

            <section className="rounded-2xl border border-gray-200 bg-white p-6 shadow-sm">
              <p className="text-base font-bold text-gray-900">댓글 {post.comments.length}개</p>

              <form onSubmit={handleCreateComment} className="mt-4 flex gap-2">
                <input
                  type="text"
                  value={commentContent}
                  onChange={(event) => setCommentContent(event.target.value)}
                  placeholder="댓글을 입력해 주세요."
                  className="h-10 flex-1 rounded-xl border border-gray-200 px-3 text-sm outline-none focus:border-gray-400"
                />
                <button
                  type="submit"
                  disabled={isCommentSubmitting}
                  className="rounded-xl bg-gray-900 px-4 text-sm font-medium text-white transition-colors hover:bg-gray-700 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {isCommentSubmitting ? "작성 중..." : "작성"}
                </button>
              </form>

              {post.comments.length === 0 ? (
                <p className="mt-6 text-center text-sm text-gray-400">아직 댓글이 없습니다.</p>
              ) : (
                <>
                  <ul className="mt-4 space-y-3">
                    {post.comments.slice(0, visibleCount).map((comment) => (
                      <CommentItem
                        key={comment.id}
                        comment={comment}
                        canManage={canManageComment}
                        getCommentLikeUi={getCommentLikeUi}
                        onToggleCommentLike={handleToggleCommentLike}
                        editingCommentId={editingCommentId}
                        editingCommentContent={editingCommentContent}
                        setEditingCommentContent={setEditingCommentContent}
                        onStartCommentEdit={startEditComment}
                        onCancelCommentEdit={cancelEditComment}
                        onSubmitCommentEdit={submitEditComment}
                        onDeleteComment={handleDeleteComment}
                      />
                    ))}
                  </ul>
                  <div className="mt-4 flex gap-2">
                    {visibleCount < post.comments.length && (
                      <button
                        type="button"
                        onClick={() => setVisibleCount((prev) => prev + 5)}
                        className="flex-1 rounded-xl border border-gray-200 py-2.5 text-sm text-gray-500 transition-colors hover:bg-gray-50"
                      >
                        댓글 더 보기 ({post.comments.length - visibleCount}개 남음)
                      </button>
                    )}
                    {visibleCount > 5 && (
                      <button
                        type="button"
                        onClick={() => setVisibleCount(5)}
                        className="flex-1 rounded-xl border border-gray-200 py-2.5 text-sm text-gray-500 transition-colors hover:bg-gray-50"
                      >
                        접기
                      </button>
                    )}
                  </div>
                </>
              )}
            </section>

            <div>
              <Link
                href={`/boards/${post.boardId}/posts`}
                className="inline-flex rounded-xl border border-gray-200 px-4 py-2 text-sm text-gray-600 transition-colors hover:bg-gray-100"
              >
                ← 목록으로
              </Link>
            </div>
          </>
        )}
      </main>
    </div>
  );
}