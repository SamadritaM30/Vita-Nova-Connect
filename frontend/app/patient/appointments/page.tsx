"use client";

import { useState } from "react";
import Link from "next/link";
import { useQuery } from "@tanstack/react-query";
import {
  Ban,
  Building2,
  CalendarDays,
  Clock3,
  Plus,
  Stethoscope
} from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { Button, buttonClass } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { StatusBadge } from "@/components/ui/badge";
import { EmptyState, ErrorState, SkeletonCards } from "@/components/ui/states";
import { Modal } from "@/components/ui/modal";
import { Pagination } from "@/components/ui/pagination";
import { CancelAppointmentForm } from "@/features/appointments/cancel-appointment-form";
import { appointmentsApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import { cn, formatDateTime } from "@/lib/utils";
import type {
  Appointment,
  AppointmentSearchParams
} from "@/types/api";

type Tab = "UPCOMING" | "PAST" | "CANCELLED";

const tabs: Array<{ value: Tab; label: string }> = [
  { value: "UPCOMING", label: "Upcoming" },
  { value: "PAST", label: "Past" },
  { value: "CANCELLED", label: "Cancelled" }
];

export default function AppointmentsPage() {
  const [tab, setTab] = useState<Tab>("UPCOMING");
  const [nowIso] = useState(() => new Date().toISOString());
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState<Appointment | null>(null);
  const filters: AppointmentSearchParams = {
    status:
      tab === "UPCOMING"
        ? "BOOKED"
        : tab === "PAST"
          ? "COMPLETED"
          : "CANCELLED",
    startsAfter:
      tab === "UPCOMING" ? nowIso : undefined,
    page,
    size: 10,
    sort: tab === "UPCOMING" ? "startsAt,asc" : "startsAt,desc"
  };
  const query = useQuery({
    queryKey: queryKeys.appointments(filters),
    queryFn: ({ signal }) =>
      appointmentsApi.list(filters, signal)
  });

  return (
    <>
      <PageHeader
        eyebrow="Your care"
        title="Appointments"
        description="Review upcoming bookings and keep a clear record of past or cancelled appointments."
        action={
          <Link href="/patient/doctors" className={buttonClass()}>
            <Plus className="size-4" aria-hidden="true" />
            Book appointment
          </Link>
        }
      />

      <div
        className="mb-6 inline-flex rounded-2xl border bg-white p-1 shadow-sm"
        role="tablist"
        aria-label="Appointment status"
      >
        {tabs.map((item) => (
          <button
            key={item.value}
            type="button"
            role="tab"
            aria-selected={tab === item.value}
            onClick={() => {
              setTab(item.value);
              setPage(0);
            }}
            className={cn(
              "rounded-xl px-4 py-2 text-sm font-bold transition-colors",
              tab === item.value
                ? "bg-brand-100 text-brand-800"
                : "text-slate-500 hover:text-ink-950"
            )}
          >
            {item.label}
          </button>
        ))}
      </div>

      {query.isLoading && <SkeletonCards count={3} />}
      {query.isError && (
        <ErrorState
          title="Appointments could not be loaded"
          message={query.error.message}
          onRetry={() => query.refetch()}
        />
      )}
      {query.isSuccess && query.data.content.length === 0 && (
        <EmptyState
          title={`No ${tab.toLowerCase()} appointments`}
          message={
            tab === "UPCOMING"
              ? "When you book a doctor, the appointment will appear here."
              : "There are no appointments in this category."
          }
          icon={CalendarDays}
          action={
            tab === "UPCOMING" ? (
              <Link
                href="/patient/doctors"
                className={buttonClass({ size: "sm" })}
              >
                Find a doctor
              </Link>
            ) : undefined
          }
        />
      )}
      {query.isSuccess && query.data.content.length > 0 && (
        <>
          <div className="space-y-4">
          {query.data.content.map((appointment) => (
            <Card key={appointment.id}>
              <CardContent className="flex flex-col gap-5 lg:flex-row lg:items-center">
                <span className="grid size-12 shrink-0 place-items-center rounded-2xl bg-brand-100 text-brand-800">
                  <Stethoscope className="size-5" aria-hidden="true" />
                </span>
                <div className="min-w-0 flex-1">
                  <div className="flex flex-wrap items-center gap-3">
                    <h2 className="font-bold text-ink-950">
                      {appointment.doctor.name}
                    </h2>
                    <StatusBadge status={appointment.status} />
                  </div>
                  <p className="mt-1 text-sm font-semibold text-brand-700">
                    {appointment.doctor.specialization}
                  </p>
                  <div className="mt-3 flex flex-wrap gap-x-5 gap-y-2 text-sm text-slate-500">
                    <p className="flex items-center gap-2">
                      <Clock3 className="size-4" aria-hidden="true" />
                      {formatDateTime(appointment.startsAt)}
                    </p>
                    <p className="flex items-center gap-2">
                      <Building2 className="size-4" aria-hidden="true" />
                      {appointment.hospital.name}
                    </p>
                  </div>
                  {appointment.cancellationReason && (
                    <p className="mt-3 rounded-xl bg-slate-50 px-3 py-2 text-xs text-slate-500">
                      Cancellation reason: {appointment.cancellationReason}
                    </p>
                  )}
                </div>
                {appointment.status === "BOOKED" && (
                  <Button
                    type="button"
                    variant="outline"
                    size="sm"
                    className="self-start text-red-600 hover:border-red-200 hover:bg-red-50 lg:self-auto"
                    onClick={() => setSelected(appointment)}
                  >
                    <Ban className="size-4" aria-hidden="true" />
                    Cancel
                  </Button>
                )}
              </CardContent>
            </Card>
          ))}
          </div>
          <Pagination
            page={query.data.page}
            totalPages={query.data.totalPages}
            onPageChange={setPage}
          />
        </>
      )}

      <Modal
        open={Boolean(selected)}
        onClose={() => setSelected(null)}
        title="Cancel appointment?"
        description={
          selected
            ? `${selected.doctor.name} · ${formatDateTime(selected.startsAt)}`
            : undefined
        }
      >
        {selected && (
          <CancelAppointmentForm
            appointment={selected}
            onSuccess={() => setSelected(null)}
          />
        )}
      </Modal>
    </>
  );
}
