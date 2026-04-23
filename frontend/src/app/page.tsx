"use client";

import Link from "next/link";
import { useEffect, useState } from "react";
import { m } from "framer-motion";
import type { LucideIcon } from "lucide-react";
import {
  ArrowRight,
  Briefcase,
  FileEdit,
  LayoutGrid,
  MessageCircle,
  Target,
  Users,
  UsersRound,
} from "lucide-react";
import { apiGet } from "@/lib/api";
import type { Board } from "@/lib/types";
import { GlassPanel } from "@/components/shell/GlassPanel";
import { SignalLogo } from "@/components/brand/SignalLogo";
import { Avatar } from "@/components/profile/Avatar";
import { useAuth } from "@/context/AuthContext";

// 게시판 이름에 따른 아이콘 및 테마 설정
const getBoardTheme = (name: string) => {
  if (name.includes("자유")) {
    return {
      Icon: MessageCircle,
      iconWrap: "border-blue-200/80 bg-blue-50 text-blue-900 shadow-[inset_0_1px_0_rgba(255,255,255,0.7)]",
      wash: "from-blue-100/40",
    };
  }
  if (name.includes("채용") || name.includes("공고")) {
    return {
      Icon: Briefcase,
      iconWrap: "border-indigo-200/80 bg-indigo-50 text-indigo-900 shadow-[inset_0_1px_0_rgba(255,255,255,0.7)]",
      wash: "from-indigo-100/40",
    };
  }
  if (name.includes("면접")) {
    return {
      Icon: UsersRound,
      iconWrap: "border-emerald-200/80 bg-emerald-50 text-emerald-900 shadow-[inset_0_1px_0_rgba(255,255,255,0.7)]",
      wash: "from-emerald-100/40",
    };
  }
  if (name.includes("자소서") || name.includes("피드백")) {
    return {
      Icon: FileEdit,
      iconWrap: "border-amber-200/80 bg-amber-50 text-amber-900 shadow-[inset_0_1px_0_rgba(255,255,255,0.7)]",
      wash: "from-amber-100/40",
    };
  }
  return {
    Icon: LayoutGrid,
    iconWrap: "border-slate-200/80 bg-slate-50 text-slate-900 shadow-[inset_0_1px_0_rgba(255,255,255,0.7)]",
    wash: "from-slate-100/40",
  };
};

const container = {
  hidden: { opacity: 0 },
  show: {
    opacity: 1,
    transition: { staggerChildren: 0.08, delayChildren: 0.1 },
  },
};

const item = {
  hidden: { opacity: 0, y: 20 },
  show: {
    opacity: 1,
    y: 0,
    transition: { duration: 0.45, ease: [0.22, 1, 0.36, 1] as const },
  },
};

