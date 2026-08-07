import type { HTMLAttributes } from "react";
import { cn, humanize } from "@/lib/utils";

type Tone = "success" | "warning" | "danger" | "neutral" | "info";

const toneClass: Record<Tone, string> = {
  success: "border-emerald-200 bg-emerald-50 text-emerald-700",
  warning: "border-amber-200 bg-amber-50 text-amber-700",
  danger: "border-red-200 bg-red-50 text-red-700",
  neutral: "border-slate-200 bg-slate-50 text-slate-600",
  info: "border-blue-200 bg-blue-50 text-blue-700"
};

export function Badge({
  tone = "neutral",
  className,
  ...props
}: HTMLAttributes<HTMLSpanElement> & { tone?: Tone }) {
  return (
    <span
      className={cn(
        "inline-flex items-center gap-1.5 rounded-full border px-2.5 py-1 text-xs font-semibold",
        toneClass[tone],
        className
      )}
      {...props}
    />
  );
}

export function StatusBadge({ status }: { status?: string | null }) {
  const tone: Tone =
    status === "AVAILABLE" ||
    status === "BOOKED" ||
    status === "COMPLETED" ||
    status === "ACTIVE"
      ? "success"
      : status === "PENDING" || status === "ON_DUTY"
        ? "warning"
        : status === "CANCELLED" ||
            status === "UNAVAILABLE" ||
            status === "SUSPENDED"
          ? "danger"
          : "neutral";

  return (
    <Badge tone={tone}>
      <span className="size-1.5 rounded-full bg-current" aria-hidden="true" />
      {humanize(status)}
    </Badge>
  );
}
