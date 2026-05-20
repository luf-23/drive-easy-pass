import { fetchDrivingRoute } from "@/api/routing";
import type { ExamRoutePathData } from "@/utils/examRoutePath";
import { syncEndpointsFromPath } from "@/utils/examRoutePath";

export type RouteResolveMeta = {
  distanceMeters?: number;
  durationSeconds?: number;
  provider?: string;
};

/** 数据库 JSON 可带 routing: "driving" 表示按起终点道路规划 */
export function needsRoadRouting(data: ExamRoutePathData): boolean {
  return data.routing === "driving";
}

/** 优先高德（后端代理），失败时尝试 OSRM 公开路由 */
export async function resolveRoadRoute(
  data: ExamRoutePathData
): Promise<{ route: ExamRoutePathData; meta: RouteResolveMeta }> {
  const { start, end } = data;

  try {
    const res = await fetchDrivingRoute(
      start.lng,
      start.lat,
      end.lng,
      end.lat,
      data.waypoints?.map(w => ({ lng: w.lng, lat: w.lat }))
    );
    return {
      route: syncEndpointsFromPath({
        ...data,
        path: res.path,
        routing: "manual",
        coordSys: "gcj02"
      }),
      meta: {
        distanceMeters: res.distanceMeters,
        durationSeconds: res.durationSeconds,
        provider: res.provider
      }
    };
  } catch {
    return resolveOsrmRoute(data);
  }
}

async function resolveOsrmRoute(
  data: ExamRoutePathData
): Promise<{ route: ExamRoutePathData; meta: RouteResolveMeta }> {
  const { start, end } = data;
  const coords = `${start.lng},${start.lat};${end.lng},${end.lat}`;
  const url = `https://router.project-osrm.org/route/v1/driving/${coords}?overview=full&geometries=geojson`;
  const resp = await fetch(url);
  if (!resp.ok) {
    throw new Error("道路规划不可用，请配置后端高德 Key（app.amap.web-key）");
  }
  const json = (await resp.json()) as {
    routes?: Array<{
      distance: number;
      duration: number;
      geometry: { coordinates: [number, number][] };
    }>;
  };
  const route = json.routes?.[0];
  if (!route?.geometry?.coordinates?.length) {
    throw new Error("未获取到沿道路的路线");
  }
  const path: [number, number][] = route.geometry.coordinates.map(
    ([lng, lat]) => [lat, lng]
  );
  return {
    route: syncEndpointsFromPath({
      ...data,
      path,
      coordSys: "wgs84"
    }),
    meta: {
      distanceMeters: Math.round(route.distance),
      durationSeconds: Math.round(route.duration),
      provider: "osrm"
    }
  };
}

export function formatRouteMeta(meta: RouteResolveMeta): string {
  if (!meta.distanceMeters && !meta.durationSeconds) return "";
  const km = meta.distanceMeters
    ? `${(meta.distanceMeters / 1000).toFixed(1)} 公里`
    : "";
  const min = meta.durationSeconds
    ? `${Math.round(meta.durationSeconds / 60)} 分钟`
    : "";
  const parts = [km, min].filter(Boolean);
  const via = meta.provider === "amap" ? "高德驾车" : "OSRM";
  return parts.length ? `${via} · 约 ${parts.join(" / ")}` : via;
}
