import type { ApiCompany, ApiUser, AuthMe } from "../types";
import { request } from "~/lib/api-client";
import type { PageResponse, PaginationParams } from "~/lib/api-client";

function appendPagination(path: string, params?: PaginationParams) {
  if (!params) {
    return path;
  }

  const query = new URLSearchParams();
  if (params.page !== undefined) {
    query.set("page", String(params.page));
  }
  if (params.size !== undefined) {
    query.set("size", String(params.size));
  }
  if (params.sort) {
    query.set("sort", params.sort);
  }

  const queryString = query.toString();
  return queryString ? `${path}?${queryString}` : path;
}

export function getMe() {
  return request<AuthMe>("/api/auth/me", { method: "GET" });
}

export function getCompanies(params?: PaginationParams) {
  return request<PageResponse<ApiCompany>>(appendPagination("/api/companies", params), { method: "GET" });
}

export function createCompany(name: string) {
  const query = new URLSearchParams({ name });
  return request<ApiCompany>(`/api/companies?${query.toString()}`, { method: "POST" });
}

export function getUsers(params?: PaginationParams) {
  return request<PageResponse<ApiUser>>(appendPagination("/api/users", params), { method: "GET" });
}

export function createMarketingUser(input: { email: string; password: string; companyId: string }) {
  return request<ApiUser>("/api/users/internal/register", {
    method: "POST",
    body: JSON.stringify({
      email: input.email,
      password: input.password,
      role: "MARKETING",
      companyId: input.companyId,
    }),
  });
}

export function createCustomerUser(input: { email: string; password: string }) {
  return request<ApiUser>("/api/users/register", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function updateUserAccountStatus(userId: string, status: ApiUser["accountStatus"]) {
  const query = new URLSearchParams({ status });
  return request<ApiUser>(`/api/users/${userId}/status?${query.toString()}`, {
    method: "PATCH",
  });
}

export function updateCompanyStatus(companyId: string, status: ApiCompany["companyStatus"]) {
  const query = new URLSearchParams({ status });
  return request<ApiCompany>(`/api/companies/${companyId}/status?${query.toString()}`, {
    method: "PATCH",
  });
}
