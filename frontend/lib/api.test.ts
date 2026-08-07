import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { apiRequest, resetCsrfTokenForTests } from "@/lib/api";

describe("apiRequest CSRF protection", () => {
  beforeEach(() => {
    resetCsrfTokenForTests();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("fetches and sends the session-bound CSRF token with mutations", async () => {
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            success: true,
            data: {
              headerName: "X-CSRF-TOKEN",
              parameterName: "_csrf",
              token: "session-token"
            }
          }),
          {
            status: 200,
            headers: { "content-type": "application/json" }
          }
        )
      )
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({ success: true, data: { saved: true } }),
          {
            status: 200,
            headers: { "content-type": "application/json" }
          }
        )
      );

    await apiRequest<{ saved: boolean }>("/api/v1/profile", {
      method: "PATCH",
      body: JSON.stringify({ displayName: "Priya Sharma" })
    });

    expect(fetchMock).toHaveBeenCalledTimes(2);
    expect(fetchMock.mock.calls[0][0]).toContain("/api/v1/auth/csrf");
    const request = fetchMock.mock.calls[1];
    const headers = new Headers((request[1] as RequestInit).headers);
    expect(headers.get("X-CSRF-TOKEN")).toBe("session-token");
    expect((request[1] as RequestInit).credentials).toBe("include");
  });

  it("does not add the CSRF header to safe GET requests", async () => {
    const fetchMock = vi.spyOn(globalThis, "fetch").mockResolvedValue(
      new Response(JSON.stringify({ success: true, data: { id: "1" } }), {
        status: 200,
        headers: { "content-type": "application/json" }
      })
    );

    await apiRequest("/api/v1/auth/me");

    const headers = new Headers(
      (fetchMock.mock.calls[0][1] as RequestInit).headers
    );
    expect(headers.has("X-CSRF-TOKEN")).toBe(false);
  });

  it("refreshes an expired CSRF token once after a 403", async () => {
    const csrfResponse = (token: string) =>
      new Response(
        JSON.stringify({
          success: true,
          data: {
            headerName: "X-CSRF-TOKEN",
            parameterName: "_csrf",
            token
          }
        }),
        {
          status: 200,
          headers: { "content-type": "application/json" }
        }
      );
    const fetchMock = vi
      .spyOn(globalThis, "fetch")
      .mockResolvedValueOnce(csrfResponse("expired-token"))
      .mockResolvedValueOnce(
        new Response(
          JSON.stringify({
            title: "Forbidden",
            status: 403,
            detail: "Invalid CSRF token"
          }),
          {
            status: 403,
            headers: { "content-type": "application/problem+json" }
          }
        )
      )
      .mockResolvedValueOnce(csrfResponse("fresh-token"))
      .mockResolvedValueOnce(
        new Response(JSON.stringify({ success: true, data: { saved: true } }), {
          status: 200,
          headers: { "content-type": "application/json" }
        })
      );

    await apiRequest("/api/v1/profile", {
      method: "PATCH",
      body: JSON.stringify({ displayName: "Priya Sharma" })
    });

    expect(fetchMock).toHaveBeenCalledTimes(4);
    const retryHeaders = new Headers(
      (fetchMock.mock.calls[3][1] as RequestInit).headers
    );
    expect(retryHeaders.get("X-CSRF-TOKEN")).toBe("fresh-token");
  });
});
