"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Ambulance as AmbulanceIcon, MapPin } from "lucide-react";
import { Button } from "@/components/ui/button";
import { FieldShell, Input, Textarea } from "@/components/ui/form-controls";
import { SafetyNotice } from "@/components/safety-notice";
import { useToast } from "@/components/ui/toast";
import { ambulancesApi, profileApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import type { Ambulance } from "@/types/api";

export function RequestAmbulanceForm({
  ambulance,
  onSuccess
}: {
  ambulance: Ambulance;
  onSuccess: () => void;
}) {
  const { showToast } = useToast();
  const queryClient = useQueryClient();
  const [pickupAddress, setPickupAddress] = useState<string | null>(null);
  const [contactNumber, setContactNumber] = useState<string | null>(null);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const profile = useQuery({
    queryKey: queryKeys.profile,
    queryFn: ({ signal }) => profileApi.get(signal),
    staleTime: 60_000
  });

  const displayedPickupAddress =
    pickupAddress ?? profile.data?.address ?? "";
  const displayedContactNumber =
    contactNumber ?? profile.data?.contactNumber ?? "";

  const mutation = useMutation({
    mutationFn: () =>
      ambulancesApi.request({
        hospitalId: ambulance.hospital.id,
        pickupAddress: displayedPickupAddress.trim(),
        contactNumber: displayedContactNumber.trim()
      }),
    onSuccess: async () => {
      await queryClient.invalidateQueries({
        queryKey: ["ambulance-requests"]
      });
      showToast({
        tone: "success",
        title: "Transport request recorded",
        description:
          "This demonstration stores the request for the selected hospital; it does not confirm or dispatch a vehicle."
      });
      onSuccess();
    },
    onError: (error) =>
      showToast({
        tone: "error",
        title: "Request could not be sent",
        description: error.message
      })
  });

  function submit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const nextErrors: Record<string, string> = {};
    if (displayedPickupAddress.trim().length < 8) {
      nextErrors.pickupAddress = "Enter a clear pickup address";
    }
    if (!/^[+]?[0-9][0-9\s-]{7,14}$/.test(displayedContactNumber.trim())) {
      nextErrors.contactNumber = "Enter a valid contact number";
    }
    setErrors(nextErrors);
    if (Object.keys(nextErrors).length === 0) mutation.mutate();
  }

  return (
    <form onSubmit={submit} className="space-y-5">
      <SafetyNotice compact />
      <div className="rounded-2xl bg-slate-50 p-4">
        <p className="text-xs font-medium text-slate-500">Requesting from</p>
        <p className="mt-1 font-bold text-ink-950">{ambulance.hospital.name}</p>
        <p className="mt-1 text-xs text-slate-500">
          This demonstration records an intake request only. It does not notify
          the hospital or dispatch a vehicle.
        </p>
      </div>
      <FieldShell
        label="Pickup address"
        htmlFor="pickup-address"
        error={errors.pickupAddress}
        hint="Include a nearby landmark when possible."
      >
        <div className="relative">
          <MapPin
            className="pointer-events-none absolute left-3.5 top-3.5 size-4 text-slate-400"
            aria-hidden="true"
          />
          <Textarea
            id="pickup-address"
            className="pl-10"
            value={displayedPickupAddress}
            onChange={(event) => {
              setPickupAddress(event.target.value);
              setErrors((current) => ({ ...current, pickupAddress: "" }));
            }}
            invalid={Boolean(errors.pickupAddress)}
          />
        </div>
      </FieldShell>
      <FieldShell
        label="Contact number"
        htmlFor="ambulance-contact"
        error={errors.contactNumber}
      >
        <Input
          id="ambulance-contact"
          type="tel"
          inputMode="tel"
          autoComplete="tel"
          value={displayedContactNumber}
          onChange={(event) => {
            setContactNumber(event.target.value);
            setErrors((current) => ({ ...current, contactNumber: "" }));
          }}
          invalid={Boolean(errors.contactNumber)}
        />
      </FieldShell>
      {mutation.isError && (
        <p className="rounded-xl bg-red-50 p-3 text-sm text-red-700" role="alert">
          {mutation.error.message}
        </p>
      )}
      <Button type="submit" className="w-full" loading={mutation.isPending}>
        <AmbulanceIcon className="size-4" aria-hidden="true" />
        Send transport request
      </Button>
    </form>
  );
}
