import { Building2, ShieldCheck } from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { Card, CardContent } from "@/components/ui/card";
import { ProfileForm } from "@/features/profile/profile-form";

export default function RepresentativeProfilePage() {
  return (
    <>
      <PageHeader
        eyebrow="Account"
        title="Representative profile"
        description="Manage your contact details. Hospital assignments and access are controlled by an administrator."
      />
      <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_300px]">
        <Card>
          <CardContent>
            <ProfileForm />
          </CardContent>
        </Card>
        <div className="space-y-4">
          <Card>
            <CardContent>
              <Building2 className="size-5 text-brand-700" aria-hidden="true" />
              <h2 className="mt-4 font-bold">Hospital assignments</h2>
              <p className="mt-2 text-sm leading-6 text-slate-500">
                Contact your project administrator to add or change a hospital
                assignment.
              </p>
            </CardContent>
          </Card>
          <Card className="bg-ink-950 text-white">
            <CardContent>
              <ShieldCheck className="size-5 text-brand-300" aria-hidden="true" />
              <p className="mt-4 text-sm leading-6 text-slate-300">
                All operational changes are authorised by the backend and may be
                recorded in the activity log.
              </p>
            </CardContent>
          </Card>
        </div>
      </div>
    </>
  );
}
