import type { Route } from "./+types/admin.company.$companyId";
import { AdminCompanyDetailPage } from "../../features/admin/pages/company-detail-page";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Company Detail | Vouchera" },
    { name: "description", content: "Company detail view for Vouchera admin." },
  ];
}

export default AdminCompanyDetailPage;
