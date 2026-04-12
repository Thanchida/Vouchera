import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router";
import { BadgePercent, Ticket, CheckCircle2 } from "lucide-react";
import { CustomerShell } from "../components/customer-shell";
import { LoadingSpinner } from "../components/loading-spinner";
import {
  getCampaignById,
  getCurrentUser,
  getVoucherTypesByCampaign,
  redeemVoucher,
  getUserRedemptions,
} from "../api/customer-api";
import type { CustomerCampaign, CustomerUser, CustomerVoucherType, CustomerRedemption } from "../types";
import { error as toastError, success as toastSuccess } from "~/lib/toast";

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

function voucherName(voucher: CustomerVoucherType) {
  return `Voucher ${voucher.discountPercent}% off`;
}

export function CustomerCampaignPage() {
  const navigate = useNavigate();
  const { campaignId } = useParams();

  const [user, setUser] = useState<CustomerUser | null>(null);
  const [campaign, setCampaign] = useState<CustomerCampaign | null>(null);
  const [voucherTypes, setVoucherTypes] = useState<CustomerVoucherType[]>([]);
  const [redemptions, setRedemptions] = useState<CustomerRedemption[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [claimingVoucherId, setClaimingVoucherId] = useState<string | null>(null);

  useEffect(() => {
    let active = true;

    if (!campaignId) {
      setError("Campaign id is missing.");
      setLoading(false);
      return undefined;
    }

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

        const [campaignDetail, vouchers] = await Promise.all([
          getCampaignById(campaignId),
          getVoucherTypesByCampaign(campaignId),
        ]);

        if (!active) {
          return;
        }

        const userRedemptions = await getUserRedemptions(currentUser.id);

        if (!active) {
          return;
        }

        setUser(currentUser);
        setCampaign(campaignDetail);
        setVoucherTypes(vouchers);
        setRedemptions(userRedemptions);
      } catch (err) {
        if (!active) {
          return;
        }

        setError(err instanceof Error ? err.message : "Failed to load campaign.");
      } finally {
        if (active) {
          setLoading(false);
        }
      }
    })();

    return () => {
      active = false;
    };
  }, [campaignId, navigate]);

  async function refreshVoucherTypes() {
    if (!campaignId) {
      return;
    }

    const [vouchers, userRedemptions] = await Promise.all([
      getVoucherTypesByCampaign(campaignId),
      getUserRedemptions(user!.id),
    ]);
    setVoucherTypes(vouchers);
    setRedemptions(userRedemptions);
  }

  async function handleReceiveVoucher(voucher: CustomerVoucherType) {
    if (!user) {
      return;
    }

    setClaimingVoucherId(voucher.id);
    try {
      await redeemVoucher({ voucherTypeId: voucher.id });

      toastSuccess("Voucher received successfully");
      await refreshVoucherTypes();
    } catch (err) {
      toastError(err instanceof Error ? err.message : "Failed to receive voucher.");
    } finally {
      setClaimingVoucherId(null);
    }
  }

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
          <h2 className="text-xl font-semibold text-zinc-950">Could not load campaign detail</h2>
          <p className="mt-2 text-sm text-zinc-700">{error || "Missing customer session."}</p>
        </div>
      </main>
    );
  }

  const campaignIsActive = campaign?.status === "ACTIVE";

  return (
    <CustomerShell title={campaign?.name ?? "Campaign"} user={user}>
      {error ? (
        <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      ) : null}

      <section className="rounded-3xl border border-zinc-200 bg-white p-5 shadow-sm sm:p-6">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div className="max-w-2xl">
            <p className="text-xs uppercase tracking-[0.22em] text-zinc-500">Campaign detail</p>
            <h2 className="mt-1 text-2xl font-semibold tracking-tight text-zinc-950">
              {campaign?.name ?? "Campaign details"}
            </h2>
            <p className="mt-2 text-sm leading-6 text-zinc-600">{campaign?.description ?? ""}</p>
          </div>

          {campaign ? (
            <span className={`rounded-full border px-3 py-1 text-xs font-medium ${statusTone(campaign.status)}`}>
              {campaign.status}
            </span>
          ) : null}
        </div>

        {campaign ? (
          <div className="mt-5 flex flex-wrap gap-3 text-sm text-zinc-500">
            <div className="rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-2">
              <p className="text-xs uppercase tracking-[0.18em] text-zinc-500">Company</p>
              <p className="mt-1 font-medium text-zinc-900">{campaign.company?.name ?? "Unknown company"}</p>
            </div>
            <div className="rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-2">
              <p className="text-xs uppercase tracking-[0.18em] text-zinc-500">Schedule</p>
              <p className="mt-1 font-medium text-zinc-900">
                {formatDate(campaign.startTime)} - {formatDate(campaign.endTime)}
              </p>
            </div>
          </div>
        ) : null}

        <div className="mt-8">
          <div className="flex items-end justify-between gap-4">
            <div>
              <p className="text-xs uppercase tracking-[0.22em] text-zinc-500">Voucher types</p>
              <h3 className="mt-1 text-lg font-semibold tracking-tight text-zinc-950">Available vouchers</h3>
            </div>
            <p className="text-sm text-zinc-500">Receive a voucher while quota remains.</p>
          </div>

          {voucherTypes.length === 0 ? (
            <div className="mt-6 rounded-2xl border border-dashed border-zinc-300 bg-zinc-50 px-5 py-10 text-center text-sm text-zinc-500">
              No voucher types available for this campaign.
            </div>
          ) : (
            <div className="mt-6 grid gap-3 lg:grid-cols-2">
              {voucherTypes.map(voucher => {
                const soldOut = voucher.remainingQuota <= 0;
                const isClaiming = claimingVoucherId === voucher.id;
                const alreadyClaimed = redemptions.some(r => r.voucherType.id === voucher.id);
                const campaignInactive = !campaignIsActive;

                return (
                  <article
                    key={voucher.id}
                    className="rounded-2xl border border-zinc-200 bg-white p-5 transition-shadow hover:shadow-sm"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <div className="flex items-center gap-2 text-zinc-900">
                          <BadgePercent className="h-4 w-4 text-zinc-500" aria-hidden="true" />
                          <h4 className="text-base font-semibold tracking-tight">{voucherName(voucher)}</h4>
                        </div>
                        <p className="mt-1 text-sm text-zinc-500">Discount and quota details from the backend.</p>
                      </div>
                      {alreadyClaimed ? (
                        <span className="rounded-full border border-sky-200 bg-sky-50 px-3 py-1 text-xs font-medium text-sky-700">
                          Already received
                        </span>
                      ) : campaignInactive ? (
                        <span className="rounded-full border border-zinc-200 bg-zinc-100 px-3 py-1 text-xs font-medium text-zinc-600">
                          Campaign not active
                        </span>
                      ) : soldOut ? (
                        <span className="rounded-full border border-zinc-200 bg-zinc-100 px-3 py-1 text-xs font-medium text-zinc-600">
                          Sold out
                        </span>
                      ) : (
                        <span className="rounded-full border border-emerald-200 bg-emerald-50 px-3 py-1 text-xs font-medium text-emerald-700">
                          Available
                        </span>
                      )}
                    </div>

                    <div className="mt-4 grid gap-3 sm:grid-cols-2">
                      <div className="rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-3">
                        <p className="text-xs uppercase tracking-[0.18em] text-zinc-500">Discount</p>
                        <p className="mt-1 text-lg font-semibold text-zinc-950">{voucher.discountPercent}%</p>
                      </div>
                      <div className="rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-3">
                        <p className="text-xs uppercase tracking-[0.18em] text-zinc-500">Remaining quota</p>
                        <p className="mt-1 text-lg font-semibold text-zinc-950">{voucher.remainingQuota}</p>
                      </div>
                    </div>

                    <button
                      type="button"
                      onClick={() => void handleReceiveVoucher(voucher)}
                      disabled={campaignInactive || soldOut || isClaiming || alreadyClaimed}
                      className="mt-5 inline-flex h-11 w-full items-center justify-center gap-2 rounded-xl bg-zinc-900 px-4 text-sm font-medium text-white transition-colors hover:bg-zinc-800 disabled:cursor-not-allowed disabled:bg-zinc-300"
                    >
                      {isClaiming ? (
                        <>
                          <Ticket className="h-4 w-4 animate-pulse" aria-hidden="true" />
                          Receiving...
                        </>
                      ) : alreadyClaimed ? (
                        <>
                          <CheckCircle2 className="h-4 w-4" aria-hidden="true" />
                          Already received
                        </>
                      ) : campaignInactive ? (
                        <>
                          <CheckCircle2 className="h-4 w-4" aria-hidden="true" />
                          Campaign not active
                        </>
                      ) : soldOut ? (
                        <>
                          <CheckCircle2 className="h-4 w-4" aria-hidden="true" />
                          Quota exhausted
                        </>
                      ) : (
                        <>
                          <Ticket className="h-4 w-4" aria-hidden="true" />
                          Receive Voucher
                        </>
                      )}
                    </button>
                  </article>
                );
              })}
            </div>
          )}
        </div>
      </section>
    </CustomerShell>
  );
}