import type { Metadata, Viewport } from "next";
import "@/app/globals.css";
import { Providers } from "@/components/providers";

const appUrl = process.env.NEXT_PUBLIC_APP_URL ?? "http://localhost:3000";

export const metadata: Metadata = {
  metadataBase: new URL(appUrl),
  title: {
    default: "VitaNova Connect — Care, coordinated",
    template: "%s | VitaNova Connect"
  },
  description:
    "Find doctors, hospital beds and ambulance support, and manage care operations from one secure platform.",
  applicationName: "VitaNova Connect",
  keywords: [
    "medical aid",
    "doctor appointment",
    "hospital bed availability",
    "ambulance"
  ],
  alternates: {
    canonical: "/"
  },
  openGraph: {
    type: "website",
    url: "/",
    siteName: "VitaNova Connect",
    title: "VitaNova Connect — Care, coordinated",
    description:
      "Find doctors, hospital beds and ambulance support, and manage care operations from one secure platform.",
    images: [
      {
        url: "/og.png",
        width: 1731,
        height: 909,
        alt: "VitaNova Connect — Care, coordinated."
      }
    ]
  },
  twitter: {
    card: "summary_large_image",
    title: "VitaNova Connect — Care, coordinated",
    description:
      "Find doctors, hospital beds and ambulance support in one secure platform.",
    images: [
      {
        url: "/og.png",
        alt: "VitaNova Connect — Care, coordinated."
      }
    ]
  }
};

export const viewport: Viewport = {
  width: "device-width",
  initialScale: 1,
  themeColor: "#128878"
};

export default function RootLayout({
  children
}: Readonly<{ children: React.ReactNode }>) {
  return (
    <html lang="en">
      <body>
        <a
          href="#main-content"
          className="fixed left-4 top-3 z-[100] -translate-y-20 rounded-xl bg-ink-950 px-4 py-2 text-sm font-semibold text-white transition-transform focus:translate-y-0"
        >
          Skip to content
        </a>
        <Providers>{children}</Providers>
      </body>
    </html>
  );
}
