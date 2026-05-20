<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { getAdminExamRegistrations } from "@/api/admin";
import {
  getEnrollmentDashboard,
  getEnrollmentLeads
} from "@/api/enrollment";
import { request } from "@/services/api";
import type {
  EnrollmentDashboard,
  EnrollmentFunnelStat,
  EnrollmentLead,
  ExamReservationAdminRow
} from "@/types";

defineOptions({
  name: "Welcome"
});

type StageKey = "subject1" | "subject2" | "subject3" | "subject4";

const loading = ref(false);
const error = ref("");
const dashboard = ref<EnrollmentDashboard | null>(null);
const examRows = ref<ExamReservationAdminRow[]>([]);
const followLeads = ref<EnrollmentLead[]>([]);
const passRate = ref({ passedCount: 0, totalCount: 0, rate: 0 });

const stageLabels: Record<StageKey, string> = {
  subject1: "科目一",
  subject2: "科目二",
  subject3: "科目三",
  subject4: "科目四"
};
const stageOrder: StageKey[] = ["subject1", "subject2", "subject3", "subject4"];

const terminalLeadStatuses = new Set([
  "已报名",
  "已放弃",
  "无效线索",
  "无效"
]);

const todayNewLeads = computed(
  () => dashboard.value?.todayNewLeads ?? 0
);

const pendingExamCount = computed(
  () => examRows.value.filter(r => r.status === "pending").length
);

const weekPassedCount = computed(() => {
  const start = startOfWeek(new Date());
  return examRows.value.filter(
    r =>
      r.status === "completed" &&
      r.passed === "Y" &&
      r.examDate &&
      parseDate(r.examDate) >= start
  ).length;
});

const pendingFollowCount = computed(() => {
  const funnel = dashboard.value?.funnel ?? [];
  return funnel
    .filter(f => !terminalLeadStatuses.has(f.stage))
    .reduce((sum, f) => sum + f.count, 0);
});

const conversionText = computed(() => {
  const rate = dashboard.value?.monthConversionRate;
  return rate != null ? `线索转化率 ${rate.toFixed(1)}%` : "";
});

const pendingExams = computed(() =>
  examRows.value.filter(r => r.status === "pending").slice(0, 6)
);

const completedNeedScore = computed(() =>
  examRows.value
    .filter(r => r.status === "approved")
    .slice(0, 4)
);

const subjectSummary = computed(() => {
  const counts: Record<StageKey, number> = {
    subject1: 0,
    subject2: 0,
    subject3: 0,
    subject4: 0
  };
  for (const lead of followLeads.value) {
    const stage = inferStage(lead);
    counts[stage] += 1;
  }
  return stageOrder.map(stage => ({
    stage,
    label: stageLabels[stage],
    count: counts[stage]
  }));
});

const funnelPreview = computed(() => dashboard.value?.funnel ?? []);

const passRateText = computed(() => {
  const { passedCount, totalCount, rate } = passRate.value;
  if (!totalCount) return "暂无已完成考试成绩";
  return `累计 ${totalCount} 场考试，通过 ${passedCount} 场（${(rate * 100).toFixed(1)}%）`;
});

onMounted(() => {
  loadDashboard();
});

async function loadDashboard() {
  loading.value = true;
  error.value = "";
  try {
    const [dash, exams, signed, pass] = await Promise.all([
      getEnrollmentDashboard(),
      getAdminExamRegistrations(),
      getEnrollmentLeads({ status: "已报名", page: 1, pageSize: 200 }),
      request<{ passedCount: number; totalCount: number; rate: number }>(
        "/admin/statistics/pass-rate"
      ).catch(() => ({ passedCount: 0, totalCount: 0, rate: 0 }))
    ]);
    dashboard.value = dash;
    examRows.value = exams ?? [];
    followLeads.value = signed.items ?? [];
    passRate.value = {
      passedCount: Number(pass.passedCount ?? 0),
      totalCount: Number(pass.totalCount ?? 0),
      rate: Number(pass.rate ?? 0)
    };
  } catch (err) {
    dashboard.value = null;
    examRows.value = [];
    followLeads.value = [];
    error.value = err instanceof Error ? err.message : "工作台数据加载失败";
  } finally {
    loading.value = false;
  }
}

