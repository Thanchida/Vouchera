import type { Route } from "./+types/marketing.dashboard";
import { MarketingDashboardPage } from "../../features/marketing/pages/dashboard-page";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Marketing Dashboard | Vouchera" },
    { name: "description", content: "Manage campaigns and vouchers for your company." },
  ];
}

export default MarketingDashboardPage;
