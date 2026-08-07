import { AuthBoundary } from "@/features/auth/auth-boundary";
import { PortalShell } from "@/components/portal-shell";

export default function RepresentativeLayout({
  children
}: {
  children: React.ReactNode;
}) {
  return (
    <AuthBoundary role="REPRESENTATIVE">
      <PortalShell role="REPRESENTATIVE">{children}</PortalShell>
    </AuthBoundary>
  );
}
