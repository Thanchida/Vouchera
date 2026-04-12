import type { FormEvent } from "react";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { createCampaign, getCompanyCampaigns, getMarketingSession } from "../api/marketing-api";
import { MarketingShell } from "../components/marketing-shell";
import type { ApiCampaign, MarketingUser } from "../types";
import { error as toastError, success as toastSuccess } from "~/lib/toast";

type CampaignStatus = "PENDING" | "ACTIVE" | "PAUSED" | "ENDED" | "ALL";

type CreateCampaignForm = {
  name: string;
  description: string;
  startTime: string;
  endTime: string;
};

type CreateCampaignErrors = Partial<Record<keyof CreateCampaignForm, string>>;

function toDatetimeLocalValue(date: Date) {
  const pad = (value: number) => String(value).padStart(2, "0");

  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(
    date.getHours()
  )}:${pad(date.getMinutes())}`;
}

function buildDefaultCreateCampaignForm(): CreateCampaignForm {
  const startTime = new Date();
  startTime.setHours(startTime.getHours() + 1);

  const endTime = new Date(startTime);
  endTime.setHours(endTime.getHours() + 2);

  return {
    name: "",
    description: "",
    startTime: toDatetimeLocalValue(startTime),
    endTime: toDatetimeLocalValue(endTime),
  };
}

function formatDate(value: string) {
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

function statusTone(status: string) {
  switch (status) {
    case "PENDING":
      return "border-sky-200 bg-sky-50 text-sky-700";
    case "ACTIVE":
      return "border-emerald-200 bg-emerald-50 text-emerald-700";
    case "PAUSED":
      return "border-amber-200 bg-amber-50 text-amber-700";
    case "ENDED":
      return "border-zinc-200 bg-zinc-100 text-zinc-600";
    default:
      return "border-zinc-200 bg-zinc-100 text-zinc-600";
  }
}

export function MarketingDashboardPage() {
  const PAGE_SIZE = 10;
  const navigate = useNavigate();
  const [user, setUser] = useState<MarketingUser | null>(null);
  const [campaigns, setCampaigns] = useState<ApiCampaign[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalCampaigns, setTotalCampaigns] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [statusFilter, setStatusFilter] = useState<CampaignStatus>("ALL");
  const [createModalOpen, setCreateModalOpen] = useState(false);
  const [createForm, setCreateForm] = useState<CreateCampaignForm>(buildDefaultCreateCampaignForm());
  const [createErrors, setCreateErrors] = useState<CreateCampaignErrors>({});
  const [createError, setCreateError] = useState("");
  const [creatingCampaign, setCreatingCampaign] = useState(false);

  useEffect(() => {
    void (async () => {
      setLoading(true);
      setError("");

      try {
        const session = await getMarketingSession();

        if (!session.company?.id) {
          throw new Error("Logged-in marketing user has no company.");
        }

        setUser(session);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load campaigns.");
      } finally {
        setLoading(false);
      }
    })();
  }, []);

  useEffect(() => {
    if (!user?.company?.id) {
      return;
    }

    const companyId = user.company.id;

    void (async () => {
      setLoading(true);
      setError("");

      try {
        const campaignList = await getCompanyCampaigns(companyId, {
          page,
          size: PAGE_SIZE,
          status: statusFilter === "ALL" ? undefined : statusFilter,
        });
        setCampaigns(campaignList.content);
        setTotalPages(campaignList.totalPages);
        setTotalCampaigns(campaignList.totalElements);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load campaigns.");
      } finally {
        setLoading(false);
      }
    })();
  }, [user, page, statusFilter]);

  function openCreateCampaignModal() {
    setCreateForm(buildDefaultCreateCampaignForm());
    setCreateErrors({});
    setCreateError("");
    setCreateModalOpen(true);
  }

  function closeCreateCampaignModal() {
    setCreateModalOpen(false);
    setCreateErrors({});
    setCreateError("");
  }

  function validateCreateCampaignForm(values: CreateCampaignForm) {
    const nextErrors: CreateCampaignErrors = {};

    if (!values.name.trim()) {
      nextErrors.name = "Campaign name is required.";
    } else if (values.name.trim().length > 255) {
      nextErrors.name = "Campaign name must be at most 255 characters.";
    }

    if (!values.description.trim()) {
      nextErrors.description = "Campaign description is required.";
    } else if (values.description.trim().length > 2000) {
      nextErrors.description = "Campaign description must be at most 2000 characters.";
    }

    if (!values.startTime) {
      nextErrors.startTime = "Start date/time is required.";
    }

    if (!values.endTime) {
      nextErrors.endTime = "End date/time is required.";
    }

    if (values.startTime && new Date(values.startTime).getTime() < Date.now()) {
      nextErrors.startTime = "Start date/time cannot be in the past.";
    }

    if (
      values.startTime &&
      values.endTime &&
      new Date(values.endTime).getTime() <= new Date(values.startTime).getTime()
    ) {
      nextErrors.endTime = "End date/time must be after start date/time.";
    }

    return nextErrors;
  }

  async function handleCreateCampaign(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const companyId = user?.company?.id;
    if (!companyId) {
      setCreateError("Missing company context for the current user.");
      return;
    }

    const nextErrors = validateCreateCampaignForm(createForm);
    setCreateErrors(nextErrors);
    setCreateError("");

    if (Object.keys(nextErrors).length > 0) {
      return;
    }

    setCreatingCampaign(true);

    try {
      const created = await createCampaign({
        companyId,
        name: createForm.name.trim(),
        description: createForm.description.trim(),
        startTime: createForm.startTime,
        endTime: createForm.endTime,
      });

      toastSuccess("Campaign created successfully.");
      closeCreateCampaignModal();
      navigate(`/marketing/campaign/${created.id}`);
    } catch (err) {
      toastError(err instanceof Error ? err.message : "Failed to create campaign.");
    } finally {
      setCreatingCampaign(false);
    }
  }

  if (loading) {
    return (
      <div className="min-h-screen bg-white px-6 py-20">
        <div className="mx-auto max-w-4xl animate-pulse space-y-4">
          <div className="h-8 w-64 rounded-lg bg-zinc-200" />
          <div className="h-4 w-80 rounded-lg bg-zinc-200" />
          <div className="h-64 rounded-2xl bg-zinc-200" />
        </div>
      </div>
    );
  }

  if (error || !user) {
    return (
      <div className="min-h-screen bg-white px-6 py-20">
        <div className="mx-auto w-full max-w-xl rounded-2xl border border-red-200 bg-white p-6">
          <h2 className="text-xl font-semibold text-zinc-950">Could not load dashboard</h2>
          <p className="mt-2 text-sm text-zinc-700">{error || "Missing marketing session."}</p>
        </div>
      </div>
    );
  }

  return (
    <MarketingShell title="Marketing Dashboard" user={user}>
      <section className="rounded-3xl border border-zinc-200 bg-white p-5 sm:p-6">
        <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h2 className="text-xl font-semibold tracking-tight text-zinc-950">Campaigns</h2>
            <p className="mt-1 text-sm text-zinc-600">
              Click a campaign to view details and manage vouchers.
            </p>
          </div>

          <button
            type="button"
            onClick={openCreateCampaignModal}
            className="inline-flex h-11 items-center justify-center rounded-xl bg-zinc-900 px-4 text-sm font-medium text-white transition-colors hover:bg-zinc-800"
          >
            Create Campaign
          </button>
        </div>

        <div className="mt-6 flex flex-wrap gap-2">
          {(["ALL", "PENDING", "ACTIVE", "PAUSED", "ENDED"] as const).map(status => (
            <button
              key={status}
              onClick={() => {
                setStatusFilter(status);
                setPage(0);
              }}
              className={`rounded-lg px-4 py-2 text-sm font-medium transition-colors ${
                statusFilter === status
                  ? "bg-zinc-900 text-white"
                  : "border border-zinc-200 bg-white text-zinc-700 hover:bg-zinc-50"
              }`}
            >
              {status === "ALL" ? "All Campaigns" : status}
            </button>
          ))}
        </div>

        <div className="mt-6 overflow-x-auto rounded-2xl border border-zinc-200">
          <table className="min-w-full text-left text-sm">
            <thead className="bg-zinc-50 text-xs uppercase tracking-wide text-zinc-500">
              <tr>
                <th className="px-4 py-3 font-medium">Campaign name</th>
                <th className="px-4 py-3 font-medium">Status</th>
                <th className="px-4 py-3 font-medium">Start date</th>
                <th className="px-4 py-3 font-medium">End date</th>
              </tr>
            </thead>
            <tbody>
              {campaigns.map(campaign => (
                  <tr
                    key={campaign.id}
                    onClick={() => navigate(`/marketing/campaign/${campaign.id}`)}
                    className="cursor-pointer border-t border-zinc-100 transition-colors hover:bg-zinc-50"
                  >
                    <td className="px-4 py-4 font-medium text-zinc-950">{campaign.name}</td>
                    <td className="px-4 py-4 text-zinc-700">
                      <span className={`rounded-full border px-2.5 py-1 text-xs font-medium ${statusTone(campaign.status)}`}>
                        {campaign.status}
                      </span>
                    </td>
                    <td className="px-4 py-4 text-zinc-700">{formatDate(campaign.startTime)}</td>
                    <td className="px-4 py-4 text-zinc-700">{formatDate(campaign.endTime)}</td>
                  </tr>
                ))}
              {campaigns.length === 0 ? (
                <tr>
                  <td className="px-4 py-6 text-zinc-600" colSpan={4}>
                    No campaigns available for this status.
                  </td>
                </tr>
              ) : null}
            </tbody>
          </table>
        </div>
        <div className="mt-3 flex items-center justify-between text-sm text-zinc-600">
          <p>
            Page {totalPages === 0 ? 0 : page + 1} of {totalPages} • {totalCampaigns} campaigns
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

      {createModalOpen ? (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-zinc-950/40 px-4 py-6 backdrop-blur-sm">
          <button
            aria-label="Close modal"
            className="absolute inset-0 cursor-default"
            onClick={closeCreateCampaignModal}
            type="button"
          />
          <div
            className="relative z-10 w-full max-w-xl rounded-2xl border border-zinc-200 bg-white p-6 shadow-xl"
            onClick={event => event.stopPropagation()}
          >
            <h3 className="text-lg font-semibold tracking-tight text-zinc-950">Create Campaign</h3>
            <p className="mt-1 text-sm text-zinc-600">
              Campaigns are created for {user.company?.name ?? "your company"}.
            </p>

            {createError ? (
              <div className="mt-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                {createError}
              </div>
            ) : null}

            <form className="mt-5 space-y-4" onSubmit={handleCreateCampaign}>
              <div>
                <label htmlFor="campaign-name" className="mb-1 block text-sm font-medium text-zinc-800">
                  Campaign name
                </label>
                <input
                  id="campaign-name"
                  value={createForm.name}
                  onChange={event => setCreateForm(prev => ({ ...prev, name: event.target.value }))}
                  className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
                />
                {createErrors.name ? <p className="mt-1 text-xs text-red-600">{createErrors.name}</p> : null}
              </div>

              <div>
                <label htmlFor="campaign-description" className="mb-1 block text-sm font-medium text-zinc-800">
                  Description
                </label>
                <textarea
                  id="campaign-description"
                  rows={4}
                  value={createForm.description}
                  onChange={event =>
                    setCreateForm(prev => ({ ...prev, description: event.target.value }))
                  }
                  className="w-full rounded-xl border border-zinc-300 bg-white px-3 py-2 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
                />
                {createErrors.description ? (
                  <p className="mt-1 text-xs text-red-600">{createErrors.description}</p>
                ) : null}
              </div>

              <div className="grid gap-4 sm:grid-cols-2">
                <div>
                  <label htmlFor="campaign-start-time" className="mb-1 block text-sm font-medium text-zinc-800">
                    Start date/time
                  </label>
                  <input
                    id="campaign-start-time"
                    type="datetime-local"
                    value={createForm.startTime}
                    onChange={event =>
                      setCreateForm(prev => ({ ...prev, startTime: event.target.value }))
                    }
                    className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
                  />
                  {createErrors.startTime ? (
                    <p className="mt-1 text-xs text-red-600">{createErrors.startTime}</p>
                  ) : null}
                </div>

                <div>
                  <label htmlFor="campaign-end-time" className="mb-1 block text-sm font-medium text-zinc-800">
                    End date/time
                  </label>
                  <input
                    id="campaign-end-time"
                    type="datetime-local"
                    value={createForm.endTime}
                    onChange={event =>
                      setCreateForm(prev => ({ ...prev, endTime: event.target.value }))
                    }
                    className="h-11 w-full rounded-xl border border-zinc-300 bg-white px-3 text-sm text-zinc-900 outline-none transition focus:border-zinc-500 focus:ring-2 focus:ring-zinc-200"
                  />
                  {createErrors.endTime ? (
                    <p className="mt-1 text-xs text-red-600">{createErrors.endTime}</p>
                  ) : null}
                </div>
              </div>

              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={closeCreateCampaignModal}
                  className="h-11 rounded-xl border border-zinc-300 bg-white px-4 text-sm font-medium text-zinc-800 transition-colors hover:bg-zinc-100"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={creatingCampaign}
                  className="h-11 rounded-xl bg-zinc-900 px-4 text-sm font-medium text-white transition-colors hover:bg-zinc-800 disabled:cursor-not-allowed disabled:opacity-60"
                >
                  {creatingCampaign ? "Creating..." : "Create Campaign"}
                </button>
              </div>
            </form>
          </div>
        </div>
      ) : null}
    </MarketingShell>
  );
}
