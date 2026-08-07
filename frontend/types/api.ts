export type UserRole = "PATIENT" | "REPRESENTATIVE";
export type AvailabilityStatus = "AVAILABLE" | "UNAVAILABLE";
export type WardType = "ICU" | "GENERAL";
export type BedStatus = "AVAILABLE" | "OCCUPIED" | "MAINTENANCE";
export type AppointmentStatus = "BOOKED" | "COMPLETED" | "CANCELLED";
export type AmbulanceStatus = "AVAILABLE" | "UNAVAILABLE" | "ON_DUTY";
export type AmbulanceRequestStatus =
  | "PENDING"
  | "ASSIGNED"
  | "COMPLETED"
  | "CANCELLED";

export interface ApiEnvelope<T> {
  success: boolean;
  data: T;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
}

export interface AuthUser {
  id: string;
  email: string;
  displayName: string;
  avatarUrl?: string | null;
  role: UserRole;
  profileCompleted?: boolean;
  status?: "ACTIVE" | "SUSPENDED";
}

export interface UserProfile {
  userId: string;
  email: string;
  displayName: string;
  avatarUrl?: string | null;
  role: UserRole;
  dateOfBirth?: string | null;
  contactNumber?: string | null;
  bloodGroup?: string | null;
  address?: string | null;
  employeeId?: string | null;
  organizationName?: string | null;
  assignedHospitals: Array<{ id: number; name: string }>;
  completed: boolean;
}

export interface HospitalSummary {
  id: number;
  name: string;
  address?: string | null;
  contactNumber?: string | null;
}

export interface Hospital extends HospitalSummary {
  availableIcuBeds: number;
  availableGeneralBeds: number;
  availableDoctors: number;
}

export interface Doctor {
  id: number;
  name: string;
  specialization: string;
  availabilityStatus: AvailabilityStatus;
  consultationFrom?: string | null;
  consultationTo?: string | null;
  hospital: HospitalSummary;
}

export interface DoctorSummary {
  id: number;
  name: string;
  specialization: string;
}

export interface Appointment {
  id: string;
  doctor: DoctorSummary;
  patient?: {
    id: string;
    name: string;
    contactNumber?: string | null;
  };
  hospital: HospitalSummary;
  startsAt: string;
  endsAt?: string | null;
  status: AppointmentStatus;
  cancellationReason?: string | null;
  createdAt?: string;
}

export interface Bed {
  id: number;
  bedCode: string;
  wardType: WardType;
  status: BedStatus;
  hospital: HospitalSummary;
}

export interface Ambulance {
  id: number;
  vehicleNumber: string;
  status: AmbulanceStatus;
  contactNumber?: string | null;
  driverName?: string | null;
  lastLocation?: string | null;
  hospital: HospitalSummary;
}

export interface AmbulanceRequest {
  id: string | number;
  hospital: HospitalSummary;
  ambulance?: Ambulance | null;
  pickupAddress: string;
  contactNumber: string;
  status: AmbulanceRequestStatus;
  requestedAt: string;
}

export interface DoctorSlot {
  startsAt: string;
  endsAt?: string;
  available?: boolean;
}

export interface RepresentativeDashboard {
  totalDoctors: number;
  availableDoctors: number;
  totalBeds: number;
  availableBeds: number;
  availableIcuBeds: number;
  availableGeneralBeds: number;
  todayAppointments: number;
  assignedHospitals: HospitalSummary[];
}

export interface FieldError {
  field: string;
  message: string;
}

export interface ProblemDetails {
  type?: string;
  title?: string;
  status?: number;
  detail?: string;
  instance?: string;
  code?: string;
  timestamp?: string;
  fieldErrors?: FieldError[] | Record<string, string>;
}

export interface PageParams {
  page?: number;
  size?: number;
  sort?: string;
}

export interface AppointmentSearchParams extends PageParams {
  status?: AppointmentStatus;
  dateFrom?: string;
  dateTo?: string;
  startsAfter?: string;
}

export interface DoctorSearchParams extends PageParams {
  query?: string;
  specialization?: string;
  hospitalId?: string | number;
  availabilityStatus?: AvailabilityStatus | "";
  wardType?: WardType | "";
}

export interface HospitalSearchParams extends PageParams {
  query?: string;
}
