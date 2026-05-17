<script setup lang="ts">
import { computed } from "vue";
import { useAuth } from "@/stores/auth";

const { user, isLoggedIn } = useAuth();
const profileText = computed(() => {
  if (!isLoggedIn.value || !user.value) return "请先登录后查看个人报考信息";
  return `学员：${user.value.nickname}（${user.value.username}）`;
});
</script>

<template>
  <div class="profile-page">
    <section class="panel">
      <h1>个人中心</h1>
      <p>{{ profileText }}</p>
      <ul>
        <li>报名状态：待完善</li>
        <li>缴费状态：待完善</li>
        <li>学习进度：待完善</li>
        <li>考试计划：待完善</li>
      </ul>
      <small>该页面已预留，后续可对接教学管理模块。</small>
    </section>
  </div>
</template>

<style scoped>
.panel {
  padding: 22px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 8px 20px rgb(14 46 80 / 10%);
}

ul {
  padding-left: 18px;
  margin: 10px 0;
  line-height: 1.8;
  color: #4d6784;
}

small {
  color: #6e84a0;
}
</style>
