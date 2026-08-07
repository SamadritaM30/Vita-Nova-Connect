"use client";

import { useState } from "react";
import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import {
  ArrowRight,
  BedDouble,
  CalendarDays,
  ClipboardList,
  Stethoscope,
  UserRoundCheck
} from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { StatCard } from "@/components/stat-card";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { StatusBadge } from "@/components/ui/badge";
import { ErrorState, PageLoading } from "@/components/ui/states";
import { representativeApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import { formatDateTime } from "@/lib/utils";
import { useCurrentUser } from "@/features/auth/use-auth";

export default function RepresentativeDashboardPage() {
  const { data: user } = useCurrentUser();
  const [nowIso] = useState(() => new Date().toISOString());
  const metrics = useQuery({
    queryKey: queryKeys.representativeDashboard,
    queryFn: ({ signal }) => representativeApi.dashboard(signal)
  });
  const appointments = useQuery({
    queryKey: queryKeys.representativeAppointments({
      page: 0,
      size: 5,
      sort: "startsAt,asc",
      status: "BOOKED",
      startsAfter: nowIso
    }),
    queryFn: ({ signal }) =>
      representativeApi.appointments(
        {
          page: 0,
          size: 5,
          sort: "startsAt,asc",
          status: "BOOKED",
          startsAfter: nowIso
        },
        signal
      )
  });

  const firstName = user?.displayName?.split(" ")[0] ?? "Representative";

  return (
    <>
      <PageHeader
        eyebrow="Operations overview"
        title={`Welcome back, ${firstName}`}
        description="A demonstration view of availability recorded for your assigned hospitals."
      />

      {metrics.isLoading && <PageLoading label="Loading operational metrics…" />}
      {metrics.isError && (
        <ErrorState
          title="Metrics could not be loaded"
          message={metrics.error.message}
          onRetry={() => metrics.refetch()}
        />
      )}
      {metrics.data && (
        <section
          className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4"
          aria-label="Operational summary"
        >
          <StatCard
            label="Available doctors"
            value={metrics.data.availableDoctors}
            helper={`of ${metrics.data.totalDoctors} assigned`}
            icon={UserRoundCheck}
            tone="brand"
          />
          <StatCard
            label="Available ICU beds"
            value={metrics.data.availableIcuBeds}
            helper="Reported across assigned hospitals"
            icon={BedDouble}
            tone="blue"
          />
          <StatCard
            label="Available general beds"
            value={metrics.data.availableGeneralBeds}
            helper="Reported across assigned hospitals"
            icon={BedDouble}
            tone="amber"
          />
          <StatCard
            label="Today's appointments"
            value={metrics.data.todayAppointments}
            helper="Scheduled for today"
            icon={CalendarDays}
            tone="coral"
          />
        </section>
      )}

      <div className="mt-7 grid gap-6 xl:grid-cols-[1.35fr_0.65fr]">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <div>
              <h2 className="text-lg font-bold">Upcoming appointments</h2>
              <p className="mt-1 text-sm text-slate-500">
                Next bookings across your assigned hospitals
              </p>
            </div>
            <Link
              href="/representative/appointments"
              className="text-sm font-bold text-brand-700 hover:text-brand-800"
            >
              View all
            </Link>
          </CardHeader>
          <CardContent>
            {appointments.isLoading && (
              <PageLoading label="Loading appointments…" />
            )}
            {appointments.isError && (
              <ErrorState
                title="Appointments could not be loaded"
                message={appointments.error.message}
                onRetry={() => appointments.refetch()}
              />
            )}
            {appointments.data?.content.length === 0 && (
              <div className="rounded-2xl border border-dashed p-7 text-center">
                <CalendarDays
                  className="mx-auto size-6 text-slate-400"
                  aria-hidden="true"
                />
                <p className="mt-3 text-sm font-bold">No upcoming bookings</p>
              </div>
            )}
            {appointments.data && appointments.data.content.length > 0 && (
              <div className="divide-y">
                {appointments.data.content.map((appointment) => (
                  <div
                    key={appointment.id}
                    className="flex flex-col gap-3 py-4 first:pt-0 last:pb-0 sm:flex-row sm:items-center"
                  >
                    <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-brand-50 text-brand-700">
                      <Stethoscope className="size-4" aria-hidden="true" />
                    </span>
                    <div className="min-w-0 flex-1">
                      <p className="truncate text-sm font-bold text-ink-950">
                        {appointment.patient?.name ?? "Patient"} ·{" "}
                        {appointment.doctor.name}
                      </p>
                      <p className="mt-1 text-xs text-slate-500">
                        {formatDateTime(appointment.startsAt)} ·{" "}
                        {appointment.hospital.name}
                      </p>
                    </div>
                    <StatusBadge status={appointment.status} />
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <Card className="bg-ink-950 text-white">
          <CardHeader>
            <h2 className="text-lg font-bold">Common tasks</h2>
            <p className="mt-1 text-sm text-slate-300">
              Keep public availability dependable.
            </p>
          </CardHeader>
          <CardContent className="space-y-3">
            {[
              {
                href: "/representative/doctors",
                icon: Stethoscope,
                title: "Update doctor status",
                text: "Manage clinical availability"
              },
              {
                href: "/representative/beds",
                icon: BedDouble,
                title: "Update bed status",
                text: "Maintain ward information"
              },
              {
                href: "/representative/appointments",
                icon: ClipboardList,
                title: "Review appointments",
                text: "See upcoming bookings"
              }
            ].map(({ href, icon: Icon, title, text }) => (
              <Link
                key={href}
                href={href}
                className="group flex items-center gap-3 rounded-2xl border border-white/10 bg-white/5 p-3.5 transition hover:bg-white/10"
              >
                <span className="grid size-9 place-items-center rounded-xl bg-brand-500/15 text-brand-200">
                  <Icon className="size-4" aria-hidden="true" />
                </span>
                <div>
                  <p className="text-sm font-bold">{title}</p>
                  <p className="mt-0.5 text-xs text-slate-400">{text}</p>
                </div>
                <ArrowRight
                  className="ml-auto size-4 text-slate-500 transition group-hover:translate-x-0.5 group-hover:text-brand-200"
                  aria-hidden="true"
                />
              </Link>
            ))}
          </CardContent>
        </Card>
      </div>
    </>
  );
}
