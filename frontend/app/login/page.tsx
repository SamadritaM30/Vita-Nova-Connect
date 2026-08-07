import { Suspense } from "react";
import { PageLoading } from "@/components/ui/states";
import { LoginPanel } from "@/features/auth/login-panel";

export const metadata = {
  title: "Sign in"
};

export default function LoginPage() {
  return (
    <Suspense
      fallback={
        <main className="grid min-h-screen place-items-center p-5">
          <div className="w-full max-w-md">
            <PageLoading label="Preparing secure sign in…" />
          </div>
        </main>
      }
    >
      <LoginPanel />
    </Suspense>
  );
}
