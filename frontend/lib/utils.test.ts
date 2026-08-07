import { describe, expect, it } from "vitest";
import { formatCurrency, humanize, initials } from "@/lib/utils";

describe("display utilities", () => {
  it("creates safe two-letter initials", () => {
    expect(initials("Priya Sharma")).toBe("PS");
    expect(initials("Arjun")).toBe("A");
    expect(initials()).toBe("MA");
  });

  it("humanizes enum values", () => {
    expect(humanize("ON_DUTY")).toBe("On Duty");
  });

  it("formats Indian rupees", () => {
    expect(formatCurrency(1200)).toContain("1,200");
  });
});
