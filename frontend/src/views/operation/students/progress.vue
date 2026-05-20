<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { getSignedStudents } from "@/api/enrollment";
import type { EnrollmentLead } from "@/types";

defineOptions({
  name: "StudentProgress"
});

type StageKey = "subject1" | "subject2" | "subject3" | "subject4";

const keyword = ref("");
const loading = ref(false);
const error = ref("");
const students = ref<EnrollmentLead[]>([]);

const stageLabels: Record<StageKey, string> = {
  subject1: "科目一",
  subject2: "科目二",
  subject3: "科目三",
  subject4: "科目四"
};
const stageOrder: StageKey[] = ["subject1", "subject2", "subject3", "subject4"];

const rows = computed(() =>
  students.value.map(student => {
    const stage = inferStage(student);
    const index = stageOrder.indexOf(stage);
    return {
      ...student,
      stage,
      stageText: stageLabels[stage],
      percent: Math.round(((index + 1) / stageOrder.length) * 100)
    };
  })
);

const summary = computed(() =>
  stageOrder.map(stage => ({
    stage,
    label: stageLabels[stage],
    count: rows.value.filter(item => item.stage === stage).length
  }))
);

onMounted(() => {
  loadStudents();
});

async function loadStudents() {
  loading.value = true;
  error.value = "";
  try {
    const result = await getSignedStudents({
      keyword: keyword.value || undefined,
      page: 1,
      pageSize: 200
    });
    students.value = result.items;
  } catch (err) {
    students.value = [];
    error.value = err instanceof Error ? err.message : "学员进度加载失败";
  } finally {
    loading.value = false;
  }
}

function search() {
  loadStudents();
}

function resetSearch() {
  keyword.value = "";
  loadStudents();
}

function inferStage(student: EnrollmentLead): StageKey {
  const text = `${student.remark || ""} ${student.intentLevel || ""}`;
  if (text.includes("科目四")) return "subject4";
  if (text.includes("科目三")) return "subject3";
  if (text.includes("科目二")) return "subject2";
  return "subject1";
}

function formatDateTime(value: string | null) {
  if (!value) return "-";
  return value.replace("T", " ").slice(0, 16);
}
</script>

<template>
  <div class="progress-page dep-page dep-page--fill">
    <header class="progress-header dep-page-header">
      <div>
        <p>Training Progress</p>
        <h1>学员考试进度</h1>
      </div>
      <strong class="dep-page-metric">{{ rows.length }}</strong>
    </header>

    <section class="progress-panel dep-card dep-card-fill">
      <div class="toolbar">
        <label>
          关键词
          <input
            v-model="keyword"
            placeholder="姓名 / 手机号"
            @keydown.enter.prevent="search"
          />
        </label>
        <div class="actions">
          <button class="primary" :disabled="loading" @click="search">
            查询
          </button>
          <button class="ghost" :disabled="loading" @click="resetSearch">
            重置
          </button>
        </div>
      </div>

      <div class="summary-grid">
        <article v-for="item in summary" :key="item.stage">
          <span>{{ item.label }}</span>
          <strong>{{ item.count }}</strong>
        </article>
      </div>

      <div v-if="error" class="error-message">{{ error }}</div>

      <div class="progress-table dep-table-scroll">
        <div class="progress-row progress-head">
          <span>学员</span>
          <span>手机号</span>
          <span>当前阶段</span>
          <span>进度</span>
          <span>负责人</span>
          <span>下次跟进</span>
        </div>

        <div v-if="loading" class="empty-state">加载中...</div>
        <div v-else-if="rows.length === 0" class="empty-state">
          暂无学员进度
        </div>
        <template v-else>
          <div v-for="student in rows" :key="student.id" class="progress-row">
            <span class="name-cell">{{ student.name }}</span>
            <span>{{ student.phone }}</span>
            <span><i class="stage-tag">{{ student.stageText }}</i></span>
            <span>
              <b class="progress-bar">
                <i :style="{ width: `${student.percent}%` }" />
              </b>
              {{ student.percent }}%
            </span>
            <span>{{ student.ownerName || "未分配" }}</span>
            <span>{{ formatDateTime(student.nextFollowTime) }}</span>
          </div>
        </template>
      </div>
    </section>
  </div>
</template>

<style scoped>
.progress-header {
  padding: 0;
  background: transparent;
  border: none;
}

.toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.toolbar {
  flex-wrap: wrap;
  gap: 12px;
  align-items: end;
  margin-bottom: 14px;
}

label {
  display: grid;
  gap: 6px;
  min-width: 260px;
  font-size: 13px;
  color: #46534c;
}

input {
  height: 36px;
  padding: 0 10px;
  color: #17201b;
  outline: none;
  background: #fbfcfa;
  border: 1px solid #dfe5da;
  border-radius: 8px;
}

.actions {
  display: flex;
  gap: 10px;
}

button {
  height: 36px;
  padding: 0 14px;
  font-weight: 700;
  cursor: pointer;
  border-radius: 8px;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
}

.primary {
  color: #1a1a1a;
  background: #b8dcff;
  border: 1px solid #1a1a1a;
}

.ghost {
  color: #2f4a3e;
  background: #fff;
  border: 1px solid #cbd8cf;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 14px;
}

.summary-grid article {
  padding: 12px;
  background: #fbfcfa;
  border: 1px solid #dfe5da;
  border-radius: 8px;
}

.summary-grid span {
  color: #607067;
}

.summary-grid strong {
  display: block;
  margin-top: 4px;
  font-size: 24px;
  color: #21483a;
}

.progress-table {
  overflow-x: auto;
}

.progress-row {
  display: grid;
  grid-template-columns: 110px 140px 100px 180px 110px 140px;
  gap: 12px;
  align-items: center;
  min-width: 820px;
  padding: 12px;
  border-bottom: 1px solid #eef2ec;
}

.progress-head {
  font-size: 13px;
  font-weight: 700;
  color: #4d5c54;
  background: #f7f9f6;
}

.name-cell {
  font-weight: 700;
  color: #21483a;
}

.stage-tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  font-style: normal;
  font-weight: 700;
  color: #1f5c8d;
  background: #dfefff;
  border-radius: 999px;
}

.progress-bar {
  display: inline-block;
  width: 96px;
  height: 8px;
  margin-right: 8px;
  overflow: hidden;
  vertical-align: middle;
  background: #edf4e8;
  border-radius: 999px;
}

.progress-bar i {
  display: block;
  height: 100%;
  background: #2f6f54;
}

.empty-state,
.error-message {
  padding: 20px;
  text-align: center;
  background: #fbfcfa;
  border-bottom: 1px solid #eef2ec;
}

.error-message {
  margin-bottom: 12px;
  color: #9a3030;
  background: #fff1f1;
  border: 1px solid #ffd5d5;
  border-radius: 8px;
}

@media (width <= 860px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .toolbar,
  label {
    width: 100%;
  }
}
</style>
