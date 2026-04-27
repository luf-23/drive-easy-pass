<script setup lang="ts">
import { reactive, ref } from "vue";
import { createPublicEnrollmentIntent } from "@/api/public";
import type { PublicEnrollmentIntentPayload } from "@/types";

const submitting = ref(false);
const message = ref("");
const error = ref("");

const form = reactive<PublicEnrollmentIntentPayload>({
  name: "",
  phone: "",
  vehicleType: "C2",
  classType: "普通班",
  source: "线上广告",
  remark: ""
});

async function submit() {
  submitting.value = true;
  message.value = "";
  error.value = "";
  try {
    await createPublicEnrollmentIntent(form);
    message.value = "报名信息已提交，招生顾问会尽快联系你。";
    Object.assign(form, {
      name: "",
      phone: "",
      vehicleType: "C2",
      classType: "普通班",
      source: "线上广告",
      remark: ""
    });
  } catch (err) {
    error.value = err instanceof Error ? err.message : "提交失败，请稍后重试";
  } finally {
    submitting.value = false;
  }
}
</script>

<template>
  <div class="signup-page">
    <section class="panel">
      <h1>在线报名 / 留资</h1>
      <p>填写基础信息后，系统会自动进入招生管理线索池。</p>

      <form class="signup-form" @submit.prevent="submit">
        <label>
          姓名
          <input v-model="form.name" required />
        </label>
        <label>
          手机号
          <input v-model="form.phone" required />
        </label>
        <label>
          意向车型
          <select v-model="form.vehicleType">
            <option value="C1">C1 手动挡</option>
            <option value="C2">C2 自动挡</option>
          </select>
        </label>
        <label>
          意向班型
          <select v-model="form.classType">
            <option value="普通班">普通班</option>
            <option value="VIP 班">VIP 班</option>
            <option value="周末班">周末班</option>
          </select>
        </label>
        <label>
          来源
          <select v-model="form.source">
            <option value="线上广告">线上广告</option>
            <option value="地推">地推</option>
            <option value="转介绍">转介绍</option>
          </select>
        </label>
        <label class="wide">
          备注
          <textarea v-model="form.remark" placeholder="可填写你的时间偏好"></textarea>
        </label>
        <button class="submit" :disabled="submitting" type="submit">
          {{ submitting ? "提交中..." : "立即报名" }}
        </button>
      </form>

      <p v-if="message" class="ok">{{ message }}</p>
      <p v-if="error" class="err">{{ error }}</p>
    </section>
  </div>
</template>

<style scoped>
.signup-page {
  display: grid;
}

.panel {
  padding: 22px;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 8px 20px rgba(14, 46, 80, 0.1);
}

.signup-form {
  margin-top: 14px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

label {
  display: grid;
  gap: 6px;
  color: #2f4f6c;
  font-weight: 700;
}

input,
select,
textarea {
  width: 100%;
  min-height: 40px;
  padding: 8px 10px;
  border: 1px solid #d8e6f5;
  border-radius: 10px;
  background: #f7fbff;
}

textarea {
  min-height: 90px;
  resize: vertical;
}

.wide,
.submit {
  grid-column: 1 / -1;
}

.submit {
  min-height: 42px;
  border-radius: 10px;
  color: #103c62;
  font-weight: 800;
  background: #f7be3b;
}

.ok {
  margin-top: 10px;
  color: #127645;
}

.err {
  margin-top: 10px;
  color: #be3f35;
}

@media (max-width: 768px) {
  .signup-form {
    grid-template-columns: 1fr;
  }
}
</style>
