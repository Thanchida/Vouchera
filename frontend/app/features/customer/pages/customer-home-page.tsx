import { Link, useNavigate } from "react-router";
import { useEffect, useState } from "react";
import { ChevronRight } from "lucide-react";
import { CustomerShell } from "../components/customer-shell";
import { LoadingSpinner } from "../components/loading-spinner";
import { getAllCampaigns, getCompanies, getCurrentUser } from "../api/customer-api";
import type { CustomerCampaign, CustomerCompany, CustomerUser } from "../types";

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

function formatRange(startTime: string, endTime: string) {
  return `${formatDate(startTime)} - ${formatDate(endTime)}`;
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

export function CustomerHomePage() {
  const navigate = useNavigate();
  const [user, setUser] = useState<CustomerUser | null>(null);
  const [companies, setCompanies] = useState<CustomerCompany[]>([]);
  const [campaigns, setCampaigns] = useState<CustomerCampaign[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;

    void (async () => {
      setLoading(true);
      setError("");

      try {
        const currentUser = await getCurrentUser();
        const role = currentUser.role.toLowerCase();

        if (role !== "customer") {
          if (role === "admin") {
            navigate("/admin", { replace: true });
          } else if (role === "marketing") {
            navigate("/marketing/dashboard", { replace: true });
          } else {
            navigate("/login", { replace: true });
          }
          return;
        }

        const [companyList, campaignList] = await Promise.all([
          getCompanies({ page: 0, size: 200 }),
          getAllCampaigns({ page: 0, size: 200 }),
        ]);

        if (!active) {
          return;
        }

        // Filter out ended campaigns
        const activeCampaigns = campaignList.content.filter(c => c.status !== "ENDED");

        setUser(currentUser);
        setCompanies(companyList.content);
        setCampaigns(activeCampaigns);
      } catch (err) {
        if (!active) {
          return;
        }

        setError(err instanceof Error ? err.message : "Failed to load customer home.");
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    })();

    return () => {
      active = false;
    };
  }, [navigate]);

  if (loading) {
    return (
      <main className="min-h-screen bg-white">
        <LoadingSpinner />
      </main>
    );
  }

  if (error || !user) {
    return (
      <main className="min-h-screen bg-white px-6 py-20">
        <div className="mx-auto w-full max-w-xl rounded-2xl border border-red-200 bg-white p-6">
          <h2 className="text-xl font-semibold text-zinc-950">Could not load customer home</h2>
          <p className="mt-2 text-sm text-zinc-700">{error || "Missing customer session."}</p>
        </div>
      </main>
    );
  }

  return (
    <CustomerShell title="Customer Home" user={user}>
      {error ? (
        <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      ) : null}

      <section className="rounded-3xl border border-zinc-200 bg-white p-5 shadow-sm sm:p-6">
        <div className="flex items-end justify-between gap-4">
          <div>
            <p className="text-xs uppercase tracking-[0.22em] text-zinc-500">Companies</p>
            <h2 className="mt-1 text-xl font-semibold tracking-tight text-zinc-950">Browse companies</h2>
          </div>
          <p className="text-sm text-zinc-500">Choose a company to inspect its campaigns.</p>
        </div>

        {companies.length === 0 ? (
          <div className="mt-6 rounded-2xl border border-dashed border-zinc-300 bg-zinc-50 px-5 py-10 text-center text-sm text-zinc-500">
            No companies available.
          </div>
        ) : (
          <div className="mt-6 grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
            {companies.map(company => (
              <Link
                key={company.id}
                to={`/customer/company/${company.id}`}
                className="group rounded-2xl border border-zinc-200 bg-white p-4 text-left transition-all duration-200 hover:-translate-y-0.5 hover:border-zinc-300 hover:shadow-sm"
              >
                <div className="flex items-center justify-between gap-4">
                  <div>
                    <h3 className="text-base font-semibold tracking-tight text-zinc-950">{company.name}</h3>
                    <p className="mt-1 text-sm text-zinc-500">Click to view campaigns</p>
                  </div>
                  <ChevronRight className="h-5 w-5 shrink-0 text-zinc-300 transition-colors group-hover:text-zinc-700" />
                </div>
              </Link>
            ))}
          </div>
        )}
      </section>

      <section className="mt-6 rounded-3xl border border-zinc-200 bg-white p-5 shadow-sm sm:p-6">
        <div className="flex items-end justify-between gap-4">
          <div>
            <p className="text-xs uppercase tracking-[0.22em] text-zinc-500">Campaigns</p>
            <h2 className="mt-1 text-xl font-semibold tracking-tight text-zinc-950">All available campaigns</h2>
          </div>
          <p className="text-sm text-zinc-500">Showing campaigns from every company.</p>
        </div>

        {campaigns.length === 0 ? (
          <div className="mt-6 rounded-2xl border border-dashed border-zinc-300 bg-zinc-50 px-5 py-10 text-center text-sm text-zinc-500">
            No campaigns available.
          </div>
        ) : (
          <div className="mt-6 grid gap-3 lg:grid-cols-2">
            {campaigns.map(campaign => (
              <Link
                key={campaign.id}
                to={`/customer/campaign/${campaign.id}`}
                className="group rounded-2xl border border-zinc-200 bg-white p-4 transition-all duration-200 hover:-translate-y-0.5 hover:border-zinc-300 hover:shadow-sm"
              >
                <div className="flex items-start justify-between gap-4">
                  <div>
                    <h3 className="text-base font-semibold tracking-tight text-zinc-950">{campaign.name}</h3>
                    <p className="mt-1 text-sm text-zinc-600">{campaign.company?.name ?? "Unknown company"}</p>
                  </div>
                  <span className={`rounded-full border px-3 py-1 text-xs font-medium ${statusTone(campaign.status)}`}>
                    {campaign.status}
                  </span>
                </div>

                <p className="mt-4 text-sm leading-6 text-zinc-600 line-clamp-2">{campaign.description}</p>

                <div className="mt-4 flex items-center justify-between gap-3 text-sm text-zinc-500">
                  <span>{formatRange(campaign.startTime, campaign.endTime)}</span>
                  <ChevronRight className="h-5 w-5 text-zinc-300 transition-colors group-hover:text-zinc-700" />
                </div>
              </Link>
            ))}
          </div>
        )}
      </section>
    </CustomerShell>
  );
}