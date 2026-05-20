<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { getSignedStudents } from "@/api/enrollment";
import type { EnrollmentLead } from "@/types";

defineOptions({
  name: "StudentManagement"
});

const keyword = ref("");
const page = ref(1);
const pageSize = ref(10);
const total = ref(0);
const loading = ref(false);
const error = ref("");
const students = ref<EnrollmentLead[]>([]);

const totalPages = computed(() =>
  Math.max(1, Math.ceil(total.value / pageSize.value))
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
      page: page.value,
      pageSize: pageSize.value
    });
    students.value = result.items;
    total.value = result.total;
  } catch (err) {
    students.value = [];
    total.value = 0;
    error.value = err instanceof Error ? err.message : "学员列表加载失败";
  } finally {
    loading.value = false;
  }
}

function search() {
  page.value = 1;
  loadStudents();
}

function resetSearch() {
  keyword.value = "";
  page.value = 1;
  loadStudents();
}

function prevPage() {
  if (page.value <= 1 || loading.value) return;
  page.value -= 1;
  loadStudents();
}

function nextPage() {
  if (page.value >= totalPages.value || loading.value) return;
  page.value += 1;
  loadStudents();
}

function formatDateTime(value: string | null) {
  if (!value) return "-";
  return value.replace("T", " ").slice(0, 16);
}
</script>

<template>
  <div class="student-page dep-page dep-page--fill">
    <header class="dep-page-header student-header">
      <div>
        <p class="dep-page-eyebrow">Student Pool</p>
        <h1>学员管理</h1>
      </div>
      <strong class="dep-page-metric">{{ total }}</strong>
    </header>

    <section class="student-panel dep-card dep-card-fill">
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

      <div v-if="error" class="error-message">{{ error }}</div>

      <div class="student-table dep-table-scroll">
        <div class="student-row student-head">
          <span>姓名</span>
          <span>手机号</span>
          <span>来源</span>
          <span>意向</span>
          <span>状态</span>
          <span>负责人</span>
          <span>下次跟进</span>
          <span>创建时间</span>
        </div>

        <div v-if="loading" class="empty-state">加载中...</div>
        <div v-else-if="students.length === 0" class="empty-state">
          暂无已报名学员
        </div>
        <template v-else>
          <div
            v-for="student in students"
            :key="student.id"
            class="student-row"
          >
            <span class="name-cell">{{ student.name }}</span>
            <span>{{ student.phone }}</span>
            <span>{{ student.source || "-" }}</span>
            <span>{{ student.intentLevel || "-" }}</span>
            <span><i class="status-tag">已报名</i></span>
            <span>{{ student.ownerName || "未分配" }}</span>
            <span>{{ formatDateTime(student.nextFollowTime) }}</span>
            <span>{{ formatDateTime(student.createTime) }}</span>
          </div>
        </template>
      </div>

      <div class="pager-row dep-pagination">
        <button class="ghost" :disabled="page <= 1 || loading" @click="prevPage">
          上一页
        </button>
        <span>第 {{ page }} / {{ totalPages }} 页</span>
        <button
          class="ghost"
          :disabled="page >= totalPages || loading"
          @click="nextPage"
        >
          下一页
        </button>
      </div>
    </section>
  </div>
</template>

<style scoped>
.student-header {
  padding: 0;
  background: transparent;
  border: none;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: end;
  justify-content: space-between;
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

.actions,
.pager-row {
  display: flex;
  gap: 10px;
  align-items: center;
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

.student-table {
  overflow-x: auto;
}

.student-row {
  display: grid;
  grid-template-columns: 100px 140px 110px 80px 90px 110px 140px 140px;
  gap: 12px;
  align-items: center;
  min-width: 920px;
  padding: 12px;
  border-bottom: 1px solid #eef2ec;
}

.student-head {
  font-size: 13px;
  font-weight: 700;
  color: #4d5c54;
  background: #f7f9f6;
}

.name-cell {
  font-weight: 700;
  color: #21483a;
}

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  font-size: 12px;
  font-style: normal;
  font-weight: 700;
  color: #0f6f43;
  background: #d8f4e6;
  border-radius: 999px;
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

.pager-row {
  justify-content: flex-end;
  margin-top: 14px;
}

@media (width <= 760px) {
  .student-header {
    align-items: flex-start;
  }

  .toolbar,
  label {
    width: 100%;
  }

  .actions {
    justify-content: flex-start;
  }
}
</style>
