import { useEffect, useRef, useState } from "react";
import { ChevronDown, LogOut, User } from "lucide-react";
import { Link, NavLink } from "react-router";
import type { MarketingUser } from "../types";
import { cn } from "../../../lib/utils";
import { useLogout } from "~/hooks/useLogout";

type MarketingShellProps = {
  title: string;
  user: MarketingUser;
  children: React.ReactNode;
};

export function MarketingShell({
  title,
  user,
  children,
}: MarketingShellProps) {
  const { logout } = useLogout();
  const [profileOpen, setProfileOpen] = useState(false);
  const profileMenuRef = useRef<HTMLDivElement | null>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (!profileMenuRef.current) {
        return;
      }

      if (!profileMenuRef.current.contains(event.target as Node)) {
        setProfileOpen(false);
      }
    }

    function handleEscape(event: KeyboardEvent) {
      if (event.key === "Escape") {
        setProfileOpen(false);
      }
    }

    document.addEventListener("mousedown", handleClickOutside);
    document.addEventListener("keydown", handleEscape);

    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
      document.removeEventListener("keydown", handleEscape);
    };
  }, []);


  const navigation = [
    { label: "Dashboard", to: "/marketing/dashboard", end: true },
  ];

  return (
    <div className="min-h-screen bg-white text-zinc-900 md:flex">
      <aside className="border-b border-zinc-200 bg-white md:sticky md:top-0 md:h-screen md:w-72 md:border-b-0 md:border-r">
        <div className="px-6 py-5">
          <Link to="/marketing/dashboard" className="block">
            <p className="text-[0.72rem] uppercase tracking-[0.22em] text-zinc-500">Marketing</p>
            <h1 className="mt-1 text-lg font-semibold tracking-tight text-zinc-950">Vouchera</h1>
          </Link>
        </div>

        <nav className="flex gap-2 overflow-x-auto px-4 pb-4 md:flex-col md:gap-1 md:px-4 md:pb-6">
          {navigation.map(item => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                cn(
                  "whitespace-nowrap rounded-xl px-4 py-3 text-sm font-medium transition-colors md:whitespace-normal",
                  isActive
                    ? "bg-zinc-900 text-white shadow-sm"
                    : "text-zinc-600 hover:bg-zinc-100 hover:text-zinc-950"
                )
              }
            >
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="flex-1">
        <header className="border-b border-zinc-200 bg-white">
          <div className="mx-auto flex max-w-7xl items-center justify-between gap-4 px-6 py-4 sm:px-8">
            <div>
              <p className="text-sm text-zinc-500">Marketing dashboard</p>
              <h2 className="text-xl font-semibold tracking-tight text-zinc-950">{title}</h2>
            </div>

            <div className="relative" ref={profileMenuRef}>
              <button
                type="button"
                onClick={() => setProfileOpen(prev => !prev)}
                className="inline-flex h-10 items-center gap-2 rounded-xl border border-zinc-300 bg-white px-3 text-sm font-medium text-zinc-800 transition-colors hover:bg-zinc-100"
              >
                <span className="flex h-7 w-7 items-center justify-center rounded-full bg-zinc-100 text-zinc-700">
                  <User className="h-4 w-4" aria-hidden="true" />
                </span>
                <span className="hidden max-w-44 truncate sm:block">{user.email}</span>
                <ChevronDown className="h-4 w-4 text-zinc-500" aria-hidden="true" />
              </button>

              {profileOpen ? (
                <div className="absolute right-0 z-20 mt-2 w-64 rounded-2xl border border-zinc-200 bg-white p-2 shadow-lg">
                  <div className="rounded-xl px-3 py-2">
                    <p className="text-sm font-medium text-zinc-900">{user.email}</p>
                    <p className="text-xs text-zinc-500">{user.company?.name ?? "No company"}</p>
                  </div>
                  <button
                    type="button"
                    onClick={logout}
                    className="mt-1 inline-flex w-full items-center gap-2 rounded-xl px-3 py-2 text-left text-sm font-medium text-zinc-700 transition-colors hover:bg-zinc-100 hover:text-zinc-900"
                  >
                    <LogOut className="h-4 w-4" aria-hidden="true" />
                    Logout
                  </button>
                </div>
              ) : null}
            </div>
          </div>
        </header>

        <main className="mx-auto max-w-7xl px-6 py-6 sm:px-8">{children}</main>
      </div>
    </div>
  );
}
