<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { request } from '../services/api'
import { useAuth } from '../stores/auth'
import type { Question, WrongQuestion } from '../types'

const { isLoggedIn, user } = useAuth()
const questions = ref<Question[]>([])
const wrongQuestions = ref<WrongQuestion[]>([])
const loading = ref(false)
const error = ref('')
const examCount = 20

const wrongCount = computed(() => {
  const ids = new Set(wrongQuestions.value.map((item) => item.question.id))
  return ids.size
})

onMounted(loadDashboard)

async function loadDashboard() {
  loading.value = true
  error.value = ''

  try {
    questions.value = await request<Question[]>('/questions')
    wrongQuestions.value = isLoggedIn.value ? await request<WrongQuestion[]>('/wrong-questions') : []
  } catch (err) {
    error.value = err instanceof Error ? err.message : '首页数据加载失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="drive-page">
    <div v-if="error" class="message error">{{ error }}</div>
    <div v-if="loading" class="message">正在加载数据...</div>

    <section class="home-grid home-dashboard">
      <article class="hero-panel driving-hero">
        <div>
          <p class="eyebrow">Drive Easy Pass</p>
          <h2>{{ isLoggedIn ? `${user?.nickname}，继续冲刺驾考` : '驾考刷题、模拟、错题复盘一站完成' }}</h2>
          <p>围绕科目一常见训练流程设计：先顺序练习建立题感，再用 20 题模拟考试检验掌握情况，最后回到错题本集中复盘。</p>
        </div>
        <div class="hero-actions">
          <RouterLink class="primary" to="/practice">开始练习</RouterLink>
          <RouterLink class="ghost light" to="/exam">模拟考试</RouterLink>
        </div>
      </article>

      <article class="metric">
        <span>题库数量</span>
        <strong>{{ questions.length }}</strong>
        <p>顺序练习覆盖全部题目</p>
      </article>
      <article class="metric">
        <span>模拟考试</span>
        <strong>{{ examCount }} 题</strong>
        <p>每题 5 分，答完提交计分</p>
      </article>
      <article class="metric">
        <span>错题复盘</span>
        <strong>{{ isLoggedIn ? wrongCount : '登录后启用' }}</strong>
        <p>同一道题只保留一条记录</p>
      </article>

      <article class="route-card practice-route">
        <span>01</span>
        <h3>顺序练习</h3>
        <p>答题后立刻显示解析，适合把题库先完整过一遍。</p>
        <RouterLink to="/practice">进入练习</RouterLink>
      </article>
      <article class="route-card exam-route">
        <span>02</span>
        <h3>模拟考试</h3>
        <p>随机抽题，错误选项标红、正确答案标绿，反馈更直观。</p>
        <RouterLink to="/exam">开始考试</RouterLink>
      </article>
      <article class="route-card wrong-route">
        <span>03</span>
        <h3>错题本</h3>
        <p>分页查看薄弱题目，考前集中处理高频失分点。</p>
        <RouterLink to="/wrong">查看错题</RouterLink>
      </article>
    </section>
  </div>
</template>
