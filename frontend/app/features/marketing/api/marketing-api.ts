import type { ApiCampaign, ApiVoucherType, MarketingUser } from "../types";
import { request } from "~/lib/api-client";
import type { PageResponse, PaginationParams } from "~/lib/api-client";

function appendPagination(path: string, params?: PaginationParams & { status?: string }) {
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
  if (params.status) {
    query.set("status", params.status);
  }

  const queryString = query.toString();
  return queryString ? `${path}?${queryString}` : path;
}

export function getMarketingSession() {
  return request<MarketingUser>("/api/auth/me", { method: "GET" });
}

export function getCompanyCampaigns(
  companyId: string,
  params?: PaginationParams & { status?: "PENDING" | "ACTIVE" | "PAUSED" | "ENDED" }
) {
  return request<PageResponse<ApiCampaign>>(
    appendPagination(`/api/companies/${companyId}/campaigns`, params),
    { method: "GET" }
  );
}

export function getCampaignById(campaignId: string) {
  return request<ApiCampaign>(`/api/campaigns/${campaignId}`, { method: "GET" });
}

export function createCampaign(input: {
  companyId: string;
  name: string;
  description: string;
  startTime: string;
  endTime: string;
}) {
  return request<ApiCampaign>("/api/campaigns", {
    method: "POST",
    body: JSON.stringify(input),
  });
}

export function updateCampaign(input: {
  campaignId: string;
  name: string;
  description: string;
  startTime: string;
  endTime: string;
}) {
  return request<ApiCampaign>(`/api/campaigns/${input.campaignId}`, {
    method: "PUT",
    body: JSON.stringify({
      name: input.name,
      description: input.description,
      startTime: input.startTime,
      endTime: input.endTime,
    }),
  });
}

export function deleteCampaign(campaignId: string) {
  return request<void>(`/api/campaigns/${campaignId}`, {
    method: "DELETE",
  });
}

export function pauseCampaign(campaignId: string) {
  return request<ApiCampaign>(`/api/campaigns/${campaignId}/pause`, {
    method: "POST",
  });
}

export function resumeCampaign(campaignId: string) {
  return request<ApiCampaign>(`/api/campaigns/${campaignId}/resume`, {
    method: "POST",
  });
}

export function endCampaign(campaignId: string) {
  return request<ApiCampaign>(`/api/campaigns/${campaignId}/end`, {
    method: "POST",
  });
}

export function getVoucherTypesByCampaign(campaignId: string) {
  return request<ApiVoucherType[]>(`/api/voucher-types/campaigns/${campaignId}`, { method: "GET" });
}

export function createVoucherType(input: {
  campaignId: string;
  discountPercent: number;
  totalQuota: number;
}) {
  const query = new URLSearchParams({
    discountPercent: String(input.discountPercent),
    totalQuota: String(input.totalQuota),
  });
  return request<ApiVoucherType>(
    `/api/voucher-types/campaigns/${input.campaignId}?${query.toString()}`,
    { method: "POST" }
  );
}

export function increaseVoucherTypeQuota(voucherTypeId: string, amount: number) {
  const query = new URLSearchParams({ amount: String(amount) });
  return request<ApiVoucherType>(`/api/voucher-types/${voucherTypeId}/increase-quota?${query.toString()}`, {
    method: "POST",
  });
}
