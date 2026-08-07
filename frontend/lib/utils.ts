export function cn(
  ...classes: Array<string | false | null | undefined>
): string {
  return classes.filter(Boolean).join(" ");
}

export function initials(name?: string | null): string {
  if (!name) return "MA";
  return name
    .trim()
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part.charAt(0).toUpperCase())
    .join("");
}

export function formatDateTime(
  value?: string | null,
  options?: Intl.DateTimeFormatOptions
): string {
  if (!value) return "Not available";
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "Not available";

  return new Intl.DateTimeFormat("en-IN", {
    day: "numeric",
    month: "short",
    year: "numeric",
    hour: "numeric",
    minute: "2-digit",
    ...options
  }).format(date);
}

export function formatDate(value?: string | null): string {
  return formatDateTime(value, {
    day: "numeric",
    month: "long",
    year: "numeric"
  });
}

export function formatCurrency(value?: number | null): string {
  if (value === undefined || value === null) return "Fee not listed";
  return new Intl.NumberFormat("en-IN", {
    style: "currency",
    currency: "INR",
    maximumFractionDigits: 0
  }).format(value);
}

export function formatTime(value?: string | null): string {
  if (!value) return "Not listed";
  const match = value.match(/^(\d{2}):(\d{2})/);
  if (!match) return value;
  const date = new Date();
  date.setHours(Number(match[1]), Number(match[2]), 0, 0);
  return new Intl.DateTimeFormat("en-IN", {
    hour: "numeric",
    minute: "2-digit"
  }).format(date);
}

export function humanize(value?: string | null): string {
  if (!value) return "Not available";
  return value
    .toLowerCase()
    .replaceAll("_", " ")
    .replace(/\b\w/g, (char) => char.toUpperCase());
}

export function todayInputValue(): string {
  return localDateInputValue(new Date());
}

export function yesterdayInputValue(): string {
  const yesterday = new Date();
  yesterday.setDate(yesterday.getDate() - 1);
  return localDateInputValue(yesterday);
}

function localDateInputValue(date: Date): string {
  const offset = date.getTimezoneOffset();
  return new Date(date.getTime() - offset * 60_000)
    .toISOString()
    .slice(0, 10);
}

export function toOffsetDateTime(date: string, time: string): string {
  const local = new Date(`${date}T${time}:00`);
  return local.toISOString();
}
