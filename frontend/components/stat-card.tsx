import type { LucideIcon } from "lucide-react";
import { Card, CardContent } from "@/components/ui/card";
import { cn } from "@/lib/utils";

const tones = {
  brand: "bg-brand-50 text-brand-700",
  blue: "bg-blue-50 text-blue-700",
  amber: "bg-amber-50 text-amber-700",
  coral: "bg-coral-50 text-coral-600"
};

export function StatCard({
  label,
  value,
  helper,
  icon: Icon,
  tone = "brand"
}: {
  label: string;
  value: string | number;
  helper?: string;
  icon: LucideIcon;
  tone?: keyof typeof tones;
}) {
  return (
    <Card>
      <CardContent className="flex items-start justify-between gap-4">
        <div>
          <p className="text-sm font-medium text-slate-500">{label}</p>
          <p className="mt-2 text-3xl font-bold tracking-[-0.04em] text-ink-950">
            {value}
          </p>
          {helper && <p className="mt-1 text-xs text-slate-400">{helper}</p>}
        </div>
        <span className={cn("grid size-11 place-items-center rounded-2xl", tones[tone])}>
          <Icon className="size-5" aria-hidden="true" />
        </span>
      </CardContent>
    </Card>
  );
}
