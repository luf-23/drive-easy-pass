<script setup lang="ts">
import { ref, watch } from "vue";
import ExamRouteMap from "./index.vue";
import type { ExamRoutePathData } from "@/utils/examRoutePath";

defineOptions({ name: "ExamRouteMapDialog" });

const visible = defineModel<boolean>("visible", { default: false });

defineProps<{
  title: string;
  subtitle?: string;
  route: ExamRoutePathData | null;
}>();

const mapReady = ref(false);

watch(visible, open => {
  mapReady.value = false;
  if (open) {
    requestAnimationFrame(() => {
      mapReady.value = true;
    });
  }
});

function close() {
  visible.value = false;
}
</script>

<template>
  <Teleport to="body">
    <div
      v-if="visible"
      class="route-map-dialog-backdrop"
      role="dialog"
      aria-modal="true"
      @click.self="close"
    >
      <div class="route-map-dialog dep-card">
        <header class="dialog-head">
          <div>
            <p class="dep-page-eyebrow">Exam Route</p>
            <h2>{{ title }}</h2>
            <p v-if="subtitle" class="subtitle">{{ subtitle }}</p>
          </div>
          <button type="button" class="ghost" @click="close">关闭</button>
        </header>
        <ExamRouteMap
          v-if="route && mapReady"
          :route="route"
          :ready="mapReady"
          height="min(62vh, 480px)"
        />
        <p v-else-if="!route" class="empty">暂无可用路线轨迹数据</p>
        <p v-else class="empty">地图加载中…</p>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
.route-map-dialog-backdrop {
  position: fixed;
  inset: 0;
  z-index: 2000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  background: rgb(0 0 0 / 45%);
}

.route-map-dialog {
  width: min(800px, 100%);
  max-height: 92vh;
  padding: 20px;
  overflow-y: auto;
}

.dialog-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.dialog-head h2 {
  margin: 4px 0 0;
  font-size: 20px;
}

.subtitle {
  margin: 6px 0 0;
  font-size: 13px;
  color: #6b7a72;
}

button.ghost {
  height: 36px;
  padding: 0 14px;
  font-weight: 600;
  cursor: pointer;
  background: #fff;
  border: 1px solid #1a1a1a;
  border-radius: 8px;
}

.empty {
  padding: 24px;
  text-align: center;
  color: #6b7a72;
}
</style>
