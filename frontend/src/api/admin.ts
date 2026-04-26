import { request } from "@/services/api";
import type { AppRoute, Role } from "@/types";

export type AppRoutePayload = Omit<AppRoute, "id">;
export type RolePayload = Omit<Role, "id">;

export function getAdminRoutes() {
  return request<AppRoute[]>("/admin/routes");
}

export function createAdminRoute(payload: AppRoutePayload) {
  return request<AppRoute>("/admin/routes", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function updateAdminRoute(id: number, payload: AppRoutePayload) {
  return request<AppRoute>(`/admin/routes/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

export function deleteAdminRoute(id: number) {
  return request<void>(`/admin/routes/${id}`, {
    method: "DELETE"
  });
}

export function getRoles() {
  return request<Role[]>("/admin/roles");
}

export function createRole(payload: RolePayload) {
  return request<Role>("/admin/roles", {
    method: "POST",
    body: JSON.stringify(payload)
  });
}

export function updateRole(id: number, payload: RolePayload) {
  return request<Role>(`/admin/roles/${id}`, {
    method: "PUT",
    body: JSON.stringify(payload)
  });
}

export function deleteRole(id: number) {
  return request<void>(`/admin/roles/${id}`, {
    method: "DELETE"
  });
}
