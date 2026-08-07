import Link from "next/link";
import {
  Activity,
  Ambulance,
  ArrowRight,
  BedDouble,
  CalendarCheck2,
  Check,
  Clock3,
  HeartHandshake,
  LockKeyhole,
  MapPin,
  Search,
  ShieldCheck,
  Stethoscope,
  UsersRound
} from "lucide-react";
import { Brand } from "@/components/brand";
import { DemoNotice } from "@/components/demo-notice";
import { buttonClass } from "@/components/ui/button";

const features = [
  {
    icon: Stethoscope,
    title: "Find the right doctor",
    description:
      "Search by specialization and hospital, then review current availability before booking."
  },
  {
    icon: BedDouble,
    title: "Check bed availability",
    description:
      "Compare sample ICU and general ward capacity across listed hospitals."
  },
  {
    icon: Ambulance,
    title: "Request transport support",
    description:
      "Discover ambulance services linked to hospitals and send a clear pickup request."
  }
];

const steps = [
  {
    number: "01",
    title: "Sign in securely",
    description: "Use your Google account—there is no additional password to remember."
  },
  {
    number: "02",
    title: "Find available care",
    description:
      "Filter doctors, beds, and ambulance services using one straightforward workspace."
  },
  {
    number: "03",
    title: "Stay organised",
    description:
      "Book appointments, review details, and manage changes from your personal dashboard."
  }
];

