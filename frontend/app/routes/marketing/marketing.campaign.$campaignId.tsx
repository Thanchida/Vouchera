import type { Route } from "./+types/marketing.campaign.$campaignId";
import { MarketingCampaignDetailPage } from "../../features/marketing/pages/campaign-detail-page";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Campaign Detail | Marketing | Vouchera" },
    { name: "description", content: "View campaign detail and manage voucher types." },
  ];
}

export default MarketingCampaignDetailPage;
