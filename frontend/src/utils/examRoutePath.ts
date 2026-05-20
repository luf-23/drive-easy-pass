export interface ExamRoutePoint {
  lat: number;
  lng: number;
  label?: string;
}

export interface ExamRoutePathData {
  start: ExamRoutePoint;
  end: ExamRoutePoint;
  /** [纬度, 经度]；routing=driving 时可仅存起终点，由路径规划 API 生成 */
  path: [number, number][];
  /** 途经点（科目二场内环线等） */
  waypoints?: ExamRoutePoint[];
  /** venue=仅展示考场位置；manual=使用 path 折线；driving=科目三道路规划 */
  routing?: "venue" | "manual" | "driving";
  /** 路径点坐标系；高德规划结果为 gcj02 */
  coordSys?: "gcj02" | "wgs84";
}

export function parseRoutePath(raw: string | null | undefined): ExamRoutePathData | null {
  if (raw == null || raw.trim() === "") return null;
  try {
    const data = JSON.parse(raw) as ExamRoutePathData;
    if (
      typeof data?.start?.lat !== "number" ||
      typeof data?.start?.lng !== "number" ||
      typeof data?.end?.lat !== "number" ||
      typeof data?.end?.lng !== "number"
    ) {
      return null;
    }
    if (!Array.isArray(data.path)) {
      data.path = [
        [data.start.lat, data.start.lng],
        [data.end.lat, data.end.lng]
      ];
    }
    if (data.routing === "venue") {
      data.path = [[data.start.lat, data.start.lng]];
      data.end = { ...data.end, lat: data.start.lat, lng: data.start.lng };
      return data;
    }
    if (data.path.length < 2 && data.routing !== "driving") {
      return null;
    }
    if (data.path.length < 2) {
      data.path = [
        [data.start.lat, data.start.lng],
        [data.end.lat, data.end.lng]
      ];
    }
    return data;
  } catch {
    return null;
  }
}

/** 起终点标记与折线对齐（道路规划后 path 与库内 start/end 可能不一致） */
export function syncEndpointsFromPath(route: ExamRoutePathData): ExamRoutePathData {
  if (route.path.length < 2) return route;
  const [sLat, sLng] = route.path[0];
  const [eLat, eLng] = route.path[route.path.length - 1];
  return {
    ...route,
    start: { ...route.start, lat: sLat, lng: sLng },
    end: { ...route.end, lat: eLat, lng: eLng }
  };
}

export function isVenueLocationMode(routePath: string | null | undefined): boolean {
  const data = parseRoutePath(routePath);
  return data?.routing === "venue";
}

export function hasDrawableRoute(
  routePath: string | null | undefined,
  mediaType: string
): boolean {
  return mediaType === "map" && parseRoutePath(routePath) != null;
}

export function mapActionLabel(routePath: string | null | undefined): string {
  return isVenueLocationMode(routePath) ? "查看考场位置" : "查看路线地图";
}
