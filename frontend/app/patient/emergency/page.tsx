"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  BedDouble,
  Building2,
  MapPin,
  Phone,
  Search,
  Stethoscope
} from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { SafetyNotice } from "@/components/safety-notice";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { FieldShell, Input, Select } from "@/components/ui/form-controls";
import { EmptyState, ErrorState, SkeletonCards } from "@/components/ui/states";
import { hospitalsApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import type { WardType } from "@/types/api";

interface Filters {
  query: string;
  wardType: WardType;
}

const initialFilters: Filters = {
  query: "",
  wardType: "ICU"
};

export default function EmergencyAvailabilityPage() {
  const [draft, setDraft] = useState(initialFilters);
  const [filters, setFilters] = useState(initialFilters);
  const query = useQuery({
    queryKey: queryKeys.emergency(filters),
    queryFn: ({ signal }) =>
      hospitalsApi.emergencyAvailability(filters, signal)
  });

  return (
    <>
      <PageHeader
        eyebrow="Urgent care search"
        title="Hospital bed availability"
        description="Review hospitals reporting matching beds and available doctors."
      />
      <div className="mb-6">
        <SafetyNotice />
      </div>

      <Card className="mb-7">
        <CardContent>
          <form
            className="grid gap-4 md:grid-cols-[1.25fr_0.85fr_auto]"
            onSubmit={(event) => {
              event.preventDefault();
              setFilters(draft);
            }}
          >
            <FieldShell label="Hospital or area" htmlFor="emergency-query">
              <div className="relative">
                <MapPin
                  className="pointer-events-none absolute left-3.5 top-1/2 size-4 -translate-y-1/2 text-slate-400"
                  aria-hidden="true"
                />
                <Input
                  id="emergency-query"
                  className="pl-10"
                  placeholder="e.g. Bhangagarh or Brahmaputra"
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
            <FieldShell label="Bed type" htmlFor="ward-type">
              <Select
                id="ward-type"
                value={draft.wardType}
                onChange={(event) =>
                  setDraft((current) => ({
                    ...current,
                    wardType: event.target.value as WardType
                  }))
                }
              >
                <option value="ICU">ICU</option>
                <option value="GENERAL">General ward</option>
              </Select>
            </FieldShell>
            <div className="flex items-end">
              <Button type="submit" className="w-full md:w-auto">
                <Search className="size-4" aria-hidden="true" />
                Search
              </Button>
            </div>
          </form>
        </CardContent>
      </Card>

      {query.isLoading && <SkeletonCards count={4} />}
      {query.isError && (
        <ErrorState
          title="Availability could not be loaded"
          message={query.error.message}
          onRetry={() => query.refetch()}
        />
      )}
      {query.isSuccess && query.data.content.length === 0 && (
        <EmptyState
          title="No matching availability was reported"
          message="Try a nearby area, another ward type, or a broader hospital search. For urgent danger, contact local emergency services."
          icon={BedDouble}
        />
      )}
      {query.isSuccess && query.data.content.length > 0 && (
        <div className="grid gap-5 md:grid-cols-2">
          {query.data.content.map((hospital) => (
            <Card key={hospital.id}>
              <CardContent>
                <div className="flex items-start justify-between gap-4">
                  <span className="grid size-11 shrink-0 place-items-center rounded-2xl bg-blue-100 text-blue-800">
                    <Building2 className="size-5" aria-hidden="true" />
                  </span>
                  <Badge tone="info">{filters.wardType} reported</Badge>
                </div>
                <h2 className="mt-5 text-lg font-bold text-ink-950">
                  {hospital.name}
                </h2>
                <p className="mt-2 flex items-start gap-2 text-sm leading-6 text-slate-500">
                  <MapPin
                    className="mt-1 size-4 shrink-0 text-slate-400"
                    aria-hidden="true"
                  />
                  {hospital.address ?? "Location not listed"}
                </p>

                <div className="mt-5 grid grid-cols-2 gap-3">
                  <div className="rounded-2xl bg-brand-50 p-4">
                    <BedDouble
                      className="size-4 text-brand-700"
                      aria-hidden="true"
                    />
                    <p className="mt-3 text-2xl font-bold text-ink-950">
                      {filters.wardType === "ICU"
                        ? hospital.availableIcuBeds
                        : hospital.availableGeneralBeds}
                    </p>
                    <p className="mt-1 text-xs text-slate-500">
                      beds reported available
                    </p>
                  </div>
                  <div className="rounded-2xl bg-slate-50 p-4">
                    <Stethoscope
                      className="size-4 text-slate-600"
                      aria-hidden="true"
                    />
                    <p className="mt-3 text-2xl font-bold text-ink-950">
                      {hospital.availableDoctors}
                    </p>
                    <p className="mt-1 text-xs text-slate-500">
                      doctors reported available
                    </p>
                  </div>
                </div>

                <div className="mt-5 flex items-center justify-between border-t pt-4">
                  <p className="text-xs text-slate-400">
                    Confirm directly before travelling.
                  </p>
                  {hospital.contactNumber ? (
                    <a
                      href={`tel:${hospital.contactNumber}`}
                      className="inline-flex items-center gap-2 rounded-xl border px-3 py-2 text-sm font-bold text-ink-800 transition hover:border-brand-300 hover:bg-brand-50"
                    >
                      <Phone className="size-4" aria-hidden="true" />
                      Call
                    </a>
                  ) : (
                    <span className="text-xs text-slate-400">
                      Phone not listed
                    </span>
                  )}
                </div>
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </>
  );
}
