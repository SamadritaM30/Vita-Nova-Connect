"use client";

import {
  Ambulance,
  BedDouble,
  CalendarDays,
  ChevronRight,
  LayoutDashboard,
  LogOut,
  Menu,
  Search,
  Stethoscope,
  UserRound,
  X,
  type LucideIcon
} from "lucide-react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Brand } from "@/components/brand";
import { DemoNotice } from "@/components/demo-notice";
import { Button } from "@/components/ui/button";
import { useToast } from "@/components/ui/toast";
import { authApi } from "@/lib/api";
import { cn, initials } from "@/lib/utils";
import { useCurrentUser } from "@/features/auth/use-auth";
import type { UserRole } from "@/types/api";

interface NavItem {
  label: string;
  href: string;
  icon: LucideIcon;
}

const patientNav: NavItem[] = [
  { label: "Dashboard", href: "/patient/dashboard", icon: LayoutDashboard },
  { label: "Find doctors", href: "/patient/doctors", icon: Search },
  { label: "Emergency beds", href: "/patient/emergency", icon: BedDouble },
  {
    label: "Appointments",
    href: "/patient/appointments",
    icon: CalendarDays
  },
  { label: "Ambulances", href: "/patient/ambulances", icon: Ambulance },
  { label: "Profile", href: "/patient/profile", icon: UserRound }
];

const representativeNav: NavItem[] = [
  {
    label: "Dashboard",
    href: "/representative/dashboard",
    icon: LayoutDashboard
  },
  {
    label: "Doctors",
    href: "/representative/doctors",
    icon: Stethoscope
  },
  { label: "Beds", href: "/representative/beds", icon: BedDouble },
  {
    label: "Appointments",
    href: "/representative/appointments",
    icon: CalendarDays
  },
  { label: "Profile", href: "/representative/profile", icon: UserRound }
];

function Navigation({
  items,
  pathname,
  onNavigate
}: {
  items: NavItem[];
  pathname: string;
  onNavigate?: () => void;
}) {
  return (
    <nav aria-label="Main navigation" className="space-y-1">
      {items.map((item) => {
        const active =
          pathname === item.href || pathname.startsWith(`${item.href}/`);
        const Icon = item.icon;
        return (
          <Link
            key={item.href}
            href={item.href}
            onClick={onNavigate}
            aria-current={active ? "page" : undefined}
            className={cn(
              "group flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-semibold transition-colors",
              active
                ? "bg-brand-50 text-brand-800"
                : "text-slate-600 hover:bg-slate-50 hover:text-ink-950"
            )}
          >
            <Icon
              aria-hidden="true"
              className={cn(
                "size-[18px]",
                active
                  ? "text-brand-700"
                  : "text-slate-400 group-hover:text-slate-600"
              )}
            />
            {item.label}
            {active && (
              <ChevronRight
                className="ml-auto size-4 text-brand-500"
                aria-hidden="true"
              />
            )}
          </Link>
        );
      })}
    </nav>
  );
}

export function PortalShell({
  role,
  children
}: {
  role: UserRole;
  children: React.ReactNode;
}) {
  const pathname = usePathname();
  const router = useRouter();
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const { data: user } = useCurrentUser();
  const [mobileOpen, setMobileOpen] = useState(false);
  const items = role === "PATIENT" ? patientNav : representativeNav;
  const currentItem = items.find(
    (item) => pathname === item.href || pathname.startsWith(`${item.href}/`)
  );

  const logout = useMutation({
    mutationFn: authApi.logout,
    onSuccess: () => {
      queryClient.clear();
      router.replace("/login");
      router.refresh();
    },
    onError: (error) => {
      showToast({
        tone: "error",
        title: "Could not sign out",
        description: error.message
      });
    }
  });

  const sidebarContent = (
    <>
      <div className="flex h-20 items-center justify-between px-5">
        <Brand />
        <Button
          type="button"
          size="icon"
          variant="ghost"
          className="lg:hidden"
          onClick={() => setMobileOpen(false)}
          aria-label="Close navigation"
        >
          <X className="size-5" aria-hidden="true" />
        </Button>
      </div>
      <div className="flex flex-1 flex-col overflow-y-auto px-4 pb-5">
        <p className="mb-3 px-3 text-[11px] font-bold uppercase tracking-[0.15em] text-slate-400">
          {role === "PATIENT" ? "Patient portal" : "Care operations"}
        </p>
        <Navigation
          items={items}
          pathname={pathname}
          onNavigate={() => setMobileOpen(false)}
        />
        <div className="mt-auto pt-6">
          <div className="rounded-2xl border bg-slate-50 p-3">
            <div className="flex min-w-0 items-center gap-3">
              <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-brand-100 text-sm font-bold text-brand-800">
                {initials(user?.displayName)}
              </span>
              <div className="min-w-0">
                <p className="truncate text-sm font-bold text-ink-950">
                  {user?.displayName}
                </p>
                <p className="truncate text-xs text-slate-500">{user?.email}</p>
              </div>
            </div>
            <button
              type="button"
              onClick={() => logout.mutate()}
              disabled={logout.isPending}
              className="mt-3 flex w-full items-center gap-2 rounded-xl px-2 py-2 text-left text-xs font-semibold text-slate-500 transition hover:bg-white hover:text-red-600 disabled:opacity-50"
            >
              <LogOut className="size-4" aria-hidden="true" />
              {logout.isPending ? "Signing out…" : "Sign out"}
            </button>
          </div>
        </div>
      </div>
    </>
  );

  return (
    <div className="min-h-screen bg-[#f7faf9]">
      <aside className="fixed inset-y-0 left-0 z-30 hidden w-64 flex-col border-r bg-white lg:flex">
        {sidebarContent}
      </aside>

      {mobileOpen && (
        <div className="fixed inset-0 z-50 lg:hidden">
          <button
            type="button"
            className="absolute inset-0 bg-ink-950/45 backdrop-blur-sm"
            onClick={() => setMobileOpen(false)}
            aria-label="Close navigation overlay"
          />
          <aside className="relative flex h-full w-[min(20rem,88vw)] flex-col bg-white shadow-2xl">
            {sidebarContent}
          </aside>
        </div>
      )}

      <div className="lg:pl-64">
        <header className="sticky top-0 z-20 border-b border-slate-200/80 bg-white/90 backdrop-blur-xl">
          <div className="flex h-16 items-center gap-3 px-4 sm:px-6 lg:px-8">
            <Button
              type="button"
              variant="ghost"
              size="icon"
              className="-ml-2 lg:hidden"
              onClick={() => setMobileOpen(true)}
              aria-label="Open navigation"
              aria-expanded={mobileOpen}
            >
              <Menu className="size-5" aria-hidden="true" />
            </Button>
            <div>
              <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-400">
                {role === "PATIENT" ? "Your care" : "Operations"}
              </p>
              <p className="text-sm font-bold text-ink-950">
                {currentItem?.label ?? "VitaNova Connect"}
              </p>
            </div>
            <Link
              href={
                role === "PATIENT"
                  ? "/patient/profile"
                  : "/representative/profile"
              }
              className="ml-auto grid size-9 place-items-center rounded-xl bg-brand-100 text-xs font-bold text-brand-800 transition hover:bg-brand-200"
              aria-label="Open profile"
            >
              {initials(user?.displayName)}
            </Link>
          </div>
          <DemoNotice className="border-b-0" />
        </header>
        <main id="main-content" className="px-4 py-7 sm:px-6 lg:px-8 lg:py-9">
          <div className="mx-auto max-w-7xl">{children}</div>
        </main>
      </div>
    </div>
  );
}
