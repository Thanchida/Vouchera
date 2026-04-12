import { useEffect, useState } from "react";
import { AdminShell } from "../components/admin-shell";
import {
  createCustomerUser,
  createMarketingUser,
  getCompanies,
  getUsers,
  updateUserAccountStatus,
} from "../api/admin-api";
import { ACCOUNT_STATUS_OPTIONS } from "../types";
import type { ApiCompany, ApiUser } from "../types";
import { error as toastError, success as toastSuccess } from "~/lib/toast";

function getCompanyName(companyId: string | null, companies: ApiCompany[]) {
  if (!companyId) {
    return "-";
  }

  return companies.find(company => company.id === companyId)?.name ?? "Unknown";
}

export function AdminUsersPage() {
  const PAGE_SIZE = 10;
  const [users, setUsers] = useState<ApiUser[]>([]);
  const [companies, setCompanies] = useState<ApiCompany[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalUsers, setTotalUsers] = useState(0);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [role, setRole] = useState<"CUSTOMER" | "MARKETING">("CUSTOMER");
  const [companyId, setCompanyId] = useState("");
  const [userStatusDrafts, setUserStatusDrafts] = useState<Record<string, ApiUser["accountStatus"]>>({});
  const [updatingUserId, setUpdatingUserId] = useState<string | null>(null);
  const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);

  useEffect(() => {
    void loadData(page);
  }, [page]);

  async function loadData(targetPage: number) {
    setLoading(true);
    try {
      const [usersResult, companiesResult] = await Promise.all([
        getUsers({ page: targetPage, size: PAGE_SIZE }),
        getCompanies({ page: 0, size: 500 }),
      ]);
      setUsers(usersResult.content);
      setCompanies(companiesResult.content);
      setUserStatusDrafts(
        Object.fromEntries(usersResult.content.map(user => [user.id, user.accountStatus]))
      );
      setTotalPages(usersResult.totalPages);
      setTotalUsers(usersResult.totalElements);
    } catch (err) {
      setMessage({
        type: "error",
        text: err instanceof Error ? err.message : "Failed to load users.",
      });
    } finally {
      setLoading(false);
    }
  }

  function closeModal() {
    setModalOpen(false);
    setEmail("");
    setPassword("");
    setRole("CUSTOMER");
    setCompanyId("");
  }

  function handleRoleChange(nextRole: "CUSTOMER" | "MARKETING") {
    setRole(nextRole);
    if (nextRole !== "MARKETING") {
      setCompanyId("");
    }
  }

  async function handleCreate() {
    if (!email.trim() || !password.trim()) {
      setMessage({ type: "error", text: "Email and password are required." });
      return;
    }

    if (role === "MARKETING" && !companyId) {
      setMessage({ type: "error", text: "Select a company for a marketing user." });
      return;
    }

    try {
      if (role === "MARKETING") {
        await createMarketingUser({ email: email.trim(), password, companyId });
      } else {
        await createCustomerUser({ email: email.trim(), password });
      }

      await loadData(page);
      toastSuccess("User created successfully.");
      closeModal();
    } catch (err) {
      toastError(err instanceof Error ? err.message : "Failed to create user.");
      setMessage({
        type: "error",
        text: err instanceof Error ? err.message : "Failed to create user.",
      });
    }
  }

  async function handleUserStatusUpdate(user: ApiUser) {
    const nextStatus = userStatusDrafts[user.id] ?? user.accountStatus;

    if (nextStatus === user.accountStatus) {
      return;
    }

    try {
      setUpdatingUserId(user.id);
      await updateUserAccountStatus(user.id, nextStatus);
      await loadData(page);
      toastSuccess("User status updated successfully.");
    } catch (err) {
      toastError(err instanceof Error ? err.message : "Failed to update user status.");
      setMessage({
        type: "error",
        text: err instanceof Error ? err.message : "Failed to update user status.",
      });
      setUserStatusDrafts(prev => ({
        ...prev,
        [user.id]: user.accountStatus,
      }));
    } finally {
      setUpdatingUserId(null);
    }
  }

  return (
    <AdminShell title="Users">
      <div className="mb-4 flex items-center justify-between gap-4">
        <div>
          <h3 className="text-lg font-semibold tracking-tight text-zinc-950">Users</h3>
          <p className="mt-1 text-sm text-zinc-600">View and create platform users.</p>
        </div>
        <button
          type="button"
          onClick={() => setModalOpen(true)}
          className="inline-flex h-11 items-center justify-center rounded-xl bg-zinc-900 px-5 text-sm font-medium text-white transition-colors hover:bg-zinc-800"
        >
          Create User
        </button>
      </div>

      {message ? (
        <div
          className={
            message.type === "success"
              ? "mb-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800"
              : "mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
          }
        >
          {message.text}
        </div>
      ) : null}

      <section className="overflow-hidden rounded-2xl border border-zinc-200 bg-white shadow-sm">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-zinc-200 text-left text-sm">
            <thead className="bg-zinc-50 text-zinc-500">
              <tr>
                <th className="px-5 py-4 font-medium">Email</th>
                <th className="px-5 py-4 font-medium">Role</th>
                <th className="px-5 py-4 font-medium">Company</th>
                <th className="px-5 py-4 font-medium">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200">
              {users.map(user => (
                <tr key={user.id} className="transition-colors hover:bg-zinc-50/80">
                  <td className="px-5 py-4 font-medium text-zinc-950">{user.email}</td>
                  <td className="px-5 py-4 text-zinc-700">{user.role}</td>
                  <td className="px-5 py-4 text-zinc-700">{getCompanyName(user.company?.id ?? null, companies)}</td>
                  <td className="px-5 py-4">
                    <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
                      <select
                        value={userStatusDrafts[user.id] ?? user.accountStatus}
                        onChange={event =>
                          setUserStatusDrafts(prev => ({
                            ...prev,
                            [user.id]: event.target.value as ApiUser["accountStatus"],
                          }))
                        }
                        className="h-10 rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
                      >
                        {ACCOUNT_STATUS_OPTIONS.map(status => (
                          <option key={status} value={status}>
                            {status}
                          </option>
                        ))}
                      </select>
                      <button
                        type="button"
                        onClick={() => void handleUserStatusUpdate(user)}
                        disabled={
                          updatingUserId === user.id ||
                          (userStatusDrafts[user.id] ?? user.accountStatus) === user.accountStatus
                        }
                        className="h-10 rounded-xl border border-zinc-300 bg-white px-3 text-sm font-medium text-zinc-800 transition-colors hover:bg-zinc-100 disabled:cursor-not-allowed disabled:opacity-50"
                      >
                        {updatingUserId === user.id ? "Updating..." : "Update"}
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
              {!loading && users.length === 0 ? (
                <tr>
                  <td className="px-5 py-6 text-sm text-zinc-500" colSpan={4}>
                    No users found.
                  </td>
                </tr>
              ) : null}
            </tbody>
          </table>
        </div>
        <div className="flex items-center justify-between border-t border-zinc-200 px-5 py-3 text-sm text-zinc-600">
          <p>
            Page {totalPages === 0 ? 0 : page + 1} of {totalPages} • {totalUsers} users
          </p>
          <div className="flex items-center gap-2">
            <button
              type="button"
              onClick={() => setPage(prev => Math.max(0, prev - 1))}
              disabled={page === 0}
              className="rounded-lg border border-zinc-300 px-3 py-1.5 text-sm font-medium text-zinc-800 transition-colors hover:bg-zinc-100 disabled:cursor-not-allowed disabled:opacity-50"
            >
              Previous
            </button>
            <button
              type="button"
              onClick={() => setPage(prev => (prev + 1 < totalPages ? prev + 1 : prev))}
              disabled={page + 1 >= totalPages}
              className="rounded-lg border border-zinc-300 px-3 py-1.5 text-sm font-medium text-zinc-800 transition-colors hover:bg-zinc-100 disabled:cursor-not-allowed disabled:opacity-50"
            >
              Next
            </button>
          </div>
        </div>
      </section>

      {modalOpen ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-zinc-950/40 px-4 py-6 backdrop-blur-sm">
          <button
            aria-label="Close modal"
            className="absolute inset-0 cursor-default"
            onClick={closeModal}
            type="button"
          />
          <div
            className="relative z-10 w-full max-w-md rounded-2xl border border-zinc-200 bg-white p-6 shadow-xl"
            onClick={event => event.stopPropagation()}
          >
            <h3 className="text-lg font-semibold tracking-tight text-zinc-950">Create User</h3>
            <p className="mt-1 text-sm text-zinc-600">Add a new admin, marketing, or customer user.</p>

            <div className="mt-5 space-y-4">
              <div>
                <label className="mb-2 block text-sm font-medium text-zinc-800" htmlFor="user-email">
                  Email
                </label>
                <input
                  id="user-email"
                  value={email}
                  onChange={event => setEmail(event.target.value)}
                  className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
                />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium text-zinc-800" htmlFor="user-password">
                  Password
                </label>
                <input
                  id="user-password"
                  type="password"
                  value={password}
                  onChange={event => setPassword(event.target.value)}
                  className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
                />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium text-zinc-800" htmlFor="user-role">
                  Role
                </label>
                <select
                  id="user-role"
                  value={role}
                  onChange={event => handleRoleChange(event.target.value as "CUSTOMER" | "MARKETING")}
                  className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
                >
                  <option value="CUSTOMER">CUSTOMER</option>
                  <option value="MARKETING">MARKETING</option>
                </select>
              </div>
              {role === "MARKETING" ? (
                <div>
                  <label className="mb-2 block text-sm font-medium text-zinc-800" htmlFor="user-company">
                    Company
                  </label>
                  <select
                    id="user-company"
                    value={companyId}
                    onChange={event => setCompanyId(event.target.value)}
                    className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
                  >
                    <option value="">Select a company</option>
                    {companies.map(company => (
                      <option key={company.id} value={company.id}>
                        {company.name}
                      </option>
                    ))}
                  </select>
                </div>
              ) : null}
            </div>

            <div className="mt-6 flex items-center justify-end gap-3">
              <button
                type="button"
                onClick={closeModal}
                className="h-11 rounded-xl border border-zinc-300 bg-white px-4 text-sm font-medium text-zinc-800 transition-colors hover:bg-zinc-100"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleCreate}
                className="h-11 rounded-xl bg-zinc-900 px-4 text-sm font-medium text-white transition-colors hover:bg-zinc-800"
              >
                Create User
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </AdminShell>
  );
}
