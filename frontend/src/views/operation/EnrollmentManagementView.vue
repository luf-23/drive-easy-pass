<script setup lang="ts">
import { computed, onMounted, reactive, ref } from "vue";
import {
  assignLeadOwner,
  createEnrollmentLead,
  createLeadFollowUp,
  getEnrollmentDashboard,
  getEnrollmentLeads,
  getLeadFollowUps,
  getSignedStudents,
  updateEnrollmentLead
} from "@/api/enrollment";
import type {
  EnrollmentDashboard,
  EnrollmentFollowUp,
  EnrollmentFollowUpPayload,
  EnrollmentLead,
  EnrollmentLeadPayload
} from "@/types";

const statusOptions = ["新线索", "已联系", "已到访", "已报名", "无效"];
const sourceOptions = ["线上广告", "地推", "转介绍", "抖音", "小红书", "门店", "其他"];
const followTypeOptions = ["电话", "微信", "到店", "短信", "其他"];

const activeTab = ref<"leads" | "students">("leads");
const dashboard = ref<EnrollmentDashboard | null>(null);
const leads = ref<EnrollmentLead[]>([]);
const students = ref<EnrollmentLead[]>([]);
const total = ref(0);
const studentsTotal = ref(0);
const page = ref(1);
const pageSize = ref(10);
const loading = ref(false);
const saving = ref(false);
const error = ref("");

const filters = reactive({
  keyword: "",
  status: "",
  source: "",
  startDate: "",
  endDate: ""
});

const editingId = ref<number | null>(null);
const leadForm = reactive<EnrollmentLeadPayload>({
  name: "",
  phone: "",
  source: "其他",
  intentLevel: "中",
  status: "新线索",
  ownerUserId: null,
  nextFollowTime: null,
  remark: ""
});

const selectedLead = ref<EnrollmentLead | null>(null);
const followUps = ref<EnrollmentFollowUp[]>([]);
const followLoading = ref(false);
const followSaving = ref(false);
const followForm = reactive<EnrollmentFollowUpPayload>({
  content: "",
  followType: "电话",
  nextFollowTime: null
});

const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)));
const studentPages = computed(() => Math.max(1, Math.ceil(studentsTotal.value / pageSize.value)));

onMounted(async () => {
  await Promise.all([loadDashboard(), loadLeads(), loadStudents()]);
});

async function loadDashboard() {
  try {
    dashboard.value = await getEnrollmentDashboard();
  } catch {
    dashboard.value = null;
  }
}

