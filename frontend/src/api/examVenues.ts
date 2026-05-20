import { request } from "@/services/api";
import type { ExamVenueDetail, ExamVenueRouteItem, ExamVenueSummary } from "@/types";

export function getExamVenues() {
  return request<ExamVenueSummary[]>("/public/exam-venues");
}

export function getExamVenueDetail(id: number) {
  return request<ExamVenueDetail>(`/public/exam-venues/${id}`);
}

export function updateExamVenueRoutes(id: number, routes: ExamVenueRouteItem[]) {
  return request<ExamVenueDetail>(`/admin/exam-venues/${id}/routes`, {
    method: "PUT",
    body: JSON.stringify({ routes })
  });
}
