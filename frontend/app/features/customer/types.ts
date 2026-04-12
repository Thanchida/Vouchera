export type CustomerCompany = {
  id: string;
  name: string;
  companyStatus: "PENDING" | "ACTIVE" | "SUSPENDED" | "REJECTED";
};

export type CustomerCampaign = {
  id: string;
  name: string;
  description: string;
  status: "PENDING" | "ACTIVE" | "PAUSED" | "ENDED";
  startTime: string;
  endTime: string;
  company: CustomerCompany;
};

export type CustomerVoucherType = {
  id: string;
  discountPercent: number;
  totalQuota: number;
  remainingQuota: number;
};

export type CustomerUser = {
  id: string;
  email: string;
  role: "ADMIN" | "MARKETING" | "CUSTOMER";
  accountStatus: "ACTIVE" | "SUSPENDED";
  company?: CustomerCompany | null;
};

export type CustomerRedemption = {
  id: string;
  voucherType: CustomerVoucherType;
  redeemedAt: string;
  usedAt: string | null;
  status: "CLAIMED" | "USED" | "EXPIRED";
};