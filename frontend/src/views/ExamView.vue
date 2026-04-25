<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AppLayout from '../components/AppLayout.vue'
import { request } from '../services/api'
import type { ExamResult, OptionKey, Question } from '../types'

const examQuestions = ref<Question[]>([])
const examAnswers = ref<Record<number, OptionKey>>({})
const examResult = ref<ExamResult | null>(null)
const loading = ref(false)
const error = ref('')
const optionKeys: OptionKey[] = ['A', 'B', 'C', 'D']

const answeredCount = computed(() => Object.keys(examAnswers.value).length)

onMounted(startExam)

function optionText(question: Question, key: OptionKey) {
  return question[`option${key}` as keyof Question] as string
}

async function startExam() {
  loading.value = true
  error.value = ''

  try {
    examQuestions.value = await request<Question[]>('/questions/random?count=8')
    examAnswers.value = {}
    examResult.value = null
  } catch (err) {
    error.value = err instanceof Error ? err.message : '抽题失败'
  } finally {
    loading.value = false
  }
}

function chooseAnswer(questionId: number, answer: OptionKey) {
  examAnswers.value = { ...examAnswers.value, [questionId]: answer }
}

async function submitExam() {
  loading.value = true
  error.value = ''

  try {
    const answers = examQuestions.value.map((question) => ({
      questionId: question.id,
      answer: examAnswers.value[question.id] ?? '',
    }))
    examResult.value = await request<ExamResult>('/exam/submit', {
      method: 'POST',
      body: JSON.stringify({ answers }),
    })
  } catch (err) {
    error.value = err instanceof Error ? err.message : '提交考试失败'
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
          <p class="eyebrow">模拟考试</p>
          <h2>完成全部题目后提交，系统自动评分。</h2>
        </div>
        <button class="ghost" @click="startExam">重新抽题</button>
      </div>

      <div class="exam-list">
        <article v-for="(question, index) in examQuestions" :key="question.id" class="exam-item">
          <h3>{{ index + 1 }}. {{ question.content }}</h3>
          <div class="compact-options">
            <button
              v-for="key in optionKeys"
              :key="key"
              :class="{ selected: examAnswers[question.id] === key }"
              @click="chooseAnswer(question.id, key)"
            >
              {{ key }}. {{ optionText(question, key) }}
            </button>
          </div>
        </article>
      </div>

      <div class="exam-footer">
        <span>已答 {{ answeredCount }} / {{ examQuestions.length }}</span>
        <button class="primary" :disabled="answeredCount !== examQuestions.length" @click="submitExam">提交考试</button>
      </div>

      <div v-if="examResult" class="result-box">
        <strong>{{ examResult.score }} 分</strong>
        <span>答对 {{ examResult.correct }} / {{ examResult.total }} 题</span>
        <p v-if="examResult.wrongQuestions.length">错题已自动进入错题本。</p>
        <p v-else>本次考试全部答对。</p>
      </div>
    </section>
  </AppLayout>
</template>