export default function HomePage() {
  const { user, loading: authLoading } = useAuth();
  const [boards, setBoards] = useState<Board[]>([]);
  const [boardsReady, setBoardsReady] = useState(false);
  const [err, setErr] = useState("");

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const r = await apiGet<Board[]>("/boards");
        if (!cancelled) setBoards(r.data ?? []);
      } catch (e) {
        if (!cancelled)
          setErr(e instanceof Error ? e.message : "게시판을 불러오지 못했습니다.");
      } finally {
        if (!cancelled) setBoardsReady(true);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const totalPosts = boards.reduce((a, b) => a + (b.postCount ?? 0), 0);

  return (
    <main className="flex flex-1 flex-col bg-white">
      <section className="relative overflow-hidden px-4 pb-20 pt-10 sm:px-6 sm:pt-16 lg:pt-24">
        <div className="absolute left-1/2 top-0 -z-10 h-[600px] w-full -translate-x-1/2 bg-[radial-gradient(50%_50%_at_50%_0%,rgba(37,99,235,0.08)_0%,rgba(255,255,255,0)_100%)]" />
        
        <m.div
          variants={container}
          initial="hidden"
          animate="show"
          className="mx-auto max-w-6xl"
        >
          <m.div variants={item} className="max-w-3xl text-center sm:text-left">
            <div className="flex flex-wrap items-center justify-center gap-4 sm:justify-start">
              <SignalLogo
                size={72}
                className="h-16 w-16 shrink-0 rounded-2xl border border-blue-100 bg-white shadow-xl shadow-blue-900/5 sm:h-[4.5rem] sm:w-[4.5rem]"
              />
              <p className="inline-flex items-center gap-2 rounded-xl border border-blue-100 bg-blue-50/50 px-4 py-1.5 text-xs font-bold tracking-wide text-blue-700">
                취업 준비생 커뮤니티 · 합격시그널
              </p>
            </div>
            <h1 className="mt-8 text-4xl font-extrabold leading-[1.1] tracking-tight text-slate-900 sm:text-5xl lg:text-[3.5rem]">
              합격까지, 함께 받는 <span className="text-blue-600">시그널</span>
              <span className="mt-2 block text-xl font-semibold text-slate-500 sm:text-2xl lg:text-[1.75rem]">
                정보·경험·응원이 모이는 메인 허브
              </span>
            </h1>
            <p className="mt-6 max-w-xl text-lg leading-relaxed text-slate-600 mx-auto sm:mx-0">
              채용 공고·자소서·면접·멘토링까지 한곳에서. 질문하고 답하고, 합격
              기록을 남기며 다음 목표까지 함께 가져가요.
            </p>
            {!authLoading && !user && (
              <div className="mt-10 flex flex-wrap justify-center gap-3 sm:justify-start">
                <Link
                  href="/signup"
                  className="inline-flex h-13 items-center gap-2 rounded-xl bg-blue-600 px-8 py-4 text-sm font-bold text-white shadow-lg shadow-blue-200 transition hover:bg-blue-700 active:scale-95"
                >
                  시작하기
                  <ArrowRight className="h-4 w-4" />
                </Link>
                <Link
                  href="/login"
                  className="inline-flex h-13 items-center rounded-xl border border-slate-200 bg-white px-8 py-4 text-sm font-bold text-slate-700 transition hover:bg-slate-50 active:scale-95"
                >
                  로그인
                </Link>
              </div>
            )}
          </m.div>

          {!authLoading && (
            <m.div
              variants={item}
              className="mx-auto mt-12 flex max-w-6xl items-center gap-5 rounded-2xl border border-blue-100 bg-white p-5 shadow-sm sm:max-w-6xl"
            >
              <Avatar
                src={user?.profileImage}
                alt={user?.nickname ?? "프로필"}
                size={72}
                className="h-[72px] w-[72px] ring-4 ring-blue-50"
              />
              <div className="min-w-0 flex-1">
                {user ? (
                  <>
                    <p className="text-lg font-bold text-slate-900">
                      <span className="text-black-600">{user.nickname}</span>님, 환영합니다!
                    </p>
                    <p className="mt-1 text-sm text-slate-500 leading-tight">
                      마이페이지에서 프로필 사진을 변경하고 나만의 합격 기록을 관리해보세요.
                    </p>
                    <Link
                      href="/mypage"
                      className="mt-3 inline-block text-sm font-bold text-blue-600 underline-offset-4 hover:underline"
                    >
                      마이페이지로 이동 &rarr;
                    </Link>
                  </>
                ) : (
                  <>
                    <p className="font-bold text-slate-900">
                      합격시그널과 함께해요
                    </p>
                    <p className="mt-1 text-sm text-slate-500">
                      로그인하면 내 프로필과 맞춤형 채용 정보를 확인 할 수 있습니다.
                    </p>
                    <div className="mt-3 flex flex-wrap gap-4 text-sm">
                      <Link
                        href="/login"
                        className="font-bold text-blue-600 underline-offset-4 hover:underline"
                      >
                        로그인
                      </Link>
                      <Link
                        href="/signup"
                        className="font-bold text-slate-400 underline-offset-4 hover:underline"
                      >
                        회원가입
                      </Link>
                    </div>
                  </>
                )}
              </div>
            </m.div>
          )}

          <m.div
            variants={item}
            className="mt-16 grid gap-6 sm:grid-cols-3"
          >
            {[
              {
                icon: Users,
                label: "커뮤니티",
                value: "함께 성장",
                sub: "준비생·선배가 한 공간에",
                color: "text-blue-600"
              },
              {
                icon: MessageCircle,
                label: "누적 게시글",
                value: String(totalPosts || "—"),
                sub: "전체 게시판 합계",
                color: "text-indigo-600"
              },
              {
                icon: Target,
                label: "목표",
                value: "합격까지",
                sub: "기록하고 피드백 받기",
                color: "text-cyan-600"
              },
            ].map((stat, i) => (
              <GlassPanel key={i} className="p-6 border-slate-100 bg-white/80 shadow-sm">
                <stat.icon className={`h-9 w-9 ${stat.color}`} strokeWidth={1.5} />
                <p className="mt-4 text-[11px] font-bold uppercase tracking-widest text-slate-400">
                  {stat.label}
                </p>
                <p className="mt-1 text-2xl font-extrabold text-slate-900">
                  {stat.value}
                </p>
                <p className="mt-1 text-sm text-slate-500">{stat.sub}</p>
              </GlassPanel>
            ))}
          </m.div>
        </m.div>
      </section>

      <section id="boards" className="scroll-mt-20 bg-slate-50/50 px-4 pb-32 pt-24 sm:px-6">
        <div className="mx-auto max-w-6xl">
          <div className="mb-16 flex flex-col items-center text-center sm:items-start sm:text-left">
            <h2 className="flex items-center gap-3 text-3xl font-black tracking-tight text-slate-900 sm:text-4xl">
              <span className="flex h-11 w-11 items-center justify-center rounded-2xl bg-blue-600 shadow-lg shadow-blue-200">
                <LayoutGrid className="h-5 w-5 text-white" />
              </span>
              게시판 바로가기
            </h2>
            <p className="mt-6 max-w-xl text-lg font-medium text-slate-500">
              주제별 보드로 들어가 대화를 시작해 보세요.
            </p>
            {err && (
              <p className="mt-4 text-sm font-bold text-red-500 bg-red-50 px-4 py-2 rounded-lg">
                {err}
              </p>
            )}
          </div>

          <m.div
            initial="hidden"
            whileInView="show"
            viewport={{ once: true, margin: "-60px" }}
            variants={container}
            className="grid gap-8 sm:grid-cols-2"
          >
            {boardsReady ? (
              boards.map((board, idx) => {
                const theme = getBoardTheme(board.boardName);
                return (
                  <m.div key={board.id} variants={item}>
                    <article className="group relative flex h-full flex-col overflow-hidden rounded-[2.5rem] border border-slate-100 bg-white p-10 transition-all duration-300 hover:border-blue-200 hover:shadow-2xl hover:shadow-blue-900/5">
                      <div className={`absolute inset-0 bg-gradient-to-br ${theme.wash} to-transparent opacity-0 transition-opacity duration-500 group-hover:opacity-100`} />
                      
                      <div className="relative mb-8 flex items-start justify-between">
                        <div className={`flex h-16 w-16 items-center justify-center rounded-2xl border ${theme.iconWrap}`}>
                          <theme.Icon className="h-7 w-7" strokeWidth={2} />
                        </div>
                        <span className="text-4xl font-black text-slate-100 transition-colors group-hover:text-blue-100">
                          {String(idx + 1).padStart(2, "0")}
                        </span>
                      </div>

                      <div className="relative flex-1">
                        <h3 className="text-2xl font-black tracking-tight text-slate-900">{board.boardName}</h3>
                        <p className="mt-4 text-base font-medium leading-relaxed text-slate-500">
                          {board.description || "새로운 시그널을 확인하고 동료들과 소통해 보세요."}
                        </p>
                      </div>

                      <div className="relative mt-10">
                        <Link
                          href={`/boards/${board.id}/posts`}
                          className="flex w-full items-center justify-center gap-2 rounded-2xl bg-slate-900 py-4.5 text-sm font-bold text-white transition-all hover:bg-blue-600 hover:shadow-xl hover:shadow-blue-200"
                        >
                          입장하기
                          <ArrowRight className="h-4 w-4" />
                        </Link>
                      </div>
                    </article>
                  </m.div>
                );
              })
            ) : (
              // 데이터 로딩 중 스켈레톤
              [1, 2, 3, 4].map((n) => (
                <div key={n} className="h-80 animate-pulse rounded-[2.5rem] bg-slate-100" />
              ))
            )}
          </m.div>
        </div>
      </section>

      <section
        id="community"
        className="scroll-mt-20 border-t border-neutral-200 bg-neutral-50 px-4 py-16 sm:px-6"
      >
        <div className="mx-auto max-w-6xl">
          <h2 className="text-xl font-bold text-neutral-900">시그널 피드</h2>
          <p className="mt-2 max-w-2xl text-neutral-600">
            인기 글·실시간 알림은 API와 연동되면 이 영역에 표시됩니다.
          </p>
          <div className="mt-8 grid gap-4 md:grid-cols-2">
            <GlassPanel className="p-6">
              <p className="text-sm font-semibold text-neutral-500">오늘 많이 본 글</p>
              <p className="mt-2 text-lg font-semibold text-neutral-900">면접·자소서 인기글</p>
              <p className="mt-2 text-sm text-neutral-600">게시글 목록 API 연동 후 자동으로 채워집니다.</p>
            </GlassPanel>
            <GlassPanel className="p-6">
              <p className="text-sm font-semibold text-neutral-500">방금 올라온 이야기</p>
              <p className="mt-2 text-lg font-semibold text-neutral-900">새 글 피드</p>
              <p className="mt-2 text-sm text-neutral-600">알림(SSE) 연결 시 실시간으로 갱신할 수 있어요.</p>
            </GlassPanel>
          </div>
        </div>
      </section>
    </main>
  );
}