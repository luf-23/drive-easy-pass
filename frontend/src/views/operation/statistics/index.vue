<script setup lang="ts">
import { ref, onMounted } from "vue";
import {
  getStatisticsOverview,
  getEnrollmentTrend,
  getPassRate
} from "@/api/statistics";

defineOptions({
  name: "StatisticsCenter"
});

const overview = ref({
  totalUsers: 0,
  activeUsers: 0,
  totalEnrollments: 0,
  monthlyEnrollments: 0
});

const trendData = ref([]);
const passRateData = ref<any>({});

const fetchOverview = async () => {
  try {
    const res = await getStatisticsOverview();
    if (res?.success) {
      overview.value = res.data;
    }
  } catch (e) {
    console.error(e);
  }
};

const fetchTrend = async () => {
  try {
    const res = await getEnrollmentTrend({ lastMonths: 6 });
    if (res?.success && Array.isArray(res.data)) {
      trendData.value = res.data;
    } else if (res?.success && res.data?.trends) {
      trendData.value = res.data.trends;
    }
  } catch (e) {
    console.error(e);
  }
};

const fetchPassRate = async () => {
  try {
    const res = await getPassRate();
    if (res?.success) {
      passRateData.value = res.data;
    }
  } catch (e) {
    console.error(e);
  }
};

onMounted(() => {
  fetchOverview();
  fetchTrend();
  fetchPassRate();
});

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
    <el-row :gutter="16" class="mb-4">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <template #header>
            <div class="font-bold text-gray-700">总用户数</div>
          </template>
          <div class="text-3xl font-bold text-blue-500">
            {{ overview.totalUsers || 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <template #header>
            <div class="font-bold text-gray-700">活跃用户数</div>
          </template>
          <div class="text-3xl font-bold text-green-500">
            {{ overview.activeUsers || 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <template #header>
            <div class="font-bold text-gray-700">总报名数</div>
          </template>
          <div class="text-3xl font-bold text-purple-500">
            {{ overview.totalEnrollments || 0 }}
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover">
          <template #header>
            <div class="font-bold text-gray-700">本月新增报名</div>
          </template>
          <div class="text-3xl font-bold text-orange-500">
            {{ overview.monthlyEnrollments || 0 }}
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16">
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <span class="font-bold text-gray-700">报名趋势 (近6个月)</span>
          </template>
          <el-table :data="trendData" style="width: 100%" height="250">
            <el-table-column prop="month" label="月份" />
            <el-table-column prop="count" label="报名人数" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :xs="24" :md="12">
        <el-card shadow="hover">
          <template #header>
            <span class="font-bold text-gray-700">考试通过率</span>
          </template>
          <div v-if="passRateData.subject1 !== undefined" class="p-4 space-y-4">
            <div class="flex items-center">
              <span class="inline-block w-20 text-gray-600">科目一:</span>
              <el-progress
                class="flex-1"
                :percentage="passRateData.subject1 || 0"
                :color="customColors"
              />
            </div>
            <div class="flex items-center">
              <span class="inline-block w-20 text-gray-600">科目二:</span>
              <el-progress
                class="flex-1"
                :percentage="passRateData.subject2 || 0"
                :color="customColors"
              />
            </div>
            <div class="flex items-center">
              <span class="inline-block w-20 text-gray-600">科目三:</span>
              <el-progress
                class="flex-1"
                :percentage="passRateData.subject3 || 0"
                :color="customColors"
              />
            </div>
            <div class="flex items-center">
              <span class="inline-block w-20 text-gray-600">科目四:</span>
              <el-progress
                class="flex-1"
                :percentage="passRateData.subject4 || 0"
                :color="customColors"
              />
            </div>
          </div>
          <el-empty v-else description="暂无通过率数据" />
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
