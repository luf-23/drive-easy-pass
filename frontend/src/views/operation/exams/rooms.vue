<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { getPublicExamSchedules } from "@/api/examBooking";
import type { ExamScheduleCard } from "@/types";

defineOptions({ name: "ExamRoomManagement" });

const schedules = ref<ExamScheduleCard[]>([]);
const loading = ref(false);
const error = ref("");

const rooms = computed(() => {
  const map = new Map<
    number,
    { venueId: number; venueName: string; count: number; capacity: number }
  >();
  schedules.value.forEach(s => {
    const current = map.get(s.venueId) || {
      venueId: s.venueId,
      venueName: s.venueName,
      count: 0,
      capacity: 0
    };
    current.count += 1;
    current.capacity += s.capacity;
    map.set(s.venueId, current);
  });
  return [...map.values()];
});

onMounted(load);

async function load() {
  loading.value = true;
  error.value = "";
  try {
    schedules.value = await getPublicExamSchedules();
  } catch (err) {
    schedules.value = [];
    error.value = err instanceof Error ? err.message : "考场列表加载失败";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="exam-rooms-page dep-page dep-page--fill">
    <header class="dep-page-header">
      <div>
        <p class="dep-page-eyebrow">Exam Rooms</p>
        <h1>考场管理</h1>
      </div>
      <strong class="dep-page-metric">{{ rooms.length }}</strong>
    </header>

    <section class="exam-panel dep-card dep-card-fill">
      <div class="toolbar">
        <span />
        <button :disabled="loading" @click="load">刷新</button>
      </div>
      <p v-if="error" class="error">{{ error }}</p>
      <div class="table dep-table-scroll">
        <div class="row head">
          <span>考场</span><span>近期场次</span><span>总容量</span>
        </div>
        <div v-if="loading" class="empty">加载中...</div>
        <div v-else-if="rooms.length === 0" class="empty">暂无考场数据</div>
        <template v-else>
          <div v-for="room in rooms" :key="room.venueId" class="row">
            <span>{{ room.venueName }}</span>
            <span>{{ room.count }}</span>
            <span>{{ room.capacity }}</span>
          </div>
        </template>
      </div>
    </section>
  </div>
</template>

<style scoped>
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

button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.row {
  display: grid;
  grid-template-columns: 220px 100px 100px;
  gap: 12px;
  min-width: 460px;
  padding: 12px;
  border-bottom: 1px solid #eef2ec;
}

.head {
  font-weight: 700;
  color: #4d5c54;
  background: #f7f9f6;
}

.empty,
.error {
  padding: 20px;
  text-align: center;
  background: #fbfcfa;
}

.error {
  flex-shrink: 0;
  margin-bottom: 12px;
  color: #9a3030;
  background: #fff1f1;
  border: 1px solid #ffd5d5;
  border-radius: 8px;
}
</style>
