import Link from "next/link";
import { ArrowLeft, MapPinOff } from "lucide-react";
import { Brand } from "@/components/brand";
import { buttonClass } from "@/components/ui/button";

export default function NotFound() {
  return (
    <main
      id="main-content"
      className="grid min-h-screen place-items-center px-5 py-12"
    >
      <div className="max-w-md text-center">
        <Brand className="mb-10" />
        <span className="mx-auto grid size-16 place-items-center rounded-3xl bg-brand-50 text-brand-700">
          <MapPinOff className="size-7" aria-hidden="true" />
        </span>
        <p className="mt-6 text-xs font-bold uppercase tracking-[0.2em] text-brand-700">
          Error 404
        </p>
        <h1 className="mt-2 text-3xl font-bold tracking-tight text-ink-950">
          This page isn’t here
        </h1>
        <p className="mt-3 leading-7 text-slate-500">
          The link may be outdated, or the page may have moved.
        </p>
        <Link href="/" className={buttonClass({ className: "mt-7" })}>
          <ArrowLeft className="size-4" aria-hidden="true" />
          Back to home
        </Link>
      </div>
    </main>
  );
}
