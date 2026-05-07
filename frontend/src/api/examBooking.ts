import { request } from "@/services/api";
import type { ExamRegistrationRow, ExamScheduleCard } from "@/types";

export function getPublicExamSchedules(examType?: string) {
  const q =
    examType && examType.length > 0
      ? `?examType=${encodeURIComponent(examType)}`
      : "";
  return request<ExamScheduleCard[]>(`/public/exam-schedules${q}`);
}

export function getMyExamRegistrations() {
  return request<ExamRegistrationRow[]>("/exam-registrations/mine");
}

export function applyExamRegistration(scheduleId: number) {
  return request<ExamRegistrationRow>("/exam-registrations", {
    method: "POST",
    body: JSON.stringify({ scheduleId })
  });
}

export function cancelExamRegistration(id: number) {
  return request<void>(`/exam-registrations/${id}`, {
    method: "DELETE"
  });
}
