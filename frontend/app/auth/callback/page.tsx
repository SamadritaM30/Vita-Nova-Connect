"use client";

import { useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import { CheckCircle2 } from "lucide-react";
import { Brand } from "@/components/brand";
import { PageLoading, ErrorState } from "@/components/ui/states";
import { buttonClass } from "@/components/ui/button";
import { useCurrentUser, isUnauthenticated } from "@/features/auth/use-auth";

export default function AuthCallbackPage() {
  const router = useRouter();
  const query = useCurrentUser();

  useEffect(() => {
    if (!query.data) return;
    const destination =
      query.data.role === "REPRESENTATIVE"
        ? "/representative/dashboard"
        : query.data.profileCompleted === false
          ? "/patient/onboarding"
          : "/patient/dashboard";
    router.replace(destination);
  }, [query.data, router]);

  return (
    <main
      id="main-content"
      className="grid min-h-screen place-items-center px-5 py-12"
    >
      <div className="w-full max-w-lg">
        <Brand className="mx-auto mb-8 w-fit" />
        {query.isLoading && (
          <PageLoading label="Completing your secure sign in…" />
        )}
        {query.data && (
          <div className="surface p-8 text-center">
            <CheckCircle2
              className="mx-auto size-10 text-emerald-600"
              aria-hidden="true"
            />
            <h1 className="mt-5 text-xl font-bold">You’re signed in</h1>
            <p className="mt-2 text-sm text-slate-500">
              Preparing your workspace…
            </p>
          </div>
        )}
        {query.error && (
          <>
            <ErrorState
              title={
                isUnauthenticated(query.error)
                  ? "Sign in was not completed"
                  : "We couldn’t verify your sign in"
              }
              message={query.error.message}
              onRetry={() => query.refetch()}
            />
            <Link
              href="/login"
              className={buttonClass({
                variant: "outline",
                className: "mx-auto mt-5 flex w-fit"
              })}
            >
              Return to sign in
            </Link>
          </>
        )}
      </div>
    </main>
  );
}
