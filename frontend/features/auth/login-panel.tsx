"use client";

import { useEffect } from "react";
import Link from "next/link";
import { useRouter, useSearchParams } from "next/navigation";
import {
  ArrowLeft,
  Check,
  HeartPulse,
  LockKeyhole,
  ShieldCheck
} from "lucide-react";
import { Brand } from "@/components/brand";
import { getGoogleOAuthUrl } from "@/lib/api";
import { useCurrentUser } from "@/features/auth/use-auth";

function GoogleMark() {
  return (
    <span
      aria-hidden="true"
      className="grid size-5 place-items-center rounded-full bg-white text-sm font-extrabold"
      style={{ color: "#4285F4" }}
    >
      G
    </span>
  );
}

export function LoginPanel() {
  const router = useRouter();
  const params = useSearchParams();
  const { data: user, isLoading } = useCurrentUser();
  const representative = params.get("for") === "representative";
  const next = params.get("next");

  useEffect(() => {
    if (!user) return;
    const roleHome =
      user.role === "REPRESENTATIVE"
        ? "/representative/dashboard"
        : user.profileCompleted === false
          ? "/patient/onboarding"
          : "/patient/dashboard";
    const safeNext =
      next?.startsWith("/") && !next.startsWith("//") ? next : roleHome;
    router.replace(safeNext);
  }, [next, router, user]);

  return (
    <main
      id="main-content"
      className="grid min-h-screen bg-white lg:grid-cols-[1.05fr_0.95fr]"
    >
      <section className="relative hidden overflow-hidden bg-ink-950 p-12 text-white lg:flex lg:flex-col">
        <div
          className="absolute -right-24 -top-24 size-[30rem] rounded-full bg-brand-600/25 blur-3xl"
          aria-hidden="true"
        />
        <div
          className="absolute -bottom-36 -left-24 size-96 rounded-full bg-blue-500/15 blur-3xl"
          aria-hidden="true"
        />
        <Brand className="relative [&_span]:text-white" />
        <div className="relative my-auto max-w-xl py-16">
          <span className="grid size-14 place-items-center rounded-3xl bg-white/10 text-brand-200 ring-1 ring-white/10">
            <HeartPulse className="size-7" aria-hidden="true" />
          </span>
          <h1 className="text-balance mt-8 text-5xl font-bold leading-[1.08] tracking-[-0.05em]">
            Care decisions feel better when the path is clear.
          </h1>
          <p className="mt-6 max-w-lg text-lg leading-8 text-slate-300">
            Securely access your appointments and care search, or manage
            hospital availability from your authorised workspace.
          </p>
          <div className="mt-10 grid gap-3 text-sm text-slate-200">
            {[
              "No separate password to remember",
              "Role-based access enforced by the service",
              "Your session is protected with secure cookies"
            ].map((item) => (
              <p key={item} className="flex items-center gap-3">
                <span className="grid size-6 place-items-center rounded-full bg-brand-500/20 text-brand-200">
                  <Check className="size-3.5" strokeWidth={3} aria-hidden="true" />
                </span>
                {item}
              </p>
            ))}
          </div>
        </div>
        <p className="relative text-xs text-slate-400">
          VitaNova Connect · Secure healthcare coordination
        </p>
      </section>

      <section className="flex min-h-screen items-center justify-center px-5 py-10 sm:px-10">
        <div className="w-full max-w-md">
          <div className="mb-10 flex items-center justify-between lg:hidden">
            <Brand />
            <Link
              href="/"
              className="rounded-xl p-2 text-slate-500 hover:bg-slate-100"
              aria-label="Back to home"
            >
              <ArrowLeft className="size-5" aria-hidden="true" />
            </Link>
          </div>

          <p className="text-xs font-bold uppercase tracking-[0.18em] text-brand-700">
            {representative ? "Representative access" : "Welcome back"}
          </p>
          <h2 className="mt-3 text-3xl font-bold tracking-[-0.04em] text-ink-950 sm:text-4xl">
            Sign in to VitaNova
          </h2>
          <p className="mt-3 leading-7 text-slate-500">
            Continue with your Google account. Your access level is assigned
            securely by VitaNova Connect.
          </p>

          {params.get("error") && (
            <div
              className="mt-6 rounded-2xl border border-red-200 bg-red-50 p-4 text-sm leading-6 text-red-700"
              role="alert"
            >
              Sign in could not be completed. Please try again or use the
              account registered with your profile.
            </div>
          )}

          <a
            href={getGoogleOAuthUrl()}
            className="mt-8 flex h-12 w-full items-center justify-center gap-3 rounded-2xl bg-ink-950 px-5 text-sm font-bold text-white shadow-lg shadow-ink-950/10 transition hover:bg-ink-800 focus-visible:outline focus-visible:outline-4 focus-visible:outline-brand-500/20"
            aria-disabled={isLoading}
          >
            <GoogleMark />
            Continue with Google
          </a>

          <div className="mt-7 rounded-2xl border bg-slate-50 p-4">
            <div className="flex gap-3">
              <ShieldCheck
                className="mt-0.5 size-5 shrink-0 text-brand-700"
                aria-hidden="true"
              />
              <div>
                <p className="text-sm font-bold text-ink-950">
                  Protected access
                </p>
                <p className="mt-1 text-xs leading-5 text-slate-500">
                  Patients can only access their own care information.
                  Representatives are limited to assigned hospitals.
                </p>
              </div>
            </div>
          </div>

          <p className="mt-8 flex items-start gap-2 text-xs leading-5 text-slate-400">
            <LockKeyhole
              className="mt-0.5 size-3.5 shrink-0"
              aria-hidden="true"
            />
            By continuing, you agree to use this service responsibly. Google
            verifies your identity; VitaNova Connect controls application
            permissions.
          </p>

          <Link
            href="/"
            className="mt-8 hidden w-fit items-center gap-2 text-sm font-semibold text-slate-500 transition hover:text-brand-700 lg:flex"
          >
            <ArrowLeft className="size-4" aria-hidden="true" />
            Back to home
          </Link>
        </div>
      </section>
    </main>
  );
}
