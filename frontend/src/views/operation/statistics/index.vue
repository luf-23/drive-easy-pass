<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import {
  getEnrollmentTrend,
  getPassRate,
  getStatisticsOverview,
  type EnrollmentTrendRow,
  type PassRateStats,
  type StatisticsOverview
} from "@/api/statistics";

defineOptions({
  name: "StatisticsCenter"
});

const loading = ref(false);
const error = ref("");

const overview = ref<StatisticsOverview>({
  totalUsers: 0,
  activeUsers: 0,
  totalEnrollments: 0,
  monthlyEnrollments: 0,
  totalExams: 0
});

const trendData = ref<EnrollmentTrendRow[]>([]);
const passRateData = ref<PassRateStats>({
  passedCount: 0,
  totalCount: 0,
  rate: 0
});

const hasSubjectPassRate = computed(() =>
  ["subject1", "subject2", "subject3", "subject4"].some(
    key => passRateData.value[key as keyof PassRateStats] !== undefined
  )
);

const passRateSummary = computed(() => {
  const { passedCount, totalCount, rate } = passRateData.value;
  if (!totalCount) return "暂无已完成考试成绩";
  return `共 ${totalCount} 场，通过 ${passedCount} 场，总通过率 ${(rate * 100).toFixed(1)}%`;
});

onMounted(() => {
  loadAll();
});

async function loadAll() {
  loading.value = true;
  error.value = "";
  try {
    const [overviewRes, trendRes, passRes] = await Promise.all([
      getStatisticsOverview(),
      getEnrollmentTrend(),
      getPassRate()
    ]);
    overview.value = {
      totalUsers: Number(overviewRes?.totalUsers ?? 0),
      activeUsers: Number(overviewRes?.activeUsers ?? 0),
      totalEnrollments: Number(overviewRes?.totalEnrollments ?? 0),
      monthlyEnrollments: Number(overviewRes?.monthlyEnrollments ?? 0),
      totalExams: Number(overviewRes?.totalExams ?? 0)
    };
    trendData.value = normalizeTrend(trendRes);
    passRateData.value = {
      passedCount: Number(passRes?.passedCount ?? 0),
      totalCount: Number(passRes?.totalCount ?? 0),
      rate: Number(passRes?.rate ?? 0),
      subject1: passRes?.subject1,
      subject2: passRes?.subject2,
      subject3: passRes?.subject3,
      subject4: passRes?.subject4
    };
  } catch (e) {
    error.value = e instanceof Error ? e.message : "统计数据加载失败";
  } finally {
    loading.value = false;
  }
}

function normalizeTrend(raw: unknown): EnrollmentTrendRow[] {
  if (!Array.isArray(raw)) return [];
  return raw.map(row => {
    const item = row as Record<string, unknown>;
    const month = String(item.month ?? item.date ?? "");
    return {
      month,
      count: Number(item.count ?? 0)
    };
  });
}

const customColors = [
  { color: "#f56c6c", percentage: 20 },
  { color: "#e6a23c", percentage: 40 },
  { color: "#5cb87a", percentage: 60 },
  { color: "#1989fa", percentage: 80 },
  { color: "#6f7ad3", percentage: 100 }
];
</script>

<template>
  <div class="statistics-center main">
    <header class="page-head dep-page-header">
      <h1>统计分析</h1>
      <el-button :loading="loading" @click="loadAll">刷新</el-button>
    </header>

    <el-alert v-if="error" :title="error" type="error" show-icon class="mb-4" />

    <el-row :gutter="16" class="mb-4">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" v-loading="loading">
          <template #header>
            <div class="font-bold text-gray-700">总用户数</div>
          </template>
          <div class="text-2xl font-bold text-blue-500">
            {{ overview.totalUsers }}
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" v-loading="loading">
          <template #header>
            <div class="font-bold text-gray-700">启用用户数</div>
          </template>
          <div class="text-2xl font-bold text-green-500">
            {{ overview.activeUsers }}
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" v-loading="loading">
          <template #header>
            <div class="font-bold text-gray-700">招生线索总数</div>
          </template>
          <div class="text-2xl font-bold text-purple-500">
            {{ overview.totalEnrollments }}
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" v-loading="loading">
          <template #header>
            <div class="font-bold text-gray-700">本月新增线索</div>
          </template>
          <div class="text-2xl font-bold text-orange-500">
            {{ overview.monthlyEnrollments }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" class="mb-4">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" v-loading="loading">
          <template #header>
            <div class="font-bold text-gray-700">考试预约总数</div>
          </template>
          <div class="text-2xl font-bold text-teal-600">
            {{ overview.totalExams ?? 0 }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" v-loading="loading">
          <template #header>
            <span class="font-bold text-gray-700">报名趋势（近 6 个月）</span>
          </template>
          <el-table
            v-if="trendData.length"
            :data="trendData"
            style="width: 100%"
            height="250"
          >
            <el-table-column prop="month" label="月份" />
            <el-table-column prop="count" label="线索数量" />
          </el-table>
          <el-empty v-else description="暂无趋势数据" />
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover" v-loading="loading">
          <template #header>
            <span class="font-bold text-gray-700">考试通过率（按科目）</span>
          </template>
          <p v-if="passRateData.totalCount" class="pass-summary">
            {{ passRateSummary }}
          </p>
          <div v-if="hasSubjectPassRate" class="p-4 space-y-4">
            <div class="flex items-center">
              <span class="inline-block w-20 text-gray-600">科目一:</span>
              <el-progress
                class="flex-1"
                :percentage="passRateData.subject1 ?? 0"
                :color="customColors"
              />
            </div>
            <div class="flex items-center">
              <span class="inline-block w-20 text-gray-600">科目二:</span>
              <el-progress
                class="flex-1"
                :percentage="passRateData.subject2 ?? 0"
                :color="customColors"
              />
            </div>
            <div class="flex items-center">
              <span class="inline-block w-20 text-gray-600">科目三:</span>
              <el-progress
                class="flex-1"
                :percentage="passRateData.subject3 ?? 0"
                :color="customColors"
              />
            </div>
            <div class="flex items-center">
              <span class="inline-block w-20 text-gray-600">科目四:</span>
              <el-progress
                class="flex-1"
                :percentage="passRateData.subject4 ?? 0"
                :color="customColors"
              />
            </div>
          </div>
          <el-empty v-else description="暂无通过率数据（需有已录入成绩的考试）" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.page-head {
  display: flex;
  flex-shrink: 0;
  align-items: center;
  justify-content: space-between;
}

.page-head h1 {
  margin: 0;
}

.statistics-center :deep(.el-row:last-of-type) {
  flex: 1;
  min-height: 0;
  overflow: auto;
}

.pass-summary {
  margin: 0 0 12px;
  padding: 0 16px;
  font-size: 13px;
  color: #64736c;
}
</style>
