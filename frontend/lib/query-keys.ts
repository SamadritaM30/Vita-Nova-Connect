export const queryKeys = {
  auth: ["auth", "me"] as const,
  profile: ["profile"] as const,
  doctors: (filters: object) => ["doctors", filters] as const,
  doctor: (id: string | number) => ["doctor", id] as const,
  slots: (id: string | number, date: string) =>
    ["doctor", id, "slots", date] as const,
  hospitals: (filters: object) => ["hospitals", filters] as const,
  emergency: (filters: object) => ["emergency", filters] as const,
  appointments: (filters: object = {}) => ["appointments", filters] as const,
  ambulances: (filters: object = {}) => ["ambulances", filters] as const,
  ambulanceRequests: (filters: object = {}) =>
    ["ambulance-requests", filters] as const,
  representativeDashboard: ["representative", "dashboard"] as const,
  representativeDoctors: (filters: object) =>
    ["representative", "doctors", filters] as const,
  representativeBeds: (filters: object) =>
    ["representative", "beds", filters] as const,
  representativeAppointments: (filters: object) =>
    ["representative", "appointments", filters] as const
};
