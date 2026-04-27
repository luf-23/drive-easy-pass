import { request } from "@/services/api";
import type {
  CoursePackage,
  EnrollmentLead,
  ExamSite,
  PublicEnrollmentIntentPayload
} from "@/types";

export function getCoursePackages() {
  return request<CoursePackage[]>("/public/course-packages");
}

export function getExamSites() {
  return request<ExamSite[]>("/public/exam-sites");
}

export function createPublicEnrollmentIntent(payload: PublicEnrollmentIntentPayload) {
  return request<EnrollmentLead>("/public/enrollment-intents", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}
