import type { Route } from "./+types/admin.users";
import { AdminUsersPage } from "../../features/admin/pages/users-page";

export function meta({}: Route.MetaArgs) {
  return [
    { title: "Users | Admin | Vouchera" },
    { name: "description", content: "Manage users in Vouchera." },
  ];
}

export default AdminUsersPage;
