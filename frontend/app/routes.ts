import { type RouteConfig, index, route } from "@react-router/dev/routes";

export default [
  index("routes/home.tsx"),
  route("register", "routes/register.tsx"),
  route("login", "routes/login.tsx"),
  route("customer", "routes/customer/customer.tsx"),
  route("customer/my-vouchers", "routes/customer/customer.my-vouchers.tsx"),
  route("customer/company/:companyId", "routes/customer/customer.company.$companyId.tsx"),
  route("customer/campaign/:campaignId", "routes/customer/customer.campaign.$campaignId.tsx"),
  route("marketing/dashboard", "routes/marketing/marketing.dashboard.tsx"),
  route("marketing/campaign/:campaignId", "routes/marketing/marketing.campaign.$campaignId.tsx"),
  route("admin", "routes/admin/admin.tsx"),
  route("admin/companies", "routes/admin/admin.companies.tsx"),
  route("admin/companies/:companyId", "routes/admin/admin.company.$companyId.tsx"),
  route("admin/users", "routes/admin/admin.users.tsx"),
] satisfies RouteConfig;