function inferStage(student: EnrollmentLead): StageKey {
  const text = `${student.remark || ""} ${student.intentLevel || ""}`;
  if (text.includes("科目四")) return "subject4";
  if (text.includes("科目三")) return "subject3";
  if (text.includes("科目二")) return "subject2";
  return "subject1";
}

function funnelBarWidth(item: EnrollmentFunnelStat, list: EnrollmentFunnelStat[]) {
  const max = Math.max(...list.map(f => f.count), 1);
  return `${Math.max(8, (item.count / max) * 100)}%`;
}

function startOfWeek(date: Date) {
  const d = new Date(date);
  const day = d.getDay() || 7;
  d.setHours(0, 0, 0, 0);
  d.setDate(d.getDate() - day + 1);
  return d;
}

function parseDate(value: string) {
  const d = new Date(value);
  d.setHours(0, 0, 0, 0);
  return d;
}

function formatExamLine(row: ExamReservationAdminRow) {
  return `${row.username} · ${row.examType} · ${row.examDate} ${row.startTime || ""}`.trim();
}
</script>

<template>
  <div class="dashboard-shell">
    <header class="dashboard-head">
      <div>
        <p>驾校运营后台</p>
        <h1>工作台</h1>
      </div>
      <div class="head-actions">
        <span v-if="conversionText" class="hint">{{ conversionText }}</span>
        <button type="button" class="refresh-btn" :disabled="loading" @click="loadDashboard">
          {{ loading ? "加载中…" : "刷新" }}
        </button>
      </div>
    </header>

    <p v-if="error" class="message error">{{ error }}</p>

    <section class="metric-grid">
      <article>
        <span>今日报名意向</span>
        <strong>{{ todayNewLeads }}</strong>
      </article>
      <article>
        <span>待审核预约</span>
        <strong>{{ pendingExamCount }}</strong>
      </article>
      <article>
        <span>本周考试通过</span>
        <strong>{{ weekPassedCount }}</strong>
      </article>
      <article>
        <span>待跟进线索</span>
        <strong>{{ pendingFollowCount }}</strong>
      </article>
    </section>

    <section class="panel-grid">
      <article>
        <h2>待办事项</h2>
        <p v-if="loading" class="muted">加载中…</p>
        <template v-else>
          <div v-if="pendingExams.length" class="todo-block">
            <h3>待审核预约（{{ pendingExamCount }}）</h3>
            <ul>
              <li v-for="row in pendingExams" :key="row.id">
                {{ formatExamLine(row) }}
              </li>
            </ul>
          </div>
          <div v-if="completedNeedScore.length" class="todo-block">
            <h3>待录入成绩</h3>
            <ul>
              <li v-for="row in completedNeedScore" :key="row.id">
                {{ formatExamLine(row) }}（已通过审核）
              </li>
            </ul>
          </div>
          <p
            v-if="!pendingExams.length && !completedNeedScore.length"
            class="muted"
          >
            暂无待办预约
          </p>
        </template>
      </article>

      <article>
        <h2>科目进度（已报名学员）</h2>
        <p v-if="loading" class="muted">加载中…</p>
        <template v-else-if="followLeads.length">
          <ul class="stage-list">
            <li v-for="item in subjectSummary" :key="item.stage">
              <span>{{ item.label }}</span>
              <strong>{{ item.count }} 人</strong>
              <i
                class="stage-bar"
                :style="{
                  width: `${Math.max(8, (item.count / followLeads.length) * 100)}%`
                }"
              />
            </li>
          </ul>
          <p class="muted small">{{ passRateText }}</p>
        </template>
        <p v-else class="muted">暂无已报名学员</p>
      </article>
    </section>

    <section v-if="funnelPreview.length" class="funnel-section">
      <h2>招生漏斗</h2>
      <div class="funnel-list">
        <div
          v-for="item in funnelPreview"
          :key="item.stage"
          class="funnel-row"
        >
          <span class="funnel-label">{{ item.stage }}</span>
          <div class="funnel-track">
            <i
              class="funnel-fill"
              :style="{ width: funnelBarWidth(item, funnelPreview) }"
            />
          </div>
          <strong>{{ item.count }}</strong>
        </div>
      </div>
    </section>

    <section
      v-if="dashboard?.sourceDistribution?.length"
      class="source-section"
    >
      <h2>渠道分布</h2>
      <div class="source-list">
        <div
          v-for="item in dashboard.sourceDistribution"
          :key="item.source"
          class="source-row"
        >
          <span>{{ item.source }}</span>
          <strong>{{ item.count }}</strong>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.dashboard-shell {
  display: grid;
  gap: 16px;
  padding: 22px 28px;
}

