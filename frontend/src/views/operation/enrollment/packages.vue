<script setup lang="ts">
import { onMounted, ref } from "vue";
import { request } from "@/services/api";
import type { CoursePackage } from "@/types";

defineOptions({ name: "CoursePackageManagement" });

type AdminCoursePackage = CoursePackage & {
  id: number;
  vehicleType: string;
  sortNo: number;
  enabled: boolean;
};

const packages = ref<AdminCoursePackage[]>([]);
const loading = ref(false);
const error = ref("");

onMounted(load);

async function load() {
  loading.value = true;
  error.value = "";
  try {
    packages.value = await request<AdminCoursePackage[]>("/admin/course-packages");
  } catch (err) {
    packages.value = [];
    error.value = err instanceof Error ? err.message : "课程套餐加载失败";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <section class="page">
    <header>
      <div><p>Course Packages</p><h1>课程套餐</h1></div>
      <button :disabled="loading" @click="load">刷新</button>
    </header>
    <p v-if="error" class="error">{{ error }}</p>
    <div class="cards">
      <article v-for="item in packages" :key="item.id">
        <div class="top">
          <strong>{{ item.name }}</strong>
          <i :class="{ off: !item.enabled }">{{ item.enabled ? "上架" : "下架" }}</i>
        </div>
        <p>{{ item.code }} / {{ item.vehicleType }}</p>
        <b>￥{{ item.price }}</b>
        <span>{{ item.lessonHours }} 课时</span>
        <ul><li v-for="h in item.highlights" :key="h">{{ h }}</li></ul>
      </article>
      <div v-if="loading" class="empty">加载中...</div>
      <div v-else-if="packages.length === 0" class="empty">暂无课程套餐</div>
    </div>
  </section>
</template>

<style scoped>
.page { display: grid; gap: 16px; padding: 18px; background: #fff; border: 1px solid #dfe5da; border-radius: 8px; }
header, .top { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
p { margin: 0; color: #607067; }
header p { margin-bottom: 4px; font-size: 12px; font-weight: 700; text-transform: uppercase; }
h1 { margin: 0; font-size: 24px; color: #17201b; }
button { height: 36px; padding: 0 14px; font-weight: 700; color: #fff; cursor: pointer; background: #2f6f54; border: 1px solid #2f6f54; border-radius: 8px; }
.cards { display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 12px; }
article { display: grid; gap: 8px; padding: 14px; background: #fbfcfa; border: 1px solid #dfe5da; border-radius: 8px; }
strong { color: #17201b; }
b { font-size: 22px; color: #21483a; }
i { padding: 2px 8px; font-style: normal; font-weight: 700; color: #0f6f43; background: #d8f4e6; border-radius: 999px; }
i.off { color: #9a3030; background: #ffe2e2; }
ul { padding-left: 18px; margin: 0; color: #46534c; }
.empty, .error { padding: 20px; text-align: center; background: #fbfcfa; }
.error { color: #9a3030; background: #fff1f1; border: 1px solid #ffd5d5; border-radius: 8px; }
</style>
