<script setup lang="ts">
import { onMounted, ref } from "vue";
import { getUserList } from "@/api/userManagement";

defineOptions({ name: "CoachManagement" });

type Coach = {
  id: number;
  username: string;
  nickname: string;
  email: string;
  status: number;
  createTime: string;
};

const coaches = ref<Coach[]>([]);
const loading = ref(false);
const error = ref("");

onMounted(load);

async function load() {
  loading.value = true;
  error.value = "";
  try {
    const res = await getUserList({ role: "coach", page: 1, size: 100 });
    coaches.value = res?.data?.list || [];
  } catch (err) {
    coaches.value = [];
    error.value = err instanceof Error ? err.message : "教练列表加载失败";
  } finally {
    loading.value = false;
  }
}

function statusText(status: number) {
  return status === 1 ? "启用" : "禁用";
}
</script>

<template>
  <section class="page">
    <header>
      <div>
        <p>Coach Team</p>
        <h1>教练管理</h1>
      </div>
      <button :disabled="loading" @click="load">刷新</button>
    </header>
    <p v-if="error" class="error">{{ error }}</p>
    <div class="table">
      <div class="row head">
        <span>账号</span><span>昵称</span><span>邮箱</span><span>状态</span><span>创建时间</span>
      </div>
      <div v-if="loading" class="empty">加载中...</div>
      <div v-else-if="coaches.length === 0" class="empty">暂无教练账号</div>
      <template v-else>
        <div v-for="coach in coaches" :key="coach.id" class="row">
          <span>{{ coach.username }}</span>
          <span>{{ coach.nickname || "-" }}</span>
          <span>{{ coach.email || "-" }}</span>
          <span><i :class="{ off: coach.status !== 1 }">{{ statusText(coach.status) }}</i></span>
          <span>{{ coach.createTime || "-" }}</span>
        </div>
      </template>
    </div>
  </section>
</template>

<style scoped>
.page { display: grid; gap: 16px; padding: 18px; background: #fff; border: 1px solid #dfe5da; border-radius: 8px; }
header { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
p { margin: 0 0 4px; color: #607067; font-size: 12px; font-weight: 700; text-transform: uppercase; }
h1 { margin: 0; font-size: 24px; color: #17201b; }
button { height: 36px; padding: 0 14px; font-weight: 700; color: #fff; cursor: pointer; background: #2f6f54; border: 1px solid #2f6f54; border-radius: 8px; }
button:disabled { cursor: not-allowed; opacity: .55; }
.table { overflow-x: auto; }
.row { display: grid; grid-template-columns: 150px 140px 220px 90px 170px; gap: 12px; min-width: 820px; padding: 12px; border-bottom: 1px solid #eef2ec; }
.head { font-weight: 700; color: #4d5c54; background: #f7f9f6; }
i { display: inline-block; padding: 2px 8px; font-style: normal; font-weight: 700; color: #0f6f43; background: #d8f4e6; border-radius: 999px; }
i.off { color: #9a3030; background: #ffe2e2; }
.empty, .error { padding: 20px; text-align: center; background: #fbfcfa; }
.error { color: #9a3030; background: #fff1f1; border: 1px solid #ffd5d5; border-radius: 8px; }
</style>
