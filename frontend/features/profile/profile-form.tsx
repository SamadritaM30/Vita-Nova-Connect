"use client";

import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useRouter } from "next/navigation";
import { z } from "zod";
import { Save } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  FieldShell,
  Input,
  Select,
  Textarea
} from "@/components/ui/form-controls";
import { ErrorState, PageLoading } from "@/components/ui/states";
import { useToast } from "@/components/ui/toast";
import { ApiError, profileApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import { yesterdayInputValue } from "@/lib/utils";

const profileSchema = z.object({
  displayName: z
    .string()
    .trim()
    .min(2, "Enter your full name")
    .max(100, "Name is too long"),
  dateOfBirth: z.string().optional(),
  contactNumber: z
    .string()
    .trim()
    .regex(/^[+]?[0-9][0-9\s-]{7,14}$/, "Enter a valid contact number")
    .or(z.literal("")),
  bloodGroup: z.string().optional(),
  address: z.string().trim().max(500, "Address is too long").optional(),
  employeeId: z.string().trim().max(80, "Employee ID is too long").optional(),
  organizationName: z
    .string()
    .trim()
    .max(160, "Organization name is too long")
    .optional()
});

type ProfileValues = z.infer<typeof profileSchema>;

export function ProfileForm({
  onboarding = false
}: {
  onboarding?: boolean;
}) {
  const router = useRouter();
  const queryClient = useQueryClient();
  const { showToast } = useToast();
  const [overrides, setOverrides] = useState<Partial<ProfileValues>>({});
  const [errors, setErrors] = useState<Record<string, string>>({});

  const profileQuery = useQuery({
    queryKey: queryKeys.profile,
    queryFn: ({ signal }) => profileApi.get(signal)
  });

  const values: ProfileValues = {
    dateOfBirth: profileQuery.data?.dateOfBirth ?? "",
    bloodGroup: profileQuery.data?.bloodGroup ?? "",
    address: profileQuery.data?.address ?? "",
    employeeId: profileQuery.data?.employeeId ?? "",
    organizationName: profileQuery.data?.organizationName ?? "",
    ...overrides,
    displayName:
      overrides.displayName ?? profileQuery.data?.displayName ?? "",
    contactNumber:
      overrides.contactNumber ?? profileQuery.data?.contactNumber ?? ""
  };

  const mutation = useMutation({
    mutationFn: profileApi.update,
    onSuccess: async (profile) => {
      queryClient.setQueryData(queryKeys.profile, profile);
      setOverrides({});
      await queryClient.invalidateQueries({ queryKey: queryKeys.auth });
      showToast({
        tone: "success",
        title: onboarding ? "Your profile is ready" : "Profile updated",
        description: onboarding
          ? "Welcome to your VitaNova Connect workspace."
          : "Your latest details have been saved."
      });
      if (onboarding) router.replace("/patient/dashboard");
    },
    onError: (error) => {
      if (error instanceof ApiError && error.fieldErrors) {
        const nextErrors: Record<string, string> = {};
        if (Array.isArray(error.fieldErrors)) {
          error.fieldErrors.forEach((item) => {
            nextErrors[item.field] = item.message;
          });
        } else {
          Object.assign(nextErrors, error.fieldErrors);
        }
        setErrors(nextErrors);
      }
      showToast({
        tone: "error",
        title: "Profile could not be saved",
        description: error.message
      });
    }
  });

  const [maxBirthDate] = useState(yesterdayInputValue);

  function updateField<K extends keyof ProfileValues>(
    field: K,
    value: ProfileValues[K]
  ) {
    setOverrides((current) => ({ ...current, [field]: value }));
    setErrors((current) => {
      const next = { ...current };
      delete next[field];
      return next;
    });
  }

  function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const result = profileSchema.safeParse(values);
    if (!result.success) {
      const nextErrors: Record<string, string> = {};
      result.error.issues.forEach((issue) => {
        nextErrors[String(issue.path[0])] = issue.message;
      });
      setErrors(nextErrors);
      return;
    }
    const completionErrors: Record<string, string> = {};
    const patientProfile = profileQuery.data?.role === "PATIENT";
    if (patientProfile && !result.data.dateOfBirth) {
      completionErrors.dateOfBirth = "Date of birth is required";
    }
    if (patientProfile && !result.data.bloodGroup) {
      completionErrors.bloodGroup = "Blood group is required";
    }
    if (patientProfile && !result.data.contactNumber) {
      completionErrors.contactNumber = "Contact number is required";
    }
    if (
      profileQuery.data?.role === "REPRESENTATIVE" &&
      !result.data.employeeId
    ) {
      completionErrors.employeeId = "Employee ID is required";
    }
    if (
      profileQuery.data?.role === "REPRESENTATIVE" &&
      !result.data.organizationName
    ) {
      completionErrors.organizationName = "Organization name is required";
    }
    if (Object.keys(completionErrors).length > 0) {
      setErrors(completionErrors);
      return;
    }
    setErrors({});
    const common = {
      displayName: result.data.displayName,
      contactNumber: result.data.contactNumber || undefined
    };
    mutation.mutate(
      profileQuery.data?.role === "REPRESENTATIVE"
        ? {
            ...common,
            employeeId: result.data.employeeId || undefined,
            organizationName: result.data.organizationName || undefined
          }
        : {
            ...common,
            dateOfBirth: result.data.dateOfBirth || undefined,
            bloodGroup: result.data.bloodGroup || undefined,
            address: result.data.address ?? ""
          }
    );
  }

  if (profileQuery.isLoading) {
    return <PageLoading label="Loading your profile…" />;
  }

  if (profileQuery.isError) {
    return (
      <ErrorState
        title="We couldn’t load your profile"
        message={profileQuery.error.message}
        onRetry={() => profileQuery.refetch()}
      />
    );
  }

  return (
    <form onSubmit={handleSubmit} noValidate className="space-y-6">
      <div className="grid gap-5 sm:grid-cols-2">
        <div className="sm:col-span-2">
          <FieldShell
            label="Full name"
            htmlFor="displayName"
            error={errors.displayName}
          >
            <Input
              id="displayName"
              name="displayName"
              autoComplete="name"
              value={values.displayName}
              onChange={(event) =>
                updateField("displayName", event.target.value)
              }
              invalid={Boolean(errors.displayName)}
              aria-describedby={
                errors.displayName ? "displayName-message" : undefined
              }
            />
          </FieldShell>
        </div>

        {profileQuery.data?.role === "PATIENT" && (
          <FieldShell
            label="Date of birth"
            htmlFor="dateOfBirth"
            error={errors.dateOfBirth}
          >
            <Input
              id="dateOfBirth"
              name="dateOfBirth"
              type="date"
              max={maxBirthDate}
              required
              value={values.dateOfBirth}
              onChange={(event) =>
                updateField("dateOfBirth", event.target.value)
              }
              invalid={Boolean(errors.dateOfBirth)}
            />
          </FieldShell>
        )}

        <FieldShell
          label="Contact number"
          htmlFor="contactNumber"
          hint="Include your country code if applicable."
          error={errors.contactNumber}
        >
          <Input
            id="contactNumber"
            name="contactNumber"
            type="tel"
            inputMode="tel"
            autoComplete="tel"
          placeholder="+91 98765 43210"
          required={profileQuery.data?.role === "PATIENT"}
            value={values.contactNumber}
            onChange={(event) =>
              updateField("contactNumber", event.target.value)
            }
            invalid={Boolean(errors.contactNumber)}
          />
        </FieldShell>

        {profileQuery.data?.role === "PATIENT" && (
          <FieldShell
            label="Blood group"
            htmlFor="bloodGroup"
            error={errors.bloodGroup}
          >
            <Select
              id="bloodGroup"
              name="bloodGroup"
              required
              value={values.bloodGroup}
              onChange={(event) => updateField("bloodGroup", event.target.value)}
              invalid={Boolean(errors.bloodGroup)}
            >
              <option value="">Select blood group</option>
              {["A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"].map(
                (group) => (
                  <option key={group} value={group}>
                    {group}
                  </option>
                )
              )}
            </Select>
          </FieldShell>
        )}

        {profileQuery.data?.role === "PATIENT" && (
          <div className="sm:col-span-2">
            <FieldShell
              label="Address"
              htmlFor="address"
              optional
              error={errors.address}
              hint="Used to make ambulance requests faster. You can change it later."
            >
              <Textarea
                id="address"
                name="address"
                autoComplete="street-address"
                placeholder="House or flat, street, area, city, postal code"
                value={values.address}
                onChange={(event) => updateField("address", event.target.value)}
                invalid={Boolean(errors.address)}
              />
            </FieldShell>
          </div>
        )}

        {profileQuery.data?.role === "REPRESENTATIVE" && (
          <>
            <FieldShell
              label="Employee ID"
              htmlFor="employeeId"
              error={errors.employeeId}
            >
              <Input
                id="employeeId"
                value={values.employeeId}
                onChange={(event) =>
                  updateField("employeeId", event.target.value)
                }
                invalid={Boolean(errors.employeeId)}
              />
            </FieldShell>
            <FieldShell
              label="Organization name"
              htmlFor="organizationName"
              error={errors.organizationName}
            >
              <Input
                id="organizationName"
                value={values.organizationName}
                onChange={(event) =>
                  updateField("organizationName", event.target.value)
                }
                invalid={Boolean(errors.organizationName)}
              />
            </FieldShell>
          </>
        )}
      </div>

      {mutation.isError &&
        !(
          mutation.error instanceof ApiError && mutation.error.fieldErrors
        ) && (
          <p
            className="rounded-xl bg-red-50 p-3 text-sm text-red-700"
            role="alert"
          >
            {mutation.error.message}
          </p>
        )}

      <div className="flex flex-col-reverse gap-3 border-t pt-6 sm:flex-row sm:items-center sm:justify-between">
        <p className="text-xs leading-5 text-slate-400">
          Your Google email is managed by your sign-in account and cannot be
          changed here.
        </p>
        <Button type="submit" loading={mutation.isPending} className="shrink-0">
          <Save className="size-4" aria-hidden="true" />
          {onboarding ? "Complete profile" : "Save changes"}
        </Button>
      </div>
    </form>
  );
}
