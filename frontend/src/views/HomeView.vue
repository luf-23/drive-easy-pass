<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppLayout from '../components/AppLayout.vue'
import { request } from '../services/api'
import { useAuth } from '../stores/auth'
import type { Question } from '../types'

const { isLoggedIn } = useAuth()
const questions = ref<Question[]>([])
const loading = ref(false)
const error = ref('')

onMounted(loadQuestions)

async function loadQuestions() {
  loading.value = true
  error.value = ''

  try {
    questions.value = await request<Question[]>('/questions')
  } catch (err) {
    error.value = err instanceof Error ? err.message : '题库加载失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AppLayout>
    <div v-if="error" class="message error">{{ error }}</div>
    <div v-if="loading" class="message">正在加载数据...</div>

    <section class="home-grid">
      <article class="hero-panel">
        <div>
          <p class="eyebrow">今日练习</p>
          <h2>把题刷顺，把错题留给自己的账号。</h2>
          <p>登录后，练习和考试中的错题会写入当前用户的错题本，重新打开也能继续复习。</p>
        </div>
        <RouterLink class="primary" to="/practice">开始练习</RouterLink>
      </article>

      <article class="metric">
        <span>题库数量</span>
        <strong>{{ questions.length }}</strong>
      </article>
      <article class="metric">
        <span>模拟考试</span>
        <strong>8 题</strong>
      </article>
      <article class="metric">
        <span>账号状态</span>
        <strong>{{ isLoggedIn ? '已登录' : '未登录' }}</strong>
      </article>
    </section>
  </AppLayout>
</template>
