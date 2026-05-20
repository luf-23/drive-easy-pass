<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import {
  getExamVenueDetail,
  getExamVenues,
  updateExamVenueRoutes
} from "@/api/examVenues";
import ExamRouteMapDialog from "@/components/ExamRouteMap/ExamRouteMapDialog.vue";
import { useUserStoreHook } from "@/store/modules/user";
import type {
  ExamVenueDetail,
  ExamVenueMediaType,
  ExamVenueRouteItem,
  ExamVenueSummary
} from "@/types";
import {
  hasDrawableRoute,
  mapActionLabel,
  parseRoutePath,
  type ExamRoutePathData
} from "@/utils/examRoutePath";

defineOptions({ name: "ExamRoomManagement" });

const userStore = useUserStoreHook();
const canEdit = computed(() =>
  userStore.roles.some(r => r === "admin" || r === "coach")
);

const venues = ref<ExamVenueSummary[]>([]);
const loading = ref(false);
const error = ref("");

const selectedId = ref<number | null>(null);
const detail = ref<ExamVenueDetail | null>(null);
const detailLoading = ref(false);
const detailError = ref("");

const editing = ref(false);
const saving = ref(false);
const saveError = ref("");
const editRoutes = ref<ExamVenueRouteItem[]>([]);

const mapDialogVisible = ref(false);
const mapDialogTitle = ref("");
const mapDialogSubtitle = ref("");
const mapDialogRoute = ref<ExamRoutePathData | null>(null);

const mediaOptions: { value: ExamVenueMediaType; label: string }[] = [
  { value: "map", label: "地图链接" },
  { value: "video", label: "视频" },
  { value: "image", label: "图片" },
  { value: "article", label: "图文" },
  { value: "gpx", label: "GPX 轨迹" }
];

onMounted(load);

async function load() {
  loading.value = true;
  error.value = "";
  try {
    venues.value = await getExamVenues();
  } catch (err) {
    venues.value = [];
    error.value = err instanceof Error ? err.message : "考场列表加载失败";
  } finally {
    loading.value = false;
  }
}

async function openDetail(venueId: number) {
  selectedId.value = venueId;
  editing.value = false;
  saveError.value = "";
  await loadDetail(venueId);
}

async function loadDetail(venueId: number) {
  detailLoading.value = true;
  detailError.value = "";
  try {
    detail.value = await getExamVenueDetail(venueId);
  } catch (err) {
    detail.value = null;
    detailError.value = err instanceof Error ? err.message : "考场详情加载失败";
  } finally {
    detailLoading.value = false;
  }
}

function closeDetail() {
  selectedId.value = null;
  detail.value = null;
  editing.value = false;
  saveError.value = "";
}

function startEdit() {
  if (!detail.value) return;
  editRoutes.value = detail.value.routes.map(cloneRoute);
  editing.value = true;
  saveError.value = "";
}

function cancelEdit() {
  editing.value = false;
  saveError.value = "";
}

function cloneRoute(r: ExamVenueRouteItem): ExamVenueRouteItem {
  return { ...r };
}

async function saveRoutes() {
  if (selectedId.value == null) return;
  saving.value = true;
  saveError.value = "";
  try {
    detail.value = await updateExamVenueRoutes(
      selectedId.value,
      editRoutes.value
    );
    editing.value = false;
    await load();
  } catch (err) {
    saveError.value = err instanceof Error ? err.message : "保存失败";
  } finally {
    saving.value = false;
  }
}

function routeStatus(v: ExamVenueSummary, subject: "科目二" | "科目三") {
  return subject === "科目二" ? v.hasSubject2Route : v.hasSubject3Route;
}

function openExternal(url: string) {
  if (!url) return;
  window.open(url, "_blank", "noopener,noreferrer");
}

function routePathData(route: ExamVenueRouteItem) {
  return parseRoutePath(route.routePath);
}

function openRouteMap(route: ExamVenueRouteItem, venueName?: string) {
  const data = routePathData(route);
  if (!data) {
    if (route.routeUrl) openExternal(route.routeUrl);
    return;
  }
  mapDialogTitle.value = `${route.examType} · ${route.title || "考场路线"}`;
  mapDialogSubtitle.value = venueName
    ? `${venueName}${route.remark ? ` · ${route.remark}` : ""}`
    : route.remark || "";
  mapDialogRoute.value = data;
  mapDialogVisible.value = true;
}
</script>

