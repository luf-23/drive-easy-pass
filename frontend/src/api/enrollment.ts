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

function queryString(query: object) {
  const params = new URLSearchParams();
  Object.entries(query as Record<string, unknown>).forEach(([key, value]) => {
    if (value === undefined || value === null || value === "") return;
    params.set(key, String(value));
  });
  const s = params.toString();
  return s ? `?${s}` : "";
}

export function getEnrollmentDashboard() {
  return request<EnrollmentDashboard>("/admin/dashboard/summary");
}

export function getEnrollmentLeads(query: EnrollmentLeadQuery = {}) {
  return request<PageResult<EnrollmentLead>>(
    `/admin/enrollment-intents${queryString(query)}`
  );
}

export function getSignedStudents(query: EnrollmentLeadQuery = {}) {
  return request<PageResult<EnrollmentLead>>(`/admin/students${queryString(query)}`);
}

export function createEnrollmentLead(payload: EnrollmentLeadPayload) {
  return request<EnrollmentLead>("/admin/enrollment-intents", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function updateEnrollmentLead(id: number, payload: EnrollmentLeadPayload) {
  return request<EnrollmentLead>(`/admin/enrollment-intents/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

export function assignLeadOwner(id: number, ownerUserId: number) {
  return request<EnrollmentLead>(`/admin/enrollment-intents/${id}/owner`, {
    method: "PATCH",
    body: JSON.stringify({ ownerUserId })
  });
}

export function updateEnrollmentLeadStatus(id: number, status: string) {
  return request<EnrollmentLead>(`/admin/enrollment-intents/${id}/status`, {
    method: "PATCH",
    body: JSON.stringify({ status })
  });
}

export function convertEnrollmentLeadToStudent(id: number) {
  return request<EnrollmentLead>(
    `/admin/enrollment-intents/${id}/convert-to-student`,
    { method: "POST" }
  );
}

export function getLeadFollowUps(leadId: number) {
  return request<EnrollmentFollowUp[]>(
    `/admin/enrollment-intents/${leadId}/follow-records`
  );
}

export function createLeadFollowUp(
  leadId: number,
  payload: EnrollmentFollowUpPayload
) {
  return request<EnrollmentFollowUp>(
    `/admin/enrollment-intents/${leadId}/follow-records`,
    {
      method: "POST",
      body: JSON.stringify(payload)
    }
  );
}
