import type { Route } from "./+types/customer";
import { CustomerHomePage } from "../../features/customer/pages/customer-home-page";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Customer Home | Vouchera" },
    { name: "description", content: "Browse companies and campaigns as a customer." },
  ];
}

export default CustomerHomePage;