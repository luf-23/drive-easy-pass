<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import {
  applyExamRegistration,
  cancelExamRegistration,
  getMyExamRegistrations,
  getPublicExamSchedules
} from "@/api/examBooking";
import { useAuth } from "@/stores/auth";
import type { ExamRegistrationRow, ExamScheduleCard } from "@/types";

const router = useRouter();
const { isLoggedIn } = useAuth();

const subjectOptions = ["", "科目一", "科目二", "科目三", "科目四"] as const;
const filterExamType = ref<string>("");

const schedules = ref<ExamScheduleCard[]>([]);
const mine = ref<ExamRegistrationRow[]>([]);

const loading = ref(false);
const mineLoading = ref(false);
const actionId = ref<number | null>(null);
const msg = ref("");
const err = ref("");

const subjectHint = computed(
  () =>
    "科目一可直接预约；科目二需在科目一「考试完成且标记为已通过」后继续，以此类推。"
);

async function reloadAll() {
  await Promise.all([loadSchedules(), loadMine()]);
}

async function loadSchedules() {
  loading.value = true;
  err.value = "";
  try {
    schedules.value = await getPublicExamSchedules(
      filterExamType.value || undefined
    );
  } catch (e) {
    schedules.value = [];
    err.value = e instanceof Error ? e.message : "加载场次失败";
  } finally {
    loading.value = false;
  }
}

async function loadMine() {
  if (!isLoggedIn.value) {
    mine.value = [];
    return;
  }
  mineLoading.value = true;
  try {
    mine.value = await getMyExamRegistrations();
  } catch {
    mine.value = [];
  } finally {
    mineLoading.value = false;
  }
}

function goLogin() {
  router.push({ path: "/login", query: { redirect: "/service/exam-booking" } });
}

async function book(row: ExamScheduleCard) {
  if (!isLoggedIn.value) {
    goLogin();
    return;
  }
  msg.value = "";
  err.value = "";
  actionId.value = row.id;
  try {
    await applyExamRegistration(row.id);
    msg.value = `已预约：${row.examType} ${row.examDate} ${row.startTime}`;
    await reloadAll();
  } catch (e) {
    err.value = e instanceof Error ? e.message : "预约失败";
  } finally {
    actionId.value = null;
  }
}

async function cancelReg(row: ExamRegistrationRow) {
  if (!confirm("确认取消该场预约？")) return;
  msg.value = "";
  err.value = "";
  actionId.value = row.id;
  try {
    await cancelExamRegistration(row.id);
    msg.value = "预约已取消";
    await reloadAll();
  } catch (e) {
    err.value = e instanceof Error ? e.message : "取消失败";
  } finally {
    actionId.value = null;
  }
}

function statusLabel(s: string) {
  const map: Record<string, string> = {
    confirmed: "已确认",
    cancelled: "已取消",
    completed: "已完成",
    absent: "缺考"
  };
  return map[s] ?? s;
}

onMounted(reloadAll);
</script>