<template>
  <div class="exam-rooms-page dep-page dep-page--fill">
    <header class="dep-page-header">
      <div>
        <p class="dep-page-eyebrow">Exam Rooms</p>
        <h1>考场管理</h1>
        <p class="hint">
          科目二展示考场位置；科目三展示沿道路的考试路线。Mock 数据已预置，无需手工录入。
        </p>
      </div>
      <strong class="dep-page-metric">{{ venues.length }}</strong>
    </header>

    <section class="exam-panel dep-card dep-card-fill">
      <div class="toolbar">
        <span />
        <button :disabled="loading" @click="load">刷新</button>
      </div>
      <p v-if="error" class="error">{{ error }}</p>
      <div class="table dep-table-scroll">
        <div class="row head">
          <span>考场</span>
          <span>地址</span>
          <span>科目二位置</span>
          <span>科目三路线</span>
          <span />
        </div>
        <div v-if="loading" class="empty">加载中...</div>
        <div v-else-if="venues.length === 0" class="empty">暂无考场数据</div>
        <template v-else>
          <div
            v-for="v in venues"
            :key="v.id"
            class="row clickable"
            @click="openDetail(v.id)"
          >
            <span class="name">{{ v.name }}</span>
            <span class="muted">{{ v.address || "—" }}</span>
            <span>
              <span
                class="badge"
                :class="routeStatus(v, '科目二') ? 'ok' : 'missing'"
              >
                {{ routeStatus(v, "科目二") ? "已录入" : "未录入" }}
              </span>
            </span>
            <span>
              <span
                class="badge"
                :class="routeStatus(v, '科目三') ? 'ok' : 'missing'"
              >
                {{ routeStatus(v, "科目三") ? "已录入" : "未录入" }}
              </span>
            </span>
            <span class="action">查看</span>
          </div>
        </template>
      </div>
    </section>

    <div v-if="selectedId != null" class="drawer-backdrop" @click.self="closeDetail">
      <aside class="drawer dep-card">
        <header class="drawer-head">
          <div>
            <p class="dep-page-eyebrow">Venue Detail</p>
            <h2>{{ detail?.name || "考场详情" }}</h2>
          </div>
          <button type="button" class="ghost" @click="closeDetail">关闭</button>
        </header>

        <p v-if="detailLoading" class="empty">加载中...</p>
        <p v-else-if="detailError" class="error">{{ detailError }}</p>
        <template v-else-if="detail">
          <div class="base-info">
            <p><strong>地址</strong> {{ detail.address || "—" }}</p>
            <p><strong>电话</strong> {{ detail.contactPhone || "—" }}</p>
          </div>

          <div v-if="!editing" class="route-blocks">
            <article
              v-for="route in detail.routes"
              :key="route.examType"
              class="route-card"
            >
              <h3>
                {{ route.examType }}
                {{ route.examType === "科目二" ? "考场位置" : "考试路线" }}
              </h3>
              <template
                v-if="
                  route.enabled &&
                  (hasDrawableRoute(route.routePath, route.mediaType) ||
                    route.routeUrl)
                "
              >
                <p v-if="route.title" class="route-title">{{ route.title }}</p>
                <p v-if="route.remark" class="muted">{{ route.remark }}</p>
                <div v-if="route.mediaType === 'image' && route.routeUrl" class="media">
                  <img :src="route.routeUrl" :alt="route.title || route.examType" />
                </div>
                <div v-else-if="route.mediaType === 'video' && route.routeUrl" class="media">
                  <video controls :src="route.routeUrl" />
                </div>
                <button
                  v-else-if="hasDrawableRoute(route.routePath, route.mediaType)"
                  type="button"
                  class="link-btn"
                  @click="openRouteMap(route, detail.name)"
                >
                  {{ mapActionLabel(route.routePath) }}
                </button>
                <button
                  v-else-if="route.routeUrl"
                  type="button"
                  class="link-btn"
                  @click="openExternal(route.routeUrl)"
                >
                  打开路线资源
                </button>
              </template>
              <p v-else class="muted">暂未录入该科目考场路线。</p>
            </article>
          </div>

          <form v-else class="edit-form" @submit.prevent="saveRoutes">
            <fieldset
              v-for="route in editRoutes"
              :key="route.examType"
              class="route-edit"
            >
              <legend>{{ route.examType }}</legend>
              <label>
                <span>启用</span>
                <input v-model="route.enabled" type="checkbox" />
              </label>
              <label>
                <span>类型</span>
                <select v-model="route.mediaType">
                  <option
                    v-for="opt in mediaOptions"
                    :key="opt.value"
                    :value="opt.value"
                  >
                    {{ opt.label }}
                  </option>
                </select>
              </label>
              <label>
                <span>标题</span>
                <input v-model="route.title" type="text" placeholder="如：科目二标准五项路线" />
              </label>
              <label>
                <span>路线链接</span>
                <input
                  v-model="route.routeUrl"
                  type="url"
                  placeholder="高德/百度地图、视频或图片 URL"
                />
              </label>
              <label>
                <span>封面（可选）</span>
                <input v-model="route.coverUrl" type="url" />
              </label>
              <label>
                <span>备注</span>
                <input v-model="route.remark" type="text" />
              </label>
            </fieldset>
            <p v-if="saveError" class="error">{{ saveError }}</p>
            <div class="form-actions">
              <button type="button" class="ghost" :disabled="saving" @click="cancelEdit">
                取消
              </button>
              <button type="submit" :disabled="saving">
                {{ saving ? "保存中..." : "保存路线" }}
              </button>
            </div>
          </form>

          <div v-if="canEdit && !editing" class="drawer-actions">
            <button type="button" @click="startEdit">编辑路线</button>
          </div>
        </template>
      </aside>
    </div>

    <ExamRouteMapDialog
      v-model:visible="mapDialogVisible"
      :title="mapDialogTitle"
      :subtitle="mapDialogSubtitle"
      :route="mapDialogRoute"
    />
  </div>
