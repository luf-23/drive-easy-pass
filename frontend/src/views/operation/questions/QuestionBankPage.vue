<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { request } from "@/services/api";
import type { Question } from "@/types";

const props = defineProps<{
  examType: string;
  title: string;
}>();

const questions = ref<Question[]>([]);
const keyword = ref("");
const loading = ref(false);
const error = ref("");

const filtered = computed(() => {
  const kw = keyword.value.trim();
  if (!kw) return questions.value;
  return questions.value.filter(q => q.content.includes(kw));
});

onMounted(load);

async function load() {
  loading.value = true;
  error.value = "";
  try {
    questions.value = await request<Question[]>(
      `/questions?examType=${encodeURIComponent(props.examType)}`
    );
  } catch (err) {
    questions.value = [];
    error.value = err instanceof Error ? err.message : "题库加载失败";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <section class="page">
    <header>
      <div><p>Question Bank</p><h1>{{ title }}</h1></div>
      <strong>{{ filtered.length }}</strong>
    </header>
    <div class="toolbar">
      <input v-model="keyword" placeholder="搜索题干" />
      <button :disabled="loading" @click="load">刷新</button>
    </div>
    <p v-if="error" class="error">{{ error }}</p>
    <div v-if="loading" class="empty">加载中...</div>
    <div v-else-if="filtered.length === 0" class="empty">暂无题目</div>
    <article v-for="q in filtered" :key="q.id">
      <h2>{{ q.id }}. {{ q.content }}</h2>
      <div class="options">
        <span>A. {{ q.optionA }}</span>
        <span>B. {{ q.optionB }}</span>
        <span>C. {{ q.optionC }}</span>
        <span>D. {{ q.optionD }}</span>
      </div>
      <footer>
        <b>答案：{{ q.answer }}</b>
        <span>{{ q.explanation || "暂无解析" }}</span>
      </footer>
    </article>
  </section>
</template>

<style scoped>
.page { display: grid; gap: 12px; padding: 18px; background: #fff; border: 1px solid #dfe5da; border-radius: 8px; }
header, .toolbar, footer { display: flex; align-items: center; justify-content: space-between; gap: 12px; }
p { margin: 0; color: #607067; }
header p { margin-bottom: 4px; font-size: 12px; font-weight: 700; text-transform: uppercase; }
h1 { margin: 0; font-size: 24px; color: #17201b; }
header strong { font-size: 28px; color: #21483a; }
input { flex: 1; height: 36px; padding: 0 10px; outline: none; background: #fbfcfa; border: 1px solid #dfe5da; border-radius: 8px; }
button { height: 36px; padding: 0 14px; font-weight: 700; color: #fff; cursor: pointer; background: #2f6f54; border: 1px solid #2f6f54; border-radius: 8px; }
article { display: grid; gap: 10px; padding: 14px; background: #fbfcfa; border: 1px solid #dfe5da; border-radius: 8px; }
h2 { margin: 0; font-size: 15px; color: #17201b; line-height: 1.6; }
.options { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px; color: #46534c; }
footer { align-items: flex-start; justify-content: flex-start; color: #607067; }
b { color: #21483a; }
.empty, .error { padding: 20px; text-align: center; background: #fbfcfa; }
.error { color: #9a3030; background: #fff1f1; border: 1px solid #ffd5d5; border-radius: 8px; }
@media (width <= 720px) { .options, .toolbar { grid-template-columns: 1fr; display: grid; } }
</style>
