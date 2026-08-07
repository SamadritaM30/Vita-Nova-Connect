import Link from "next/link";
import {
  ArrowRight,
  Building2,
  MapPin,
  Stethoscope
} from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { StatusBadge } from "@/components/ui/badge";
import { buttonClass } from "@/components/ui/button";
import { formatTime } from "@/lib/utils";
import type { Doctor } from "@/types/api";

export function DoctorCard({ doctor }: { doctor: Doctor }) {
  const doctorName = doctor.name;

  return (
    <Card className="flex h-full flex-col transition duration-200 hover:-translate-y-0.5 hover:border-brand-200 hover:shadow-soft">
      <CardContent className="flex h-full flex-col">
        <div className="flex items-start gap-4">
          <span className="grid size-12 shrink-0 place-items-center rounded-2xl bg-brand-100 text-brand-800">
            <Stethoscope className="size-5" aria-hidden="true" />
          </span>
          <div className="min-w-0 flex-1">
            <div className="flex flex-wrap items-start justify-between gap-2">
              <div>
                <h2 className="font-bold text-ink-950">{doctorName}</h2>
                <p className="mt-1 text-sm font-semibold text-brand-700">
                  {doctor.specialization}
                </p>
              </div>
              <StatusBadge status={doctor.availabilityStatus} />
            </div>
          </div>
        </div>

        <dl className="mt-5 space-y-3 text-sm text-slate-500">
          <div className="flex gap-2.5">
            <Building2
              className="mt-0.5 size-4 shrink-0 text-slate-400"
              aria-hidden="true"
            />
            <div>
              <dt className="sr-only">Hospital</dt>
              <dd className="font-medium text-ink-800">{doctor.hospital.name}</dd>
            </div>
          </div>
          {doctor.hospital.address && (
            <div className="flex gap-2.5">
              <MapPin
                className="mt-0.5 size-4 shrink-0 text-slate-400"
                aria-hidden="true"
              />
              <div>
                <dt className="sr-only">Location</dt>
                <dd className="line-clamp-2">
                  {doctor.hospital.address}
                </dd>
              </div>
            </div>
          )}
        </dl>

        <div className="mt-auto flex items-center justify-between gap-3 border-t pt-5">
          <div>
            <p className="text-[11px] font-medium uppercase tracking-wide text-slate-400">
              Consultation hours
            </p>
            <p className="mt-0.5 text-sm font-bold text-ink-950">
              {formatTime(doctor.consultationFrom)} –{" "}
              {formatTime(doctor.consultationTo)}
            </p>
          </div>
          <Link
            href={`/patient/doctors/${doctor.id}`}
            className={buttonClass({
              variant:
                doctor.availabilityStatus === "AVAILABLE"
                  ? "primary"
                  : "outline",
              size: "sm"
            })}
          >
            View details
            <ArrowRight className="size-4" aria-hidden="true" />
          </Link>
        </div>
      </CardContent>
    </Card>
  );
}
