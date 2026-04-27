import { request } from "@/services/api";
import type {
  EnrollmentDashboard,
  EnrollmentFollowUp,
  EnrollmentFollowUpPayload,
  EnrollmentLead,
  EnrollmentLeadPayload,
  EnrollmentLeadQuery,
  PageResult
} from "@/types";

function toSearchParams(query: EnrollmentLeadQuery) {
  const params = new URLSearchParams();
  if (query.keyword) params.set("keyword", query.keyword);
  if (query.status) params.set("status", query.status);
  if (query.source) params.set("source", query.source);
  if (query.ownerUserId != null) {
    params.set("ownerUserId", String(query.ownerUserId));
  }
  if (query.startDate) params.set("startDate", query.startDate);
  if (query.endDate) params.set("endDate", query.endDate);
  if (query.page) params.set("page", String(query.page));
  if (query.pageSize) params.set("pageSize", String(query.pageSize));
  return params.toString();
}

export function getEnrollmentLeads(query: EnrollmentLeadQuery) {
  const qs = toSearchParams(query);
  return request<PageResult<EnrollmentLead>>(
    `/enrollment/leads${qs ? `?${qs}` : ""}`
  );
}

export function getEnrollmentLead(id: number) {
  return request<EnrollmentLead>(`/enrollment/leads/${id}`);
}

export function createEnrollmentLead(payload: EnrollmentLeadPayload) {
  return request<EnrollmentLead>("/enrollment/leads", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function updateEnrollmentLead(
  id: number,
  payload: EnrollmentLeadPayload
) {
  return request<EnrollmentLead>(`/enrollment/leads/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

export function assignLeadOwner(id: number, ownerUserId: number) {
  return request<EnrollmentLead>(
    `/enrollment/leads/${id}/owner/${ownerUserId}`,
    {
      method: "PUT"
    }
  );
}

export function getLeadFollowUps(leadId: number) {
  return request<EnrollmentFollowUp[]>(
    `/enrollment/leads/${leadId}/follow-ups`
  );
}

export function createLeadFollowUp(
  leadId: number,
  payload: EnrollmentFollowUpPayload
) {
  return request<EnrollmentFollowUp>(`/enrollment/leads/${leadId}/follow-ups`, {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function getEnrollmentDashboard() {
  return request<EnrollmentDashboard>("/enrollment/dashboard");
}

export function getSignedStudents(query: EnrollmentLeadQuery) {
  const qs = toSearchParams(query);
  return request<PageResult<EnrollmentLead>>(
    `/enrollment/students${qs ? `?${qs}` : ""}`
  );
}
