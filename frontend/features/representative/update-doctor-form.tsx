"use client";

import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { FieldShell, Select } from "@/components/ui/form-controls";
import { useToast } from "@/components/ui/toast";
import { representativeApi } from "@/lib/api";
import type { AvailabilityStatus, Doctor } from "@/types/api";

export function UpdateDoctorForm({
  doctor,
  onSuccess
}: {
  doctor: Doctor;
  onSuccess: () => void;
}) {
  const [status, setStatus] = useState<AvailabilityStatus>(
    doctor.availabilityStatus
  );
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const mutation = useMutation({
    mutationFn: () =>
      representativeApi.updateDoctorAvailability(doctor.id, status),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ["representative", "doctors"]
        }),
        queryClient.invalidateQueries({
          queryKey: queryKeysPrefixDashboard
        })
      ]);
      showToast({
        tone: "success",
        title: "Doctor availability updated",
        description: `${doctor.name} is now ${status.toLowerCase()}.`
      });
      onSuccess();
    },
    onError: (error) =>
      showToast({
        tone: "error",
        title: "Availability could not be updated",
        description: error.message
      })
  });

  return (
    <form
      className="space-y-5"
      onSubmit={(event) => {
        event.preventDefault();
        mutation.mutate();
      }}
    >
      <div className="rounded-2xl bg-slate-50 p-4">
        <p className="font-bold text-ink-950">
          {doctor.name}
        </p>
        <p className="mt-1 text-sm text-slate-500">
          {doctor.specialization} · {doctor.hospital.name}
        </p>
      </div>
      <FieldShell
        label="Availability status"
        htmlFor="doctor-status-update"
        hint="This status is shown in patient doctor searches."
      >
        <Select
          id="doctor-status-update"
          value={status}
          onChange={(event) =>
            setStatus(event.target.value as AvailabilityStatus)
          }
        >
          <option value="AVAILABLE">Available</option>
          <option value="UNAVAILABLE">Unavailable</option>
        </Select>
      </FieldShell>
      {mutation.isError && (
        <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700" role="alert">
          {mutation.error.message}
        </p>
      )}
      <Button
        type="submit"
        className="w-full"
        loading={mutation.isPending}
        disabled={status === doctor.availabilityStatus}
      >
        <RefreshCw className="size-4" aria-hidden="true" />
        Update availability
      </Button>
    </form>
  );
}

const queryKeysPrefixDashboard = ["representative", "dashboard"] as const;
