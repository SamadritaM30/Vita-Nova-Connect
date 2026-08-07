"use client";

import { useEffect } from "react";
import { usePathname, useRouter } from "next/navigation";
import { ShieldAlert } from "lucide-react";
import type { UserRole } from "@/types/api";
import { useCurrentUser, isUnauthenticated } from "@/features/auth/use-auth";
import { PageLoading, ErrorState } from "@/components/ui/states";

export function AuthBoundary({
  role,
  children
}: {
  role: UserRole;
  children: React.ReactNode;
}) {
  const router = useRouter();
  const pathname = usePathname();
  const query = useCurrentUser();

  useEffect(() => {
    if (query.isLoading) return;

    if (query.error && isUnauthenticated(query.error)) {
      router.replace(`/login?next=${encodeURIComponent(pathname)}`);
      return;
    }

    if (query.data && query.data.role !== role) {
      const destination =
        query.data.role === "REPRESENTATIVE"
          ? "/representative/dashboard"
          : "/patient/dashboard";
      router.replace(destination);
      return;
    }

    if (
      role === "PATIENT" &&
      query.data?.profileCompleted === false &&
      pathname !== "/patient/onboarding"
    ) {
      router.replace("/patient/onboarding");
    }
  }, [pathname, query.data, query.error, query.isLoading, role, router]);

  if (query.isLoading) {
    return (
      <main className="grid min-h-screen place-items-center p-5">
        <div className="w-full max-w-lg">
          <PageLoading label="Securing your workspace…" />
        </div>
      </main>
    );
  }

  if (query.error) {
    if (isUnauthenticated(query.error)) {
      return (
        <main className="grid min-h-screen place-items-center p-5">
          <div className="w-full max-w-lg">
            <PageLoading label="Taking you to sign in…" />
          </div>
        </main>
      );
    }
    return (
      <main className="grid min-h-screen place-items-center p-5">
        <div className="w-full max-w-lg">
          <ErrorState
            title="We couldn’t verify your session"
            message={query.error.message}
            onRetry={() => query.refetch()}
          />
        </div>
      </main>
    );
  }

  if (!query.data || query.data.role !== role) {
    return (
      <main className="grid min-h-screen place-items-center p-5">
        <div className="surface max-w-md p-8 text-center">
          <ShieldAlert
            className="mx-auto size-8 text-brand-700"
            aria-hidden="true"
          />
          <p className="mt-4 font-semibold">Checking access…</p>
        </div>
      </main>
    );
  }

  return <>{children}</>;
}
