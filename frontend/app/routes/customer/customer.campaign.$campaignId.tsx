import type { Route } from "./+types/customer.campaign.$campaignId";
import { CustomerCampaignPage } from "../../features/customer/pages/customer-campaign-page";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Campaign Detail | Customer | Vouchera" },
    { name: "description", content: "View voucher types and receive a voucher." },
  ];
}

export default CustomerCampaignPage;