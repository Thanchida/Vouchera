import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { BadgePercent, Calendar, CheckCircle2 } from "lucide-react";
import { CustomerShell } from "../components/customer-shell";
import { LoadingSpinner } from "../components/loading-spinner";
import {
  getCurrentUser,
  getUserRedemptions,
} from "../api/customer-api";
import type { CustomerUser, CustomerRedemption } from "../types";

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
    case "CLAIMED":
      return "border-blue-200 bg-blue-50 text-blue-700";
    case "USED":
      return "border-emerald-200 bg-emerald-50 text-emerald-700";
    case "EXPIRED":
      return "border-red-200 bg-red-50 text-red-700";
    default:
      return "border-zinc-200 bg-zinc-100 text-zinc-600";
  }
}

export function CustomerMyVouchersPage() {
  const navigate = useNavigate();

  const [user, setUser] = useState<CustomerUser | null>(null);
  const [redemptions, setRedemptions] = useState<CustomerRedemption[]>([]);
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

        const userRedemptions = await getUserRedemptions(currentUser.id);

        if (!active) {
          return;
        }

        setUser(currentUser);
        setRedemptions(userRedemptions);
      } catch (err) {
        if (!active) {
          return;
        }

        setError(err instanceof Error ? err.message : "Failed to load vouchers.");
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
          <h2 className="text-xl font-semibold text-zinc-950">Could not load your vouchers</h2>
          <p className="mt-2 text-sm text-zinc-700">{error || "Missing customer session."}</p>
        </div>
      </main>
    );
  }

  const claimedVouchers = redemptions.filter(r => r.status === "CLAIMED");
  const usedVouchers = redemptions.filter(r => r.status === "USED");
  const expiredVouchers = redemptions.filter(r => r.status === "EXPIRED");

  return (
    <CustomerShell title="My Vouchers" user={user}>
      {error ? (
        <div className="mb-6 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      ) : null}

      {redemptions.length === 0 ? (
        <section className="rounded-3xl border border-zinc-200 bg-white p-5 shadow-sm sm:p-6">
          <div className="rounded-2xl border border-dashed border-zinc-300 bg-zinc-50 px-5 py-10 text-center">
            <div className="flex justify-center">
              <BadgePercent className="h-8 w-8 text-zinc-400" aria-hidden="true" />
            </div>
            <h3 className="mt-4 text-lg font-semibold text-zinc-950">No vouchers yet</h3>
            <p className="mt-1 text-sm text-zinc-600">
              Browse available campaigns to receive your first voucher.
            </p>
          </div>
        </section>
      ) : (
        <>
          {claimedVouchers.length > 0 && (
            <section className="rounded-3xl border border-zinc-200 bg-white p-5 shadow-sm sm:p-6">
              <div>
                <p className="text-xs uppercase tracking-[0.22em] text-zinc-500">Active vouchers</p>
                <h2 className="mt-1 text-xl font-semibold tracking-tight text-zinc-950">
                  Claimed ({claimedVouchers.length})
                </h2>
              </div>

              <div className="mt-6 grid gap-3 lg:grid-cols-2">
                {claimedVouchers.map(redemption => (
                  <article
                    key={redemption.id}
                    className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <div className="flex items-center gap-2 text-zinc-900">
                          <BadgePercent className="h-4 w-4 text-zinc-500" aria-hidden="true" />
                          <h4 className="text-base font-semibold tracking-tight">
                            Voucher {redemption.voucherType.discountPercent}% off
                          </h4>
                        </div>
                        <p className="mt-1 text-sm text-zinc-500">Ready to be used</p>
                      </div>
                      <span className={`rounded-full border px-3 py-1 text-xs font-medium ${statusTone(redemption.status)}`}>
                        {redemption.status}
                      </span>
                    </div>

                    <div className="mt-4 grid gap-3 sm:grid-cols-2">
                      <div className="rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-3">
                        <p className="text-xs uppercase tracking-[0.18em] text-zinc-500">Discount</p>
                        <p className="mt-1 text-lg font-semibold text-zinc-950">
                          {redemption.voucherType.discountPercent}%
                        </p>
                      </div>
                      <div className="rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-3">
                        <p className="text-xs uppercase tracking-[0.18em] text-zinc-500">Received</p>
                        <p className="mt-1 text-sm font-medium text-zinc-950">{formatDate(redemption.redeemedAt)}</p>
                      </div>
                    </div>
                  </article>
                ))}
              </div>
            </section>
          )}

          {usedVouchers.length > 0 && (
            <section className="mt-6 rounded-3xl border border-zinc-200 bg-white p-5 shadow-sm sm:p-6">
              <div>
                <p className="text-xs uppercase tracking-[0.22em] text-zinc-500">Used vouchers</p>
                <h2 className="mt-1 text-xl font-semibold tracking-tight text-zinc-950">
                  Redeemed ({usedVouchers.length})
                </h2>
              </div>

              <div className="mt-6 grid gap-3 lg:grid-cols-2">
                {usedVouchers.map(redemption => (
                  <article
                    key={redemption.id}
                    className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm transition-shadow hover:shadow-md"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <div className="flex items-center gap-2 text-zinc-900">
                          <CheckCircle2 className="h-4 w-4 text-emerald-600" aria-hidden="true" />
                          <h4 className="text-base font-semibold tracking-tight">
                            Voucher {redemption.voucherType.discountPercent}% off
                          </h4>
                        </div>
                        <p className="mt-1 text-sm text-zinc-500">Successfully used</p>
                      </div>
                      <span className={`rounded-full border px-3 py-1 text-xs font-medium ${statusTone(redemption.status)}`}>
                        {redemption.status}
                      </span>
                    </div>

                    <div className="mt-4 grid gap-3 sm:grid-cols-2">
                      <div className="rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-3">
                        <p className="text-xs uppercase tracking-[0.18em] text-zinc-500">Discount</p>
                        <p className="mt-1 text-lg font-semibold text-zinc-950">
                          {redemption.voucherType.discountPercent}%
                        </p>
                      </div>
                      <div className="rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-3">
                        <div className="flex items-center gap-2">
                          <Calendar className="h-4 w-4 text-zinc-400" aria-hidden="true" />
                          <div>
                            <p className="text-xs uppercase tracking-[0.18em] text-zinc-500">Used on</p>
                            <p className="mt-1 text-sm font-medium text-zinc-950">
                              {redemption.usedAt ? formatDate(redemption.usedAt) : "-"}
                            </p>
                          </div>
                        </div>
                      </div>
                    </div>
                  </article>
                ))}
              </div>
            </section>
          )}

          {expiredVouchers.length > 0 && (
            <section className="mt-6 rounded-3xl border border-zinc-200 bg-white p-5 shadow-sm sm:p-6">
              <div>
                <p className="text-xs uppercase tracking-[0.22em] text-zinc-500">Expired vouchers</p>
                <h2 className="mt-1 text-xl font-semibold tracking-tight text-zinc-950">
                  Expired ({expiredVouchers.length})
                </h2>
              </div>

              <div className="mt-6 grid gap-3 lg:grid-cols-2">
                {expiredVouchers.map(redemption => (
                  <article
                    key={redemption.id}
                    className="rounded-2xl border border-zinc-200 bg-white p-5 shadow-sm opacity-60"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div>
                        <div className="flex items-center gap-2 text-zinc-900">
                          <BadgePercent className="h-4 w-4 text-zinc-500" aria-hidden="true" />
                          <h4 className="text-base font-semibold tracking-tight">
                            Voucher {redemption.voucherType.discountPercent}% off
                          </h4>
                        </div>
                        <p className="mt-1 text-sm text-zinc-500">No longer valid</p>
                      </div>
                      <span className={`rounded-full border px-3 py-1 text-xs font-medium ${statusTone(redemption.status)}`}>
                        {redemption.status}
                      </span>
                    </div>

                    <div className="mt-4 grid gap-3 sm:grid-cols-2">
                      <div className="rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-3">
                        <p className="text-xs uppercase tracking-[0.18em] text-zinc-500">Discount</p>
                        <p className="mt-1 text-lg font-semibold text-zinc-950">
                          {redemption.voucherType.discountPercent}%
                        </p>
                      </div>
                      <div className="rounded-2xl border border-zinc-200 bg-zinc-50 px-4 py-3">
                        <p className="text-xs uppercase tracking-[0.18em] text-zinc-500">Received</p>
                        <p className="mt-1 text-sm font-medium text-zinc-950">{formatDate(redemption.redeemedAt)}</p>
                      </div>
                    </div>
                  </article>
                ))}
              </div>
            </section>
          )}
        </>
      )}
    </CustomerShell>
  );
}
