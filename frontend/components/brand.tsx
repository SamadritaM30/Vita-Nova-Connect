import Link from "next/link";
import { HeartPulse } from "lucide-react";
import { cn } from "@/lib/utils";

export function Brand({
  compact = false,
  className
}: {
  compact?: boolean;
  className?: string;
}) {
  return (
    <Link
      href="/"
      className={cn(
        "inline-flex items-center gap-2.5 rounded-lg text-ink-950",
        className
      )}
      aria-label="VitaNova Connect home"
    >
      <span className="grid size-10 place-items-center rounded-2xl bg-brand-600 text-white shadow-lg shadow-brand-600/15">
        <HeartPulse aria-hidden="true" className="size-5" strokeWidth={2.4} />
      </span>
      {!compact && (
        <span className="text-lg font-bold tracking-[-0.035em]">
          VitaNova <span className="text-brand-700">Connect</span>
        </span>
      )}
    </Link>
  );
}
