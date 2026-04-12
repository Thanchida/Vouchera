const API_BASE = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";
const CSRF_COOKIE_NAME = "XSRF-TOKEN";
const CSRF_HEADER_NAME = "X-XSRF-TOKEN";

export type PageResponse<T> = {
  content: T[];
  number: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
};

export type PaginationParams = {
  page?: number;
  size?: number;
  sort?: string;
};

function readCookie(name: string) {
  const value = document.cookie
    .split("; ")
    .find(cookie => cookie.startsWith(`${encodeURIComponent(name)}=`));

  if (!value) {
    return "";
  }

  return decodeURIComponent(value.split("=").slice(1).join("="));
}

function shouldAttachCsrf(method: string) {
  const upperMethod = method.toUpperCase();
  return upperMethod !== "GET" && upperMethod !== "HEAD" && upperMethod !== "OPTIONS";
}

async function ensureCsrfCookie() {
  if (readCookie(CSRF_COOKIE_NAME)) {
    return;
  }

  await fetch(`${API_BASE}/api/auth/csrf`, {
    credentials: "include",
    method: "GET",
  });
}

export async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const method = (init.method ?? "GET").toUpperCase();
  const headers = new Headers(init.headers ?? {});

  if (shouldAttachCsrf(method) && path !== "/api/auth/csrf") {
    await ensureCsrfCookie();

    const csrfToken = readCookie(CSRF_COOKIE_NAME);
    if (csrfToken) {
      headers.set(CSRF_HEADER_NAME, csrfToken);
    }
  }

  if (init.body && !headers.has("Content-Type")) {
    headers.set("Content-Type", "application/json");
  }

  const response = await fetch(`${API_BASE}${path}`, {
    credentials: "include",
    ...init,
    headers,
  });

  if (!response.ok) {
    const payload = await response.json().catch(() => null);

    if (response.status === 401) {
      throw new Error("UNAUTHORIZED");
    }

    if (response.status === 403) {
      throw new Error("FORBIDDEN");
    }

    const message = payload?.message ?? `Request failed: ${response.status}`;
    throw new Error(message);
  }

  if (response.status === 204) {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}