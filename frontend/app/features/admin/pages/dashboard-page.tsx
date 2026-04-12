import { Link } from "react-router";
import { useEffect, useMemo, useState } from "react";
import { AdminShell } from "../components/admin-shell";
import { getCompanies, getUsers } from "../api/admin-api";
import type { ApiCompany, ApiUser } from "../types";

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

export function AdminDashboardPage() {
  const [companies, setCompanies] = useState<ApiCompany[]>([]);
  const [users, setUsers] = useState<ApiUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    void (async () => {
      setLoading(true);
      setError("");

      try {
        const [companiesResult, usersResult] = await Promise.all([getCompanies(), getUsers()]);
        setCompanies(companiesResult.content);
        setUsers(usersResult.content);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load dashboard data.");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  const stats = useMemo(
    () => [
      {
        label: "Total companies",
        value: companies.length,
        helper: "All onboarded companies",
      },
      {
        label: "Active companies",
        value: companies.filter(company => company.companyStatus === "ACTIVE").length,
        helper: "Ready for campaigns",
      },
      {
        label: "Total users",
        value: users.length,
        helper: "Customers, marketing, and admins",
      },
    ],
    [companies, users]
  );

  const recentCompanies = useMemo(() => {
    return [...companies]
      .sort((a, b) => {
        const aTime = a.createdAt ? new Date(a.createdAt).getTime() : 0;
        const bTime = b.createdAt ? new Date(b.createdAt).getTime() : 0;
        return bTime - aTime;
      })
      .slice(0, 3);
  }, [companies]);

  return (
    <AdminShell title="Dashboard">
      {error ? (
        <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      ) : null}

      <div className="grid gap-4 md:grid-cols-3">
        {stats.map(stat => (
          <article
            key={stat.label}
            className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm"
          >
            <p className="text-sm text-zinc-500">{stat.label}</p>
            <p className="mt-3 text-3xl font-semibold tracking-tight text-zinc-950">
              {loading ? "..." : stat.value}
            </p>
            <p className="mt-2 text-sm text-zinc-600">{stat.helper}</p>
          </article>
        ))}
      </div>

      <div className="mt-6 grid gap-6 lg:grid-cols-[1.4fr_0.9fr]">
        <section className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
          <div className="flex items-center justify-between gap-4">
            <div>
              <h3 className="text-lg font-semibold tracking-tight text-zinc-950">Recent companies</h3>
              <p className="mt-1 text-sm text-zinc-600">Latest company records in the system</p>
            </div>
            <Link
              to="/admin/companies"
              className="rounded-xl border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-800 transition-colors hover:bg-zinc-100"
            >
              View all
            </Link>
          </div>

          <div className="mt-5 space-y-3">
            {recentCompanies.map(company => (
              <div key={company.id} className="flex items-center justify-between rounded-xl border border-zinc-200 px-4 py-3">
                <div>
                  <p className="font-medium text-zinc-950">{company.name}</p>
                  <p className="mt-1 text-sm text-zinc-500">Created {formatDate(company.createdAt)}</p>
                </div>
                <span className="rounded-full border border-zinc-200 px-3 py-1 text-xs font-medium text-zinc-700">
                  {company.companyStatus}
                </span>
              </div>
            ))}
            {!loading && recentCompanies.length === 0 ? (
              <p className="rounded-xl border border-zinc-200 px-4 py-3 text-sm text-zinc-600">
                No companies found.
              </p>
            ) : null}
          </div>
        </section>

        <section className="rounded-2xl border border-zinc-200 bg-white p-6 shadow-sm">
          <h3 className="text-lg font-semibold tracking-tight text-zinc-950">Quick actions</h3>
          <p className="mt-1 text-sm text-zinc-600">Common admin operations</p>

          <div className="mt-5 space-y-3">
            <Link
              to="/admin/companies"
              className="flex h-11 items-center justify-center rounded-xl bg-zinc-900 px-4 text-sm font-medium text-white transition-colors hover:bg-zinc-800"
            >
              Create Company
            </Link>
            <Link
              to="/admin/users"
              className="flex h-11 items-center justify-center rounded-xl border border-zinc-300 bg-white px-4 text-sm font-medium text-zinc-800 transition-colors hover:bg-zinc-100"
            >
              Create User
            </Link>
          </div>
        </section>
      </div>
    </AdminShell>
  );
}
