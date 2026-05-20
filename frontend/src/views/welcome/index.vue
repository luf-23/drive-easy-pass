<script setup lang="ts">
import { computed, onMounted, ref, useTemplateRef } from "vue";
import { useEchart } from "@/composables/useEchart";
import { getAdminExamRegistrations } from "@/api/admin";
import {
  getEnrollmentDashboard,
  getEnrollmentLeads
} from "@/api/enrollment";
import { request } from "@/services/api";
import type {
  EnrollmentDashboard,
  EnrollmentLead,
  ExamReservationAdminRow
} from "@/types";
import type { EChartsOption } from "echarts";

defineOptions({
  name: "Welcome"
});

type StageKey = "subject1" | "subject2" | "subject3" | "subject4";

const CHART_COLORS = [
  "#b8dcff",
  "#91caff",
  "#69b1ff",
  "#95de64",
  "#ffd666",
  "#ff9c6e",
  "#b37feb",
  "#5cdbd3"
];

const loading = ref(false);
const error = ref("");
const dashboard = ref<EnrollmentDashboard | null>(null);
const examRows = ref<ExamReservationAdminRow[]>([]);
const followLeads = ref<EnrollmentLead[]>([]);
const passRate = ref({ passedCount: 0, totalCount: 0, rate: 0 });

const funnelChartRef = useTemplateRef<HTMLElement>("funnelChartRef");
const sourceChartRef = useTemplateRef<HTMLElement>("sourceChartRef");

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

const sourcePreview = computed(
  () => dashboard.value?.sourceDistribution ?? []
);

const hasFunnelChart = computed(() => funnelPreview.value.length > 0);

const hasSourceChart = computed(() => sourcePreview.value.length > 0);

const funnelChartOption = computed<EChartsOption | null>(() => {
  const list = funnelPreview.value;
  if (!list.length) return null;
  const stages = list.map(f => f.stage);
  const counts = list.map(f => f.count);
  return {
    color: ["#69b1ff"],
    tooltip: {
      trigger: "axis",
      axisPointer: { type: "shadow" }
    },
    grid: {
      left: 4,
      right: 48,
      top: 12,
      bottom: 8,
      containLabel: true
    },
    xAxis: {
      type: "value",
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: {
        lineStyle: { type: "dashed", color: "#e5e7eb" }
      }
    },
    yAxis: {
      type: "category",
      data: stages,
      inverse: true,
      axisLine: { show: false },
      axisTick: { show: false },
      axisLabel: {
        color: "#4b5563",
        fontSize: 12
      }
    },
    series: [
      {
        name: "线索数",
        type: "bar",
        data: counts,
        barMaxWidth: 22,
        itemStyle: {
          borderRadius: [0, 6, 6, 0],
          color: "#69b1ff",
          borderColor: "#1a1a1a",
          borderWidth: 1
        },
        label: {
          show: true,
          position: "right",
          color: "#1a1a1a",
          fontWeight: 600
        }
      }
    ]
  };
});

const sourceChartOption = computed<EChartsOption | null>(() => {
  const list = sourcePreview.value;
  if (!list.length) return null;
  return {
    color: CHART_COLORS,
    tooltip: {
      trigger: "item",
      formatter: "{b}<br/>{c} 条 ({d}%)"
    },
    legend: {
      type: "scroll",
      bottom: 4,
      left: "center",
      textStyle: { fontSize: 12, color: "#4b5563" }
    },
    series: [
      {
        name: "渠道",
        type: "pie",
        radius: ["40%", "62%"],
        center: ["50%", "42%"],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 6,
          borderColor: "#fff",
          borderWidth: 2
        },
        label: {
          show: true,
          fontSize: 11,
          formatter: "{b}\n{d}%"
        },
        emphasis: {
          label: { show: true, fontSize: 12, fontWeight: "bold" }
        },
        data: list.map(item => ({
          name: item.source,
          value: item.count
        }))
      }
    ]
  };
});

useEchart(funnelChartRef, funnelChartOption);
useEchart(sourceChartRef, sourceChartOption);

const passRateText = computed(() => {
  const { passedCount, totalCount, rate } = passRate.value;
  if (!totalCount) return "暂无已完成考试成绩";
  return `累计 ${totalCount} 场考试，通过 ${passedCount} 场（${(rate * 100).toFixed(1)}%）`;
});

