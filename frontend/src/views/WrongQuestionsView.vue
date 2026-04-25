<script setup lang="ts">
import { onMounted, ref } from 'vue'
import AppLayout from '../components/AppLayout.vue'
import { request } from '../services/api'
import { useAuth } from '../stores/auth'
import type { WrongQuestion } from '../types'

const { user } = useAuth()
const wrongQuestions = ref<WrongQuestion[]>([])
const loading = ref(false)
const error = ref('')

onMounted(loadWrongQuestions)

async function loadWrongQuestions() {
  loading.value = true
  error.value = ''

  try {
    wrongQuestions.value = await request<WrongQuestion[]>('/wrong-questions')
  } catch (err) {
    error.value = err instanceof Error ? err.message : '错题加载失败'
  } finally {
    loading.value = false
  }
}

async function removeWrongQuestion(id: number) {
  loading.value = true
  error.value = ''

  try {
    await request(`/wrong-questions/${id}`, { method: 'DELETE' })
    await loadWrongQuestions()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '移除错题失败'
  } finally {
    loading.value = false
  }
}

async function clearWrongQuestions() {
  loading.value = true
  error.value = ''

  try {
    await request('/wrong-questions', { method: 'DELETE' })
    await loadWrongQuestions()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '清空错题失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <AppLayout>
    <div v-if="error" class="message error">{{ error }}</div>
    <div v-if="loading" class="message">正在加载数据...</div>

    <section class="panel">
      <div class="section-head">
        <div>
          <p class="eyebrow">错题本</p>
          <h2>{{ user?.nickname }} 当前共有 {{ wrongQuestions.length }} 道错题</h2>
        </div>
        <button class="ghost" :disabled="wrongQuestions.length === 0" @click="clearWrongQuestions">清空错题</button>
      </div>

      <div v-if="wrongQuestions.length === 0" class="empty-state">暂无错题，练习或考试中答错后会自动记录。</div>

      <article v-for="item in wrongQuestions" :key="item.id" class="wrong-item">
        <div>
          <h3>{{ item.question.content }}</h3>
          <p>正确答案：{{ item.question.answer }}。{{ item.question.explanation }}</p>
          <span>记录时间：{{ item.createTime.replace('T', ' ').slice(0, 19) }}</span>
        </div>
        <button class="danger" @click="removeWrongQuestion(item.id)">移除</button>
      </article>
    </section>
  </AppLayout>
</template>
