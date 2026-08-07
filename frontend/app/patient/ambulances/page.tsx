"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import {
  Ambulance as AmbulanceIcon,
  Building2,
  Clock3,
  MapPin,
  Phone,
  Send
} from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { SafetyNotice } from "@/components/safety-notice";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Badge, StatusBadge } from "@/components/ui/badge";
import { EmptyState, ErrorState, SkeletonCards } from "@/components/ui/states";
import { Modal } from "@/components/ui/modal";
import { RequestAmbulanceForm } from "@/features/ambulances/request-ambulance-form";
import { ambulancesApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import { formatDateTime } from "@/lib/utils";
import type { Ambulance } from "@/types/api";

export default function AmbulancesPage() {
  const [selected, setSelected] = useState<Ambulance | null>(null);
  const query = useQuery({
    queryKey: queryKeys.ambulances({ page: 0, size: 30 }),
    queryFn: ({ signal }) =>
      ambulancesApi.list({ page: 0, size: 30 }, signal)
  });
  const requests = useQuery({
    queryKey: queryKeys.ambulanceRequests({ page: 0, size: 5 }),
    queryFn: ({ signal }) =>
      ambulancesApi.requests({ page: 0, size: 5 }, signal)
  });

  return (
    <>
      <PageHeader
        eyebrow="Transport support"
        title="Ambulance services"
        description="Explore sample hospital transport records and demonstrate a pickup request workflow."
      />
      <div className="mb-6">
        <SafetyNotice />
      </div>

      {(requests.isLoading ||
        requests.isError ||
        (requests.data && requests.data.content.length > 0)) && (
        <section className="mb-7" aria-labelledby="recent-requests-title">
          <div className="mb-3 flex items-center justify-between">
            <div>
              <h2 id="recent-requests-title" className="font-bold text-ink-950">
                Your recent requests
              </h2>
              <p className="mt-1 text-xs text-slate-500">
                Hospital-level transport requests and their latest status
              </p>
            </div>
            {requests.isFetching && !requests.isLoading && (
              <span className="text-xs text-slate-400">Refreshing…</span>
            )}
          </div>
          {requests.isLoading && (
            <div className="h-24 animate-pulse-soft rounded-2xl border bg-white" />
          )}
          {requests.isError && (
            <div
              className="rounded-2xl border border-amber-200 bg-amber-50 p-4 text-sm text-amber-800"
              role="status"
            >
              Recent request history is temporarily unavailable.
            </div>
          )}
          {requests.data && requests.data.content.length > 0 && (
            <div className="grid gap-3 lg:grid-cols-2">
              {requests.data.content.map((request) => (
                <Card key={request.id}>
                  <CardContent className="flex items-start gap-4 p-4 sm:p-4">
                    <span className="grid size-10 shrink-0 place-items-center rounded-xl bg-coral-50 text-coral-600">
                      <AmbulanceIcon className="size-4" aria-hidden="true" />
                    </span>
                    <div className="min-w-0 flex-1">
                      <div className="flex flex-wrap items-center justify-between gap-2">
                        <p className="truncate text-sm font-bold text-ink-950">
                          {request.hospital.name}
                        </p>
                        <StatusBadge status={request.status} />
                      </div>
                      <p className="mt-2 flex items-center gap-2 text-xs text-slate-500">
                        <Clock3 className="size-3.5" aria-hidden="true" />
                        {formatDateTime(request.requestedAt)}
                      </p>
                      {request.ambulance && (
                        <Badge className="mt-2">
                          Vehicle {request.ambulance.vehicleNumber} assigned
                        </Badge>
                      )}
                    </div>
                  </CardContent>
                </Card>
              ))}
            </div>
          )}
        </section>
      )}

      {query.isLoading && <SkeletonCards count={6} />}
      {query.isError && (
        <ErrorState
          title="Ambulance services could not be loaded"
          message={query.error.message}
          onRetry={() => query.refetch()}
        />
      )}
      {query.isSuccess && query.data.content.length === 0 && (
        <EmptyState
          title="No ambulance services were reported"
          message="Try again shortly or contact hospitals directly. If someone is in immediate danger, contact local emergency services."
          icon={AmbulanceIcon}
        />
      )}
      {query.isSuccess && query.data.content.length > 0 && (
        <div className="grid gap-5 md:grid-cols-2 xl:grid-cols-3">
          {query.data.content.map((ambulance) => (
            <Card key={ambulance.id}>
              <CardContent>
                <div className="flex items-start justify-between gap-3">
                  <span className="grid size-11 place-items-center rounded-2xl bg-coral-100 text-coral-600">
                    <AmbulanceIcon className="size-5" aria-hidden="true" />
                  </span>
                  <StatusBadge status={ambulance.status} />
                </div>
                <h2 className="mt-5 font-bold text-ink-950">
                  Hospital ambulance
                </h2>
                <p className="mt-1 text-sm font-semibold text-brand-700">
                  {ambulance.vehicleNumber}
                </p>
                <div className="mt-5 space-y-3 text-sm text-slate-500">
                  <p className="flex items-start gap-2.5">
                    <Building2
                      className="mt-0.5 size-4 shrink-0 text-slate-400"
                      aria-hidden="true"
                    />
                    <span>{ambulance.hospital.name}</span>
                  </p>
                  <p className="flex items-start gap-2.5">
                    <MapPin
                      className="mt-0.5 size-4 shrink-0 text-slate-400"
                      aria-hidden="true"
                    />
                    <span>
                      {ambulance.lastLocation ??
                        ambulance.hospital.address ??
                        "Location not listed"}
                    </span>
                  </p>
                  {(ambulance.contactNumber ||
                    ambulance.hospital.contactNumber) && (
                    <p className="flex items-center gap-2.5">
                      <Phone
                        className="size-4 shrink-0 text-slate-400"
                        aria-hidden="true"
                      />
                      <a
                        href={`tel:${ambulance.contactNumber ?? ambulance.hospital.contactNumber}`}
                        className="font-semibold text-ink-800 hover:text-brand-700"
                      >
                        {ambulance.contactNumber ??
                          ambulance.hospital.contactNumber}
                      </a>
                    </p>
                  )}
                </div>
                <Button
                  type="button"
                  variant={
                    ambulance.status === "AVAILABLE" ? "primary" : "outline"
                  }
                  className="mt-6 w-full"
                  disabled={ambulance.status !== "AVAILABLE"}
                  onClick={() => setSelected(ambulance)}
                >
                  <Send className="size-4" aria-hidden="true" />
                  {ambulance.status === "AVAILABLE"
                    ? "Request from hospital"
                    : "Not available"}
                </Button>
              </CardContent>
            </Card>
          ))}
        </div>
      )}

      <Modal
        open={Boolean(selected)}
        onClose={() => setSelected(null)}
        title="Request ambulance support"
        description="Record a demonstration transport request for the selected hospital."
      >
        {selected && (
          <RequestAmbulanceForm
            ambulance={selected}
            onSuccess={() => setSelected(null)}
          />
        )}
      </Modal>
    </>
  );
}
