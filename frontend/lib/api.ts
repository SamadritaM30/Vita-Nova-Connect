import type {
  Ambulance,
  AmbulanceRequest,
  Appointment,
  AppointmentSearchParams,
  AuthUser,
  AvailabilityStatus,
  Bed,
  BedStatus,
  Doctor,
  DoctorSearchParams,
  DoctorSlot,
  Hospital,
  HospitalSearchParams,
  PageParams,
  PageResponse,
  ProblemDetails,
  RepresentativeDashboard,
  UserProfile,
  WardType
} from "@/types/api";

const API_BASE_URL = (
  process.env.NEXT_PUBLIC_API_BASE_URL ?? "http://localhost:8080"
).replace(/\/$/, "");

interface CsrfCredentials {
  headerName: string;
  token: string;
}

let inMemoryCsrf: CsrfCredentials | null = null;

export class ApiError extends Error {
  status: number;
  code?: string;
  fieldErrors?: ProblemDetails["fieldErrors"];

  constructor(message: string, status: number, problem?: ProblemDetails) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.code = problem?.code;
    this.fieldErrors = problem?.fieldErrors;
  }
}

function queryString(params: object): string {
  const search = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== "") {
      search.set(key, String(value));
    }
  });
  const result = search.toString();
  return result ? `?${result}` : "";
}

function isEnvelope<T>(value: unknown): value is { data: T } {
  return (
    typeof value === "object" &&
    value !== null &&
    "data" in value &&
    ("success" in value || "timestamp" in value)
  );
}

async function getCsrfCredentials(forceRefresh = false): Promise<CsrfCredentials> {
  if (inMemoryCsrf && !forceRefresh) return inMemoryCsrf;

  const response = await fetch(`${API_BASE_URL}/api/v1/auth/csrf`, {
    method: "GET",
    headers: { Accept: "application/json" },
    credentials: "include"
  });
  const payload = await response.json().catch(() => null);
  if (!response.ok) {
    const problem = (payload ?? {}) as ProblemDetails;
    throw new ApiError(
      problem.detail ?? "Your secure session could not be prepared.",
      response.status,
      problem
    );
  }
  const data = (
    isEnvelope<CsrfCredentials>(payload) ? payload.data : payload
  ) as Partial<CsrfCredentials> | null;
  if (!data?.token || !data?.headerName) {
    throw new ApiError("The service did not provide a security token.", 0);
  }
  inMemoryCsrf = {
    headerName: data.headerName,
    token: data.token
  };
  return inMemoryCsrf;
}

export async function apiRequest<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const baseHeaders = new Headers(options.headers);
  baseHeaders.set("Accept", "application/json");
  if (options.body && !(options.body instanceof FormData)) {
    baseHeaders.set("Content-Type", "application/json");
  }
  const method = (options.method ?? "GET").toUpperCase();
  const mutating = ["POST", "PUT", "PATCH", "DELETE"].includes(method);
  let response: Response | undefined;
  let payload: unknown = null;

  for (let attempt = 0; attempt < (mutating ? 2 : 1); attempt += 1) {
    const headers = new Headers(baseHeaders);
    if (mutating) {
      const csrf = await getCsrfCredentials(attempt > 0);
      headers.set(csrf.headerName, csrf.token);
    }

    try {
      response = await fetch(`${API_BASE_URL}${path}`, {
        ...options,
        headers,
        credentials: "include"
      });
    } catch (error) {
      if (
        error instanceof DOMException &&
        error.name === "AbortError"
      ) {
        throw error;
      }
      throw new ApiError(
        "We could not reach the service. Check your connection and try again.",
        0
      );
    }

    const contentType = response.headers.get("content-type") ?? "";
    payload = contentType.includes("json")
      ? await response.json()
      : null;

    // A 403 on an unsafe request can mean the session-bound token expired.
    // Spring rejects it before invoking business logic, so one refresh/retry
    // is safe. A genuine authorization failure is returned after that retry.
    if (!(mutating && response.status === 403 && attempt === 0)) break;
    inMemoryCsrf = null;
  }

  if (!response) {
    throw new ApiError("The service did not return a response.", 0);
  }

  if (!response.ok) {
    const problem = (payload ?? {}) as ProblemDetails;
    throw new ApiError(
      problem.detail ??
        problem.title ??
        "Something went wrong. Please try again.",
      response.status,
      problem
    );
  }

  if (response.status === 204 || payload === null) {
    return undefined as T;
  }

  return isEnvelope<T>(payload) ? payload.data : (payload as T);
}

export function resetCsrfTokenForTests(): void {
  if (process.env.NODE_ENV === "test") inMemoryCsrf = null;
}

export function getGoogleOAuthUrl(): string {
  const path =
    process.env.NEXT_PUBLIC_GOOGLE_OAUTH_PATH ??
    "/oauth2/authorization/google";
  return `${API_BASE_URL}${path.startsWith("/") ? path : `/${path}`}`;
}

export const authApi = {
  me: (signal?: AbortSignal) =>
    apiRequest<AuthUser>("/api/v1/auth/me", { signal }),
  logout: () => apiRequest<void>("/api/v1/auth/logout", { method: "POST" })
};

