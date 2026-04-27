<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { request } from '../services/api'
import type { ExamResult, OptionKey, Question } from '../types'

const examQuestions = ref<Question[]>([])
const examAnswers = ref<Record<number, OptionKey>>({})
const examResult = ref<ExamResult | null>(null)
const loading = ref(false)
const error = ref('')
const optionKeys: OptionKey[] = ['A', 'B', 'C', 'D']
const examCount = 20
const questionScore = 5

const currentExamType = ref(localStorage.getItem('reservedExamType') || '')
const switchExamType = (type: string) => {
  if (type !== localStorage.getItem('reservedExamType')) {
    alert(`请先在「考场信息」中预约${type}考试`)
    return
  }
  currentExamType.value = type
  localStorage.setItem('reservedExamType', type)
  startExam()
}
const hasReserved = ref(false)

const checkReserved = () => {
  const type = localStorage.getItem('reservedExamType')
  hasReserved.value = type === '科目一' || type === '科目四'
}

checkReserved()

window.addEventListener('storage', () => {
  checkReserved()
})
const answeredCount = computed(() => Object.keys(examAnswers.value).length)
const examProgress = computed(() => `${answeredCount.value} / ${examQuestions.value.length}`)

onMounted(startExam)

function optionText(question: Question, key: OptionKey) {
  return question[`option${key}` as keyof Question] as string
}

async function startExam() {
  loading.value = true
  error.value = ''
  try {
    const type = currentExamType.value
    const url = type
      ? `/questions/random?count=${examCount}&examType=${type}`
      : `/questions/random?count=${examCount}`
    examQuestions.value = await request<Question[]>(url)
    examAnswers.value = {}
    examResult.value = null
  } catch (err) {
    error.value = err instanceof Error ? err.message : '抽题失败'
  } finally {
    loading.value = false
  }
}

function chooseAnswer(questionId: number, answer: OptionKey) {
  if (examAnswers.value[questionId]) return
  examAnswers.value = { ...examAnswers.value, [questionId]: answer }
}

function optionState(question: Question, key: OptionKey) {
  const selected = examAnswers.value[question.id]
  return {
    selected: selected === key,
    correct: !!selected && question.answer === key,
    wrong: selected === key && selected !== question.answer,
  }
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
  <div class="drive-page">
    <div v-if="hasReserved" class="exam-type-switch">
      <button :class="{ active: currentExamType === '科目一' }" @click="switchExamType('科目一')">📝 科目一</button>
      <button :class="{ active: currentExamType === '科目四' }" @click="switchExamType('科目四')">🛡️ 科目四</button>
    </div>
    <div v-else class="no-reserve">
      请先在「考场信息」中预约科目一或科目四考试
    </div>


    <div v-if="error" class="message error">{{ error }}</div>
    <div v-if="loading" class="message">正在加载数据...</div>

    <section class="panel exam-panel">
      <div class="exam-fixed-head">
        <div class="section-head">
          <div>
            <p class="eyebrow">模拟考试</p>
            <h2>随机抽取 {{ examCount }} 道题，每题 {{ questionScore }} 分</h2>
          </div>
          <button class="ghost" @click="startExam">重新抽题</button>
        </div>
        <div class="exam-summary">
          <span>考试题量：{{ examQuestions.length }} 题</span>
          <span>单题分值：{{ questionScore }} 分</span>
          <span>答题进度：{{ examProgress }}</span>
        </div>
      </div>
      <div class="exam-scroll">
        <div class="exam-list">
          <article v-for="(question, index) in examQuestions" :key="question.id" class="exam-item">
            <h3>{{ index + 1 }}. {{ question.content }}</h3>
            <div class="compact-options">
              <button
                v-for="key in optionKeys" :key="key"
                :class="optionState(question, key)"
                :disabled="!!examAnswers[question.id]"
                @click="chooseAnswer(question.id, key)"
              >
                <b>{{ key }}</b>
                <span>{{ optionText(question, key) }}</span>
              </button>
            </div>
            <div v-if="examAnswers[question.id]" class="exam-feedback">
              <strong>{{ examAnswers[question.id] === question.answer ? '回答正确' : '回答错误' }}</strong>
              <span>正确答案：{{ question.answer }}。{{ question.explanation }}</span>
            </div>
          </article>
        </div>
        <div v-if="examResult" class="result-box">
          <strong>{{ examResult.score }} 分</strong>
          <span>答对 {{ examResult.correct }} / {{ examResult.total }} 题</span>
          <p v-if="examResult.wrongQuestions.length">错题已自动进入错题本。</p>
          <p v-else>本次考试全部答对。</p>
        </div>
      </div>
      <div class="exam-footer">
        <span>已答 {{ examProgress }}</span>
        <button class="primary" :disabled="answeredCount !== examQuestions.length" @click="submitExam">提交考试</button>
      </div>
    </section>
  </div>
</template>
<style scoped>
.exam-type-switch { display: flex; gap: 15px; margin-bottom: 20px; }
.exam-type-switch button { padding: 12px 40px; border: none; border-radius: 8px; cursor: pointer; font-size: 16px; font-weight: bold; }
.exam-type-switch button.active { background: #1890ff; color: white; }
</style>
