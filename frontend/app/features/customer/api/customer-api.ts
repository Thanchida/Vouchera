import type { CustomerCampaign, CustomerCompany, CustomerUser, CustomerVoucherType, CustomerRedemption } from "../types";
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

export function getCurrentUser() {
  return request<CustomerUser>("/api/auth/me", { method: "GET" });
}

export function getCompanies(params?: PaginationParams) {
  return request<PageResponse<CustomerCompany>>(appendPagination("/api/companies", params), { method: "GET" });
}

export function getCompanyById(companyId: string) {
  return request<CustomerCompany>(`/api/companies/${companyId}`, { method: "GET" });
}

export function getAllCampaigns(params?: PaginationParams) {
  return request<PageResponse<CustomerCampaign>>(appendPagination("/api/campaigns", params), { method: "GET" });
}

export function getCompanyCampaigns(companyId: string, params?: PaginationParams) {
  return request<PageResponse<CustomerCampaign>>(
    appendPagination(`/api/companies/${companyId}/campaigns`, params),
    { method: "GET" }
  );
}

export function getCampaignById(campaignId: string) {
  return request<CustomerCampaign>(`/api/campaigns/${campaignId}`, { method: "GET" });
}

export function getVoucherTypesByCampaign(campaignId: string) {
  return request<CustomerVoucherType[]>(`/api/voucher-types/campaigns/${campaignId}`, {
    method: "GET",
  });
}

export function redeemVoucher(input: { voucherTypeId: string }) {
  return request(`/api/redemptions/redeem`, {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function getUserRedemptions(userId: string) {
  return request<CustomerRedemption[]>(`/api/redemptions/users/${userId}`, {
    method: "GET",
  });
}