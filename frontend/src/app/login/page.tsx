"use client";

import { FormEvent, useEffect, useMemo, useRef, useState } from "react";
import Link from "next/link";

type LoginRequest = {
  email: string;
  password: string;
};

type LoginApiResponse = {
  success: boolean;
  code: string | null;
  message: string | null;
  data: string | null;
};

const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function getApiEndpoint() {
  const baseUrl = process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";
  const loginPath = process.env.NEXT_PUBLIC_LOGIN_PATH ?? "/api/auth/login";
  return `${baseUrl}${loginPath}`;
}

export default function LoginPage() {
  const [form, setForm] = useState<LoginRequest>({ email: "", password: "" });
  const [errorMessage, setErrorMessage] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const subscribeAbortRef = useRef<AbortController | null>(null);

  useEffect(() => {
    return () => {
      if (subscribeAbortRef.current) {
        subscribeAbortRef.current.abort();
      }
    };
  }, []);

  async function connectNotification(token: string) {
    if (subscribeAbortRef.current) {
      subscribeAbortRef.current.abort();
      subscribeAbortRef.current = null;
    }

    const controller = new AbortController();
    subscribeAbortRef.current = controller;
    const subscribeUrl = "http://localhost:8080/subscribe";

    const response = await fetch(subscribeUrl, {
      method: "GET",
      headers: {
        Accept: "text/event-stream",
        Authorization: `Bearer ${token}`,
      },
      signal: controller.signal,
      cache: "no-store",
    });

    if (!response.ok) {
      const responseText = await response.text();
      throw new Error(responseText || "알림 연결에 실패했습니다.");
    }

    if (!response.body) {
      throw new Error("알림 스트림 응답 본문이 없습니다.");
    }

    alert("연결 완료");

    const reader = response.body.getReader();
    const decoder = new TextDecoder("utf-8");
    let buffer = "";

    while (true) {
      const { value, done } = await reader.read();
      if (done) {
        break;
      }

      buffer += decoder.decode(value, { stream: true });
      const events = buffer.split("\n\n");
      buffer = events.pop() ?? "";

      for (const eventBlock of events) {
        const dataLines = eventBlock
          .split("\n")
          .filter((line) => line.startsWith("data:"))
          .map((line) => line.slice(5).trim());

        if (!dataLines.length) {
          continue;
        }

        const rawData = dataLines.join("\n");
        try {
          const parsed = JSON.parse(rawData) as { content?: string };
          alert(parsed.content ?? rawData);
        } catch {
          alert(rawData);
        }
      }
    }
  }

  const validationError = useMemo(() => {
    if (!form.email.trim()) {
      return "이메일은 필수 입력 값입니다.";
    }
    if (!EMAIL_REGEX.test(form.email)) {
      return "올바른 이메일 형식이 아닙니다.";
    }
    if (!form.password) {
      return "비밀번호는 필수 입력 값입니다.";
    }
    if (form.password.length < 4) {
      return "비밀번호는 최소 4글자 이상이어야 합니다.";
    }
    return "";
  }, [form]);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setErrorMessage("");
    setSuccessMessage("");

    if (validationError) {
      setErrorMessage(validationError);
      return;
    }

    setIsSubmitting(true);
    try {
      const response = await fetch(getApiEndpoint(), {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          email: form.email.trim(),
          password: form.password,
        }),
      });

      if (!response.ok) {
        const responseText = await response.text();
        throw new Error(responseText || "로그인 요청에 실패했습니다.");
      }

      const loginResult = (await response.json()) as LoginApiResponse;
      const token = loginResult?.data;
      if (!token) {
        throw new Error("로그인 토큰을 받지 못했습니다.");
      }

      setSuccessMessage("로그인 완료");
      setIsLoggedIn(true);
      await connectNotification(token);
    } catch (error) {
      if (error instanceof Error) {
        setErrorMessage(error.message);
      } else {
        setErrorMessage("알 수 없는 오류가 발생했습니다.");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div className="flex flex-1 items-center justify-center bg-zinc-50 px-6 py-10 font-sans dark:bg-black">
      <main className="w-full max-w-md rounded-xl bg-white p-8 shadow-sm dark:bg-zinc-950">
        <h1 className="text-2xl font-semibold text-zinc-900 dark:text-zinc-50">로그인</h1>
        <p className="mt-2 text-sm text-zinc-600 dark:text-zinc-300">
          이메일과 비밀번호를 입력해 주세요.
        </p>

        {!isLoggedIn && (
          <form onSubmit={handleSubmit} className="mt-6 space-y-4">
            <div>
              <label
                htmlFor="email"
                className="mb-1 block text-sm font-medium text-zinc-700 dark:text-zinc-200"
              >
                이메일
              </label>
              <input
                id="email"
                type="email"
                value={form.email}
                onChange={(event) =>
                  setForm((prev) => ({
                    ...prev,
                    email: event.target.value,
                  }))
                }
                placeholder="example@email.com"
                className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm outline-none transition focus:border-zinc-500 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100"
              />
            </div>

            <div>
              <label
                htmlFor="password"
                className="mb-1 block text-sm font-medium text-zinc-700 dark:text-zinc-200"
              >
                비밀번호
              </label>
              <input
                id="password"
                type="password"
                value={form.password}
                onChange={(event) =>
                  setForm((prev) => ({
                    ...prev,
                    password: event.target.value,
                  }))
                }
                placeholder="최소 4글자"
                className="w-full rounded-md border border-zinc-300 px-3 py-2 text-sm outline-none transition focus:border-zinc-500 dark:border-zinc-700 dark:bg-zinc-900 dark:text-zinc-100"
              />
            </div>

            {errorMessage && (
              <p className="text-sm text-red-600 dark:text-red-400">{errorMessage}</p>
            )}

            <button
              type="submit"
              disabled={isSubmitting}
              className="w-full rounded-md bg-zinc-900 px-4 py-2 text-sm font-medium text-white transition hover:bg-zinc-700 disabled:cursor-not-allowed disabled:opacity-60 dark:bg-zinc-100 dark:text-zinc-900 dark:hover:bg-zinc-300"
            >
              {isSubmitting ? "로그인 요청 중..." : "로그인"}
            </button>
          </form>
        )}

        {successMessage && (
          <p className="mt-6 text-sm text-green-600 dark:text-green-400">{successMessage}</p>
        )}

        <Link
          href="/"
          className="mt-5 inline-block text-sm text-zinc-600 underline-offset-4 hover:underline dark:text-zinc-300"
        >
          메인 화면으로 돌아가기
        </Link>
      </main>
    </div>
  );
}
