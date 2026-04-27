<script setup lang="ts">
import { onMounted, ref } from "vue";
import { getExamSites } from "@/api/public";
import type { ExamSite } from "@/types";

const sites = ref<ExamSite[]>([]);
const loading = ref(false);
const error = ref("");

onMounted(loadSites);

async function loadSites() {
  loading.value = true;
  error.value = "";
  try {
    sites.value = await getExamSites();
  } catch (err) {
    error.value = err instanceof Error ? err.message : "考场数据加载失败";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="site-page">
    <section class="title-panel">
      <h1>考场信息与线路图</h1>
      <p>展示真实考场、可考科目与动线说明，便于学员提前熟悉考试流程。</p>
    </section>

    <section v-if="loading" class="status">加载中...</section>
    <section v-else-if="error" class="status error">{{ error }}</section>

    <section class="site-grid">
      <article v-for="site in sites" :key="site.code" class="site-card">
        <img :src="site.imageUrl" :alt="site.name" />
        <div class="site-body">
          <h2>{{ site.name }}</h2>
          <p class="addr">{{ site.address }}</p>
          <div class="subjects">
            <span v-for="subject in site.subjects" :key="subject">{{ subject }}</span>
          </div>
          <p class="route">动线：{{ site.routeDescription }}</p>
        </div>
      </article>
    </section>
  </div>
</template>

<style scoped>
.site-page {
  display: grid;
  gap: 16px;
}

.title-panel,
.status,
.site-card {
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 8px 20px rgba(14, 46, 80, 0.1);
}

.title-panel,
.status {
  padding: 18px;
}

.site-grid {
  display: grid;
  gap: 14px;
}

.site-card {
  display: grid;
  grid-template-columns: 340px 1fr;
  overflow: hidden;
}

img {
  width: 100%;
  height: 100%;
  min-height: 240px;
  object-fit: cover;
}

.site-body {
  padding: 16px;
}

.addr {
  color: #4b6987;
  margin-top: 6px;
}

.subjects {
  margin-top: 12px;
  display: flex;
  gap: 8px;
}

.subjects span {
  padding: 5px 10px;
  border-radius: 999px;
  color: #0b4d82;
  background: #deedff;
  font-size: 12px;
  font-weight: 700;
}

.route {
  margin-top: 12px;
  line-height: 1.7;
}

@media (max-width: 900px) {
  .site-card {
    grid-template-columns: 1fr;
  }
}
</style>