export default function LandingPage() {
  return (
    <div className="min-h-screen overflow-hidden bg-[#fbfdfc]">
      <header className="relative z-30 border-b border-slate-200/70 bg-white/85 backdrop-blur-xl">
        <div className="page-container flex h-20 items-center">
          <Brand />
          <nav
            className="ml-auto hidden items-center gap-8 md:flex"
            aria-label="Primary navigation"
          >
            <a
              href="#features"
              className="text-sm font-semibold text-slate-600 transition hover:text-brand-700"
            >
              Features
            </a>
            <a
              href="#how-it-works"
              className="text-sm font-semibold text-slate-600 transition hover:text-brand-700"
            >
              How it works
            </a>
            <a
              href="#for-hospitals"
              className="text-sm font-semibold text-slate-600 transition hover:text-brand-700"
            >
              For hospitals
            </a>
          </nav>
          <Link
            href="/login"
            className={buttonClass({
              size: "sm",
              className: "ml-auto md:ml-8"
            })}
          >
            Sign in
            <ArrowRight className="size-4" aria-hidden="true" />
          </Link>
        </div>
      </header>
      <DemoNotice />

      <main id="main-content">
        <section className="relative">
          <div
            className="absolute inset-x-0 top-0 -z-0 h-[34rem] bg-[radial-gradient(circle_at_72%_18%,rgba(123,222,202,0.34),transparent_38%),radial-gradient(circle_at_20%_28%,rgba(219,234,254,0.6),transparent_32%)]"
            aria-hidden="true"
          />
          <div className="page-container relative z-10 grid min-h-[calc(100vh-5rem)] items-center gap-12 py-16 lg:grid-cols-[1.05fr_0.95fr] lg:py-20">
            <div className="max-w-2xl animate-fade-up">
              <span className="inline-flex items-center gap-2 rounded-full border border-brand-200 bg-white/80 px-3 py-1.5 text-xs font-bold text-brand-800 shadow-sm">
                <span className="relative flex size-2">
                  <span className="absolute inline-flex size-full animate-ping rounded-full bg-brand-400 opacity-50" />
                  <span className="relative inline-flex size-2 rounded-full bg-brand-600" />
                </span>
                One connected healthcare workspace
              </span>
              <h1 className="text-balance mt-7 text-5xl font-bold leading-[1.05] tracking-[-0.055em] text-ink-950 sm:text-6xl lg:text-7xl">
                The path to care,
                <span className="block text-brand-700">made clearer.</span>
              </h1>
              <p className="mt-7 max-w-xl text-lg leading-8 text-ink-600">
                Find doctors, review reported hospital bed availability, request
                ambulance support, and keep appointments organised—all in one
                secure place.
              </p>
              <div className="mt-9 flex flex-col gap-3 sm:flex-row">
                <Link
                  href="/login"
                  className={buttonClass({ size: "lg" })}
                >
                  Find care
                  <ArrowRight className="size-5" aria-hidden="true" />
                </Link>
                <Link
                  href="/login?for=representative"
                  className={buttonClass({ variant: "outline", size: "lg" })}
                >
                  Hospital representative
                </Link>
              </div>
              <div className="mt-8 flex flex-wrap gap-x-6 gap-y-3 text-sm text-slate-500">
                {["Google secure sign-in", "Role-protected access", "Mobile friendly"].map(
                  (item) => (
                    <span key={item} className="inline-flex items-center gap-2">
                      <Check
                        className="size-4 text-brand-600"
                        strokeWidth={2.5}
                        aria-hidden="true"
                      />
                      {item}
                    </span>
                  )
                )}
              </div>
            </div>

            <div
              className="relative mx-auto w-full max-w-xl lg:ml-auto"
              aria-label="VitaNova Connect dashboard preview"
            >
              <div
                className="absolute -left-6 top-24 size-24 rounded-full bg-coral-200/65 blur-2xl"
                aria-hidden="true"
              />
              <div className="relative rounded-[2rem] border border-white/90 bg-white/85 p-3 shadow-[0_30px_80px_rgba(16,42,44,0.16)] backdrop-blur-xl sm:p-5">
                <div className="rounded-[1.4rem] border bg-[#f7faf9] p-4 sm:p-6">
                  <div className="mb-6 flex items-center justify-between">
                    <div>
                      <p className="text-xs font-semibold text-slate-400">
                        Good morning, Priya
                      </p>
                      <p className="mt-1 text-lg font-bold text-ink-950">
                        How can we help today?
                      </p>
                    </div>
                    <span className="grid size-10 place-items-center rounded-2xl bg-brand-100 font-bold text-brand-800">
                      PS
                    </span>
                  </div>
                  <div className="grid grid-cols-3 gap-2 sm:gap-3">
                    {[
                      { icon: Search, label: "Find doctor", tone: "bg-brand-100 text-brand-800" },
                      { icon: BedDouble, label: "Check beds", tone: "bg-blue-100 text-blue-800" },
                      { icon: Ambulance, label: "Ambulance", tone: "bg-coral-100 text-coral-600" }
                    ].map(({ icon: Icon, label, tone }) => (
                      <div
                        key={label}
                        className="rounded-2xl border bg-white p-3 shadow-sm sm:p-4"
                      >
                        <span className={`grid size-9 place-items-center rounded-xl ${tone}`}>
                          <Icon className="size-4" aria-hidden="true" />
                        </span>
                        <p className="mt-3 text-[11px] font-bold text-ink-800 sm:text-xs">
                          {label}
                        </p>
                      </div>
                    ))}
                  </div>
                  <div className="mt-5 rounded-2xl border bg-white p-4 shadow-sm">
                    <div className="flex items-center justify-between">
                      <div className="flex items-center gap-2">
                        <CalendarCheck2
                          className="size-4 text-brand-700"
                          aria-hidden="true"
                        />
                        <p className="text-xs font-bold text-ink-950">
                          Next appointment
                        </p>
                      </div>
                      <span className="rounded-full bg-emerald-50 px-2 py-1 text-[10px] font-bold text-emerald-700">
                        Confirmed
                      </span>
                    </div>
                    <div className="mt-4 flex items-center gap-3">
                      <span className="grid size-11 place-items-center rounded-2xl bg-slate-100 text-ink-600">
                        <Stethoscope className="size-5" aria-hidden="true" />
                      </span>
                      <div className="min-w-0">
                        <p className="text-sm font-bold text-ink-950">
                          Dr. Arjun Sharma
                        </p>
                        <p className="mt-0.5 text-xs text-slate-500">
                          Cardiologist · Brahmaputra City Hospital
                        </p>
                      </div>
                    </div>
                    <div className="mt-4 flex gap-4 border-t pt-3 text-[11px] font-medium text-slate-500">
                      <span className="inline-flex items-center gap-1.5">
                        <Clock3 className="size-3.5" aria-hidden="true" />
                        28 Jul, 10:00 AM
                      </span>
                      <span className="inline-flex items-center gap-1.5">
                        <MapPin className="size-3.5" aria-hidden="true" />
                        Bhangagarh, Guwahati
                      </span>
                    </div>
                  </div>
                </div>
              </div>
              <div className="absolute -bottom-5 -left-3 hidden items-center gap-3 rounded-2xl border bg-white px-4 py-3 shadow-soft sm:flex">
                <span className="grid size-9 place-items-center rounded-xl bg-emerald-50 text-emerald-700">
                  <ShieldCheck className="size-4" aria-hidden="true" />
                </span>
                <div>
                  <p className="text-xs font-bold text-ink-950">Secure by design</p>
                  <p className="text-[10px] text-slate-500">Protected role-based access</p>
                </div>
              </div>
            </div>
          </div>
        </section>

        <section id="features" className="border-y bg-white py-20 sm:py-24">
          <div className="page-container">
            <div className="mx-auto max-w-2xl text-center">
              <p className="text-xs font-bold uppercase tracking-[0.18em] text-brand-700">
                Care discovery, simplified
              </p>
              <h2 className="text-balance mt-3 text-3xl font-bold tracking-[-0.04em] text-ink-950 sm:text-4xl">
                The essentials you need, without the noise
              </h2>
              <p className="mt-4 leading-7 text-slate-500">
                Clear information and focused workflows help patients take the
                next sensible step.
              </p>
            </div>
            <div className="mt-12 grid gap-5 lg:grid-cols-3">
              {features.map(({ icon: Icon, title, description }, index) => (
                <article
                  key={title}
                  className="group rounded-3xl border bg-white p-7 shadow-card transition duration-300 hover:-translate-y-1 hover:shadow-soft"
                >
                  <div className="flex items-center justify-between">
                    <span className="grid size-12 place-items-center rounded-2xl bg-brand-50 text-brand-700 transition group-hover:bg-brand-100">
                      <Icon className="size-5" aria-hidden="true" />
                    </span>
                    <span className="text-sm font-bold text-slate-200">
                      0{index + 1}
                    </span>
                  </div>
                  <h3 className="mt-7 text-xl font-bold tracking-tight text-ink-950">
                    {title}
                  </h3>
                  <p className="mt-3 text-sm leading-7 text-slate-500">
                    {description}
                  </p>
                </article>
              ))}
            </div>
          </div>
        </section>

        <section id="how-it-works" className="py-20 sm:py-28">
          <div className="page-container grid gap-14 lg:grid-cols-[0.82fr_1.18fr] lg:items-center">
            <div>
              <span className="inline-flex size-12 items-center justify-center rounded-2xl bg-brand-100 text-brand-800">
                <HeartHandshake className="size-5" aria-hidden="true" />
              </span>
              <p className="mt-6 text-xs font-bold uppercase tracking-[0.18em] text-brand-700">
                How it works
              </p>
              <h2 className="text-balance mt-3 text-3xl font-bold tracking-[-0.04em] text-ink-950 sm:text-4xl">
                From search to appointment in a few clear steps
              </h2>
              <p className="mt-5 max-w-lg leading-7 text-slate-500">
                VitaNova Connect keeps each decision focused and gives you a
                reliable record of your requests and appointments.
              </p>
            </div>
            <ol className="space-y-4">
              {steps.map((step) => (
                <li
                  key={step.number}
                  className="flex gap-5 rounded-3xl border bg-white p-5 shadow-card sm:p-6"
                >
                  <span className="grid size-11 shrink-0 place-items-center rounded-2xl bg-brand-50 text-sm font-bold text-brand-800">
                    {step.number}
                  </span>
                  <div>
                    <h3 className="font-bold text-ink-950">{step.title}</h3>
                    <p className="mt-1.5 text-sm leading-6 text-slate-500">
                      {step.description}
                    </p>
                  </div>
                </li>
              ))}
            </ol>
          </div>
        </section>

        <section id="for-hospitals" className="page-container pb-20 sm:pb-28">
          <div className="relative overflow-hidden rounded-[2.25rem] bg-ink-950 px-6 py-12 text-white sm:px-10 lg:px-14 lg:py-16">
            <div
              className="absolute -right-24 -top-24 size-80 rounded-full bg-brand-600/25 blur-3xl"
              aria-hidden="true"
            />
            <div className="relative grid gap-10 lg:grid-cols-[1fr_auto] lg:items-center">
              <div className="max-w-2xl">
                <span className="inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/10 px-3 py-1.5 text-xs font-bold text-brand-100">
                  <UsersRound className="size-4" aria-hidden="true" />
                  For hospital representatives
                </span>
                <h2 className="text-balance mt-5 text-3xl font-bold tracking-[-0.04em] sm:text-4xl">
                  Keep operational availability accurate and useful.
                </h2>
                <p className="mt-4 max-w-xl leading-7 text-slate-300">
                  Authorised teams can update doctor and bed status, review
                  appointments, and maintain a clear activity trail.
                </p>
              </div>
              <Link
                href="/login?for=representative"
                className={buttonClass({
                  variant: "secondary",
                  size: "lg",
                  className: "relative bg-white text-ink-950 hover:bg-brand-50"
                })}
              >
                Open representative portal
                <ArrowRight className="size-5" aria-hidden="true" />
              </Link>
            </div>
            <div className="relative mt-10 grid gap-3 border-t border-white/10 pt-7 sm:grid-cols-3">
              {[
                [Activity, "Availability updates"],
                [CalendarCheck2, "Appointment oversight"],
                [LockKeyhole, "Assigned-hospital access"]
              ].map(([Icon, label]) => {
                const ItemIcon = Icon as typeof Activity;
                return (
                  <div
                    key={label as string}
                    className="flex items-center gap-3 text-sm font-semibold text-slate-200"
                  >
                    <ItemIcon
                      className="size-4 text-brand-300"
                      aria-hidden="true"
                    />
                    {label as string}
                  </div>
                );
              })}
            </div>
          </div>
        </section>
      </main>

      <footer className="border-t bg-white">
        <div className="page-container flex flex-col gap-6 py-8 sm:flex-row sm:items-center sm:justify-between">
          <Brand />
        </div>
      </footer>
    </div>
  );
}
