import type { Route } from "./+types/admin";
import { AdminDashboardPage } from "../../features/admin/pages/dashboard-page";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Admin Dashboard | Vouchera" },
    { name: "description", content: "Admin overview for Vouchera." },
  ];
}

export default AdminDashboardPage;
