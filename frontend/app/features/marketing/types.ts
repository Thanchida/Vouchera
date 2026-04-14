export type MarketingCompany = {
  id: string;
  name: string;
  companyStatus: "PENDING" | "ACTIVE" | "SUSPENDED" | "REJECTED";
};

export type MarketingUser = {
  id: string;
  email: string;
  role: "MARKETING" | "ADMIN" | "CUSTOMER";
  accountStatus: "ACTIVE" | "SUSPENDED";
  company?: MarketingCompany | null;
};

export type ApiCampaignStatus = "PENDING" | "ACTIVE" | "PAUSED" | "ENDED";

export type ApiCampaign = {
  id: string;
  name: string;
  description?: string;
  status: ApiCampaignStatus;
  startTime: string;
  endTime: string;
};

export type ApiVoucherType = {
  id: string;
  discountPercent: number;
  totalQuota: number;
  remainingQuota: number;
};

export type AddVoucherTypeInput = {
  name: string;
  discountPercent: number;
  totalQuota: number;
};
