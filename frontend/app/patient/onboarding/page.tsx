import { ClipboardCheck, LockKeyhole } from "lucide-react";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { ProfileForm } from "@/features/profile/profile-form";

export default function PatientOnboardingPage() {
  return (
    <div className="mx-auto max-w-3xl">
      <div className="mb-7 text-center">
        <span className="mx-auto grid size-12 place-items-center rounded-2xl bg-brand-100 text-brand-800">
          <ClipboardCheck className="size-5" aria-hidden="true" />
        </span>
        <p className="mt-5 text-xs font-bold uppercase tracking-[0.16em] text-brand-700">
          One quick step
        </p>
        <h1 className="mt-2 text-3xl font-bold tracking-[-0.04em] text-ink-950">
          Complete your patient profile
        </h1>
        <p className="mx-auto mt-3 max-w-xl leading-7 text-slate-500">
          These details help us connect your appointments and requests to the
          right person.
        </p>
      </div>
      <Card>
        <CardHeader className="flex-row items-start gap-3">
          <span className="grid size-9 shrink-0 place-items-center rounded-xl bg-slate-100 text-slate-600">
            <LockKeyhole className="size-4" aria-hidden="true" />
          </span>
          <div>
            <h2 className="font-bold text-ink-950">Your details</h2>
            <p className="mt-1 text-sm text-slate-500">
              Only authorised care workflows can access this information.
            </p>
          </div>
        </CardHeader>
        <CardContent>
          <ProfileForm onboarding />
        </CardContent>
      </Card>
    </div>
  );
}
