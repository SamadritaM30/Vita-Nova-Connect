import { AuthBoundary } from "@/features/auth/auth-boundary";
import { PortalShell } from "@/components/portal-shell";

export default function PatientLayout({
  children
}: {
  children: React.ReactNode;
}) {
  return (
    <AuthBoundary role="PATIENT">
      <PortalShell role="PATIENT">{children}</PortalShell>
    </AuthBoundary>
  );
}
