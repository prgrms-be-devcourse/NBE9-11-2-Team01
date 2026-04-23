# 🎯 합격시그널

> **취업 준비생을 위한 커뮤니티 플랫폼**

합격시그널은 취준생이 정보 공유, 질문/답변, 소통을 한 곳에서 할 수 있도록 만든 커뮤니티 서비스입니다.
본 프로젝트는 6인 팀(백엔드 중심)으로 **2주간** 진행한 팀 프로젝트입니다.

<br>

## 목차


1. [프로젝트 소개](#1-프로젝트-소개)
2. [기술 스택](#2-기술-스택)
3. [핵심 기능](#3-핵심-기능)
4. [시스템 아키텍처](#4-시스템-아키텍처)
5. [ERD](#5-erd)
6. [API 명세](#6-api-명세)
7. [트러블슈팅](#7-트러블슈팅)
8. [환경 변수 및 보안 설정](#8-환경-변수-및-보안-설정)
9. [팀원 소개 및 역할](#9-팀원-소개-및-역할)
910. [실행 방법](#-실행-방법)

<br>

## 1. 프로젝트 소개

> 취업 준비의 모든 정보를 한 곳에서 — 합격시그널

| 항목 | 내용 |
|:---:|:---|
| 프로젝트명 | 합격시그널 |
| 프로젝트 유형 | 취업 준비생 커뮤니티 플랫폼 |
| 팀 구성 | 6인 팀 프로젝트 (백엔드 중심) |
| 개발 기간 | 2주 |

**핵심 목표**
- 게시판 중심 커뮤니티 기능 구현
- JWT + HttpOnly Cookie 기반 인증/인가 강화
- Redis 캐싱 및 실시간 알림(SSE) 기반 사용자 경험 개선

<br>

## 2. 기술 스택

### Backend
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-6DB33F?logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-Enabled-6DB33F?logo=spring&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-5.1.0-00599C)
![Spring Security](https://img.shields.io/badge/Spring%20Security-Enabled-6DB33F?logo=springsecurity&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-0.11.5-000000?logo=jsonwebtokens&logoColor=white)
![H2](https://img.shields.io/badge/H2-Database-09476B)
![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?logo=redis&logoColor=white)

### Frontend
![Next.js](https://img.shields.io/badge/Next.js-16-000000?logo=nextdotjs&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6?logo=typescript&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-4.x-06B6D4?logo=tailwindcss&logoColor=white)

### Infra / Docs
![Docker](https://img.shields.io/badge/Docker-Redis-2496ED?logo=docker&logoColor=white)
![Swagger](https://img.shields.io/badge/Swagger-3.0.2-85EA2D?logo=swagger&logoColor=black)

<br>

## 3. 핵심 기능

### 👥 사용자/인증
- JWT + HttpOnly Cookie 기반 로그인/로그아웃/토큰 재발급
- 이메일 인증 코드 기반 아이디 찾기 / 비밀번호 재설정
- 회원 탈퇴 (소프트 삭제)

### 📝 커뮤니티
- 게시판/카테고리별 게시글 CRUD
- 댓글/대댓글(계층형) 작성, 수정, 삭제
- 게시글/댓글 좋아요 토글

### ⚡ 성능/실시간
- 좋아요 기반 게시판별 인기글 Top5 (Redis 캐싱 + 캐시 무효화)
- SSE(Server-Sent Events) 기반 실시간 댓글/답글 알림

### 🔍 검색/탐색
- 게시글 키워드 검색 / 카테고리 필터
- 최신순/인기순 정렬 / 페이지네이션

### 🛠️ 관리자
- 게시판/카테고리 생성, 수정, 삭제
- 회원 목록 조회

<br>

## 4. 시스템 아키텍처


```Client (Browser)
↓ HTTP
Frontend (Next.js 16)
↓ REST API + Cookie
Backend (Spring Boot 4)
├── H2 Database  (도메인 데이터)
├── Redis        (인기글 캐시)
└── Mail Server  (이메일 인증)
↑ SSE
Frontend (Next.js 16)
```

| 컴포넌트 | 역할 |
|:---:|:---|
| Spring Security Filter Chain | JWT 인증/인가, XSS 방지 필터 |
| Controller / Service / Repository | 도메인 레이어 분리 |
| Redis | 인기글 Top5 캐싱으로 DB 부하 감소 |
| SSE | 실시간 알림 스트림 제공 |

<br>

## 5. ERD

<img width="3694" height="2729" alt="image" src="https://github.com/user-attachments/assets/6086d0a0-94fc-4ae0-9f72-d4cddc7f9691" />

<br>

## 6. API 명세

백엔드 실행 후 Swagger UI에서 전체 API를 확인할 수 있습니다.

| 항목 | URL |
|:---:|:---|
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` |
| OpenAPI Docs | `http://localhost:8080/v3/api-docs` |

<br>

## 7. 트러블슈팅

### ① N+1 쿼리 문제 (게시글 상세 조회)

- **문제**: 게시글 상세 조회 시 게시판, 카테고리, 작성자 정보 접근마다 개별 쿼리가 실행되어 1번 요청에 4번 쿼리 발생
- **원인**: JPA 기본 Lazy Loading으로 인해 연관 엔티티 접근 시점마다 추가 SELECT 실행
- **해결**: `@EntityGraph`로 board, category, author를 단일 JOIN 쿼리로 한 번에 조회
- **결과**: 게시글 상세 조회 쿼리 4번 → 1번으로 감소

### ② XSS 방지 처리 위치 문제

- **문제**: XSS sanitize 로직이 `PostRepositoryImpl` 안에 있어 게시글 외 다른 도메인은 보호되지 않는 구조
- **원인**: 관심사 분리 원칙 위반, Repository가 보안 책임을 담당하는 구조
- **해결**: `XssFilter` + `XssRequestWrapper` 추가로 Filter 레이어에서 모든 요청을 일괄 처리
- **결과**: 전 도메인에 XSS 방지 자동 적용, Repository 단일 책임 원칙 준수

### ③ 인기글 Top5 캐시 데이터 불일치

- **문제**: 좋아요 변경 시 DB는 업데이트되지만 Redis 캐시는 이전 데이터를 반환
- **원인**: 캐시를 유지하면 실시간 반영 불가, 매번 초기화하면 Redis 사용 의미 퇴색
- **해결**: Cache-Aside 패턴 + 게시글 작성/삭제/좋아요 변경 시 캐시 무효화(evict), TTL 설정으로 방어선 구축
- **결과**: 데이터 정합성 확보 및 반복 조회 구간 DB 부하 감소

<br>

## 8. 환경 변수 및 보안 설정

본 프로젝트는 JWT Secret Key, 메일 서버 설정 등 민감한 정보를 코드에 직접 노출하지 않기 위해
설정 파일을 분리하여 관리합니다. 모든 파일은 `.gitignore`에 등록되어 있어 Git 원격 저장소에 포함되지 않습니다.

실행 시에는 팀 내부 채널을 통해 공유받은 내용을 직접 생성하여 사용합니다.

| 파일 | 위치 | 용도 |
|:---:|:---:|:---|
| `.env` | `backend/` | JWT Secret Key, 메일 서버 계정 정보 |
| `application-secret.yaml` | `backend/src/main/resources/` | JWT Secret Key 설정 |
| `application-mail.yaml` | `backend/src/main/resources/` | 메일 서버 설정 (GreenMail 로컬 테스트용) |

<br>

## 9. 팀원 소개 및 역할

<table>
  <tr>
    <th>이름</th>
    <th>GitHub</th>
    <th>역할</th>
  </tr>
  <tr>
    <td>김강산</td>
    <td><a href="https://github.com/csgangsai06512-ue">@csgangsai06512-ue</a></td>
    <td>백엔드, 회원가입/로그인/인증, 마이페이지</td>
  </tr>
  <tr>
    <td>김경탁</td>
    <td><a href="https://github.com/gyeongtaggim865-hue">@gyeongtaggim865-hue</a></td>
    <td>백엔드, 게시글 좋아요, 댓글 작성/수정</td>
  </tr>
  <tr>
    <td>김민혁</td>
    <td><a href="https://github.com/mk-404lab">@mk-404lab</a></td>
    <td>백엔드, 게시글 CRUD/정렬/인기글</td>
  </tr>
  <tr>
    <td>김정욱</td>
    <td><a href="https://github.com/study-withme">@study-withme</a></td>
    <td>백엔드, 댓글 조회/삭제/좋아요</td>
  </tr>
  <tr>
    <td>김하늘</td>
    <td><a href="https://github.com/c1oud-dev">@c1oud-dev</a></td>
    <td>백엔드, 게시글 조회/검색/페이징</td>
  </tr>
  <tr>
    <td>이유진</td>
    <td><a href="https://github.com/YJin33">@YJin33</a></td>
    <td>백엔드, 관리자 기능, 실시간 알림(SSE)</td>
  </tr>
</table>

<br>

## 10. 실행 방법

### ▶ Backend
```bash
cd backend
./gradlew bootRun
```

### ▶ Frontend
```bash
cd frontend
npm install
npm run dev
```

### ▶ Redis (Docker)
```bash
# 최초 실행
docker run -d --name redis -p 6379:6379 redis

# 재시작
docker start redis
```

<br>

## 📌 프로젝트 한 줄 요약

> **합격시그널은 취준생의 정보 공유와 소통을 빠르고 안전하게 지원하는, 인증·실시간 알림·캐싱 기반 커뮤니티 플랫폼입니다.**
