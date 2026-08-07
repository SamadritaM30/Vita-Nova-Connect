"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Edit3, Search, Stethoscope } from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input, Select } from "@/components/ui/form-controls";
import { StatusBadge } from "@/components/ui/badge";
import { EmptyState, ErrorState, PageLoading } from "@/components/ui/states";
import { Pagination } from "@/components/ui/pagination";
import { Modal } from "@/components/ui/modal";
import { UpdateDoctorForm } from "@/features/representative/update-doctor-form";
import { representativeApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import type { AvailabilityStatus, Doctor } from "@/types/api";

export default function RepresentativeDoctorsPage() {
  const [queryText, setQueryText] = useState("");
  const [appliedQuery, setAppliedQuery] = useState("");
  const [status, setStatus] = useState<AvailabilityStatus | "">("");
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState<Doctor | null>(null);
  const filters = {
    query: appliedQuery,
    availabilityStatus: status,
    page,
    size: 15
  };
  const query = useQuery({
    queryKey: queryKeys.representativeDoctors(filters),
    queryFn: ({ signal }) => representativeApi.doctors(filters, signal)
  });

  return (
    <>
      <PageHeader
        eyebrow="Clinical availability"
        title="Doctors"
        description="Review and update doctor availability for your assigned hospitals."
      />
      <Card className="mb-6">
        <CardContent>
          <form
            className="flex flex-col gap-3 sm:flex-row"
            onSubmit={(event) => {
              event.preventDefault();
              setAppliedQuery(queryText);
              setPage(0);
            }}
          >
            <div className="relative flex-1">
              <Search
                className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-slate-400"
                aria-hidden="true"
              />
              <Input
                aria-label="Search doctors"
                className="pl-10"
                placeholder="Search name, specialization, or hospital"
                value={queryText}
                onChange={(event) => setQueryText(event.target.value)}
              />
            </div>
            <Select
              aria-label="Filter by availability"
              className="sm:w-48"
              value={status}
              onChange={(event) => {
                setStatus(event.target.value as AvailabilityStatus | "");
                setPage(0);
              }}
            >
              <option value="">All statuses</option>
              <option value="AVAILABLE">Available</option>
              <option value="UNAVAILABLE">Unavailable</option>
            </Select>
            <Button type="submit">Search</Button>
          </form>
        </CardContent>
      </Card>

      {query.isLoading && <PageLoading label="Loading assigned doctors…" />}
      {query.isError && (
        <ErrorState
          title="Doctors could not be loaded"
          message={query.error.message}
          onRetry={() => query.refetch()}
        />
      )}
      {query.isSuccess && query.data.content.length === 0 && (
        <EmptyState
          title="No doctors match this view"
          message="Try another search or clear the availability filter."
          icon={Stethoscope}
        />
      )}
      {query.isSuccess && query.data.content.length > 0 && (
        <Card className="overflow-hidden">
          <div className="overflow-x-auto">
            <table className="data-table">
              <thead>
                <tr>
                  <th scope="col">Doctor</th>
                  <th scope="col">Specialization</th>
                  <th scope="col">Hospital</th>
                  <th scope="col">Status</th>
                  <th scope="col">
                    <span className="sr-only">Actions</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {query.data.content.map((doctor) => (
                  <tr key={doctor.id}>
                    <td>
                      <p className="font-bold text-ink-950">
                        {doctor.name}
                      </p>
                      <p className="mt-1 text-xs text-slate-400">
                        {doctor.consultationFrom && doctor.consultationTo
                          ? `${doctor.consultationFrom.slice(0, 5)}–${doctor.consultationTo.slice(0, 5)}`
                          : "Hours not listed"}
                      </p>
                    </td>
                    <td className="text-slate-600">{doctor.specialization}</td>
                    <td className="text-slate-600">{doctor.hospital.name}</td>
                    <td>
                      <StatusBadge status={doctor.availabilityStatus} />
                    </td>
                    <td className="text-right">
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => setSelected(doctor)}
                      >
                        <Edit3 className="size-4" aria-hidden="true" />
                        Update
                      </Button>
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
        title="Update doctor availability"
        description="Only assigned hospital representatives can make this change."
      >
        {selected && (
          <UpdateDoctorForm
            doctor={selected}
            onSuccess={() => setSelected(null)}
          />
        )}
      </Modal>
    </>
  );
}
