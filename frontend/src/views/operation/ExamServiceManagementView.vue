<script setup lang="ts">
import { onMounted, reactive, ref } from "vue";
import {
  getAdminExamRegistrations,
  updateAdminExamRegistration
} from "@/api/admin";
import type {
  AdminExamRegistrationUpdate,
  ExamReservationAdminRow
} from "@/types";

const rows = ref<ExamReservationAdminRow[]>([]);
const loading = ref(false);
const saving = ref(false);
const error = ref("");
const ok = ref("");

const drawer = ref(false);
const editing = ref<ExamReservationAdminRow | null>(null);

const form = reactive({
  status: "",
  score: null as number | null,
  passed: "" as string,
  remark: "" as string
});

async function load() {
  loading.value = true;
  error.value = "";
  try {
    rows.value = await getAdminExamRegistrations();
  } catch (e) {
    rows.value = [];
    error.value = e instanceof Error ? e.message : "加载失败";
  } finally {
    loading.value = false;
  }
}

function openEdit(r: ExamReservationAdminRow) {
  editing.value = r;
  form.status = r.status;
  form.score = r.score;
  form.passed = r.passed ?? "";
  form.remark = r.remark ?? "";
  ok.value = "";
  error.value = "";
  drawer.value = true;
}

async function save() {
  if (!editing.value) return;
  saving.value = true;
  ok.value = "";
  error.value = "";
  try {
    const n = Number(form.score);
    const payload: AdminExamRegistrationUpdate = {
      status: (form.status ?? "").trim() || null,
      score: Number.isFinite(n) ? n : null,
      passed:
        typeof form.passed === "string" && form.passed.trim()
          ? form.passed.trim().toUpperCase()
          : null,
      remark:
        typeof form.remark === "string" ? form.remark.trim() || null : null
    };
    await updateAdminExamRegistration(editing.value.id, payload);
    ok.value = "已保存";
    drawer.value = false;
    await load();
  } catch (e) {
    error.value = e instanceof Error ? e.message : "保存失败";
  } finally {
    saving.value = false;
  }
}

function statusZh(s: string) {
  const m: Record<string, string> = {
    pending: "待审核",
    approved: "已通过",
    rejected: "已拒绝",
    cancelled: "已取消",
    completed: "已完成"
  };
  return m[s] ?? s;
}

onMounted(load);
</script>

<template>
  <div class="wrap">
    <header class="head">
      <div>
        <h1>报考服务</h1>
        <p class="muted">
          维护学员各科预约，并在考试结束后登记成绩与是否合格。
        </p>
      </div>
      <button type="button" class="ghost" :disabled="loading" @click="load">
        刷新
      </button>
    </header>

    <p v-if="ok" class="ok">{{ ok }}</p>
    <p v-if="error" class="bad">{{ error }}</p>

    <div v-if="loading" class="muted">加载中…</div>
    <div v-else class="panel">
      <table class="grid">
        <thead>
          <tr>
            <th>学员</th>
            <th>科目</th>
            <th>考场</th>
            <th>日期</th>
            <th>状态</th>
            <th>成绩 / 合格</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in rows" :key="r.id">
            <td>{{ r.username }}</td>
            <td>{{ r.examType }}</td>
            <td>{{ r.venueName }}</td>
            <td>{{ r.examDate }} {{ r.startTime }}</td>
            <td>{{ statusZh(r.status) }}</td>
            <td>
              {{ r.score ?? "—" }} /
              {{ r.passed === "Y" ? "Y" : r.passed === "N" ? "N" : "—" }}
            </td>
            <td>
              <button type="button" class="sm" @click="openEdit(r)">
                编辑
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-if="rows.length === 0" class="muted">暂无报考数据。</p>
    </div>

    <Teleport to="body">
      <div v-if="drawer" class="mask" @click.self="drawer = false">
        <div class="dlg">
          <h2>编辑报考 #{{ editing?.id }}</h2>
          <div class="fld">
            <label>状态</label>
            <select v-model="form.status">
              <option value="pending">pending 待审核</option>
              <option value="approved">approved 已通过</option>
              <option value="rejected">rejected 已拒绝</option>
              <option value="cancelled">cancelled 已取消</option>
              <option value="completed">completed 已完成</option>
            </select>
          </div>
          <div class="fld">
            <label>分数（理论/实操成绩）</label>
            <input
              v-model.number="form.score"
              type="number"
              min="0"
              max="100"
            />
          </div>
          <div class="fld">
            <label>是否合格</label>
            <select v-model="form.passed">
              <option value="">未填</option>
              <option value="Y">Y 合格</option>
              <option value="N">N 不合格</option>
            </select>
          </div>
          <div class="fld">
            <label>备注</label>
            <textarea v-model="form.remark" rows="3" />
          </div>
          <footer>
            <button type="button" class="ghost" @click="drawer = false">
              关闭
            </button>
            <button
              type="button"
              class="primary"
              :disabled="saving"
              @click="save"
            >
              {{ saving ? "保存中…" : "保存" }}
            </button>
          </footer>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.wrap {
  display: grid;
  gap: 16px;
  padding: 8px 4px 24px;
}

.head {
  display: flex;
  gap: 12px;
  align-items: flex-start;
  justify-content: space-between;
}

.head h1 {
  margin: 0;
  font-size: 22px;
  color: #123a5f;
}

.muted {
  margin: 4px 0 0;
  font-size: 14px;
  color: #5a7594;
}

.panel {
  padding: 12px;
  overflow-x: auto;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgb(14 46 80 / 8%);
}

.grid {
  width: 100%;
  min-width: 880px;
  font-size: 14px;
  border-collapse: collapse;
}

.grid th,
.grid td {
  padding: 10px 8px;
  text-align: left;
  border-bottom: 1px solid #e8eff7;
}

button {
  padding: 8px 12px;
  font-weight: 700;
  cursor: pointer;
  border: none;
  border-radius: 8px;
}

button.primary {
  color: #fff;
  background: #0b3d6b;
}

button.ghost {
  color: #0b3d6b;
  background: #e9f3ff;
}

button.sm {
  padding: 5px 10px;
  font-size: 13px;
  color: #0b3d6b;
  background: #f0f6ff;
}

.ok {
  color: #157347;
}

.bad {
  color: #b42318;
}

.mask {
  position: fixed;
  inset: 0;
  z-index: 50;
  display: grid;
  place-items: center;
  background: rgb(18 45 70 / 35%);
}

.dlg {
  display: grid;
  gap: 12px;
  width: min(420px, 92vw);
  padding: 18px 18px 14px;
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 20px 44px rgb(12 40 70 / 25%);
}

.dlg h2 {
  margin: 0;
  font-size: 18px;
  color: #123a5f;
}

.fld {
  display: grid;
  gap: 6px;
}

.fld label {
  font-size: 13px;
  font-weight: 700;
  color: #2f4f6c;
}

.fld select,
.fld input,
.fld textarea {
  padding: 8px 10px;
  font-size: 14px;
  border: 1px solid #c9dcef;
  border-radius: 8px;
}

.dlg footer {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  padding-top: 6px;
}
</style>
