"use client";

import { useEffect } from "react";
import { ErrorState } from "@/components/ui/states";

export default function GlobalError({
  error,
  reset
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error("Unhandled page error", error);
  }, [error]);

  return (
    <main id="main-content" className="grid min-h-screen place-items-center p-5">
      <div className="w-full max-w-xl">
        <ErrorState
          title="This page hit an unexpected problem"
          message="Your information is safe. Try loading the page again."
          onRetry={reset}
        />
      </div>
    </main>
  );
}
