import type { Metadata } from "next";
import { Plus_Jakarta_Sans } from "next/font/google";
import { Geist_Mono } from "next/font/google";
import { AuthProvider } from "@/context/AuthContext";
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
  description:
    "합격시그널 — 취업 준비생을 위한 커뮤니티. 채용·자소서·면접·멘토링 정보를 나누고, 합격까지 함께합니다.",
  icons: {
    icon: "/signal-logo.svg",
  },
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
          <AppShell>{children}</AppShell>
        </AuthProvider>
      </body>
    </html>
  );
}
