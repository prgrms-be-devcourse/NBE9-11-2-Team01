// middleware.ts
import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  const accessToken = request.cookies.get('accessToken')?.value;
  const { pathname } = request.nextUrl;

  // 1. 관리자 페이지 접근 시도 시
  if (pathname.startsWith('/admin')) {
    if (!accessToken) {
      return NextResponse.redirect(new URL('/login', request.url));
    }

    try {
      // 2. Secret Key 없이 페이로드만 디코딩 (Base64)
      const payloadPart = accessToken.split('.')[1];
      const decodedPayload = JSON.parse(
        Buffer.from(payloadPart, 'base64').toString('utf-8')
      );

      // 3. 권한 체크
      if (decodedPayload.role !== 'ADMIN') {
        // 관리자가 아니면 메인으로 리다이렉트
        return NextResponse.redirect(new URL('/', request.url));
      }
    } catch (error) {
      // 토큰 형식이 잘못된 경우
      return NextResponse.redirect(new URL('/login', request.url));
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: ['/admin/:path*'], // 관리자 관련 경로에만 미들웨어 적용
};