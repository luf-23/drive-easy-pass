import { request } from "@/services/api";

export interface StatisticsOverview {
  totalUsers: number;
  activeUsers: number;
  totalEnrollments: number;
  monthlyEnrollments: number;
  totalExams?: number;
}

export interface EnrollmentTrendRow {
  month: string;
  count: number;
}

export interface PassRateStats {
  passedCount: number;
  totalCount: number;
  rate: number;
  subject1?: number;
  subject2?: number;
  subject3?: number;
  subject4?: number;
}

export function getStatisticsOverview() {
  return request<StatisticsOverview>("/admin/statistics/overview");
}

export function getEnrollmentTrend() {
  return request<EnrollmentTrendRow[]>("/admin/statistics/enrollment-trend");
}

export function getPassRate() {
  return request<PassRateStats>("/admin/statistics/pass-rate");
}
