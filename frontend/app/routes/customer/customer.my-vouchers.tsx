import type { Route } from "./+types/customer";
import { CustomerMyVouchersPage } from "../../features/customer/pages/customer-my-vouchers-page";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "My Vouchers | Vouchera" },
    { name: "description", content: "List of redemption history." },
  ];
}

export default CustomerMyVouchersPage;
