<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { getPublicExamSchedules } from "@/api/examBooking";
import type { ExamScheduleCard } from "@/types";

defineOptions({ name: "ExamScheduleManagement" });

const examType = ref("");
const rows = ref<ExamScheduleCard[]>([]);
const loading = ref(false);
const error = ref("");
const types = ["", "科目一", "科目二", "科目三", "科目四"];

const filtered = computed(() => rows.value);

onMounted(load);

async function load() {
  loading.value = true;
  error.value = "";
  try {
    rows.value = await getPublicExamSchedules(examType.value || undefined);
  } catch (err) {
    rows.value = [];
    error.value = err instanceof Error ? err.message : "考试场次加载失败";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="exam-schedules-page dep-page dep-page--fill">
    <header class="dep-page-header">
      <div>
        <p class="dep-page-eyebrow">Exam Schedules</p>
        <h1>考试场次</h1>
      </div>
      <strong class="dep-page-metric">{{ filtered.length }}</strong>
    </header>

    <section class="exam-panel dep-card dep-card-fill">
      <div class="toolbar">
        <select v-model="examType" @change="load">
          <option v-for="item in types" :key="item" :value="item">
            {{ item || "全部科目" }}
          </option>
        </select>
        <button :disabled="loading" @click="load">刷新</button>
      </div>
      <p v-if="error" class="error">{{ error }}</p>
      <div class="table dep-table-scroll">
        <div class="row head">
          <span>科目</span>
          <span>考场</span>
          <span>日期</span>
          <span>时间</span>
          <span>容量</span>
          <span>剩余</span>
        </div>
        <div v-if="loading" class="empty">加载中...</div>
        <div v-else-if="filtered.length === 0" class="empty">暂无考试场次</div>
        <template v-else>
          <div v-for="r in filtered" :key="r.id" class="row">
            <span>{{ r.examType }}</span>
            <span>{{ r.venueName }}</span>
            <span>{{ r.examDate }}</span>
            <span>{{ r.startTime }} - {{ r.endTime }}</span>
            <span>{{ r.capacity }}</span>
            <span>{{ r.availableSlots }}</span>
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
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

select,
button {
  height: 36px;
  padding: 0 12px;
  border: 1px solid #d9dde3;
  border-radius: 8px;
  background: #fff;
}

button {
  font-weight: 600;
  color: #1a1a1a;
  cursor: pointer;
  background: #b8dcff;
  border-color: #1a1a1a;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.row {
  display: grid;
  grid-template-columns: 90px 180px 120px 170px 80px 80px;
  gap: 12px;
  min-width: 780px;
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
