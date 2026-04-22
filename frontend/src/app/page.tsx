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

const boardShortcutCards: readonly {
  title: string;
  description: string;
  Icon: LucideIcon;
  iconWrap: string;
  wash: string;
}[] = [
  {
    title: "자유 게시판",
    description:
      "취업 준비의 고단함부터 소소한 일상까지, 우리들만의 솔직한 이야기를 나누는 공간입니다.",
    Icon: MessageCircle,
    iconWrap:
      "border-amber-200/80 bg-amber-50 text-amber-900 shadow-[inset_0_1px_0_rgba(255,255,255,0.7)]",
    wash: "from-amber-100/35",
  },
  {
    title: "취업 공고",
    description:
      "꿈을 향한 첫걸음, 최신 채용 공고를 확인하고 당신의 커리어를 시작하세요.",
    Icon: Briefcase,
    iconWrap:
      "border-sky-200/80 bg-sky-50 text-sky-950 shadow-[inset_0_1px_0_rgba(255,255,255,0.7)]",
    wash: "from-sky-100/40",
  },
  {
    title: "자소서 피드백",
    description:
      "혼자 쓰면 막막한 자기소개서, 합격 선배와 동료들의 꼼꼼한 첨삭으로 완성도를 높여보세요.",
    Icon: FileEdit,
    iconWrap:
      "border-violet-200/80 bg-violet-50 text-violet-950 shadow-[inset_0_1px_0_rgba(255,255,255,0.7)]",
    wash: "from-violet-100/35",
  },
  {
    title: "멘토링",
    description:
      "현직자의 생생한 직무 조언부터 커리어 로드맵까지, 앞서간 선배들의 노하우를 직접 들어보세요.",
    Icon: UsersRound,
    iconWrap:
      "border-emerald-200/80 bg-emerald-50 text-emerald-950 shadow-[inset_0_1px_0_rgba(255,255,255,0.7)]",
    wash: "from-emerald-100/35",
  },
];

