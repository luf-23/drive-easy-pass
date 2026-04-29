<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { request } from "../services/api";
import { useAuth } from "../stores/auth";
import type { OptionKey, Question } from "../types";

const { isLoggedIn } = useAuth();
const questions = ref<Question[]>([]);
const practiceIndex = ref(0);
const selectedAnswer = ref<OptionKey | "">("");
const loading = ref(false);
const error = ref("");
const practiceExamType = ref(localStorage.getItem("reservedExamType") || "科目一");

const optionKeys = computed<OptionKey[]>(() => {
  const q = currentQuestion.value;
  if (!q) return ["A", "B", "C", "D"];
  if (!q.optionC && !q.optionD) return ["A", "B"];
  return ["A", "B", "C", "D"];
});

const currentQuestion = computed(() => questions.value[practiceIndex.value]);
const progress = computed(() => {
  if (!questions.value.length) return "0 / 0";
  return `${practiceIndex.value + 1} / ${questions.value.length}`;
});

const answerTitle = computed(() => {
  if (!selectedAnswer.value || !currentQuestion.value) return "请选择一个答案";
  if (selectedAnswer.value === currentQuestion.value.answer) return "回答正确";
  return isLoggedIn.value ? "回答错误，已加入错题本" : "回答错误，登录后可保存错题";
});

onMounted(loadQuestions);

function switchPracticeExamType(type: string) {
  practiceExamType.value = type;
  localStorage.setItem("reservedExamType", type);
  practiceIndex.value = 0;
  selectedAnswer.value = "";
  loadQuestions();
}

async function loadQuestions() {
  loading.value = true;
  error.value = "";
  try {
    const type = practiceExamType.value;
    const url = type
      ? `/questions/random?count=20&examType=${type}`
      : "/questions/random?count=20";
    questions.value = await request<Question[]>(url);
  } catch (err) {
    error.value = err instanceof Error ? err.message : "题目加载失败";
  } finally {
    loading.value = false;
  }
}

function optionText(question: Question, key: OptionKey) {
  return question[`option${key}` as keyof Question] as string;
}

async function chooseAnswer(answer: OptionKey) {
  selectedAnswer.value = answer;
  const question = currentQuestion.value;
  if (question && answer !== question.answer && isLoggedIn.value) {
    await request("/wrong-questions", {
      method: "POST",
      body: JSON.stringify({ questionId: question.id })
    });
  }
}

function nextQuestion() {
  selectedAnswer.value = "";
  practiceIndex.value = (practiceIndex.value + 1) % questions.value.length;
}
</script>

<template>
  <div class="drive-page">
    <div class="exam-type-switch">
      <button
        :class="{ active: practiceExamType === '科目一' }"
        @click="switchPracticeExamType('科目一')"
      >
        科目一
      </button>
      <button
        :class="{ active: practiceExamType === '科目四' }"
        @click="switchPracticeExamType('科目四')"
      >
        科目四
      </button>
    </div>

    <div v-if="error" class="message error">{{ error }}</div>
    <div v-if="loading" class="message">正在加载题目...</div>

    <section v-if="currentQuestion" class="panel practice-panel">
      <div class="section-head practice-head">
        <div>
          <p class="eyebrow">顺序练习</p>
          <h2>{{ currentQuestion.content }}</h2>
        </div>
        <span class="pill">{{ progress }}</span>
      </div>

      <div class="option-list practice-options">
        <button
          v-for="key in optionKeys"
          :key="key"
          class="option"
          :class="{
            selected: selectedAnswer === key,
            correct: selectedAnswer && currentQuestion.answer === key,
            wrong: selectedAnswer === key && selectedAnswer !== currentQuestion.answer
          }"
          :disabled="!!selectedAnswer"
          @click="chooseAnswer(key)"
        >
          <b>{{ key }}</b>
          <span>{{ optionText(currentQuestion, key) }}</span>
        </button>
      </div>

      <div class="answer-card practice-feedback" :class="{ empty: !selectedAnswer }">
        <div>
          <strong>{{ answerTitle }}</strong>
          <p v-if="selectedAnswer">
            正确答案：{{ currentQuestion.answer }}。{{ currentQuestion.explanation }}
          </p>
          <p v-else>作答后这里会显示对错状态和本题解析。</p>
        </div>
        <button v-if="selectedAnswer" class="primary" @click="nextQuestion">
          下一题
        </button>
      </div>
    </section>
  </div>
</template>
