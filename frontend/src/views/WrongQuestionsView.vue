<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import AppPagination from '../components/AppPagination.vue'
import { request } from '../services/api'
import { useAuth } from '../stores/auth'
import type { WrongQuestion } from '../types'

const { user } = useAuth()
const wrongQuestions = ref<WrongQuestion[]>([])
const loading = ref(false)
const error = ref('')
const page = ref(1)
const pageSize = 5

const uniqueWrongQuestions = computed(() => {
  const seen = new Set<number>()
  return wrongQuestions.value.filter((item) => {
    if (seen.has(item.question.id)) return false
    seen.add(item.question.id)
    return true
  })
})
const totalPages = computed(() => Math.max(1, Math.ceil(uniqueWrongQuestions.value.length / pageSize)))
const pagedWrongQuestions = computed(() => {
  const start = (page.value - 1) * pageSize
  return uniqueWrongQuestions.value.slice(start, start + pageSize)
})

watch(totalPages, (value) => {
  if (page.value > value) page.value = value
})

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
    page.value = 1
    await loadWrongQuestions()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '清空错题失败'
  } finally {
    loading.value = false
  }
}

function goPage(value: number) {
  page.value = Math.min(Math.max(value, 1), totalPages.value)
}
</script>

<template>
  <div class="drive-page">
    <div v-if="error" class="message error">{{ error }}</div>
    <div v-if="loading" class="message">正在加载数据...</div>

    <section class="panel wrong-book-panel">
      <div class="section-head">
        <div>
          <p class="eyebrow">错题本</p>
          <h2>{{ user?.nickname }} 当前共有 {{ uniqueWrongQuestions.length }} 道错题</h2>
        </div>
        <button class="ghost" :disabled="uniqueWrongQuestions.length === 0" @click="clearWrongQuestions">清空错题</button>
      </div>

      <div v-if="uniqueWrongQuestions.length === 0" class="empty-state">
        暂无错题，练习或考试中答错后会自动记录。
      </div>

      <div v-else class="wrong-list-scroll">
        <div class="wrong-list">
          <article v-for="item in pagedWrongQuestions" :key="item.question.id" class="wrong-item">
            <div>
              <h3>{{ item.question.content }}</h3>
              <p>正确答案：{{ item.question.answer }}。{{ item.question.explanation }}</p>
              <span>记录时间：{{ item.createTime.replace('T', ' ').slice(0, 19) }}</span>
            </div>
            <button class="danger" @click="removeWrongQuestion(item.id)">移除</button>
          </article>
        </div>
      </div>

      <AppPagination
        v-if="uniqueWrongQuestions.length > pageSize"
        :page="page"
        :total-pages="totalPages"
        @change="goPage"
      />
    </section>
  </div>
</template>
