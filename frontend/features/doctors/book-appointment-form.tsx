"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { CalendarCheck2, Clock3 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { FieldShell, Input, Select } from "@/components/ui/form-controls";
import { ErrorState, PageLoading } from "@/components/ui/states";
import { useToast } from "@/components/ui/toast";
import { appointmentsApi, doctorsApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import { formatDateTime, todayInputValue } from "@/lib/utils";
import type { Doctor } from "@/types/api";

export function BookAppointmentForm({
  doctor,
  onSuccess
}: {
  doctor: Doctor;
  onSuccess?: () => void;
}) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const [date, setDate] = useState(todayInputValue());
  const [selectedSlot, setSelectedSlot] = useState("");

  const slots = useQuery({
    queryKey: queryKeys.slots(doctor.id, date),
    queryFn: ({ signal }) => doctorsApi.slots(doctor.id, date, signal),
    enabled: Boolean(date) && doctor.availabilityStatus === "AVAILABLE"
  });

  const mutation = useMutation({
    mutationFn: () =>
      appointmentsApi.create({
        doctorId: doctor.id,
        startsAt: selectedSlot
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({ queryKey: ["appointments"] });
      showToast({
        tone: "success",
        title: "Appointment booked",
        description: `Your appointment with ${doctor.name} is confirmed.`
      });
      onSuccess?.();
      router.push("/patient/appointments");
    },
    onError: (error) => {
      showToast({
        tone: "error",
        title: "Booking could not be completed",
        description: error.message
      });
    }
  });

  const slotValues = slots.data?.map((slot) => ({
    value: typeof slot === "string" ? slot : slot.startsAt,
    label: formatDateTime(typeof slot === "string" ? slot : slot.startsAt, {
      hour: "numeric",
      minute: "2-digit"
    })
  }));

  function handleDateChange(value: string) {
    setDate(value);
    setSelectedSlot("");
  }

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!selectedSlot) return;
    mutation.mutate();
  }

  if (doctor.availabilityStatus !== "AVAILABLE") {
    return (
      <div className="rounded-2xl border border-amber-200 bg-amber-50 p-5 text-sm leading-6 text-amber-800">
        This doctor is not currently accepting new appointments. Availability
        can change, so please check again later.
      </div>
    );
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-5">
      <FieldShell label="Appointment date" htmlFor="appointment-date">
        <Input
          id="appointment-date"
          type="date"
          min={todayInputValue()}
          value={date}
          onChange={(event) => handleDateChange(event.target.value)}
          required
        />
      </FieldShell>

      {slots.isLoading && <PageLoading label="Checking available times…" />}
      {slots.isError && (
        <ErrorState
          title="Time slots are unavailable"
          message={slots.error.message}
          onRetry={() => slots.refetch()}
        />
      )}
      {slots.isSuccess && slotValues?.length === 0 && (
        <div className="rounded-2xl border border-dashed bg-slate-50 p-5 text-center">
          <Clock3
            className="mx-auto size-5 text-slate-400"
            aria-hidden="true"
          />
          <p className="mt-3 text-sm font-bold text-ink-950">
            No slots on this date
          </p>
          <p className="mt-1 text-xs text-slate-500">
            Choose another date to continue.
          </p>
        </div>
      )}
      {slots.isSuccess && slotValues && slotValues.length > 0 && (
        <FieldShell label="Available time" htmlFor="appointment-time">
          <Select
            id="appointment-time"
            value={selectedSlot}
            onChange={(event) => setSelectedSlot(event.target.value)}
            required
          >
            <option value="">Choose a time</option>
            {slotValues.map((slot) => (
              <option key={slot.value} value={slot.value}>
                {slot.label}
              </option>
            ))}
          </Select>
        </FieldShell>
      )}

      {mutation.isError && (
        <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700" role="alert">
          {mutation.error.message}
        </p>
      )}

      <Button
        type="submit"
        className="w-full"
        disabled={!selectedSlot}
        loading={mutation.isPending}
      >
        <CalendarCheck2 className="size-4" aria-hidden="true" />
        Confirm appointment
      </Button>
    </form>
  );
}
