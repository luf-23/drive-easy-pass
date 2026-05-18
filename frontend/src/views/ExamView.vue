<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { request } from "../services/api";
import type { ExamResult, OptionKey, Question } from "../types";
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

const examQuestions = ref<Question[]>([]);
const examAnswers = ref<Record<number, string>>({});
const examLocked = ref<Record<number, boolean>>({});
const examResult = ref<ExamResult | null>(null);
const loading = ref(false);
const error = ref("");
const examCount = 20;
const questionScore = 5;
const currentExamType = ref(
  localStorage.getItem("reservedExamType") || "科目一"
);

const answeredCount = computed(
  () => Object.values(examLocked.value).filter(Boolean).length
);
const examProgress = computed(
  () => `${answeredCount.value} / ${examQuestions.value.length}`
);

onMounted(startExam);

function switchExamType(type: string) {
  currentExamType.value = type;
  localStorage.setItem("reservedExamType", type);
  startExam();
}

function isMulti(question: Question) {
  return isMultiAnswer(question.answer);
}

function isLocked(questionId: number) {
  return !!examLocked.value[questionId];
}

async function startExam() {
  loading.value = true;
  error.value = "";
  try {
    const type = currentExamType.value;
    const url = type
      ? `/questions/random?count=${examCount}&examType=${type}`
      : `/questions/random?count=${examCount}`;
    examQuestions.value = await request<Question[]>(url);
    examAnswers.value = {};
    examLocked.value = {};
    examResult.value = null;
  } catch (err) {
    error.value = err instanceof Error ? err.message : "试卷加载失败";
  } finally {
    loading.value = false;
  }
}

function chooseAnswer(question: Question, answer: OptionKey) {
  if (isLocked(question.id)) return;

  if (isMulti(question)) {
    const current = examAnswers.value[question.id] ?? "";
    examAnswers.value = {
      ...examAnswers.value,
      [question.id]: toggleAnswer(current, answer)
    };
    return;
  }

  examAnswers.value = { ...examAnswers.value, [question.id]: answer };
  examLocked.value = { ...examLocked.value, [question.id]: true };
}

function confirmMultiAnswer(question: Question) {
  if (!examAnswers.value[question.id]) return;
  examLocked.value = { ...examLocked.value, [question.id]: true };
}

function optionState(question: Question, key: OptionKey) {
  const selected = normalizeAnswer(examAnswers.value[question.id] ?? "");
  const expected = normalizeAnswer(question.answer);
  const locked = isLocked(question.id);
  const variant = judgmentVariant(question, key);

  return {
    selected: selected.includes(key),
    correct: locked && expected.includes(key),
    wrong: locked && selected.includes(key) && !expected.includes(key),
    "judgment-option": isJudgmentQuestion(question),
    "judgment-option--true": variant === "true",
    "judgment-option--false": variant === "false"
  };
}

async function submitExam() {
  loading.value = true;
  error.value = "";
  try {
    const answers = examQuestions.value.map(question => ({
      questionId: question.id,
      answer: examAnswers.value[question.id] ?? ""
    }));
    examResult.value = await request<ExamResult>("/exam/submit", {
      method: "POST",
      body: JSON.stringify({ answers })
    });
  } catch (err) {
    error.value = err instanceof Error ? err.message : "提交试卷失败";
  } finally {
    loading.value = false;
  }
}
</script>

<template>
  <div class="drive-page">
    <div class="exam-type-switch">
      <button
        :class="{ active: currentExamType === '科目一' }"
        @click="switchExamType('科目一')"
      >
        科目一
      </button>
      <button
        :class="{ active: currentExamType === '科目四' }"
        @click="switchExamType('科目四')"
      >
        科目四
      </button>
    </div>

    <div v-if="error" class="message error">{{ error }}</div>
    <div v-if="loading" class="message">正在加载试卷...</div>

    <section class="panel exam-panel">
      <div class="exam-fixed-head">
        <div class="exam-toolbar">
          <div class="exam-title">
            <p class="eyebrow">模拟考试</p>
            <h2>随机抽取 {{ examCount }} 道题，每题 {{ questionScore }} 分</h2>
          </div>
          <div class="exam-actions">
            <span class="exam-meta">题量 {{ examQuestions.length }}</span>
            <span class="exam-meta">进度 {{ examProgress }}</span>
            <button class="ghost" @click="startExam">重新抽题</button>
          </div>
        </div>
      </div>

      <div class="exam-scroll">
        <div class="exam-list">
          <article
            v-for="(question, index) in examQuestions"
            :key="question.id"
            class="exam-item"
          >
            <h3>
              {{ index + 1 }}. {{ question.content }}
              <span v-if="isJudgmentQuestion(question)" class="pill">判断</span>
              <span v-else-if="isMulti(question)" class="pill">多选</span>
            </h3>
            <div
              class="compact-options"
              :class="{ 'judgment-options': isJudgmentQuestion(question) }"
            >
              <button
                v-for="key in optionKeysFor(question)"
                :key="key"
                :class="optionState(question, key)"
                :disabled="isLocked(question.id)"
                @click="chooseAnswer(question, key)"
              >
                <span
                  v-if="isJudgmentQuestion(question)"
                  class="judgment-icon"
                  aria-hidden="true"
                >{{ judgmentIcon(question, key) }}</span>
                <b v-else>{{ key }}</b>
                <span
                  :class="{
                    'judgment-label': isJudgmentQuestion(question)
                  }"
                >
                  {{ optionText(question, key) }}
                </span>
              </button>
            </div>
            <div
              v-if="isMulti(question) && !isLocked(question.id) && examAnswers[question.id]"
              class="exam-feedback"
            >
              <button class="ghost" @click="confirmMultiAnswer(question)">
                确认本题答案
              </button>
            </div>
            <div v-if="isLocked(question.id)" class="exam-feedback">
              <strong>
                {{
                  isAnswerCorrect(examAnswers[question.id] ?? "", question.answer)
                    ? "回答正确"
                    : "回答错误"
                }}
              </strong>
              <span>
                正确答案：{{ formatAnswer(question.answer) }}。{{
                  question.explanation
                }}
              </span>
            </div>
          </article>
        </div>
        <div v-if="examResult" class="result-box">
          <strong>{{ examResult.score }} 分</strong>
          <span>正确 {{ examResult.correct }} / {{ examResult.total }} 题</span>
          <p v-if="examResult.wrongQuestions.length">
            未掌握题目已汇总到错题反馈。
          </p>
          <p v-else>本次模拟考试全部答对。</p>
        </div>
      </div>

      <div class="exam-footer">
        <span>答题进度 {{ examProgress }}</span>
        <button
          class="primary"
          :disabled="answeredCount !== examQuestions.length"
          @click="submitExam"
        >
          提交试卷
        </button>
      </div>
    </section>
  </div>
</template>
