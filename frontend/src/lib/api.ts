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

export async function apiGet<T>(path: string): Promise<ApiResponse<T>> {
  const res = await fetch(apiUrl(path), {
    method: "GET",
    credentials: "include",
    cache: "no-store",
  });
  return handleResponse<T>(res);
}

export async function apiPostJson<T, B>(
  path: string,
  body: B,
): Promise<ApiResponse<T>> {
  const res = await fetch(apiUrl(path), {
    method: "POST",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  return handleResponse<T>(res);
}

/** 본문 없는 POST (쿼리스트링만 있는 엔드포인트용) */
export async function apiPostEmpty<T>(path: string): Promise<ApiResponse<T>> {
  const res = await fetch(apiUrl(path), {
    method: "POST",
    credentials: "include",
  });
  return handleResponse<T>(res);
}

export async function apiPutJson<T, B>(
  path: string,
  body: B,
): Promise<ApiResponse<T>> {
  const res = await fetch(apiUrl(path), {
    method: "PUT",
    credentials: "include",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  });
  return handleResponse<T>(res);
}

export async function apiDelete<T>(path: string): Promise<ApiResponse<T>> {
  const res = await fetch(apiUrl(path), {
    method: "DELETE",
    credentials: "include",
  });
  return handleResponse<T>(res);
}