.dashboard-head {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  justify-content: space-between;
  flex-wrap: wrap;
}

.dashboard-head p,
.dashboard-head h1 {
  margin: 0;
}

.dashboard-head p {
  font-size: 14px;
  color: #4f6b5f;
}

.dashboard-head h1 {
  margin-top: 4px;
  font-size: 28px;
  color: #14251e;
}

.head-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.hint {
  color: #64736c;
  font-size: 13px;
}

.refresh-btn {
  padding: 6px 14px;
  border: 1px solid #c5d4cc;
  border-radius: 6px;
  background: #fff;
  color: #21483a;
  cursor: pointer;
}

.refresh-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.message.error {
  margin: 0;
  padding: 10px 12px;
  color: #b42318;
  background: #fef3f2;
  border: 1px solid #fecdca;
  border-radius: 8px;
}

.metric-grid,
.panel-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.metric-grid article,
.panel-grid article,
.funnel-section,
.source-section {
  padding: 16px;
  background: #fff;
  border: 1px solid #dfe7e2;
  border-radius: 8px;
}

.metric-grid span {
  color: #64736c;
}

.metric-grid strong {
  display: block;
  margin-top: 10px;
  font-size: 28px;
  color: #21483a;
}

.panel-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.panel-grid h2,
.funnel-section h2,
.source-section h2 {
  margin: 0 0 12px;
  font-size: 18px;
  color: #14251e;
}

.muted {
  margin: 0;
  line-height: 1.7;
  color: #64736c;
}

.muted.small {
  margin-top: 12px;
  font-size: 13px;
}

.todo-block + .todo-block {
  margin-top: 14px;
}

.todo-block h3 {
  margin: 0 0 8px;
  font-size: 14px;
  color: #4f6b5f;
}

.todo-block ul,
.stage-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.todo-block li {
  padding: 6px 0;
  border-bottom: 1px solid #eef3ef;
  font-size: 14px;
  color: #2a3d34;
}

.stage-list li {
  position: relative;
  display: grid;
  grid-template-columns: 72px 56px 1fr;
  align-items: center;
  gap: 8px;
  padding: 8px 0;
}

.stage-list strong {
  color: #21483a;
}

.stage-bar {
  display: block;
  height: 8px;
  border-radius: 4px;
  background: linear-gradient(90deg, #5a9e7e, #21483a);
}

.funnel-section,
.source-section {
  grid-column: 1 / -1;
}

.funnel-list,
.source-list {
  display: grid;
  gap: 8px;
}

.funnel-row,
.source-row {
  display: grid;
  grid-template-columns: 88px 1fr 40px;
  align-items: center;
  gap: 10px;
  font-size: 14px;
}

.funnel-label {
  color: #4f6b5f;
}

.funnel-track {
  height: 10px;
  background: #eef3ef;
  border-radius: 5px;
  overflow: hidden;
}

.funnel-fill {
  display: block;
  height: 100%;
  background: #5a9e7e;
  border-radius: 5px;
}

.source-row {
  grid-template-columns: 1fr auto;
  padding: 6px 0;
  border-bottom: 1px solid #eef3ef;
}

@media (width <= 960px) {
  .metric-grid,
  .panel-grid {
    grid-template-columns: 1fr;
  }
}
</style>
