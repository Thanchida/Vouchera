import type { Route } from "./+types/customer.company.$companyId";
import { CustomerCompanyPage } from "../../features/customer/pages/customer-company-page";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Company Campaigns | Customer | Vouchera" },
    { name: "description", content: "View campaigns for a selected company." },
  ];
}

export default CustomerCompanyPage;