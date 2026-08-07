"use client";

import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { BedDouble, Edit3 } from "lucide-react";
import { PageHeader } from "@/components/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Select } from "@/components/ui/form-controls";
import { StatusBadge, Badge } from "@/components/ui/badge";
import { EmptyState, ErrorState, PageLoading } from "@/components/ui/states";
import { Pagination } from "@/components/ui/pagination";
import { Modal } from "@/components/ui/modal";
import { UpdateBedForm } from "@/features/representative/update-bed-form";
import { representativeApi } from "@/lib/api";
import { queryKeys } from "@/lib/query-keys";
import type { Bed, BedStatus, WardType } from "@/types/api";

export default function RepresentativeBedsPage() {
  const [status, setStatus] = useState<BedStatus | "">("");
  const [wardType, setWardType] = useState<WardType | "">("");
  const [page, setPage] = useState(0);
  const [selected, setSelected] = useState<Bed | null>(null);
  const filters = {
    status: status || undefined,
    wardType: wardType || undefined,
    page,
    size: 20
  };
  const query = useQuery({
    queryKey: queryKeys.representativeBeds(filters),
    queryFn: ({ signal }) => representativeApi.beds(filters, signal)
  });

  return (
    <>
      <PageHeader
        eyebrow="Ward capacity"
        title="Bed status"
        description="Keep bed records current so reported hospital availability remains useful."
      />
      <Card className="mb-6">
        <CardContent>
          <div className="grid gap-3 sm:grid-cols-2">
            <Select
              aria-label="Filter by ward type"
              value={wardType}
              onChange={(event) => {
                setWardType(event.target.value as WardType | "");
                setPage(0);
              }}
            >
              <option value="">All wards</option>
              <option value="ICU">ICU</option>
              <option value="GENERAL">General</option>
            </Select>
            <Select
              aria-label="Filter by bed status"
              value={status}
              onChange={(event) => {
                setStatus(event.target.value as BedStatus | "");
                setPage(0);
              }}
            >
              <option value="">All statuses</option>
              <option value="AVAILABLE">Available</option>
              <option value="OCCUPIED">Occupied</option>
              <option value="MAINTENANCE">Maintenance</option>
            </Select>
          </div>
        </CardContent>
      </Card>

      {query.isLoading && <PageLoading label="Loading bed records…" />}
      {query.isError && (
        <ErrorState
          title="Bed records could not be loaded"
          message={query.error.message}
          onRetry={() => query.refetch()}
        />
      )}
      {query.isSuccess && query.data.content.length === 0 && (
        <EmptyState
          title="No beds match this view"
          message="Try a different ward, status, or search."
          icon={BedDouble}
        />
      )}
      {query.isSuccess && query.data.content.length > 0 && (
        <Card className="overflow-hidden">
          <div className="overflow-x-auto">
            <table className="data-table">
              <thead>
                <tr>
                  <th scope="col">Bed</th>
                  <th scope="col">Ward</th>
                  <th scope="col">Hospital</th>
                  <th scope="col">Status</th>
                  <th scope="col">
                    <span className="sr-only">Actions</span>
                  </th>
                </tr>
              </thead>
              <tbody>
                {query.data.content.map((bed) => (
                  <tr key={bed.id}>
                    <td className="font-bold text-ink-950">
                      {bed.bedCode}
                    </td>
                    <td>
                      <Badge tone={bed.wardType === "ICU" ? "info" : "neutral"}>
                        {bed.wardType}
                      </Badge>
                    </td>
                    <td className="text-slate-600">{bed.hospital.name}</td>
                    <td>
                      <StatusBadge status={bed.status} />
                    </td>
                    <td className="text-right">
                      <Button
                        type="button"
                        variant="outline"
                        size="sm"
                        onClick={() => setSelected(bed)}
                      >
                        <Edit3 className="size-4" aria-hidden="true" />
                        Update
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div className="px-5 pb-5">
            <Pagination
              page={query.data.page}
              totalPages={query.data.totalPages}
              onPageChange={setPage}
            />
          </div>
        </Card>
      )}

      <Modal
        open={Boolean(selected)}
        onClose={() => setSelected(null)}
        title="Update bed status"
        description="This change will affect the availability shown to patients."
      >
        {selected && (
          <UpdateBedForm bed={selected} onSuccess={() => setSelected(null)} />
        )}
      </Modal>
    </>
  );
}
