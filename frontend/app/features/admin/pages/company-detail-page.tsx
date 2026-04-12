import { useEffect, useMemo, useState } from "react";
import { Link, useParams } from "react-router";
import { AdminShell } from "../components/admin-shell";
import { createMarketingUser, getCompanies, getUsers } from "../api/admin-api";
import type { ApiCompany, ApiUser } from "../types";
import { error as toastError, success as toastSuccess } from "~/lib/toast";

function formatDate(value?: string) {
  if (!value) {
    return "-";
  }

  const parsed = new Date(value);
  if (Number.isNaN(parsed.getTime())) {
    return "-";
  }

  return parsed.toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
    year: "numeric",
  });
}

export function AdminCompanyDetailPage() {
  const { companyId } = useParams();
  const [companies, setCompanies] = useState<ApiCompany[]>([]);
  const [users, setUsers] = useState<ApiUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [modalOpen, setModalOpen] = useState(false);
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);

  useEffect(() => {
    void loadData();
  }, []);

  async function loadData() {
    setLoading(true);
    try {
      const [companiesResult, usersResult] = await Promise.all([getCompanies(), getUsers()]);
      setCompanies(companiesResult.content);
      setUsers(usersResult.content);
    } catch (err) {
      setMessage({
        type: "error",
        text: err instanceof Error ? err.message : "Failed to load company detail.",
      });
    } finally {
      setLoading(false);
    }
  }

  const company = useMemo(
    () => companies.find(item => item.id === companyId),
    [companies, companyId]
  );

  if (!loading && !company) {
    return (
      <AdminShell title="Company Detail">
        <section className="rounded-2xl border border-zinc-200 bg-white p-8 shadow-sm">
          <h3 className="text-lg font-semibold text-zinc-950">Company not found</h3>
          <p className="mt-2 text-sm text-zinc-600">The requested company record does not exist.</p>
          <Link
            to="/admin/companies"
            className="mt-5 inline-flex h-11 items-center justify-center rounded-xl bg-zinc-900 px-5 text-sm font-medium text-white transition-colors hover:bg-zinc-800"
          >
            Back to Companies
          </Link>
        </section>
      </AdminShell>
    );
  }

  const companyRecord = company ?? null;
  const marketingUsers = users.filter(
    user => user.role === "MARKETING" && user.company?.id === companyId
  );

  function closeModal() {
    setModalOpen(false);
    setEmail("");
    setPassword("");
  }

  async function handleAddUser() {
    if (!email.trim() || !password.trim()) {
      setMessage({ type: "error", text: "Email and password are required." });
      return;
    }

    if (!companyRecord) {
      setMessage({ type: "error", text: "Company not found." });
      return;
    }

    try {
      await createMarketingUser({
        email: email.trim(),
        password,
        companyId: companyRecord.id,
      });
      await loadData();
      toastSuccess("Marketing user added successfully.");
      closeModal();
    } catch (err) {
      toastError(err instanceof Error ? err.message : "Failed to add marketing user.");
      setMessage({
        type: "error",
        text: err instanceof Error ? err.message : "Failed to add marketing user.",
      });
    }
  }

  const marketingCount = marketingUsers.length;

  return (
    <AdminShell title="Company Detail">
      <div className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm text-zinc-500">Company profile</p>
          <h3 className="mt-1 text-2xl font-semibold tracking-tight text-zinc-950">
            {companyRecord?.name ?? "Loading..."}
          </h3>
        </div>
        <button
          type="button"
          onClick={() => setModalOpen(true)}
          disabled={!companyRecord}
          className="inline-flex h-11 items-center justify-center rounded-xl bg-zinc-900 px-5 text-sm font-medium text-white transition-colors hover:bg-zinc-800"
        >
          Add Marketing User
        </button>
      </div>

      <div className="mt-6 grid gap-4 md:grid-cols-3">
        <article className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
          <p className="text-sm text-zinc-500">Status</p>
          <p className="mt-3 text-2xl font-semibold text-zinc-950">{companyRecord?.companyStatus ?? "-"}</p>
        </article>
        <article className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
          <p className="text-sm text-zinc-500">Created</p>
          <p className="mt-3 text-2xl font-semibold text-zinc-950">{formatDate(companyRecord?.createdAt)}</p>
        </article>
        <article className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm">
          <p className="text-sm text-zinc-500">Marketing users</p>
          <p className="mt-3 text-2xl font-semibold text-zinc-950">{marketingCount}</p>
        </article>
      </div>

      {message ? (
        <div
          className={
            message.type === "success"
              ? "mt-6 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800"
              : "mt-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
          }
        >
          {message.text}
        </div>
      ) : null}

      <section className="mt-6 rounded-2xl border border-zinc-200 bg-white shadow-sm">
        <div className="border-b border-zinc-200 px-6 py-4">
          <h4 className="text-lg font-semibold tracking-tight text-zinc-950">Marketing users</h4>
          <p className="mt-1 text-sm text-zinc-600">Users assigned to this company.</p>
        </div>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-zinc-200 text-left text-sm">
            <thead className="bg-zinc-50 text-zinc-500">
              <tr>
                <th className="px-6 py-4 font-medium">Email</th>
                <th className="px-6 py-4 font-medium">Status</th>
                <th className="px-6 py-4 font-medium">Created Date</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200">
              {marketingUsers.map(user => (
                <tr key={user.id} className="transition-colors hover:bg-zinc-50/80">
                  <td className="px-6 py-4 font-medium text-zinc-950">{user.email}</td>
                  <td className="px-6 py-4">
                    <span className="rounded-full border border-zinc-200 px-3 py-1 text-xs font-medium text-zinc-700">
                      {user.accountStatus}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-zinc-600">{formatDate(user.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
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
            <h3 className="text-lg font-semibold tracking-tight text-zinc-950">Add Marketing User</h3>
            <p className="mt-1 text-sm text-zinc-600">
              This user will be assigned to {companyRecord?.name ?? "this company"}.
            </p>

            <div className="mt-5 space-y-4">
              <div>
                <label className="mb-2 block text-sm font-medium text-zinc-800" htmlFor="marketing-email">
                  Email
                </label>
                <input
                  id="marketing-email"
                  value={email}
                  onChange={event => setEmail(event.target.value)}
                  className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
                />
              </div>
              <div>
                <label className="mb-2 block text-sm font-medium text-zinc-800" htmlFor="marketing-password">
                  Password
                </label>
                <input
                  id="marketing-password"
                  type="password"
                  value={password}
                  onChange={event => setPassword(event.target.value)}
                  className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
                />
              </div>
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
                onClick={handleAddUser}
                className="h-11 rounded-xl bg-zinc-900 px-4 text-sm font-medium text-white transition-colors hover:bg-zinc-800"
              >
                Add User
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </AdminShell>
  );
}
