import {
  forwardRef,
  type InputHTMLAttributes,
  type SelectHTMLAttributes,
  type TextareaHTMLAttributes
} from "react";
import { cn } from "@/lib/utils";

interface FieldShellProps {
  label: string;
  htmlFor: string;
  hint?: string;
  error?: string;
  optional?: boolean;
  children: React.ReactNode;
}

export function FieldShell({
  label,
  htmlFor,
  hint,
  error,
  optional,
  children
}: FieldShellProps) {
  return (
    <div className="space-y-2">
      <div className="flex items-baseline justify-between gap-3">
        <label
          htmlFor={htmlFor}
          className="text-sm font-semibold text-ink-800"
        >
          {label}
        </label>
        {optional && <span className="text-xs text-slate-400">Optional</span>}
      </div>
      {children}
      {(error || hint) && (
        <p
          id={`${htmlFor}-message`}
          className={cn(
            "text-xs leading-5",
            error ? "text-red-600" : "text-slate-500"
          )}
        >
          {error ?? hint}
        </p>
      )}
    </div>
  );
}

export const Input = forwardRef<
  HTMLInputElement,
  InputHTMLAttributes<HTMLInputElement> & { invalid?: boolean }
>(({ className, invalid, ...props }, ref) => (
  <input
    ref={ref}
    className={cn(
      "h-11 w-full rounded-xl border bg-white px-3.5 text-sm text-ink-950 shadow-sm outline-none transition placeholder:text-slate-400 hover:border-slate-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10",
      invalid && "border-red-400 focus:border-red-500 focus:ring-red-500/10",
      className
    )}
    aria-invalid={invalid || undefined}
    {...props}
  />
));
Input.displayName = "Input";

export const Select = forwardRef<
  HTMLSelectElement,
  SelectHTMLAttributes<HTMLSelectElement> & { invalid?: boolean }
>(({ className, invalid, ...props }, ref) => (
  <select
    ref={ref}
    className={cn(
      "h-11 w-full rounded-xl border bg-white px-3.5 text-sm text-ink-950 shadow-sm outline-none transition hover:border-slate-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10",
      invalid && "border-red-400 focus:border-red-500 focus:ring-red-500/10",
      className
    )}
    aria-invalid={invalid || undefined}
    {...props}
  />
));
Select.displayName = "Select";

export const Textarea = forwardRef<
  HTMLTextAreaElement,
  TextareaHTMLAttributes<HTMLTextAreaElement> & { invalid?: boolean }
>(({ className, invalid, ...props }, ref) => (
  <textarea
    ref={ref}
    className={cn(
      "min-h-24 w-full resize-y rounded-xl border bg-white px-3.5 py-3 text-sm text-ink-950 shadow-sm outline-none transition placeholder:text-slate-400 hover:border-slate-400 focus:border-brand-500 focus:ring-4 focus:ring-brand-500/10",
      invalid && "border-red-400 focus:border-red-500 focus:ring-red-500/10",
      className
    )}
    aria-invalid={invalid || undefined}
    {...props}
  />
));
Textarea.displayName = "Textarea";
