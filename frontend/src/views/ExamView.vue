<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { request } from "../services/api";
import type { ExamResult, OptionKey, Question } from "../types";

const examQuestions = ref<Question[]>([]);
const examAnswers = ref<Record<number, OptionKey>>({});
const examResult = ref<ExamResult | null>(null);
const loading = ref(false);
const error = ref("");
const optionKeys: OptionKey[] = ["A", "B", "C", "D"];
const examCount = 20;
const questionScore = 5;
const currentExamType = ref(localStorage.getItem("reservedExamType") || "科目一");

const answeredCount = computed(() => Object.keys(examAnswers.value).length);
const examProgress = computed(
  () => `${answeredCount.value} / ${examQuestions.value.length}`
);

onMounted(startExam);

function switchExamType(type: string) {
  currentExamType.value = type;
  localStorage.setItem("reservedExamType", type);
  startExam();
}

function optionText(question: Question, key: OptionKey) {
  return question[`option${key}` as keyof Question] as string;
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
    examResult.value = null;
  } catch (err) {
    error.value = err instanceof Error ? err.message : "试卷加载失败";
  } finally {
    loading.value = false;
  }
}

function chooseAnswer(questionId: number, answer: OptionKey) {
  if (examAnswers.value[questionId]) return;
  examAnswers.value = { ...examAnswers.value, [questionId]: answer };
}

function optionState(question: Question, key: OptionKey) {
  const selected = examAnswers.value[question.id];
  return {
    selected: selected === key,
    correct: !!selected && question.answer === key,
    wrong: selected === key && selected !== question.answer
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
            <h3>{{ index + 1 }}. {{ question.content }}</h3>
            <div class="compact-options">
              <button
                v-for="key in optionKeys"
                :key="key"
                :class="optionState(question, key)"
                :disabled="!!examAnswers[question.id]"
                @click="chooseAnswer(question.id, key)"
              >
                <b>{{ key }}</b>
                <span>{{ optionText(question, key) }}</span>
              </button>
            </div>
            <div v-if="examAnswers[question.id]" class="exam-feedback">
              <strong>
                {{ examAnswers[question.id] === question.answer ? "回答正确" : "回答错误" }}
              </strong>
              <span>正确答案：{{ question.answer }}。{{ question.explanation }}</span>
            </div>
          </article>
        </div>
        <div v-if="examResult" class="result-box">
          <strong>{{ examResult.score }} 分</strong>
          <span>正确 {{ examResult.correct }} / {{ examResult.total }} 题</span>
          <p v-if="examResult.wrongQuestions.length">未掌握题目已汇总到错题反馈。</p>
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
