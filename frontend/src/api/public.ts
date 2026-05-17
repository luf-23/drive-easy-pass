import { request } from "@/services/api";
import type {
  CoursePackage,
  EnrollmentLead,
  PublicEnrollmentIntentPayload
} from "@/types";

export function getCoursePackages() {
  return request<CoursePackage[]>("/public/course-packages");
}

export function createPublicEnrollmentIntent(
  payload: PublicEnrollmentIntentPayload
) {
  return request<EnrollmentLead>("/public/enrollment-intents", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}