<template>
  <div class="exam-book">
    <section class="panel">
      <h1>考试预约</h1>
      <p class="muted">{{ subjectHint }}</p>

      <div class="toolbar">
        <label>
          科目筛选
          <select v-model="filterExamType" @change="loadSchedules">
            <option value="">全部</option>
            <option v-for="s in subjectOptions.slice(1)" :key="s" :value="s">
              {{ s }}
            </option>
          </select>
        </label>
        <button type="button" class="ghost" @click="reloadAll">刷新</button>
      </div>

      <p v-if="msg" class="ok">{{ msg }}</p>
      <p v-if="err" class="bad">{{ err }}</p>

      <div v-if="loading" class="muted">场次加载中…</div>
      <div v-else class="cards">
        <article v-for="s in schedules" :key="s.id" class="card">
          <header>
            <span class="tag">{{ s.examType }}</span>
            <strong>{{ s.venueName }}</strong>
          </header>
          <dl>
            <div>
              <dt>日期</dt>
              <dd>{{ s.examDate }}</dd>
            </div>
            <div>
              <dt>时段</dt>
              <dd>{{ s.startTime }} — {{ s.endTime }}</dd>
            </div>
            <div>
              <dt>余位</dt>
              <dd>
                {{ s.availableSlots }} / {{ s.capacity }}
                <small v-if="s.remark">（{{ s.remark }}）</small>
              </dd>
            </div>
          </dl>
          <footer>
            <button
              type="button"
              class="primary"
              :disabled="s.availableSlots <= 0 || actionId !== null"
              @click="book(s)"
            >
              {{
                !isLoggedIn
                  ? "登录后预约"
                  : s.availableSlots <= 0
                    ? "名额已满"
                    : actionId === s.id
                      ? "预约中…"
                      : "预约本场"
              }}
            </button>
          </footer>
        </article>
      </div>
      <p v-if="!loading && schedules.length === 0" class="muted">
        暂无满足条件的开考计划，请改选科目或过几日再看。
      </p>
    </section>

    <section class="panel">
      <h2>我的报考</h2>
      <p v-if="!isLoggedIn" class="muted">
        请先
        <a href="#" @click.prevent="goLogin">登录学员账号</a>
        ，以查看与管理预约。
      </p>
      <div v-else-if="mineLoading" class="muted">加载中…</div>
      <table v-else-if="mine.length" class="grid">
        <thead>
          <tr>
            <th>科目</th>
            <th>考场</th>
            <th>日期 / 开考</th>
            <th>状态</th>
            <th>成绩</th>
            <th />
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in mine" :key="r.id">
            <td>{{ r.examType }}</td>
            <td>{{ r.venueName }}</td>
            <td>{{ r.examDate }} {{ r.startTime }}</td>
            <td>{{ statusLabel(r.status) }}</td>
            <td>
              <template v-if="r.status === 'completed'">
                {{ r.score ?? "—" }} /
                {{
                  r.passed === "Y" ? "合格" : r.passed === "N" ? "不合格" : "—"
                }}
              </template>
              <template v-else>—</template>
            </td>
            <td class="nowrap">
              <button
                v-if="r.status === 'confirmed'"
                type="button"
                class="ghost sm"
                :disabled="actionId !== null"
                @click="cancelReg(r)"
              >
                {{ actionId === r.id ? "处理中…" : "取消预约" }}
              </button>
            </td>
          </tr>
        </tbody>
      </table>
      <p v-else class="muted">暂无记录，可在上方场次中发起预约。</p>
    </section>
  </div>
</template>

<style scoped>
.exam-book {
  display: grid;
  gap: 18px;
}

.panel {
  padding: 22px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgb(14 46 80 / 10%);
}

.panel h1 {
  margin: 0 0 6px;
  color: #123a5f;
}

.panel h2 {
  margin: 0 0 12px;
  color: #123a5f;
}

.muted {
  font-size: 14px;
  color: #5a7594;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: flex-end;
  margin: 14px 0;
}

.toolbar label {
  display: grid;
  gap: 6px;
  font-weight: 700;
  color: #2f4f6c;
}

.toolbar select {
  padding: 8px 12px;
  border: 1px solid #c9dcef;
  border-radius: 8px;
}

button {
  padding: 9px 16px;
  font-weight: 700;
  cursor: pointer;
  border: none;
  border-radius: 10px;
}

button:disabled {
  cursor: not-allowed;
  opacity: 0.55;
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
  padding: 6px 10px;
  font-size: 13px;
}

.ok {
  color: #157347;
}

.bad {
  color: #b42318;
}

.cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
}

.card {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  background: #f9fcff;
  border: 1px solid #dfebf8;
  border-radius: 12px;
}

.card header {
  display: grid;
  gap: 8px;
}

.tag {
  display: inline-block;
  align-self: start;
  padding: 3px 8px;
  font-size: 12px;
  font-weight: 800;
  color: #145387;
  background: #deedff;
  border-radius: 999px;
}

dl {
  display: grid;
  gap: 6px;
  margin: 0;
  font-size: 14px;
  color: #45617f;
}

dl > div {
  display: grid;
  grid-template-columns: 52px 1fr;
  gap: 10px;
}

dt {
  color: #7a93ad;
}

dd {
  margin: 0;
}

.card footer {
  margin-top: auto;
}

.grid {
  width: 100%;
  font-size: 14px;
  border-collapse: collapse;
}

.grid th,
.grid td {
  padding: 10px 8px;
  text-align: left;
  border-bottom: 1px solid #e4eef8;
}

.grid th {
  font-weight: 800;
  color: #2f4f6c;
}

.nowrap {
  white-space: nowrap;
}
</style>
