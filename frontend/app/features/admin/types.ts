export type ApiCompany = {
  id: string;
  name: string;
  companyStatus: "PENDING" | "ACTIVE" | "SUSPENDED" | "REJECTED";
  createdAt?: string;
};

export const COMPANY_STATUS_OPTIONS = ["PENDING", "ACTIVE", "SUSPENDED", "REJECTED"] as const;

export type ApiUser = {
  id: string;
  email: string;
  role: "CUSTOMER" | "MARKETING" | "ADMIN";
  accountStatus: "ACTIVE" | "SUSPENDED";
  createdAt?: string;
  company?: {
    id: string;
    name: string;
    companyStatus: "PENDING" | "ACTIVE" | "SUSPENDED" | "REJECTED";
  } | null;
};

export const ACCOUNT_STATUS_OPTIONS = ["ACTIVE", "SUSPENDED"] as const;

export type AuthMe = ApiUser;
