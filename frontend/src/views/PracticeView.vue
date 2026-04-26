<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { request } from '../services/api'
import { useAuth } from '../stores/auth'
import type { OptionKey, Question } from '../types'

const { isLoggedIn } = useAuth()
const questions = ref<Question[]>([])
const practiceIndex = ref(0)
const selectedAnswer = ref<OptionKey | ''>('')
const loading = ref(false)
const error = ref('')
const optionKeys: OptionKey[] = ['A', 'B', 'C', 'D']

const currentQuestion = computed(() => questions.value[practiceIndex.value])
const progress = computed(() => {
  if (!questions.value.length) return '0 / 0'
  return `${practiceIndex.value + 1} / ${questions.value.length}`
})

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

function optionText(question: Question, key: OptionKey) {
  return question[`option${key}` as keyof Question] as string
}

async function chooseAnswer(answer: OptionKey) {
  selectedAnswer.value = answer
  const question = currentQuestion.value
  if (question && answer !== question.answer && isLoggedIn.value) {
    await request('/wrong-questions', {
      method: 'POST',
      body: JSON.stringify({ questionId: question.id }),
    })
  }
}

function nextQuestion() {
  selectedAnswer.value = ''
  practiceIndex.value = (practiceIndex.value + 1) % questions.value.length
}
</script>

<template>
  <div class="drive-page">
    <div v-if="error" class="message error">{{ error }}</div>
    <div v-if="loading" class="message">正在加载数据...</div>

    <section v-if="currentQuestion" class="panel">
      <div class="section-head">
        <div>
          <p class="eyebrow">顺序练习</p>
          <h2>{{ currentQuestion.content }}</h2>
        </div>
        <span class="pill">{{ progress }}</span>
      </div>

      <div class="option-list">
        <button
          v-for="key in optionKeys"
          :key="key"
          class="option"
          :class="{
            selected: selectedAnswer === key,
            correct: selectedAnswer && currentQuestion.answer === key,
            wrong: selectedAnswer === key && selectedAnswer !== currentQuestion.answer,
          }"
          :disabled="!!selectedAnswer"
          @click="chooseAnswer(key)"
        >
          <b>{{ key }}</b>
          <span>{{ optionText(currentQuestion, key) }}</span>
        </button>
      </div>

      <div v-if="selectedAnswer" class="answer-card">
        <strong>
          {{
            selectedAnswer === currentQuestion.answer
              ? '回答正确'
              : isLoggedIn
                ? '回答错误，已加入错题本'
                : '回答错误，登录后可保存错题'
          }}
        </strong>
        <p>正确答案：{{ currentQuestion.answer }}。{{ currentQuestion.explanation }}</p>
        <button class="primary" @click="nextQuestion">下一题</button>
      </div>
    </section>
  </div>
</template>
