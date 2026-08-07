"use client";

import Link from "next/link";
import { useParams } from "next/navigation";
import { useQuery } from "@tanstack/react-query";
import {
  ArrowLeft,
  Building2,
  Clock3,
  MapPin,
  Phone,
  Stethoscope
} from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { Badge, StatusBadge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { ErrorState, PageLoading } from "@/components/ui/states";
import { BookAppointmentForm } from "@/features/doctors/book-appointment-form";
import { doctorsApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import { formatTime, humanize } from "@/lib/utils";

export default function DoctorDetailPage() {
  const params = useParams<{ id: string }>();
  const query = useQuery({
    queryKey: queryKeys.doctor(params.id),
    queryFn: ({ signal }) => doctorsApi.detail(params.id, signal)
  });

  if (query.isLoading) return <PageLoading label="Loading doctor details…" />;
  if (query.isError) {
    return (
      <ErrorState
        title="Doctor details could not be loaded"
        message={query.error.message}
        onRetry={() => query.refetch()}
      />
    );
  }
  if (!query.data) return null;

  const doctor = query.data;
  const doctorName = doctor.name;

  return (
    <>
      <Link
        href="/patient/doctors"
        className="mb-5 inline-flex items-center gap-2 text-sm font-bold text-slate-500 transition hover:text-brand-700"
      >
        <ArrowLeft className="size-4" aria-hidden="true" />
        Back to doctors
      </Link>
      <PageHeader
        eyebrow={doctor.specialization}
        title={doctorName}
        description={doctor.hospital.name}
        action={<StatusBadge status={doctor.availabilityStatus} />}
      />

      <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_390px]">
        <div className="space-y-6">
          <Card>
            <CardContent>
              <div className="flex flex-col gap-6 sm:flex-row">
                <span className="grid size-20 shrink-0 place-items-center rounded-3xl bg-brand-100 text-brand-800">
                  <Stethoscope className="size-8" aria-hidden="true" />
                </span>
                <div className="grid flex-1 gap-5 sm:grid-cols-2">
                  <Detail
                    icon={Clock3}
                    label="Consultation hours"
                    value={`${formatTime(doctor.consultationFrom)} – ${formatTime(doctor.consultationTo)}`}
                  />
                  <Detail
                    icon={Building2}
                    label="Hospital"
                    value={doctor.hospital.name}
                  />
                  <Detail
                    icon={Stethoscope}
                    label="Current status"
                    value={humanize(doctor.availabilityStatus)}
                  />
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader>
              <h2 className="text-lg font-bold">Hospital details</h2>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 text-sm sm:grid-cols-2">
                <p className="flex items-start gap-3 rounded-2xl bg-slate-50 p-4 text-slate-600">
                  <MapPin
                    className="mt-0.5 size-4 shrink-0 text-brand-700"
                    aria-hidden="true"
                  />
                  <span>
                    <span className="block font-bold text-ink-950">Address</span>
                    <span className="mt-1 block leading-6">
                      {doctor.hospital.address ?? "Address not listed"}
                    </span>
                  </span>
                </p>
                <p className="flex items-start gap-3 rounded-2xl bg-slate-50 p-4 text-slate-600">
                  <Phone
                    className="mt-0.5 size-4 shrink-0 text-brand-700"
                    aria-hidden="true"
                  />
                  <span>
                    <span className="block font-bold text-ink-950">
                      Hospital contact
                    </span>
                    <span className="mt-1 block leading-6">
                      {doctor.hospital.contactNumber ?? "Not listed"}
                    </span>
                  </span>
                </p>
              </div>
            </CardContent>
          </Card>
        </div>

        <Card className="h-fit xl:sticky xl:top-24">
          <CardHeader>
            <Badge tone="info" className="mb-3 w-fit">
              Secure booking
            </Badge>
            <h2 className="text-xl font-bold tracking-tight">
              Book an appointment
            </h2>
            <p className="mt-2 text-sm leading-6 text-slate-500">
              Select an available date and time. The service validates the slot
              again before confirming.
            </p>
          </CardHeader>
          <CardContent>
            <BookAppointmentForm doctor={doctor} />
          </CardContent>
        </Card>
      </div>
    </>
  );
}

function Detail({
  icon: Icon,
  label,
  value
}: {
  icon: typeof Clock3;
  label: string;
  value: string;
}) {
  return (
    <div className="flex gap-3">
      <span className="grid size-9 shrink-0 place-items-center rounded-xl bg-slate-100 text-slate-500">
        <Icon className="size-4" aria-hidden="true" />
      </span>
      <div>
        <p className="text-xs font-medium text-slate-400">{label}</p>
        <p className="mt-1 text-sm font-bold text-ink-950">{value}</p>
      </div>
    </div>
  );
}