async function loadLeads() {
  loading.value = true;
  error.value = "";
  try {
    const result = await getEnrollmentLeads({
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
      source: filters.source || undefined,
      startDate: normalizeDateTime(filters.startDate),
      endDate: normalizeDateTime(filters.endDate),
      page: page.value,
      pageSize: pageSize.value
    });
    leads.value = result.items;
    total.value = result.total;

    if (selectedLead.value) {
      const refreshed = result.items.find(item => item.id === selectedLead.value?.id) || null;
      selectedLead.value = refreshed;
      if (!refreshed) {
        followUps.value = [];
      }
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : "招生线索加载失败";
  } finally {
    loading.value = false;
  }
}

async function loadStudents() {
  try {
    const result = await getSignedStudents({
      keyword: filters.keyword || undefined,
      page: page.value,
      pageSize: pageSize.value
    });
    students.value = result.items;
    studentsTotal.value = result.total;
  } catch {
    students.value = [];
    studentsTotal.value = 0;
  }
}

function resetFilters() {
  filters.keyword = "";
  filters.status = "";
  filters.source = "";
  filters.startDate = "";
  filters.endDate = "";
  page.value = 1;
  search();
}

function resetLeadForm() {
  editingId.value = null;
  Object.assign(leadForm, {
    name: "",
    phone: "",
    source: "其他",
    intentLevel: "中",
    status: "新线索",
    ownerUserId: null,
    nextFollowTime: null,
    remark: ""
  });
}

function editLead(lead: EnrollmentLead) {
  editingId.value = lead.id;
  Object.assign(leadForm, {
    name: lead.name,
    phone: lead.phone,
    source: lead.source || "其他",
    intentLevel: lead.intentLevel || "中",
    status: lead.status || "新线索",
    ownerUserId: lead.ownerUserId,
    nextFollowTime: toDateTimeInput(lead.nextFollowTime),
    remark: lead.remark || ""
  });
}

async function saveLead() {
  saving.value = true;
  error.value = "";
  try {
    const payload: EnrollmentLeadPayload = {
      ...leadForm,
      source: leadForm.source || "其他",
      intentLevel: leadForm.intentLevel || "中",
      status: leadForm.status || "新线索",
      nextFollowTime: leadForm.nextFollowTime || null,
      remark: leadForm.remark || ""
    };

    if (editingId.value) {
      await updateEnrollmentLead(editingId.value, payload);
    } else {
      await createEnrollmentLead(payload);
    }

    resetLeadForm();
    await Promise.all([loadLeads(), loadStudents(), loadDashboard()]);
  } catch (err) {
    error.value = err instanceof Error ? err.message : "线索保存失败";
  } finally {
    saving.value = false;
  }
}

async function quickAssignOwner(lead: EnrollmentLead) {
  const input = window.prompt(`为 ${lead.name} 分配负责人用户ID`, lead.ownerUserId ? String(lead.ownerUserId) : "");
  if (input === null) return;
  const ownerId = Number(input);
  if (!Number.isInteger(ownerId) || ownerId <= 0) {
    error.value = "负责人用户ID必须是正整数";
    return;
  }
  try {
    await assignLeadOwner(lead.id, ownerId);
    await Promise.all([loadLeads(), loadDashboard()]);
  } catch (err) {
    error.value = err instanceof Error ? err.message : "分配负责人失败";
  }
}

async function exportLeads() {
  try {
    const result = await getEnrollmentLeads({
      keyword: filters.keyword || undefined,
      status: filters.status || undefined,
      source: filters.source || undefined,
      startDate: normalizeDateTime(filters.startDate),
      endDate: normalizeDateTime(filters.endDate),
      page: 1,
      pageSize: 1000
    });

    const headers = ["姓名", "手机号", "来源", "意向", "状态", "负责人", "下次跟进", "创建时间"];
    const rows = result.items.map(item => [
      item.name,
      item.phone,
      item.source,
      item.intentLevel,
      item.status,
      item.ownerName || "未分配",
      formatDateTime(item.nextFollowTime),
      formatDateTime(item.createTime)
    ]);

    const csv = [headers, ...rows]
      .map(row => row.map(cell => `"${String(cell).replaceAll('"', '""')}"`).join(","))
      .join("\n");

    const blob = new Blob(["\uFEFF" + csv], { type: "text/csv;charset=utf-8;" });
    const link = document.createElement("a");
    link.href = URL.createObjectURL(blob);
    link.download = `招生线索-${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
    URL.revokeObjectURL(link.href);
  } catch (err) {
    error.value = err instanceof Error ? err.message : "导出失败";
  }
}

async function pickLead(lead: EnrollmentLead) {
  selectedLead.value = lead;
  followLoading.value = true;
  error.value = "";
  try {
    followUps.value = await getLeadFollowUps(lead.id);
  } catch (err) {
    error.value = err instanceof Error ? err.message : "跟进记录加载失败";
  } finally {
    followLoading.value = false;
  }
}

async function saveFollowUp() {
  if (!selectedLead.value) return;

  followSaving.value = true;
  error.value = "";
  try {
    await createLeadFollowUp(selectedLead.value.id, {
      ...followForm,
      nextFollowTime: followForm.nextFollowTime || null
    });
    Object.assign(followForm, {
      content: "",
      followType: "电话",
      nextFollowTime: null
    });
    followUps.value = await getLeadFollowUps(selectedLead.value.id);
    await Promise.all([loadLeads(), loadDashboard()]);
  } catch (err) {
    error.value = err instanceof Error ? err.message : "新增跟进失败";
  } finally {
    followSaving.value = false;
  }
}

async function search() {
  page.value = 1;
  await Promise.all([loadLeads(), loadStudents(), loadDashboard()]);
}

async function prevPage() {
  if (page.value <= 1) return;
  page.value -= 1;
  await syncByTab();
}

async function nextPage() {
  const max = activeTab.value === "leads" ? totalPages.value : studentPages.value;
  if (page.value >= max) return;
  page.value += 1;
  await syncByTab();
}

async function syncByTab() {
  if (activeTab.value === "leads") {
    await loadLeads();
    return;
  }
  await loadStudents();
}

function switchTab(tab: "leads" | "students") {
  activeTab.value = tab;
  page.value = 1;
  syncByTab();
}

function toDateTimeInput(value: string | null) {
  if (!value) return null;
  return value.slice(0, 16);
}

function formatDateTime(value: string | null) {
  if (!value) return "-";
  return value.replace("T", " ").slice(0, 16);
}

function normalizeDateTime(value: string) {
  if (!value) return undefined;
  return value;
}

function statusClass(status: string) {
  if (status === "已报名") return "status-tag signed";
  if (status === "已到访") return "status-tag visited";
  if (status === "已联系") return "status-tag contacted";
  if (status === "无效") return "status-tag invalid";
  return "status-tag fresh";
}
</script>

<template>
  <div class="system-page enrollment-page">
    <div v-if="error" class="message error">{{ error }}</div>

    <section class="system-panel" v-if="dashboard">
      <div class="section-head">
        <div>
          <p class="eyebrow">Enrollment Dashboard</p>
          <h2>招生数据看板</h2>
        </div>
      </div>

      <div class="dash-grid">
        <article>
          <strong>{{ dashboard.todayNewLeads }}</strong>
          <span>今日新增线索</span>
        </article>
        <article>
          <strong>{{ dashboard.monthConversionRate.toFixed(1) }}%</strong>
          <span>本月转化率</span>
        </article>
        <article>
          <strong>{{ dashboard.sourceDistribution.length }}</strong>
          <span>渠道数量</span>
        </article>
      </div>

      <div class="chart-grid">
        <div class="chart-panel">
          <h3>渠道占比</h3>
          <div v-for="item in dashboard.sourceDistribution" :key="item.source" class="bar-line">
            <span>{{ item.source }}</span>
            <div class="bar-bg">
              <div class="bar" :style="{ width: `${Math.max(4, (item.count / (dashboard.sourceDistribution[0]?.count || 1)) * 100)}%` }" />
            </div>
            <strong>{{ item.count }}</strong>
          </div>
        </div>
        <div class="chart-panel">
          <h3>负责人业绩排行</h3>
          <div v-for="item in dashboard.ownerRanking" :key="`${item.ownerUserId}-${item.ownerName}`" class="bar-line">
            <span>{{ item.ownerName }}</span>
            <div class="bar-bg">
              <div class="bar owner" :style="{ width: `${Math.max(4, (item.signedCount / (dashboard.ownerRanking[0]?.signedCount || 1)) * 100)}%` }" />
            </div>
            <strong>{{ item.signedCount }}</strong>
          </div>
        </div>
      </div>

      <div class="funnel-panel">
        <h3>阶段漏斗（线索→到店→报名）</h3>
        <div class="funnel-list">
          <div v-for="item in dashboard.funnel" :key="item.stage" class="funnel-item">
            <span>{{ item.stage }}</span>
            <strong>{{ item.count }}</strong>
          </div>
        </div>
      </div>
    </section>

    <section class="system-panel">
      <div class="section-head">
        <div>
          <p class="eyebrow">Lead Center</p>
          <h2>线索中心</h2>
        </div>
        <span class="pill">线索 {{ total }} 条 / 已报名 {{ studentsTotal }} 条</span>
      </div>

      <div class="tab-row">
        <button :class="['ghost', { active: activeTab === 'leads' }]" @click="switchTab('leads')">线索列表</button>
        <button :class="['ghost', { active: activeTab === 'students' }]" @click="switchTab('students')">学员池</button>
      </div>

      <form class="system-form enrollment-filter" @submit.prevent="search">
        <label>
          关键词
          <input v-model="filters.keyword" placeholder="姓名或手机号" />
        </label>
        <label>
          状态
          <select v-model="filters.status" :disabled="activeTab === 'students'">
            <option value="">全部</option>
            <option v-for="item in statusOptions" :key="item" :value="item">{{ item }}</option>
          </select>
        </label>
        <label>
          来源
          <select v-model="filters.source" :disabled="activeTab === 'students'">
            <option value="">全部</option>
            <option v-for="item in sourceOptions" :key="item" :value="item">{{ item }}</option>
          </select>
        </label>
        <label>
          开始时间
          <input v-model="filters.startDate" type="datetime-local" :disabled="activeTab === 'students'" />
        </label>
        <label>
          结束时间
          <input v-model="filters.endDate" type="datetime-local" :disabled="activeTab === 'students'" />
        </label>
        <div class="system-form-actions">
          <button class="primary" :disabled="loading" type="submit">查询</button>
          <button class="ghost" type="button" @click="resetFilters">重置</button>
          <button class="ghost" type="button" @click="exportLeads" :disabled="activeTab === 'students'">导出CSV</button>
        </div>
      </form>

      <div v-if="loading" class="message">正在加载数据...</div>

      <div class="system-table enrollment-table" v-if="activeTab === 'leads'">
        <div class="system-table-row system-table-head">
          <span>姓名</span>
          <span>手机号</span>
          <span>来源</span>
          <span>意向</span>
          <span>状态</span>
          <span>负责人</span>
          <span>下次跟进</span>
          <span>创建时间</span>
          <span>操作</span>
        </div>
        <div v-for="lead in leads" :key="lead.id" class="system-table-row" :class="{ active: selectedLead?.id === lead.id }">
          <span class="name-link" @click="pickLead(lead)">{{ lead.name }}</span>
          <span>
            {{ lead.phone }}
            <a class="dial-link" :href="`tel:${lead.phone}`">拨号</a>
          </span>
          <span>{{ lead.source }}</span>
          <span>{{ lead.intentLevel }}</span>
          <span>
            <i :class="statusClass(lead.status)">{{ lead.status }}</i>
          </span>
          <span>{{ lead.ownerName || "未分配" }}</span>
          <span>{{ formatDateTime(lead.nextFollowTime) }}</span>
          <span>{{ formatDateTime(lead.createTime) }}</span>
          <span class="table-actions">
            <button class="ghost" @click="editLead(lead)">编辑</button>
            <button class="ghost" @click="quickAssignOwner(lead)">分配</button>
          </span>
        </div>
      </div>

      <div class="system-table enrollment-table" v-else>
        <div class="system-table-row system-table-head">
          <span>姓名</span>
          <span>手机号</span>
          <span>来源</span>
          <span>状态</span>
          <span>负责人</span>
          <span>创建时间</span>
        </div>
        <div v-for="student in students" :key="student.id" class="system-table-row">
          <span>{{ student.name }}</span>
          <span>{{ student.phone }}</span>
          <span>{{ student.source }}</span>
          <span><i class="status-tag signed">已报名</i></span>
          <span>{{ student.ownerName || "未分配" }}</span>
          <span>{{ formatDateTime(student.createTime) }}</span>
        </div>
      </div>

      <div class="pager-row">
        <button class="ghost" :disabled="page <= 1 || loading" @click="prevPage">上一页</button>
        <span>
          第 {{ page }} / {{ activeTab === 'leads' ? totalPages : studentPages }} 页
        </span>
        <button
          class="ghost"
          :disabled="page >= (activeTab === 'leads' ? totalPages : studentPages) || loading"
          @click="nextPage"
        >
          下一页
        </button>
      </div>
    </section>

    <section class="system-panel">
      <div class="section-head">
        <div>
          <p class="eyebrow">Lead Detail</p>
          <h2>{{ selectedLead ? `线索详情 - ${selectedLead.name}` : "线索详情/跟进面板" }}</h2>
        </div>
      </div>

      <div v-if="!selectedLead" class="empty-state">点击线索姓名查看详情与跟进时间线</div>

      <template v-else>
        <div class="lead-meta">
          <span>手机号：{{ selectedLead.phone }}</span>
          <span>状态：<i :class="statusClass(selectedLead.status)">{{ selectedLead.status }}</i></span>
          <span>意向：{{ selectedLead.intentLevel }}</span>
          <span>负责人：{{ selectedLead.ownerName || "未分配" }}</span>
        </div>

        <form class="system-form follow-form" @submit.prevent="saveFollowUp">
          <label>
            跟进方式
            <select v-model="followForm.followType">
              <option v-for="item in followTypeOptions" :key="item" :value="item">{{ item }}</option>
            </select>
          </label>
          <label>
            下次跟进时间
            <input v-model="followForm.nextFollowTime" type="datetime-local" />
          </label>
          <label class="wide-field">
            跟进内容
            <textarea v-model="followForm.content" maxlength="500" placeholder="请输入跟进内容" required></textarea>
          </label>
          <div class="system-form-actions">
            <button class="primary" :disabled="followSaving" type="submit">新增跟进</button>
          </div>
        </form>

        <div v-if="followLoading" class="message">正在加载跟进记录...</div>

        <div class="timeline">
          <article v-for="item in followUps" :key="item.id" class="follow-item">
            <div class="follow-head">
              <strong>{{ item.followType }}</strong>
              <span>{{ formatDateTime(item.createTime) }}</span>
            </div>
            <p>{{ item.content }}</p>
            <small>
              下次跟进：{{ formatDateTime(item.nextFollowTime) }} ｜ 记录人：{{ item.creatorName || "系统" }}
            </small>
          </article>
          <div v-if="!followLoading && followUps.length === 0" class="empty-state">暂无跟进记录</div>
        </div>
      </template>
    </section>

    <section class="system-panel">
      <div class="section-head">
        <div>
          <p class="eyebrow">Lead Form</p>
          <h2>{{ editingId ? "编辑线索" : "新增线索" }}</h2>
        </div>
        <button class="ghost" @click="resetLeadForm">清空表单</button>
      </div>

      <form class="system-form enrollment-form" @submit.prevent="saveLead">
        <label>
          姓名
          <input v-model="leadForm.name" maxlength="50" placeholder="请输入姓名" required />
        </label>
        <label>
          手机号
          <input v-model="leadForm.phone" maxlength="20" placeholder="请输入手机号" required />
        </label>
        <label>
          来源
          <select v-model="leadForm.source">
            <option v-for="item in sourceOptions" :key="item" :value="item">{{ item }}</option>
          </select>
        </label>
        <label>
          意向等级
          <select v-model="leadForm.intentLevel">
            <option value="高">高</option>
            <option value="中">中</option>
            <option value="低">低</option>
          </select>
        </label>
        <label>
          状态
          <select v-model="leadForm.status">
            <option v-for="item in statusOptions" :key="item" :value="item">{{ item }}</option>
          </select>
        </label>
        <label>
          负责人用户ID
          <input v-model.number="leadForm.ownerUserId" min="1" type="number" placeholder="可留空" />
        </label>
        <label>
          下次跟进时间
          <input v-model="leadForm.nextFollowTime" type="datetime-local" />
        </label>
        <label class="wide-field">
          备注
          <textarea v-model="leadForm.remark" maxlength="500" placeholder="备注信息"></textarea>
        </label>
        <div class="system-form-actions">
          <button class="primary" :disabled="saving" type="submit">
            {{ editingId ? "保存修改" : "新增线索" }}
          </button>
          <button class="ghost" type="button" @click="resetLeadForm">取消</button>
        </div>
      </form>
    </section>
  </div>
</template>

<style scoped>
.enrollment-filter,
.enrollment-form,
.follow-form {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.enrollment-filter {
  margin-bottom: 14px;
}

.dash-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.dash-grid article,
.chart-panel,
.funnel-panel {
  padding: 12px;
  border: 1px solid #dfe5da;
  border-radius: 8px;
  background: #fbfcfa;
}

.dash-grid strong {
  display: block;
  font-size: 24px;
  color: #21483a;
}

.chart-grid {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.bar-line {
  display: grid;
  grid-template-columns: 80px 1fr 42px;
  gap: 8px;
  align-items: center;
  margin-top: 8px;
}

.bar-bg {
  height: 8px;
  border-radius: 999px;
  background: #edf4e8;
}

.bar {
  height: 100%;
  border-radius: 999px;
  background: #4f7665;
}

.bar.owner {
  background: #3e8bd5;
}

.funnel-panel {
  margin-top: 12px;
}

.funnel-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 10px;
}

.funnel-item {
  flex: 1 0 120px;
  padding: 10px;
  border-radius: 8px;
  background: #edf4e8;
}

.tab-row {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}

.ghost.active {
  color: #fff;
  background: #3e8bd5;
}

.enrollment-table {
  overflow-x: auto;
}

.enrollment-table .system-table-row {
  grid-template-columns: 100px 150px 90px 60px 92px 95px 138px 138px 180px;
  min-width: 1100px;
}

.enrollment-table .system-table-row.active {
  border: 1px solid #9fbaaa;
}

.name-link {
  color: #28679b;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 3px;
}

.dial-link {
  margin-left: 8px;
  color: #28679b;
  font-weight: 700;
}

.status-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.status-tag.fresh {
  color: #1f5c8d;
  background: #dfefff;
}

.status-tag.contacted {
  color: #355f20;
  background: #eaf9dc;
}

.status-tag.visited {
  color: #8d5b19;
  background: #ffefda;
}

.status-tag.signed {
  color: #0f6f43;
  background: #d8f4e6;
}

.status-tag.invalid {
  color: #9a3030;
  background: #ffe2e2;
}

.pager-row {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 14px;
}

textarea {
  min-height: 84px;
  width: 100%;
  padding: 9px 11px;
  border: 1px solid #dfe5da;
  border-radius: 8px;
  color: #17201b;
  background: #fbfcfa;
  outline: none;
  resize: vertical;
}

.lead-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 12px;
}

.lead-meta span {
  padding: 6px 10px;
  border-radius: 999px;
  background: #edf4e8;
}

.timeline {
  display: grid;
  gap: 10px;
  margin-top: 12px;
}

.follow-item {
  padding: 14px;
  border: 1px solid #dfe5da;
  border-radius: 8px;
  background: #fbfcfa;
}

.follow-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.follow-item p {
  margin: 8px 0;
  line-height: 1.7;
}

.follow-item small {
  color: #627066;
}

@media (max-width: 980px) {
  .enrollment-filter,
  .enrollment-form,
  .follow-form,
  .dash-grid,
  .chart-grid {
    grid-template-columns: 1fr;
  }
}
</style>
