import { ShieldCheck } from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { Card, CardContent } from "@/components/ui/card";
import { ProfileForm } from "@/features/profile/profile-form";

export default function PatientProfilePage() {
  return (
    <>
      <PageHeader
        eyebrow="Account"
        title="Your profile"
        description="Keep your contact and care details accurate for appointments and transport requests."
      />
      <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_280px]">
        <Card>
          <CardContent>
            <ProfileForm />
          </CardContent>
        </Card>
        <Card className="h-fit bg-brand-950 text-white">
          <CardContent>
            <ShieldCheck className="size-6 text-brand-300" aria-hidden="true" />
            <h2 className="mt-5 font-bold">Privacy by default</h2>
            <p className="mt-2 text-sm leading-6 text-brand-100/75">
              Your profile is protected by your secure session. Patient
              information is never used to decide your role or representative
              access.
            </p>
          </CardContent>
        </Card>
      </div>
    </>
  );
}