</template>

<style scoped>
.hint {
  margin: 6px 0 0;
  font-size: 13px;
  color: #6b7a72;
}

.toolbar {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: flex-end;
  margin-bottom: 12px;
}

button {
  height: 36px;
  padding: 0 14px;
  font-weight: 600;
  color: #1a1a1a;
  cursor: pointer;
  background: #b8dcff;
  border: 1px solid #1a1a1a;
  border-radius: 8px;
}

button.ghost {
  background: #fff;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.row {
  display: grid;
  grid-template-columns: 1.2fr 1.4fr 100px 100px 56px;
  gap: 12px;
  min-width: 720px;
  padding: 12px;
  border-bottom: 1px solid #eef2ec;
}

.row.clickable {
  cursor: pointer;
}

.row.clickable:hover {
  background: #f4f8f2;
}

.head {
  font-weight: 700;
  color: #4d5c54;
  background: #f7f9f6;
}

.name {
  font-weight: 600;
}

.muted {
  color: #6b7a72;
}

.badge {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  font-weight: 600;
  border-radius: 6px;
}

.badge.ok {
  color: #1f5c38;
  background: #e8f6ee;
}

.badge.missing {
  color: #6b4a1a;
  background: #fff6e8;
}

.action {
  font-weight: 600;
  color: #2a5f8f;
}

.empty,
.error {
  padding: 20px;
  text-align: center;
}

.error {
  flex-shrink: 0;
  margin-bottom: 12px;
  color: #9a3030;
  background: #fff1f1;
  border: 1px solid #ffd5d5;
  border-radius: 8px;
}

.drawer-backdrop {
  position: fixed;
  inset: 0;
  z-index: 1000;
  display: flex;
  justify-content: flex-end;
  background: rgb(0 0 0 / 35%);
}

.drawer {
  display: flex;
  flex-direction: column;
  width: min(520px, 100%);
  max-height: 100vh;
  padding: 20px;
  overflow-y: auto;
  border-radius: 0;
}

.drawer-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 16px;
}

.drawer-head h2 {
  margin: 4px 0 0;
  font-size: 20px;
}

.base-info p {
  margin: 0 0 8px;
  font-size: 14px;
}

.route-blocks {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-top: 16px;
}

.route-card {
  padding: 14px;
  background: #f7f9f6;
  border: 1px solid #e2e8e0;
  border-radius: 10px;
}

.route-card h3 {
  margin: 0 0 10px;
  font-size: 15px;
}

.route-title {
  margin: 0 0 6px;
  font-weight: 600;
}

.media img,
.media video {
  width: 100%;
  max-height: 220px;
  margin-top: 8px;
  object-fit: contain;
  border-radius: 8px;
}

.link-btn {
  margin-top: 8px;
}

.edit-form {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 12px;
}

.route-edit {
  padding: 12px;
  border: 1px solid #e2e8e0;
  border-radius: 10px;
}

.route-edit legend {
  padding: 0 4px;
  font-weight: 700;
}

.route-edit label {
  display: grid;
  gap: 4px;
  margin-top: 10px;
  font-size: 13px;
}

.route-edit input[type="text"],
.route-edit input[type="url"],
.route-edit select {
  height: 36px;
  padding: 0 10px;
  border: 1px solid #cfd8d2;
  border-radius: 8px;
}

.form-actions,
.drawer-actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
