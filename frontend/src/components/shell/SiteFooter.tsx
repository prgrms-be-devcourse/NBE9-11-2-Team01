import Link from "next/link";
import { ExternalLink } from "lucide-react";

const externalSites = [
  { name: "잡코리아", href: "https://www.jobkorea.co.kr" },
  { name: "사람인", href: "https://www.saramin.co.kr" },
  { name: "링커리어", href: "https://linkareer.com" },
  { name: "직행", href: "https://www.zighang.com" },
  { name: "프로그래머스", href: "https://programmers.co.kr" },
] as const;

const teamMemberNames =
  "김민혁, 이유진, 김정욱, 김하늘, 김경탁, 김강산";

export function SiteFooter() {
  return (
    <footer className="relative z-10 mt-auto border-t border-gray-200 bg-white/95">
      <div className="mx-auto max-w-6xl px-4 py-10 sm:px-6">
        <div className="flex flex-col gap-6">
          <div>
            <h2 className="text-xs font-semibold uppercase tracking-wide text-gray-500">
              취업·코딩 참고 사이트
            </h2>
            <p className="mt-1 text-sm text-gray-600">
              채용 공고·대외활동·코딩 테스트 준비 등에 활용할 수 있는 외부
              사이트입니다.
            </p>
            <ul className="mt-4 flex flex-wrap gap-x-4 gap-y-2">
              {externalSites.map((site) => (
                <li key={site.href}>
                  <a
                    href={site.href}
                    target="_blank"
                    rel="noopener noreferrer"
                    className="inline-flex items-center gap-1 text-sm font-medium text-gray-700 underline-offset-4 transition hover:text-black hover:underline"
                  >
                    {site.name}
                    <ExternalLink
                      className="h-3.5 w-3.5 shrink-0 text-gray-400"
                      aria-hidden
                    />
                  </a>
                </li>
              ))}
            </ul>
          </div>

          <div className="flex flex-col gap-3 border-t border-gray-200 pt-6 sm:flex-row sm:items-start sm:justify-between sm:gap-4">
            <div className="flex min-w-0 flex-col gap-1">
              <span className="text-sm text-gray-500">
                2026년 2차 팀프로젝트 &apos;1팀: 준비된사람들&apos;
              </span>
              <p className="text-xs text-black">{teamMemberNames}</p>
            </div>
            <Link
              href="/"
              className="shrink-0 text-sm text-gray-600 transition hover:text-black"
            >
              홈으로
            </Link>
          </div>
        </div>
      </div>
    </footer>
  );
}