export default function HomePage() {
  const { user, loading: authLoading } = useAuth();
  const [boards, setBoards] = useState<Board[]>([]);
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
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const totalPosts = boards.reduce((a, b) => a + (b.postCount ?? 0), 0);

  return (
    <main className="flex flex-1 flex-col bg-white">
      <section className="relative overflow-hidden px-4 pb-14 pt-10 sm:px-6 sm:pt-16 lg:pt-20">
        <m.div
          variants={container}
          initial="hidden"
          animate="show"
          className="mx-auto max-w-6xl"
        >
          <m.div variants={item} className="max-w-3xl">
            <div className="flex flex-wrap items-center gap-4">
              <SignalLogo
                size={72}
                className="h-16 w-16 shrink-0 rounded-2xl border border-neutral-200 bg-white shadow-md sm:h-[4.5rem] sm:w-[4.5rem]"
              />
              <p className="inline-flex items-center gap-2 rounded-full border border-neutral-200 bg-neutral-50 px-4 py-1.5 text-xs font-semibold tracking-wide text-neutral-600">
                취업 준비생 커뮤니티 · 합격시그널
              </p>
            </div>
            <h1 className="mt-6 text-4xl font-bold leading-[1.15] tracking-tight text-neutral-900 sm:text-5xl lg:text-[3.25rem]">
              합격까지, 함께 받는 시그널
              <span className="mt-1 block text-xl font-semibold text-neutral-600 sm:text-2xl lg:text-[1.65rem]">
                정보·경험·응원이 모이는 메인 허브
              </span>
            </h1>
            <p className="mt-6 max-w-xl text-lg leading-relaxed text-neutral-600">
              채용 공고·자소서·면접·멘토링까지 한곳에서. 질문하고 답하고, 합격
              기록을 남기며 다음 목표까지 함께 가져가요.
            </p>
            <div className="mt-10 flex flex-wrap gap-3">
              <Link
                href="/signup"
                className="inline-flex h-12 items-center gap-2 rounded-full bg-neutral-900 px-7 text-sm font-semibold text-white shadow-md transition hover:bg-neutral-800"
              >
                무료로 시작하기
                <ArrowRight className="h-4 w-4" />
              </Link>
              <Link
                href="/login"
                className="inline-flex h-12 items-center rounded-full border border-neutral-300 bg-white px-7 text-sm font-semibold text-neutral-900 transition hover:bg-neutral-50"
              >
                로그인
              </Link>
            </div>
          </m.div>

          {!authLoading && (
            <m.div
              variants={item}
              className="mx-auto mt-10 flex max-w-xl items-center gap-4 rounded-2xl border border-neutral-200 bg-neutral-50 p-4 sm:max-w-2xl"
            >
              <Avatar
                src={user?.profileImage}
                alt={user?.nickname ?? "프로필"}
                size={72}
                className="h-[72px] w-[72px] border-neutral-300"
              />
              <div className="min-w-0 flex-1">
                {user ? (
                  <>
                    <p className="font-semibold text-neutral-900">
                      {user.nickname}님, 환영합니다.
                    </p>
                    <p className="mt-1 text-sm text-neutral-600">
                      프로필 사진이 없으면 기본 이미지가 표시됩니다. 마이페이지에서
                      바꿀 수 있어요.
                    </p>
                    <Link
                      href="/mypage"
                      className="mt-2 inline-block text-sm font-medium text-neutral-900 underline-offset-4 hover:underline"
                    >
                      마이페이지
                    </Link>
                  </>
                ) : (
                  <>
                    <p className="font-semibold text-neutral-900">
                      합격시그널과 함께해요
                    </p>
                    <p className="mt-1 text-sm text-neutral-600">
                      로그인하면 내 프로필이 여기에 표시됩니다.
                    </p>
                    <div className="mt-2 flex flex-wrap gap-3 text-sm">
                      <Link
                        href="/login"
                        className="font-medium text-neutral-900 underline-offset-4 hover:underline"
                      >
                        로그인
                      </Link>
                      <Link
                        href="/signup"
                        className="text-neutral-600 underline-offset-4 hover:underline"
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
            className="mt-12 grid gap-4 sm:grid-cols-3"
          >
            {[
              {
                icon: Users,
                label: "커뮤니티",
                value: "함께 성장",
                sub: "준비생·선배가 한 공간에",
              },
              {
                icon: MessageCircle,
                label: "누적 게시글",
                value: String(totalPosts || "—"),
                sub: "전체 게시판 합계",
              },
              {
                icon: Target,
                label: "목표",
                value: "합격까지",
                sub: "기록하고 피드백 받기",
              },
            ].map((stat, i) => (
              <GlassPanel key={i} className="p-5">
                <stat.icon className="h-8 w-8 text-neutral-700" />
                <p className="mt-3 text-xs font-semibold uppercase tracking-wide text-neutral-500">
                  {stat.label}
                </p>
                <p className="mt-1 text-2xl font-bold text-neutral-900">
                  {stat.value}
                </p>
                <p className="mt-1 text-sm text-neutral-600">{stat.sub}</p>
              </GlassPanel>
            ))}
          </m.div>
        </m.div>
      </section>

      <section
        id="boards"
        className="scroll-mt-20 border-t border-neutral-200/80 bg-gradient-to-b from-neutral-50/90 to-white px-4 pb-24 pt-12 sm:px-6 sm:pt-16"
      >
        <div className="mx-auto max-w-6xl">
          <div className="max-w-2xl">
            <p className="text-xs font-semibold uppercase tracking-[0.12em] text-neutral-500">
              Boards
            </p>
            <h2 className="mt-2 flex flex-wrap items-center gap-3 text-2xl font-bold tracking-tight text-neutral-900 sm:text-3xl">
              <span className="flex h-11 w-11 items-center justify-center rounded-2xl border border-neutral-200 bg-white shadow-sm">
                <LayoutGrid className="h-5 w-5 text-neutral-700" strokeWidth={2} />
              </span>
              게시판 바로가기
            </h2>
            <p className="mt-3 text-base leading-relaxed text-neutral-600">
              주제별 보드로 들어가 대화를 시작해 보세요.
            </p>
            {err && (
              <p className="mt-3 rounded-xl border border-amber-200/80 bg-amber-50/80 px-4 py-3 text-sm text-amber-900">
                상단 통계용 게시판 수를 불러오지 못했습니다. {err}
              </p>
            )}
          </div>

          <m.div
            initial="hidden"
            whileInView="show"
            viewport={{ once: true, margin: "-60px" }}
            variants={container}
            className="mt-12 grid gap-6 sm:grid-cols-2 lg:gap-8"
          >
            {boardShortcutCards.map((card, idx) => (
              <m.div key={card.title} variants={item}>
                <article
                  className="group relative flex h-full min-h-[280px] flex-col overflow-hidden rounded-2xl border border-neutral-200/90 bg-white p-7 shadow-[0_1px_0_rgba(15,23,42,0.04)] transition duration-300 hover:-translate-y-0.5 hover:border-neutral-300 hover:shadow-[0_12px_40px_-24px_rgba(15,23,42,0.18)]"
                >
                  <div
                    className={`pointer-events-none absolute inset-0 bg-gradient-to-b ${card.wash} via-transparent to-transparent opacity-90`}
                    aria-hidden
                  />
                  <div className="relative flex items-start justify-between gap-4">
                    <div className="flex min-w-0 flex-1 items-start gap-4">
                      <span
                        className={`flex h-12 w-12 shrink-0 items-center justify-center rounded-2xl border ${card.iconWrap}`}
                        aria-hidden
                      >
                        <card.Icon className="h-5 w-5" strokeWidth={2} />
                      </span>
                      <div className="min-w-0 pt-0.5">
                        <p className="text-[11px] font-semibold tabular-nums text-neutral-400">
                          {String(idx + 1).padStart(2, "0")}
                        </p>
                        <h3 className="mt-1 text-xl font-bold leading-snug tracking-tight text-neutral-900">
                          {card.title}
                        </h3>
                      </div>
                    </div>
                  </div>
                  <p className="relative mt-5 flex-1 text-[15px] leading-[1.65] text-neutral-600">
                    {card.description}
                  </p>
                  <div className="relative mt-6 flex items-center gap-3">
                    <button
                      type="button"
                      className="inline-flex h-10 items-center rounded-full border border-neutral-200 bg-neutral-50 px-5 text-sm font-semibold text-neutral-800 transition group-hover:border-neutral-300 group-hover:bg-white"
                    >
                      Button
                    </button>
                    <span className="text-xs font-medium text-neutral-400">
                      연동 예정
                    </span>
                  </div>
                </article>
              </m.div>
            ))}
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
            인기 글·실시간 알림은 API와 연동되면 이 영역에 표시됩니다. 합격시그널
            메인에서 보여 줄 레이아웃만 준비된 상태입니다.
          </p>
          <div className="mt-8 grid gap-4 md:grid-cols-2">
            <GlassPanel className="p-6">
              <p className="text-sm font-semibold text-neutral-500">
                오늘 많이 본 글
              </p>
              <p className="mt-2 text-lg font-semibold text-neutral-900">
                면접·자소서 인기글
              </p>
              <p className="mt-2 text-sm text-neutral-600">
                게시글 목록 API 연동 후 자동으로 채워집니다.
              </p>
            </GlassPanel>
            <GlassPanel className="p-6">
              <p className="text-sm font-semibold text-neutral-500">
                방금 올라온 이야기
              </p>
              <p className="mt-2 text-lg font-semibold text-neutral-900">
                새 글 피드
              </p>
              <p className="mt-2 text-sm text-neutral-600">
                알림(SSE) 연결 시 실시간으로 갱신할 수 있어요.
              </p>
            </GlassPanel>
          </div>
        </div>
      </section>
    </main>
  );
}
