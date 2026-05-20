import { request } from "@/services/api";

export type DrivingRouteApiResponse = {
  path: [number, number][];
  distanceMeters: number;
  durationSeconds: number;
  provider: string;
};

/** path 每项为 [lat, lng] */
export function fetchDrivingRoute(
  originLng: number,
  originLat: number,
  destLng: number,
  destLat: number,
  waypoints?: Array<{ lng: number; lat: number }>
) {
  const q = new URLSearchParams({
    originLng: String(originLng),
    originLat: String(originLat),
    destLng: String(destLng),
    destLat: String(destLat)
  });
  if (waypoints?.length) {
    q.set(
      "waypoints",
      waypoints.map(w => `${w.lng},${w.lat}`).join(";")
    );
  }
  return request<DrivingRouteApiResponse>(`/public/routing/driving?${q}`);
}

export function fetchRoutingStatus() {
  return request<{ amapConfigured: boolean; mode: string }>(
    "/public/routing/status"
  );
}
