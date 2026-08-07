import { NextResponse, type NextRequest } from "next/server";

export function proxy(request: NextRequest) {
  const { pathname } = request.nextUrl;

  // Coarse route normalization only. Identity and roles are always verified
  // against /api/v1/auth/me in the client and again by the backend.
  if (pathname === "/patient") {
    return NextResponse.redirect(new URL("/patient/dashboard", request.url));
  }

  if (pathname === "/representative") {
    return NextResponse.redirect(
      new URL("/representative/dashboard", request.url)
    );
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/patient", "/representative"]
};
