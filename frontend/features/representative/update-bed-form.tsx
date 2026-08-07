"use client";

import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { RefreshCw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { FieldShell, Select } from "@/components/ui/form-controls";
import { useToast } from "@/components/ui/toast";
import { representativeApi } from "@/lib/api";
import type { Bed, BedStatus } from "@/types/api";

export function UpdateBedForm({
  bed,
  onSuccess
}: {
  bed: Bed;
  onSuccess: () => void;
}) {
  const [status, setStatus] = useState<BedStatus>(bed.status);
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const mutation = useMutation({
    mutationFn: () => representativeApi.updateBedStatus(bed.id, status),
    onSuccess: async () => {
      await Promise.all([
        queryClient.invalidateQueries({
          queryKey: ["representative", "beds"]
        }),
        queryClient.invalidateQueries({
          queryKey: ["representative", "dashboard"]
        })
      ]);
      showToast({
        tone: "success",
        title: "Bed status updated",
        description: `${bed.bedCode} is now ${status.toLowerCase()}.`
      });
      onSuccess();
    },
    onError: (error) =>
      showToast({
        tone: "error",
        title: "Bed status could not be updated",
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
          Bed {bed.bedCode}
        </p>
        <p className="mt-1 text-sm text-slate-500">
          {bed.wardType} · {bed.hospital.name}
        </p>
      </div>
      <FieldShell
        label="Bed status"
        htmlFor="bed-status-update"
        hint="Use maintenance when the bed cannot safely be assigned."
      >
        <Select
          id="bed-status-update"
          value={status}
          onChange={(event) => setStatus(event.target.value as BedStatus)}
        >
          <option value="AVAILABLE">Available</option>
          <option value="OCCUPIED">Occupied</option>
          <option value="MAINTENANCE">Maintenance</option>
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
        disabled={status === bed.status}
      >
        <RefreshCw className="size-4" aria-hidden="true" />
        Update bed status
      </Button>
    </form>
  );
}
