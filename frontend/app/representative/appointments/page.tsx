"use client";

import { useEffect, useState } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  Ban,
  CalendarDays,
  Clock3,
  UserRound
} from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input, Select } from "@/components/ui/form-controls";
import { StatusBadge } from "@/components/ui/badge";
import { EmptyState, ErrorState, PageLoading } from "@/components/ui/states";
import { Pagination } from "@/components/ui/pagination";
import { Modal } from "@/components/ui/modal";
import { CancelAppointmentForm } from "@/features/appointments/cancel-appointment-form";
import { representativeApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import { formatDateTime } from "@/lib/utils";
import type { Appointment, AppointmentStatus } from "@/types/api";

export default function RepresentativeAppointmentsPage() {
  const [status, setStatus] = useState<AppointmentStatus | "">("");
  const [dateFrom, setDateFrom] = useState("");
  const [dateTo, setDateTo] = useState("");
  const [page, setPage] = useState(0);
  const [currentTime, setCurrentTime] = useState(() => Date.now());
  const [selected, setSelected] = useState<Appointment | null>(null);
  const filters = {
    status: status || undefined,
    dateFrom: dateFrom || undefined,
    dateTo: dateTo || undefined,
    page,
    size: 20,
    sort: "startsAt,desc"
  };
  const query = useQuery({
    queryKey: queryKeys.representativeAppointments(filters),
    queryFn: ({ signal }) =>
      representativeApi.appointments(filters, signal)
  });

  useEffect(() => {
    const timer = window.setInterval(() => setCurrentTime(Date.now()), 30_000);
    return () => window.clearInterval(timer);
  }, []);

  return (
    <>
      <PageHeader
        eyebrow="Care schedule"
        title="Appointments"
        description="Review bookings for your assigned hospitals and record operational cancellations with a reason."
      />
      <Card className="mb-6">
        <CardContent>
          <div className="grid gap-3 sm:grid-cols-3">
            <Select
              aria-label="Filter by appointment status"
              className="sm:w-48"
              value={status}
              onChange={(event) => {
                setStatus(event.target.value as AppointmentStatus | "");
                setPage(0);
              }}
            >
              <option value="">All statuses</option>
              <option value="BOOKED">Booked</option>
              <option value="COMPLETED">Completed</option>
              <option value="CANCELLED">Cancelled</option>
            </Select>
            <Input
              type="date"
              aria-label="Appointments from date"
              value={dateFrom}
              onChange={(event) => {
                setDateFrom(event.target.value);
                setPage(0);
              }}
            />
            <Input
              type="date"
              min={dateFrom || undefined}
              aria-label="Appointments to date"
              value={dateTo}
              onChange={(event) => {
                setDateTo(event.target.value);
                setPage(0);
              }}
            />
          </div>
        </CardContent>
      </Card>

      {query.isLoading && <PageLoading label="Loading appointments…" />}
      {query.isError && (
        <ErrorState
          title="Appointments could not be loaded"
          message={query.error.message}
          onRetry={() => query.refetch()}
        />
      )}
      {query.isSuccess && query.data.content.length === 0 && (
        <EmptyState
          title="No appointments match this view"
          message="Try another status or remove the search text."
          icon={CalendarDays}
        />
      )}
      {query.isSuccess && query.data.content.length > 0 && (
        <Card className="overflow-hidden">
          <div className="overflow-x-auto">
            <table className="data-table">
              <thead>
                <tr>
                  <th scope="col">Patient</th>
                  <th scope="col">Doctor</th>
                  <th scope="col">Schedule</th>
                  <th scope="col">Hospital</th>
                  <th scope="col">Status</th>
                  <th scope="col">
                    <span className="sr-only">Actions</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {query.data.content.map((appointment) => (
                  <tr key={appointment.id}>
                    <td>
                      <p className="flex items-center gap-2 font-bold text-ink-950">
                        <UserRound
                          className="size-4 text-slate-400"
                          aria-hidden="true"
                        />
                        {appointment.patient?.name ?? "Patient"}
                      </p>
                      {appointment.patient?.contactNumber && (
                        <p className="mt-1 text-xs text-slate-400">
                          {appointment.patient.contactNumber}
                        </p>
                      )}
                    </td>
                    <td>
                      <p className="font-semibold text-ink-800">
                        {appointment.doctor.name}
                      </p>
                      <p className="mt-1 text-xs text-slate-400">
                        {appointment.doctor.specialization}
                      </p>
                    </td>
                    <td className="text-slate-600">
                      <p className="flex items-center gap-2">
                        <Clock3 className="size-4" aria-hidden="true" />
                        {formatDateTime(appointment.startsAt)}
                      </p>
                    </td>
                    <td className="text-slate-600">
                      {appointment.hospital.name}
                    </td>
                    <td>
                      <StatusBadge status={appointment.status} />
                    </td>
                    <td className="text-right">
                      {appointment.status === "BOOKED" &&
                        Date.parse(appointment.startsAt) > currentTime && (
                        <Button
                          type="button"
                          variant="outline"
                          size="sm"
                          className="text-red-600 hover:border-red-200 hover:bg-red-50"
                          onClick={() => setSelected(appointment)}
                        >
                          <Ban className="size-4" aria-hidden="true" />
                          Cancel
                        </Button>
                        )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="px-5 pb-5">
            <Pagination
              page={query.data.page}
              totalPages={query.data.totalPages}
              onPageChange={setPage}
            />
          </div>
        </Card>
      )}

      <Modal
        open={Boolean(selected)}
        onClose={() => setSelected(null)}
        title="Cancel this appointment?"
        description="Provide an operational reason. The cancellation remains in the audit history."
      >
        {selected && (
          <CancelAppointmentForm
            representative
            appointment={selected}
            onSuccess={() => setSelected(null)}
          />
        )}
      </Modal>
    </>
  );
}