export const profileApi = {
  get: (signal?: AbortSignal) =>
    apiRequest<UserProfile>("/api/v1/profile", { signal }),
  update: (payload: {
    displayName: string;
    dateOfBirth?: string;
    contactNumber?: string;
    bloodGroup?: string;
    address?: string;
    employeeId?: string;
    organizationName?: string;
  }) =>
    apiRequest<UserProfile>("/api/v1/profile", {
      method: "PATCH",
      body: JSON.stringify(payload)
    })
};

export const doctorsApi = {
  list: (params: DoctorSearchParams = {}, signal?: AbortSignal) =>
    apiRequest<PageResponse<Doctor>>(
      `/api/v1/doctors${queryString(params)}`,
      { signal }
    ),
  detail: (id: string | number, signal?: AbortSignal) =>
    apiRequest<Doctor>(`/api/v1/doctors/${id}`, { signal }),
  slots: (id: string | number, date: string, signal?: AbortSignal) =>
    apiRequest<DoctorSlot[]>(
      `/api/v1/doctors/${id}/slots${queryString({ date })}`,
      { signal }
    )
};

export const hospitalsApi = {
  list: (params: HospitalSearchParams = {}, signal?: AbortSignal) =>
    apiRequest<PageResponse<Hospital>>(
      `/api/v1/hospitals${queryString(params)}`,
      { signal }
    ),
  detail: (id: string | number, signal?: AbortSignal) =>
    apiRequest<Hospital>(`/api/v1/hospitals/${id}`, { signal }),
  emergencyAvailability: (
    params: {
      query?: string;
      wardType: WardType;
      page?: number;
      size?: number;
    },
    signal?: AbortSignal
  ) =>
    apiRequest<PageResponse<Hospital>>(
      `/api/v1/hospitals/emergency-availability${queryString(params)}`,
      { signal }
    )
};

export const appointmentsApi = {
  list: (params: AppointmentSearchParams = {}, signal?: AbortSignal) =>
    apiRequest<PageResponse<Appointment>>(
      `/api/v1/appointments${queryString(params)}`,
      { signal }
    ),
  create: (payload: { doctorId: string | number; startsAt: string }) =>
    apiRequest<Appointment>("/api/v1/appointments", {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  cancel: (id: string | number, reason: string) =>
    apiRequest<Appointment>(`/api/v1/appointments/${id}/cancel`, {
      method: "PATCH",
      body: JSON.stringify({ reason })
    })
};

export const ambulancesApi = {
  list: (
    params: PageParams & {
      query?: string;
      status?: string;
      hospitalId?: string | number;
    } = {},
    signal?: AbortSignal
  ) =>
    apiRequest<PageResponse<Ambulance>>(
      `/api/v1/ambulances${queryString(params)}`,
      { signal }
    ),
  request: (payload: {
    hospitalId: string | number;
    pickupAddress: string;
    contactNumber: string;
  }) =>
    apiRequest<AmbulanceRequest>("/api/v1/ambulance-requests", {
      method: "POST",
      body: JSON.stringify(payload)
    }),
  requests: (params: PageParams = {}, signal?: AbortSignal) =>
    apiRequest<PageResponse<AmbulanceRequest>>(
      `/api/v1/ambulance-requests${queryString(params)}`,
      { signal }
    )
};

export const representativeApi = {
  dashboard: (signal?: AbortSignal) =>
    apiRequest<RepresentativeDashboard>(
      "/api/v1/representative/dashboard",
      { signal }
    ),
  doctors: (params: DoctorSearchParams = {}, signal?: AbortSignal) =>
    apiRequest<PageResponse<Doctor>>(
      `/api/v1/representative/doctors${queryString(params)}`,
      { signal }
    ),
  updateDoctorAvailability: (
    id: string | number,
    availabilityStatus: AvailabilityStatus
  ) =>
    apiRequest<Doctor>(
      `/api/v1/representative/doctors/${id}/availability`,
      {
        method: "PATCH",
        body: JSON.stringify({ availabilityStatus })
      }
    ),
  beds: (
    params: PageParams & {
      hospitalId?: string | number;
      status?: BedStatus;
      wardType?: WardType;
    } = {},
    signal?: AbortSignal
  ) =>
    apiRequest<PageResponse<Bed>>(
      `/api/v1/representative/beds${queryString(params)}`,
      { signal }
    ),
  updateBedStatus: (id: string | number, status: BedStatus) =>
    apiRequest<Bed>(`/api/v1/representative/beds/${id}/status`, {
      method: "PATCH",
      body: JSON.stringify({ status })
    }),
  appointments: (
    params: PageParams & {
      hospitalId?: string | number;
      status?: string;
      dateFrom?: string;
      dateTo?: string;
      startsAfter?: string;
    } = {},
    signal?: AbortSignal
  ) =>
    apiRequest<PageResponse<Appointment>>(
      `/api/v1/representative/appointments${queryString(params)}`,
      { signal }
    ),
  cancelAppointment: (id: string | number, reason: string) =>
    apiRequest<Appointment>(
      `/api/v1/representative/appointments/${id}/cancel`,
      {
        method: "PATCH",
        body: JSON.stringify({ reason })
      }
    )
};
