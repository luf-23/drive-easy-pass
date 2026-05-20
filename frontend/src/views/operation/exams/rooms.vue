<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { getPublicExamSchedules } from "@/api/examBooking";
import type { ExamScheduleCard } from "@/types";

defineOptions({ name: "ExamRoomManagement" });

const schedules = ref<ExamScheduleCard[]>([]);
const loading = ref(false);
const error = ref("");

const rooms = computed(() => {
  const map = new Map<number, { venueId: number; venueName: string; count: number; capacity: number }>();
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
  <section class="page">
    <header><div><p>Exam Rooms</p><h1>考场管理</h1></div><strong>{{ rooms.length }}</strong></header>
    <button :disabled="loading" @click="load">刷新</button>
    <p v-if="error" class="error">{{ error }}</p>
    <div class="table">
      <div class="row head"><span>考场</span><span>近期场次</span><span>总容量</span></div>
      <div v-if="loading" class="empty">加载中...</div>
      <div v-else-if="rooms.length === 0" class="empty">暂无考场数据</div>
      <template v-else>
        <div v-for="room in rooms" :key="room.venueId" class="row">
          <span>{{ room.venueName }}</span><span>{{ room.count }}</span><span>{{ room.capacity }}</span>
        </div>
      </template>
    </div>
  </section>
</template>

<style scoped>
.page { display: grid; gap: 16px; padding: 18px; background: #fff; border: 1px solid #dfe5da; border-radius: 8px; }
header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
p { margin: 0; color: #607067; }
header p { margin-bottom: 4px; font-size: 12px; font-weight: 700; text-transform: uppercase; }
h1 { margin: 0; font-size: 24px; color: #17201b; }
strong { font-size: 28px; color: #21483a; }
button { justify-self: end; height: 36px; padding: 0 14px; font-weight: 700; color: #fff; cursor: pointer; background: #2f6f54; border: 1px solid #2f6f54; border-radius: 8px; }
.table { overflow-x: auto; }
.row { display: grid; grid-template-columns: 220px 100px 100px; gap: 12px; min-width: 460px; padding: 12px; border-bottom: 1px solid #eef2ec; }
.head { font-weight: 700; color: #4d5c54; background: #f7f9f6; }
.empty, .error { padding: 20px; text-align: center; background: #fbfcfa; }
.error { color: #9a3030; background: #fff1f1; border: 1px solid #ffd5d5; border-radius: 8px; }
</style>
