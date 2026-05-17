import { http } from "@/utils/http";
import type { BaseResult } from "./userManagement";

export const getStatisticsOverview = () => {
  return http.request<BaseResult>("get", "/api/admin/statistics/overview");
};

export const getEnrollmentTrend = (params?: object) => {
  return http.request<BaseResult>("get", "/api/admin/statistics/enrollment-trend", { params });
};

export const getPassRate = () => {
  return http.request<BaseResult>("get", "/api/admin/statistics/pass-rate");
};
