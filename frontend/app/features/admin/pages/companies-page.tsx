import { Link } from "react-router";
import { useEffect, useState } from "react";
import { AdminShell } from "../components/admin-shell";
import { createCompany, getCompanies, updateCompanyStatus } from "../api/admin-api";
import { COMPANY_STATUS_OPTIONS } from "../types";
import type { ApiCompany } from "../types";
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

export function AdminCompaniesPage() {
  const PAGE_SIZE = 10;
  const [companies, setCompanies] = useState<ApiCompany[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalCompanies, setTotalCompanies] = useState(0);
  const [createOpen, setCreateOpen] = useState(false);
  const [companyName, setCompanyName] = useState("");
  const [companyStatusDrafts, setCompanyStatusDrafts] = useState<Record<string, ApiCompany["companyStatus"]>>({});
  const [updatingCompanyId, setUpdatingCompanyId] = useState<string | null>(null);
  const [message, setMessage] = useState<{ type: "success" | "error"; text: string } | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    void loadCompanies(page);
  }, [page]);

  async function loadCompanies(targetPage: number) {
    setLoading(true);
    try {
      const result = await getCompanies({ page: targetPage, size: PAGE_SIZE });
      setCompanies(result.content);
      setCompanyStatusDrafts(
        Object.fromEntries(result.content.map(company => [company.id, company.companyStatus]))
      );
      setTotalPages(result.totalPages);
      setTotalCompanies(result.totalElements);
    } catch (err) {
      setMessage({
        type: "error",
        text: err instanceof Error ? err.message : "Failed to load companies.",
      });
    } finally {
      setLoading(false);
    }
  }

  function closeModal() {
    setCreateOpen(false);
    setCompanyName("");
  }

  async function handleCreate() {
    const trimmedName = companyName.trim();

    if (!trimmedName) {
      setMessage({ type: "error", text: "Company name is required." });
      return;
    }

    try {
      await createCompany(trimmedName);
      await loadCompanies(page);
      toastSuccess("Company created successfully.");
      closeModal();
    } catch (err) {
      toastError(err instanceof Error ? err.message : "Failed to create company.");
      setMessage({
        type: "error",
        text: err instanceof Error ? err.message : "Failed to create company.",
      });
    }
  }

  async function handleCompanyStatusUpdate(company: ApiCompany) {
    const nextStatus = companyStatusDrafts[company.id] ?? company.companyStatus;

    if (nextStatus === company.companyStatus) {
      return;
    }

    try {
      setUpdatingCompanyId(company.id);
      await updateCompanyStatus(company.id, nextStatus);
      await loadCompanies(page);
      toastSuccess("Company status updated successfully.");
    } catch (err) {
      toastError(err instanceof Error ? err.message : "Failed to update company status.");
      setMessage({
        type: "error",
        text: err instanceof Error ? err.message : "Failed to update company status.",
      });
      setCompanyStatusDrafts(prev => ({
        ...prev,
        [company.id]: company.companyStatus,
      }));
    } finally {
      setUpdatingCompanyId(null);
    }
  }

  return (
    <AdminShell title="Companies">
      <div className="mb-4 flex items-center justify-between gap-4">
        <div>
          <h3 className="text-lg font-semibold tracking-tight text-zinc-950">Companies</h3>
          <p className="mt-1 text-sm text-zinc-600">Create and review company records.</p>
        </div>
        <button
          type="button"
          onClick={() => setCreateOpen(true)}
          className="inline-flex h-11 items-center justify-center rounded-xl bg-zinc-900 px-5 text-sm font-medium text-white transition-colors hover:bg-zinc-800"
        >
          Create Company
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
                <th className="px-5 py-4 font-medium">Company Name</th>
                <th className="px-5 py-4 font-medium">Status</th>
                <th className="px-5 py-4 font-medium">Created Date</th>
                <th className="px-5 py-4 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-200">
              {companies.map(company => (
                <tr key={company.id} className="transition-colors hover:bg-zinc-50/80">
                  <td className="px-5 py-4 font-medium text-zinc-950">{company.name}</td>
                  <td className="px-5 py-4">
                    <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
                      <select
                        value={companyStatusDrafts[company.id] ?? company.companyStatus}
                        onChange={event =>
                          setCompanyStatusDrafts(prev => ({
                            ...prev,
                            [company.id]: event.target.value as ApiCompany["companyStatus"],
                          }))
                        }
                        className="h-10 rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
                      >
                        {COMPANY_STATUS_OPTIONS.map(status => (
                          <option key={status} value={status}>
                            {status}
                          </option>
                        ))}
                      </select>
                      <button
                        type="button"
                        onClick={() => void handleCompanyStatusUpdate(company)}
                        disabled={
                          updatingCompanyId === company.id ||
                          (companyStatusDrafts[company.id] ?? company.companyStatus) === company.companyStatus
                        }
                        className="h-10 rounded-xl border border-zinc-300 bg-white px-3 text-sm font-medium text-zinc-800 transition-colors hover:bg-zinc-100 disabled:cursor-not-allowed disabled:opacity-50"
                      >
                        {updatingCompanyId === company.id ? "Updating..." : "Update"}
                      </button>
                    </div>
                  </td>
                  <td className="px-5 py-4 text-zinc-600">{formatDate(company.createdAt)}</td>
                  <td className="px-5 py-4">
                    <Link
                      to={`/admin/companies/${company.id}`}
                      className="rounded-xl border border-zinc-300 px-4 py-2 text-sm font-medium text-zinc-800 transition-colors hover:bg-zinc-100"
                    >
                      View
                    </Link>
                  </td>
                </tr>
              ))}
              {!loading && companies.length === 0 ? (
                <tr>
                  <td className="px-5 py-6 text-sm text-zinc-500" colSpan={4}>
                    No companies found.
                  </td>
                </tr>
              ) : null}
            </tbody>
          </table>
        </div>
        <div className="flex items-center justify-between border-t border-zinc-200 px-5 py-3 text-sm text-zinc-600">
          <p>
            Page {totalPages === 0 ? 0 : page + 1} of {totalPages} • {totalCompanies} companies
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

      {createOpen ? (
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
            <h3 className="text-lg font-semibold tracking-tight text-zinc-950">Create Company</h3>
            <p className="mt-1 text-sm text-zinc-600">Enter a company name to create a new record.</p>

            <div className="mt-5">
              <label className="mb-2 block text-sm font-medium text-zinc-800" htmlFor="companyName">
                Company Name
              </label>
              <input
                id="companyName"
                value={companyName}
                onChange={event => setCompanyName(event.target.value)}
                className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
                placeholder="Acme Co"
              />
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
                Create Company
              </button>
            </div>
          </div>
        </div>
      ) : null}
    </AdminShell>
  );
}
