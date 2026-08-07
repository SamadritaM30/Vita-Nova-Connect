"use client";

import { useState } from "react";
import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import {
  Ambulance,
  ArrowRight,
  BedDouble,
  CalendarDays,
  Clock3,
  MapPin,
  Search,
  Stethoscope
} from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { StatusBadge } from "@/components/ui/badge";
import { ErrorState, PageLoading } from "@/components/ui/states";
import { buttonClass } from "@/components/ui/button";
import { appointmentsApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import { formatDateTime } from "@/lib/utils";
import { useCurrentUser } from "@/features/auth/use-auth";

const quickActions = [
  {
    title: "Find a doctor",
    description: "Search by speciality or hospital",
    href: "/patient/doctors",
    icon: Search,
    color: "bg-brand-100 text-brand-800"
  },
  {
    title: "Check hospital beds",
    description: "Review reported ICU and general beds",
    href: "/patient/emergency",
    icon: BedDouble,
    color: "bg-blue-100 text-blue-800"
  },
  {
    title: "Find an ambulance",
    description: "View services and send a request",
    href: "/patient/ambulances",
    icon: Ambulance,
    color: "bg-coral-100 text-coral-600"
  }
];

export default function PatientDashboardPage() {
  const { data: user } = useCurrentUser();
  const [nowIso] = useState(() => new Date().toISOString());
  const appointments = useQuery({
    queryKey: queryKeys.appointments({
      status: "BOOKED",
      startsAfter: nowIso,
      page: 0,
      size: 1
    }),
    queryFn: ({ signal }) =>
      appointmentsApi.list(
        {
          status: "BOOKED",
          startsAfter: nowIso,
          page: 0,
          size: 1,
          sort: "startsAt,asc"
        },
        signal
      )
  });
  const nextAppointment = appointments.data?.content[0];
  const firstName = user?.displayName?.split(" ")[0] ?? "there";

  return (
    <>
      <PageHeader
        eyebrow="Patient dashboard"
        title={`Welcome back, ${firstName}`}
        description="What would you like to do today?"
      />

      <section aria-labelledby="quick-actions-title">
        <h2 id="quick-actions-title" className="sr-only">
          Quick actions
        </h2>
        <div className="grid gap-4 md:grid-cols-3">
          {quickActions.map(({ icon: Icon, title, description, href, color }) => (
            <Link
              href={href}
              key={title}
              className="group rounded-3xl border bg-white p-5 shadow-card transition duration-200 hover:-translate-y-0.5 hover:border-brand-200 hover:shadow-soft sm:p-6"
            >
              <div className="flex items-start justify-between gap-4">
                <span className={`grid size-11 place-items-center rounded-2xl ${color}`}>
                  <Icon className="size-5" aria-hidden="true" />
                </span>
                <ArrowRight
                  className="size-5 text-slate-300 transition group-hover:translate-x-1 group-hover:text-brand-600"
                  aria-hidden="true"
                />
              </div>
              <h3 className="mt-5 font-bold text-ink-950">{title}</h3>
              <p className="mt-1 text-sm leading-6 text-slate-500">{description}</p>
            </Link>
          ))}
        </div>
      </section>

      <div className="mt-7 grid gap-6 xl:grid-cols-[1.35fr_0.65fr]">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <div>
              <h2 className="text-lg font-bold tracking-tight text-ink-950">
                Next appointment
              </h2>
              <p className="mt-1 text-sm text-slate-500">
                Your closest upcoming booking
              </p>
            </div>
            <Link
              href="/patient/appointments"
              className="text-sm font-bold text-brand-700 hover:text-brand-800"
            >
              View all
            </Link>
          </CardHeader>
          <CardContent>
            {appointments.isLoading && (
              <PageLoading label="Checking your appointments…" />
            )}
            {appointments.isError && (
              <ErrorState
                title="Appointments are unavailable"
                message={appointments.error.message}
                onRetry={() => appointments.refetch()}
              />
            )}
            {appointments.isSuccess && !nextAppointment && (
              <div className="rounded-2xl border border-dashed bg-slate-50 p-7 text-center">
                <CalendarDays
                  className="mx-auto size-7 text-slate-400"
                  aria-hidden="true"
                />
                <h3 className="mt-4 font-bold text-ink-950">
                  No upcoming appointments
                </h3>
                <p className="mt-2 text-sm text-slate-500">
                  Find an available doctor when you are ready.
                </p>
                <Link
                  href="/patient/doctors"
                  className={buttonClass({ size: "sm", className: "mt-5" })}
                >
                  Find a doctor
                </Link>
              </div>
            )}
            {nextAppointment && (
              <article className="rounded-2xl border bg-gradient-to-br from-white to-brand-50/45 p-5">
                <div className="flex flex-col gap-5 sm:flex-row sm:items-start">
                  <span className="grid size-14 shrink-0 place-items-center rounded-2xl bg-brand-100 text-brand-800">
                    <Stethoscope className="size-6" aria-hidden="true" />
                  </span>
                  <div className="min-w-0 flex-1">
                    <div className="flex flex-wrap items-start justify-between gap-3">
                      <div>
                        <h3 className="text-lg font-bold text-ink-950">
                          {nextAppointment.doctor.name}
                        </h3>
                        <p className="mt-1 text-sm font-medium text-brand-700">
                          {nextAppointment.doctor.specialization}
                        </p>
                      </div>
                      <StatusBadge status={nextAppointment.status} />
                    </div>
                    <div className="mt-5 grid gap-3 text-sm text-slate-500 sm:grid-cols-2">
                      <p className="flex items-center gap-2">
                        <Clock3 className="size-4 text-slate-400" aria-hidden="true" />
                        {formatDateTime(nextAppointment.startsAt)}
                      </p>
                      <p className="flex items-center gap-2">
                        <MapPin className="size-4 text-slate-400" aria-hidden="true" />
                        {nextAppointment.hospital.name}
                      </p>
                    </div>
                  </div>
                </div>
              </article>
            )}
          </CardContent>
        </Card>

        <Card className="overflow-hidden bg-ink-950 text-white">
          <CardContent className="relative">
            <div
              className="absolute -right-16 -top-16 size-48 rounded-full bg-brand-500/20 blur-3xl"
              aria-hidden="true"
            />
            <div className="relative">
              <span className="grid size-11 place-items-center rounded-2xl bg-white/10 text-brand-200">
                <CalendarDays className="size-5" aria-hidden="true" />
              </span>
              <h2 className="mt-6 text-xl font-bold">
                Keep your care details close
              </h2>
              <p className="mt-3 text-sm leading-7 text-slate-300">
                An up-to-date contact number and address make appointment and
                ambulance requests easier to coordinate.
              </p>
              <Link
                href="/patient/profile"
                className="mt-6 inline-flex items-center gap-2 text-sm font-bold text-brand-200 transition hover:text-white"
              >
                Review your profile
                <ArrowRight className="size-4" aria-hidden="true" />
              </Link>
            </div>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
