// app/layout.tsx
import type { Metadata } from "next";
import { Plus_Jakarta_Sans, Geist_Mono } from "next/font/google";
import { AuthProvider } from "@/context/AuthContext";
import { NotificationProvider } from "./notifications/NotificationProvider";// 추가
import { AppShell } from "@/components/shell/AppShell";
import "./globals.css";

const plusJakarta = Plus_Jakarta_Sans({
  variable: "--font-plus-jakarta",
  subsets: ["latin"],
});

const geistMono = Geist_Mono({
  variable: "--font-geist-mono",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  title: "합격시그널 | 취업 준비 커뮤니티",
  description: "합격시그널 — 취업 준비생을 위한 커뮤니티...",
  icons: { icon: "/signal-logo.svg" },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html
      lang="ko"
      className={`${plusJakarta.variable} ${geistMono.variable} h-full antialiased`}
    >
      <body className="min-h-full font-sans">
        <AuthProvider>
          {/* NotificationProvider로 감싸주어 전역에서 유지되도록 설정 */}
          <NotificationProvider>
            <AppShell>{children}</AppShell>
          </NotificationProvider>
        </AuthProvider>
      </body>
    </html>
  );
}