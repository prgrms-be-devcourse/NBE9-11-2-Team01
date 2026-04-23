export const API_BASE =
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080";

export function apiUrl(path: string): string {
  if (path.startsWith("http")) return path;
  const base = API_BASE.replace(/\/$/, "");
  return `${base}${path.startsWith("/") ? path : `/${path}`}`;
}

export type ApiResponse<T> = {
  success: boolean;
  code?: string | null;
  message?: string | null;
  data?: T;
};

// --- 내부 유틸리티: 응답 처리 ---
async function handleResponse<T>(res: Response): Promise<ApiResponse<T>> {
  const text = await res.text();
  let body: ApiResponse<T> | null = null;
  try {
    body = text ? (JSON.parse(text) as ApiResponse<T>) : null;
  } catch {
    throw new Error(text?.slice(0, 280) || `HTTP ${res.status}`);
  }
  
  if (!res.ok || body?.success === false) {
    throw new Error(body?.message || `HTTP ${res.status}`);
  }
  return body as ApiResponse<T>;
}

/**
 * [핵심] 모든 API 요청을 가로채서 401 발생 시 리프레시 후 재시도하는 래퍼 함수
 */
async function fetchWithRetry<T>(
  path: string,
  options: RequestInit = {}
): Promise<ApiResponse<T>> {
  let res = await fetch(apiUrl(path), options);

  // 1. 만약 401(Unauthorized) 에러가 발생했다면?
  if (res.status === 401 && !path.includes("/auth/refresh")) {
    try {
      // 2. 몰래 리프레시 API 호출 (쿠키 기반)
      const refreshRes = await fetch(apiUrl("/auth/refresh"), {
        method: "POST",
        credentials: "include",
      });

      if (refreshRes.ok) {
        // 3. 리프레시 성공 시, 원래 실패했던 요청을 한 번 더 시도
        res = await fetch(apiUrl(path), options);
      }
    } catch (e) {
      console.error("Silent refresh failed", e);
    }
  }

  return handleResponse<T>(res);
}

// --- 외부 노출 함수들 ---

export async function apiGet<T>(path: string): Promise<ApiResponse<T>> {
  return fetchWithRetry<T>(path, {
    method: "GET",
    credentials: "include",
    cache: "no-store",
  });
}

export async function apiPostJson<T, B>(
  path: string,
  body: B,
): Promise<ApiResponse<T>> {
  return fetchWithRetry<T>(path, {
    method: "POST",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}

/** 본문 없는 POST (쿼리스트링만 있는 엔드포인트용) */
export async function apiPostEmpty<T>(path: string): Promise<ApiResponse<T>> {
  return fetchWithRetry<T>(path, {
    method: "POST",
    credentials: "include",
  });
}

export async function apiPutJson<T, B>(
  path: string,
  body: B,
): Promise<ApiResponse<T>> {
  return fetchWithRetry<T>(path, {
    method: "PUT",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
}

export async function apiDelete<T>(path: string): Promise<ApiResponse<T>> {
  return fetchWithRetry<T>(path, {
    method: "DELETE",
    credentials: "include",
  });
}