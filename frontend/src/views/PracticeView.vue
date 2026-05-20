<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { request } from "../services/api";
import { useAuth } from "../stores/auth";
import type { OptionKey } from "../types";
import {
  formatAnswer,
  isAnswerCorrect,
  isMultiAnswer,
  normalizeAnswer,
  toggleAnswer
} from "../utils/answer";
import {
  isJudgmentQuestion,
  judgmentIcon,
  judgmentVariant,
  optionKeysFor,
  optionText
} from "../utils/question";

const { isLoggedIn } = useAuth();
const questions = ref<Question[]>([]);
const practiceIndex = ref(0);
const selectedAnswer = ref("");
const answerLocked = ref(false);
const loading = ref(false);
const error = ref("");
const practiceExamType = ref(
  localStorage.getItem("reservedExamType") || "科目一"
);

const currentQuestion = computed(() => questions.value[practiceIndex.value]);
const optionKeys = computed<OptionKey[]>(() =>
  currentQuestion.value ? optionKeysFor(currentQuestion.value) : []
);
const isMulti = computed(() =>
  currentQuestion.value ? isMultiAnswer(currentQuestion.value.answer) : false
);
const isJudgment = computed(() =>
  currentQuestion.value ? isJudgmentQuestion(currentQuestion.value) : false
);

const progress = computed(() => {
  if (!questions.value.length) return "0 / 0";
  return `${practiceIndex.value + 1} / ${questions.value.length}`;
});

const answerTitle = computed(() => {
  if (!answerLocked.value || !currentQuestion.value) {
    return isMulti.value ? "请选择所有正确选项后确认" : "请选择一个答案";
  }
  if (isAnswerCorrect(selectedAnswer.value, currentQuestion.value.answer)) {
    return "回答正确";
  }
  return isLoggedIn.value
    ? "回答错误，已加入错题本"
    : "回答错误，登录后可保存错题";
});

onMounted(loadQuestions);

function switchPracticeExamType(type: string) {
  practiceExamType.value = type;
  localStorage.setItem("reservedExamType", type);
  practiceIndex.value = 0;
  resetAnswer();
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
    resetAnswer();
  } catch (err) {
    error.value = err instanceof Error ? err.message : "题目加载失败";
  } finally {
    loading.value = false;
  }
}

function resetAnswer() {
  selectedAnswer.value = "";
  answerLocked.value = false;
}

function pickAnswer(answer: OptionKey) {
  if (answerLocked.value || !currentQuestion.value) return;

  if (isMulti.value) {
    selectedAnswer.value = toggleAnswer(selectedAnswer.value, answer);
    return;
  }

  selectedAnswer.value = answer;
  answerLocked.value = true;
  void recordWrongIfNeeded();
}

async function confirmMultiAnswer() {
  if (!isMulti.value || answerLocked.value || !selectedAnswer.value) return;
  answerLocked.value = true;
  await recordWrongIfNeeded();
}

async function recordWrongIfNeeded() {
  const question = currentQuestion.value;
  if (
    !question ||
    !isLoggedIn.value ||
    isAnswerCorrect(selectedAnswer.value, question.answer)
  ) {
    return;
  }

  await request("/wrong-questions", {
    method: "POST",
    body: JSON.stringify({ questionId: question.id })
  });
}

function optionClass(key: OptionKey) {
  const selected = normalizeAnswer(selectedAnswer.value);
  const expected = currentQuestion.value
    ? normalizeAnswer(currentQuestion.value.answer)
    : "";
  const question = currentQuestion.value;
  const variant = question ? judgmentVariant(question, key) : null;

  return {
    selected: selected.includes(key),
    correct: answerLocked.value && expected.includes(key),
    wrong:
      answerLocked.value && selected.includes(key) && !expected.includes(key),
    "judgment-option": isJudgment.value,
    "judgment-option--true": variant === "true",
    "judgment-option--false": variant === "false"
  };
}

function nextQuestion() {
  resetAnswer();
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
          <p class="eyebrow">
            顺序练习{{ isJudgment ? " · 判断题" : isMulti ? " · 多选题" : "" }}
          </p>
          <h2>{{ currentQuestion.content }}</h2>
        </div>
        <span class="pill">{{ progress }}</span>
      </div>

      <div
        class="option-list practice-options"
        :class="{ 'judgment-options': isJudgment }"
      >
        <button
          v-for="key in optionKeys"
          :key="key"
          class="option"
          :class="optionClass(key)"
          :disabled="answerLocked"
          @click="pickAnswer(key)"
        >
          <span v-if="isJudgment" class="judgment-icon" aria-hidden="true">{{
            judgmentIcon(currentQuestion, key)
          }}</span>
          <b v-else>{{ key }}</b>
          <span :class="{ 'judgment-label': isJudgment }">
            {{ optionText(currentQuestion, key) }}
          </span>
        </button>
      </div>

      <div
        class="answer-card practice-feedback"
        :class="{ empty: !answerLocked && !selectedAnswer }"
      >
        <div>
          <strong>{{ answerTitle }}</strong>
          <p v-if="answerLocked">
            正确答案：{{ formatAnswer(currentQuestion.answer) }}。{{
              currentQuestion.explanation
            }}
          </p>
          <p v-else-if="isMulti && selectedAnswer">
            已选 {{ formatAnswer(selectedAnswer) }}，请点击确认答案。
          </p>
          <p v-else>作答后这里会显示对错状态和本题解析。</p>
        </div>
        <button
          v-if="isMulti && !answerLocked && selectedAnswer"
          class="primary"
          @click="confirmMultiAnswer"
        >
          确认答案
        </button>
        <button v-else-if="answerLocked" class="primary" @click="nextQuestion">
          下一题
        </button>
      </div>
    </section>
  </div>
</template>
