import type { Route } from "./+types/admin.companies";
import { AdminCompaniesPage } from "../../features/admin/pages/companies-page";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Companies | Admin | Vouchera" },
    { name: "description", content: "Manage companies in Vouchera." },
  ];
}

export default AdminCompaniesPage;
