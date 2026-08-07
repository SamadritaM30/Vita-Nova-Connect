import { Info } from "lucide-react";

export function SafetyNotice({
  compact = false
}: {
  compact?: boolean;
}) {
  return (
    <aside
      className="flex gap-3 rounded-2xl border border-blue-200 bg-blue-50/70 p-4 text-blue-900"
      aria-label="Important availability notice"
    >
      <Info
        className="mt-0.5 size-5 shrink-0 text-blue-700"
        aria-hidden="true"
      />
      <div>
        <p className="text-sm font-bold">Availability is informational</p>
        <p className="mt-1 text-xs leading-5 text-blue-800/80">
          {compact
            ? "Availability can change and is not a guarantee of care. If someone is in immediate danger, contact local emergency services."
            : "Hospital and ambulance status shown here is sample project data and can change. It does not guarantee admission, transport, or treatment. If someone is in immediate danger, contact local emergency services now."}
        </p>
      </div>
    </aside>
  );
}
