<script setup lang="ts">
import { computed, nextTick, onMounted, onUnmounted, ref, watch } from "vue";
import L from "leaflet";
import "leaflet/dist/leaflet.css";
import type { ExamRoutePathData } from "@/utils/examRoutePath";
import {
  formatRouteMeta,
  needsRoadRouting,
  resolveRoadRoute,
  type RouteResolveMeta
} from "@/utils/drivingRoute";
import { routePathToGcj02 } from "@/utils/geoCoord";
import { syncEndpointsFromPath } from "@/utils/examRoutePath";

defineOptions({ name: "ExamRouteMap" });

const props = withDefaults(
  defineProps<{
    route: ExamRoutePathData;
    height?: string;
    ready?: boolean;
  }>(),
  { height: "420px", ready: true }
);

const isVenuePoint = computed(() => props.route.routing === "venue");

const mapRoot = ref<HTMLElement | null>(null);
const loading = ref(false);
const loadError = ref("");
const routeMeta = ref<RouteResolveMeta>({});
let map: L.Map | null = null;

function addChinaStreetLayer(target: L.Map) {
  L.tileLayer(
    "https://webrd0{s}.is.autonavi.com/appmaptile?lang=zh_cn&size=1&scale=1&style=8&x={x}&y={y}&z={z}",
    {
      subdomains: ["1", "2", "3", "4"],
      maxZoom: 18,
      attribution: "© 高德地图"
    }
  ).addTo(target);
}

function toDisplayRoute(data: ExamRoutePathData): ExamRoutePathData {
  if (data.routing === "venue") {
    const converted = routePathToGcj02({
      ...data,
      path: [[data.start.lat, data.start.lng]],
      end: { ...data.end, lat: data.start.lat, lng: data.start.lng }
    });
    return converted;
  }
  const synced = syncEndpointsFromPath(data);
  if (synced.coordSys === "gcj02") return synced;
  return routePathToGcj02(synced);
}

function drawVenuePoint(display: ExamRoutePathData) {
  if (!mapRoot.value) return;
  if (map) {
    map.remove();
    map = null;
  }

  const lat = display.start.lat;
  const lng = display.start.lng;

  map = L.map(mapRoot.value, { zoomControl: true });
  addChinaStreetLayer(map);

  L.circleMarker([lat, lng], {
    radius: 12,
    color: "#1a1a1a",
    weight: 2,
    fillColor: "#3ecf8e",
    fillOpacity: 1
  })
    .addTo(map)
    .bindPopup(display.start.label || "考场位置");

  map.setView([lat, lng], 16);

  nextTick(() => {
    map?.invalidateSize();
    setTimeout(() => map?.invalidateSize(), 120);
  });
}

function drawRoute(display: ExamRoutePathData) {
  if (!mapRoot.value || display.path.length < 2) return;

  if (map) {
    map.remove();
    map = null;
  }

  const latLngs = display.path.map(
    ([lat, lng]) => [lat, lng] as L.LatLngExpression
  );
  const [sLat, sLng] = display.path[0];
  const [eLat, eLng] = display.path[display.path.length - 1];

  map = L.map(mapRoot.value, { zoomControl: true, preferCanvas: true });
  addChinaStreetLayer(map);

  const line = L.polyline(latLngs, {
    color: "#0b5cab",
    weight: 6,
    opacity: 0.92,
    lineJoin: "round"
  }).addTo(map);

  L.circleMarker([sLat, sLng], {
    radius: 10,
    color: "#1a1a1a",
    weight: 2,
    fillColor: "#3ecf8e",
    fillOpacity: 1
  })
    .addTo(map)
    .bindPopup(display.start.label || "起点");

  L.circleMarker([eLat, eLng], {
    radius: 10,
    color: "#1a1a1a",
    weight: 2,
    fillColor: "#ff6b6b",
    fillOpacity: 1
  })
    .addTo(map)
    .bindPopup(display.end.label || "终点");

  map.fitBounds(line.getBounds().pad(0.45), {
    maxZoom: 16,
    padding: [32, 32]
  });

  nextTick(() => {
    map?.invalidateSize();
    setTimeout(() => map?.invalidateSize(), 120);
  });
}

async function bootstrap() {
  if (!mapRoot.value || !props.ready) return;
  loading.value = true;
  loadError.value = "";
  routeMeta.value = {};

  try {
    let data = props.route;
    if (data.routing === "venue") {
      drawVenuePoint(toDisplayRoute(data));
      return;
    }
    if (needsRoadRouting(data)) {
      const resolved = await resolveRoadRoute(data);
      data = {
        ...resolved.route,
        coordSys: resolved.meta.provider === "amap" ? "gcj02" : "wgs84"
      };
      routeMeta.value = resolved.meta;
    }
    drawRoute(toDisplayRoute(data));
  } catch (e) {
    loadError.value = e instanceof Error ? e.message : "地图加载失败";
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  if (props.ready) bootstrap();
});

onUnmounted(() => {
  map?.remove();
  map = null;
});

watch(
  () => [props.route, props.ready] as const,
  () => {
    if (props.ready) bootstrap();
  },
  { deep: true }
);
</script>

<template>
  <div class="exam-route-map">
    <div class="legend">
      <template v-if="isVenuePoint">
        <span class="dot start">考场位置</span>
        <span class="line-hint">科目二场地考试，地图仅标注考场所在位置</span>
      </template>
      <template v-else>
        <span class="dot start">起点</span>
        <span class="dot end">终点</span>
        <span class="line-hint">蓝线为科目三沿道路的考试路线（高德驾车规划）</span>
        <span v-if="formatRouteMeta(routeMeta)" class="meta">
          {{ formatRouteMeta(routeMeta) }}
        </span>
      </template>
    </div>
    <p v-if="loading" class="status">
      {{ isVenuePoint ? "正在加载地图…" : "正在规划沿道路路线…" }}
    </p>
    <p v-else-if="loadError" class="status error">{{ loadError }}</p>
    <div ref="mapRoot" class="map-canvas" :style="{ height: props.height }" />
  </div>
</template>

<style scoped>
.exam-route-map {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.legend {
  display: flex;
  flex-wrap: wrap;
  gap: 10px 14px;
  font-size: 12px;
  color: #4d5c54;
}

.meta {
  font-weight: 600;
  color: #2a5f8f;
}

.dot::before {
  display: inline-block;
  width: 10px;
  height: 10px;
  margin-right: 4px;
  vertical-align: middle;
  content: "";
  border: 2px solid #1a1a1a;
  border-radius: 50%;
}

.dot.start::before {
  background: #3ecf8e;
}

.dot.end::before {
  background: #ff6b6b;
}

.line-hint {
  color: #6b7a72;
}

.status {
  margin: 0;
  font-size: 13px;
  color: #4d5c54;
}

.status.error {
  color: #9a3030;
}

.map-canvas {
  width: 100%;
  min-height: 280px;
  border: 1px solid #cfd8d2;
  border-radius: 10px;
}
</style>