const funnelChartHeight = computed(() => {
  const n = funnelPreview.value.length;
  return `${Math.max(260, Math.min(420, n * 44 + 48))}px`;
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
    <header class="dashboard-head dep-page-header">
      <div>
        <p class="dep-page-eyebrow">驾校运营后台</p>
        <h1>工作台</h1>
      </div>
      <div class="head-actions">
        <span v-if="conversionText" class="hint">{{ conversionText }}</span>
        <button
          type="button"
          class="refresh-btn"
          :disabled="loading"
          @click="loadDashboard"
        >
          {{ loading ? "加载中…" : "刷新" }}
        </button>
      </div>
    </header>

    <div class="dashboard-body">
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

      <section class="charts-grid">
        <article class="chart-card">
          <h2>招生漏斗</h2>
          <p v-if="loading" class="muted chart-placeholder">加载中…</p>
          <p v-else-if="!hasFunnelChart" class="muted chart-placeholder">
            暂无漏斗数据
          </p>
          <div
            v-if="hasFunnelChart && !loading"
            ref="funnelChartRef"
            class="chart-canvas"
            :style="{ height: funnelChartHeight }"
          />
        </article>

        <article class="chart-card">
          <h2>渠道分布</h2>
          <p v-if="loading" class="muted chart-placeholder">加载中…</p>
          <p v-else-if="!hasSourceChart" class="muted chart-placeholder">
            暂无渠道数据
          </p>
          <div
            v-if="hasSourceChart && !loading"
            ref="sourceChartRef"
            class="chart-canvas chart-canvas--pie"
          />
        </article>
      </section>
    </div>
  </div>
</template>

<style scoped>
.dashboard-shell {
  display: flex;
  flex-direction: column;
  height: 100%;
  min-height: 0;
  padding: var(--dep-page-pad, 14px 18px);
  overflow: hidden;
}

.dashboard-head h1 {
  margin: 0;
}

.head-actions {
  display: flex;
  gap: 12px;
  align-items: center;
}

.hint {
  font-size: 13px;
  color: #64736c;
}

.refresh-btn {
  padding: 6px 14px;
  font-weight: 600;
  color: #1a1a1a;
  cursor: pointer;
  background: #b8dcff;
  border: 1px solid #1a1a1a;
  border-radius: 8px;
}

.refresh-btn:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.dashboard-body {
  display: flex;
  flex: 1;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
  padding-right: 2px;
  overflow-y: auto;
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
.panel-grid,
.charts-grid {
  display: grid;
  gap: 12px;
}

.metric-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
  flex-shrink: 0;
}

.panel-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  flex-shrink: 0;
}

.charts-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
  flex-shrink: 0;
}

.metric-grid article,
.panel-grid article,
.chart-card {
  padding: 14px 16px;
  background: #fff;
  border: 1px solid #d9dde3;
  border-radius: 10px;
}

.metric-grid span {
  color: #64736c;
}

.metric-grid strong {
  display: block;
  margin-top: 8px;
  color: var(--dep-accent-text, #1677ff);
}

.panel-grid h2,
.chart-card h2 {
  margin: 0 0 10px;
  font-size: 15px;
  font-weight: 700;
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

.chart-placeholder {
  display: grid;
  place-items: center;
  min-height: 260px;
}

.chart-canvas {
  width: 100%;
  min-height: 260px;
}

.chart-canvas--pie {
  height: 300px;
}

.chart-card {
  display: flex;
  flex-direction: column;
  min-height: 300px;
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
  font-size: 14px;
  color: #2a3d34;
  border-bottom: 1px solid #eef3ef;
}

.stage-list li {
  display: grid;
  grid-template-columns: 72px 56px 1fr;
  gap: 8px;
  align-items: center;
  padding: 8px 0;
}

.stage-list strong {
  color: #21483a;
}

.stage-bar {
  display: block;
  height: 8px;
  border-radius: 4px;
  background: linear-gradient(90deg, #91caff, #1677ff);
}

@media (width <= 960px) {
  .metric-grid,
  .panel-grid,
  .charts-grid {
    grid-template-columns: 1fr;
  }
}
</style>
