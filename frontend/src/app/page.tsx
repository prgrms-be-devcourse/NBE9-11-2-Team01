import Link from "next/link";

export default function Home() {
  return (
    <div className="flex flex-1 items-center justify-center bg-zinc-50 px-6 font-sans dark:bg-black">
      <main className="w-full max-w-xl rounded-xl bg-white p-10 shadow-sm dark:bg-zinc-950">
        <h1 className="text-3xl font-semibold tracking-tight text-zinc-900 dark:text-zinc-50">
          메인 화면
        </h1>
        <p className="mt-3 text-zinc-600 dark:text-zinc-300">
          아래 버튼을 누르면 로그인 화면으로 이동합니다.
        </p>
        <Link
          href="/login"
          className="mt-8 inline-flex h-11 items-center justify-center rounded-md bg-zinc-900 px-5 text-sm font-medium text-white transition-colors hover:bg-zinc-700 dark:bg-zinc-100 dark:text-zinc-900 dark:hover:bg-zinc-300"
        >
          로그인
        </Link>
      </main>
    </div>
  );
}
