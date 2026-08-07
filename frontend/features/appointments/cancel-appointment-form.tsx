"use client";

import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { Ban } from "lucide-react";
import { Button } from "@/components/ui/button";
import { FieldShell, Textarea } from "@/components/ui/form-controls";
import { useToast } from "@/components/ui/toast";
import { appointmentsApi, representativeApi } from "@/lib/api";
import type { Appointment } from "@/types/api";

export function CancelAppointmentForm({
  appointment,
  representative = false,
  onSuccess
}: {
  appointment: Appointment;
  representative?: boolean;
  onSuccess: () => void;
}) {
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const [reason, setReason] = useState("");
  const [error, setError] = useState("");

  const mutation = useMutation({
    mutationFn: () =>
      representative
        ? representativeApi.cancelAppointment(appointment.id, reason.trim())
        : appointmentsApi.cancel(appointment.id, reason.trim()),
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: representative
          ? ["representative", "appointments"]
          : ["appointments"]
      });
      showToast({
        tone: "success",
        title: "Appointment cancelled",
        description: "The appointment record has been updated."
      });
      onSuccess();
    },
    onError: (mutationError) => {
      showToast({
        tone: "error",
        title: "Cancellation could not be completed",
        description: mutationError.message
      });
    }
  });

  return (
    <form
      className="space-y-5"
      onSubmit={(event) => {
        event.preventDefault();
        if (reason.trim().length < 5) {
          setError("Please provide a brief reason");
          return;
        }
        setError("");
        mutation.mutate();
      }}
    >
      <div className="rounded-2xl border border-amber-200 bg-amber-50 p-4 text-sm leading-6 text-amber-900">
        This will cancel the booking but retain its record for history and
        accountability.
      </div>
      <FieldShell
        label="Reason for cancellation"
        htmlFor="cancellation-reason"
        error={error}
      >
        <Textarea
          id="cancellation-reason"
          value={reason}
          onChange={(event) => {
            setReason(event.target.value);
            setError("");
          }}
          placeholder={
            representative
              ? "e.g. Doctor unavailable due to an emergency"
              : "e.g. My schedule has changed"
          }
          invalid={Boolean(error)}
          maxLength={300}
        />
      </FieldShell>
      {mutation.isError && (
        <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700" role="alert">
          {mutation.error.message}
        </p>
      )}
      <Button
        type="submit"
        variant="danger"
        className="w-full"
        loading={mutation.isPending}
      >
        <Ban className="size-4" aria-hidden="true" />
        Cancel appointment
      </Button>
    </form>
  );
}
