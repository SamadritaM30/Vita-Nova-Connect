import {
  AlertCircle,
  Inbox,
  LoaderCircle,
  RefreshCw,
  type LucideIcon
} from "lucide-react";
import { Button } from "@/components/ui/button";

export function PageLoading({ label = "Loading your information…" }) {
  return (
    <div
      className="flex min-h-64 flex-col items-center justify-center gap-4 rounded-3xl border border-dashed bg-white/60 p-8 text-center"
      role="status"
    >
      <span className="grid size-12 place-items-center rounded-2xl bg-brand-50 text-brand-700">
        <LoaderCircle className="size-6 animate-spin" aria-hidden="true" />
      </span>
      <p className="text-sm font-medium text-slate-600">{label}</p>
    </div>
  );
}

export function SkeletonCards({ count = 3 }: { count?: number }) {
  return (
    <div
      className="grid gap-4 md:grid-cols-2 xl:grid-cols-3"
      aria-label="Loading results"
      role="status"
    >
      {Array.from({ length: count }).map((_, index) => (
        <div
          key={index}
          className="h-56 animate-pulse-soft rounded-3xl border bg-white p-6"
        >
          <div className="mb-5 size-12 rounded-2xl bg-slate-100" />
          <div className="mb-3 h-5 w-3/5 rounded bg-slate-100" />
          <div className="mb-2 h-3 w-full rounded bg-slate-100" />
          <div className="h-3 w-4/5 rounded bg-slate-100" />
        </div>
      ))}
    </div>
  );
}

export function ErrorState({
  title = "We couldn’t load this",
  message,
  onRetry
}: {
  title?: string;
  message?: string;
  onRetry?: () => void;
}) {
  return (
    <div
      className="flex min-h-56 flex-col items-center justify-center rounded-3xl border border-red-100 bg-red-50/55 p-8 text-center"
      role="alert"
    >
      <span className="mb-4 grid size-11 place-items-center rounded-2xl bg-red-100 text-red-700">
        <AlertCircle className="size-5" aria-hidden="true" />
      </span>
      <h3 className="font-bold text-ink-950">{title}</h3>
      <p className="mt-2 max-w-md text-sm leading-6 text-slate-600">
        {message ?? "Please try again in a moment."}
      </p>
      {onRetry && (
        <Button className="mt-5" variant="outline" onClick={onRetry}>
          <RefreshCw className="size-4" aria-hidden="true" />
          Try again
        </Button>
      )}
    </div>
  );
}

export function EmptyState({
  title,
  message,
  icon: Icon = Inbox,
  action
}: {
  title: string;
  message: string;
  icon?: LucideIcon;
  action?: React.ReactNode;
}) {
  return (
    <div className="flex min-h-56 flex-col items-center justify-center rounded-3xl border border-dashed bg-white/60 p-8 text-center">
      <span className="mb-4 grid size-12 place-items-center rounded-2xl bg-slate-100 text-slate-500">
        <Icon className="size-5" aria-hidden="true" />
      </span>
      <h3 className="font-bold text-ink-950">{title}</h3>
      <p className="mt-2 max-w-md text-sm leading-6 text-slate-500">
        {message}
      </p>
      {action && <div className="mt-5">{action}</div>}
    </div>
  );
}
