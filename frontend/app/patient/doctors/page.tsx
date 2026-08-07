"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Search, SlidersHorizontal, Stethoscope } from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  FieldShell,
  Input,
  Select
} from "@/components/ui/form-controls";
import { EmptyState, ErrorState, SkeletonCards } from "@/components/ui/states";
import { Pagination } from "@/components/ui/pagination";
import { DoctorCard } from "@/features/doctors/doctor-card";
import { doctorsApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import type {
  AvailabilityStatus,
  DoctorSearchParams,
  WardType
} from "@/types/api";

const specializations = [
  "Cardiologist",
  "Dermatologist",
  "ENT Specialist",
  "Endocrinologist",
  "General Physician",
  "Gynecologist",
  "Hematologist",
  "Nephrologist",
  "Neurologist",
  "Neurosurgeon",
  "Oncologist",
  "Orthopedic",
  "Pediatrician",
  "Pulmonologist"
];

interface Filters {
  query: string;
  specialization: string;
  wardType: WardType | "";
  availabilityStatus: AvailabilityStatus | "";
}

const initialFilters: Filters = {
  query: "",
  specialization: "",
  wardType: "",
  availabilityStatus: "AVAILABLE"
};

export default function FindDoctorsPage() {
  const [draft, setDraft] = useState<Filters>(initialFilters);
  const [filters, setFilters] = useState<Filters>(initialFilters);
  const [page, setPage] = useState(0);
  const params: DoctorSearchParams = { ...filters, page, size: 9 };
  const query = useQuery({
    queryKey: queryKeys.doctors(params),
    queryFn: ({ signal }) => doctorsApi.list(params, signal)
  });

  function applyFilters(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPage(0);
    setFilters(draft);
  }

  function clearFilters() {
    setDraft(initialFilters);
    setFilters(initialFilters);
    setPage(0);
  }

  return (
    <>
      <PageHeader
        eyebrow="Find care"
        title="Find a doctor"
        description="Search by doctor or specialization, and optionally require reported bed availability at the same hospital."
      />

      <Card className="mb-7">
        <CardContent>
          <form
            onSubmit={applyFilters}
            className="grid gap-4 md:grid-cols-2 xl:grid-cols-[1.35fr_1fr_1fr_0.85fr_auto]"
            aria-label="Doctor search filters"
          >
            <FieldShell label="Doctor or hospital" htmlFor="doctor-query">
              <div className="relative">
                <Search
                  className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-slate-400"
                  aria-hidden="true"
                />
                <Input
                  id="doctor-query"
                  className="pl-10"
                  placeholder="e.g. Dr Sharma or Brahmaputra"
                  value={draft.query}
                  onChange={(event) =>
                    setDraft((current) => ({
                      ...current,
                      query: event.target.value
                    }))
                  }
                />
              </div>
            </FieldShell>
            <FieldShell label="Specialization" htmlFor="specialization">
              <Select
                id="specialization"
                value={draft.specialization}
                onChange={(event) =>
                  setDraft((current) => ({
                    ...current,
                    specialization: event.target.value
                  }))
                }
              >
                <option value="">All specializations</option>
                {specializations.map((specialization) => (
                  <option value={specialization} key={specialization}>
                    {specialization}
                  </option>
                ))}
              </Select>
            </FieldShell>
            <FieldShell label="Bed availability" htmlFor="doctor-ward">
              <Select
                id="doctor-ward"
                value={draft.wardType}
                onChange={(event) =>
                  setDraft((current) => ({
                    ...current,
                    wardType: event.target.value as WardType | ""
                  }))
                }
              >
                <option value="">No bed requirement</option>
                <option value="ICU">ICU bed reported</option>
                <option value="GENERAL">General bed reported</option>
              </Select>
            </FieldShell>
            <FieldShell label="Availability" htmlFor="doctor-availability">
              <Select
                id="doctor-availability"
                value={draft.availabilityStatus}
                onChange={(event) =>
                  setDraft((current) => ({
                    ...current,
                    availabilityStatus: event.target
                      .value as AvailabilityStatus | ""
                  }))
                }
              >
                <option value="">Any status</option>
                <option value="AVAILABLE">Available</option>
                <option value="UNAVAILABLE">Unavailable</option>
              </Select>
            </FieldShell>
            <div className="flex items-end gap-2">
              <Button type="submit" className="flex-1 xl:flex-none">
                <SlidersHorizontal className="size-4" aria-hidden="true" />
                Apply
              </Button>
            </div>
          </form>
          {(filters.query ||
            filters.specialization ||
            filters.wardType ||
            filters.availabilityStatus !== "AVAILABLE") && (
            <button
              type="button"
              onClick={clearFilters}
              className="mt-4 text-xs font-bold text-brand-700 hover:text-brand-800"
            >
              Clear all filters
            </button>
          )}
        </CardContent>
      </Card>

      {query.isLoading && <SkeletonCards count={6} />}
      {query.isError && (
        <ErrorState
          title="Doctors could not be loaded"
          message={query.error.message}
          onRetry={() => query.refetch()}
        />
      )}
      {query.isSuccess && query.data.content.length === 0 && (
        <EmptyState
          title="No doctors match those filters"
          message="Try another specialization, remove the bed requirement, or include unavailable doctors."
          icon={Stethoscope}
          action={
            <Button type="button" variant="outline" onClick={clearFilters}>
              Reset filters
            </Button>
          }
        />
      )}
      {query.isSuccess && query.data.content.length > 0 && (
        <>
          <div className="mb-4 flex items-center justify-between">
            <p className="text-sm text-slate-500">
              <span className="font-bold text-ink-800">
                {query.data.totalElements}
              </span>{" "}
              doctors found
            </p>
          </div>
          <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
            {query.data.content.map((doctor) => (
              <DoctorCard key={doctor.id} doctor={doctor} />
            ))}
          </div>
          <Pagination
            page={query.data.page}
            totalPages={query.data.totalPages}
            onPageChange={setPage}
          />
        </>
      )}
    </>
  );
}
