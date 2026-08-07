"use client";

import { useQuery } from "@tanstack/react-query";
import { authApi, ApiError } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";

export function useCurrentUser() {
  return useQuery({
    queryKey: queryKeys.auth,
    queryFn: ({ signal }) => authApi.me(signal),
    retry: false,
    staleTime: 60_000
  });
}

export function isUnauthenticated(error: unknown): boolean {
  return error instanceof ApiError && (error.status === 401 || error.status === 403);
}
