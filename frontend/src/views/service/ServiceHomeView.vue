<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { getCoursePackages } from "@/api/public";
import type { CoursePackage } from "@/types";

const packages = ref<CoursePackage[]>([]);
const loading = ref(false);
const error = ref("");
const heroImages = [
  "https://images.unsplash.com/photo-1549921296-3a6b5196f4cc?auto=format&fit=crop&w=1400&q=60",
  "https://images.unsplash.com/photo-1590362891991-f776e747a588?auto=format&fit=crop&w=1400&q=60",
  "https://images.unsplash.com/photo-1622015663084-307d19eab9f8?auto=format&fit=crop&w=1400&q=60"
];
const activeHero = ref(0);

const heroStyle = computed(() => ({
  backgroundImage: `linear-gradient(120deg, rgba(2, 21, 38, 0.65), rgba(12, 58, 101, 0.25)), url(${heroImages[activeHero.value]})`
}));

onMounted(async () => {
  await loadPackages();
  setInterval(() => {
    activeHero.value = (activeHero.value + 1) % heroImages.length;
  }, 3500);
});

async function loadPackages() {
  loading.value = true;
  error.value = "";
  try {
    packages.value = await getCoursePackages();
  } catch (err) {
    error.value = err instanceof Error ? err.message : "套餐加载失败";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="service-home">
    <section class="hero" :style="heroStyle">
      <h1>通过率高、训练场近、报名流程透明</h1>
      <p>一人一车、全程教练跟进、关键节点提醒，帮你更快拿证。</p>
      <div class="hero-actions">
        <RouterLink class="cta" to="/service/signup">立即报名</RouterLink>
        <RouterLink class="ghost" to="/service/exam-sites">查看考场线路</RouterLink>
      </div>
    </section>

    <section class="value-grid">
      <article>
        <strong>99%</strong>
        <span>近 12 个月科目二三综合通过率</span>
      </article>
      <article>
        <strong>6km</strong>
        <span>主城区到训练场平均通勤</span>
      </article>
      <article>
        <strong>7x12h</strong>
        <span>每日可预约训练时段</span>
      </article>
      <article>
        <strong>2000+</strong>
        <span>年度服务学员人数</span>
      </article>
    </section>

    <section class="panel" v-if="error">{{ error }}</section>

    <section class="packages panel">
      <div class="head">
        <h2>班型套餐</h2>
        <RouterLink to="/service/signup">填写意向表单</RouterLink>
      </div>

      <div v-if="loading">加载中...</div>
      <div v-else class="card-grid">
        <article v-for="item in packages" :key="item.code" class="pkg-card">
          <span class="tag">{{ item.tag }}</span>
          <h3>{{ item.name }}</h3>
          <p class="price">¥ {{ item.price }}</p>
          <small>{{ item.lessonHours }} 学时</small>
          <ul>
            <li v-for="h in item.highlights" :key="h">{{ h }}</li>
          </ul>
          <RouterLink class="cta" to="/service/signup">立即报名</RouterLink>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.service-home {
  display: grid;
  gap: 18px;
}

.hero {
  padding: 36px;
  border-radius: 16px;
  color: #fff;
  background-position: center;
  background-size: cover;
  box-shadow: 0 16px 34px rgba(14, 46, 80, 0.25);
  transition: background-image 0.6s ease;
}

.hero h1 {
  font-size: 34px;
  line-height: 1.25;
}

.hero p {
  max-width: 720px;
  margin-top: 10px;
  color: #dfecff;
}

.hero-actions {
  margin-top: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.cta,
.ghost {
  display: inline-grid;
  place-items: center;
  min-height: 40px;
  padding: 9px 16px;
  border-radius: 10px;
  font-weight: 700;
}

.cta {
  color: #123a5f;
  background: #f7be3b;
}

.ghost {
  color: #dceeff;
  border: 1px solid rgba(220, 238, 255, 0.6);
}

.value-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.value-grid article,
.panel {
  padding: 16px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 8px 20px rgba(14, 46, 80, 0.1);
}

.value-grid strong {
  display: block;
  font-size: 26px;
  color: #0f4f87;
}

.packages .head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.card-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.pkg-card {
  padding: 14px;
  border: 1px solid #dfebf8;
  border-radius: 12px;
  background: #f9fcff;
}

.tag {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 999px;
  color: #145387;
  background: #deedff;
  font-size: 12px;
}

.price {
  margin-top: 8px;
  font-size: 24px;
  font-weight: 800;
  color: #173f6a;
}

ul {
  margin: 10px 0;
  padding-left: 18px;
  color: #45617f;
}

@media (max-width: 1024px) {
  .value-grid,
  .card-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 768px) {
  .hero {
    padding: 24px;
  }

  .hero h1 {
    font-size: 26px;
  }

  .value-grid,
  .card-grid {
    grid-template-columns: 1fr;
  }
}
</style>
