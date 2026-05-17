<script setup lang="ts">
defineOptions({
  name: "Welcome"
});
</script>

<template>
  <div class="dashboard-shell">
    <header class="dashboard-head">
      <div>
        <p>驾校运营后台</p>
        <h1>工作台</h1>
      </div>
      <span>单驾校 · 科目一至科目四全流程</span>
    </header>

    <section class="metric-grid">
      <article>
        <span>今日报名意向</span>
        <strong>0</strong>
      </article>
      <article>
        <span>待审核预约</span>
        <strong>0</strong>
      </article>
      <article>
        <span>本周考试通过</span>
        <strong>0</strong>
      </article>
      <article>
        <span>待跟进学员</span>
        <strong>0</strong>
      </article>
    </section>

    <section class="panel-grid">
      <article>
        <h2>待办事项</h2>
        <p>报名意向、预约审核、成绩录入会在这里集中展示。</p>
      </article>
      <article>
        <h2>科目进度</h2>
        <p>后续按科目一、科目二、科目三、科目四统计学员阶段与通过率。</p>
      </article>
    </section>
  </div>
</template>

<style scoped>
.dashboard-shell {
  display: grid;
  gap: 16px;
  padding: 22px 28px;
}

.dashboard-head {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  justify-content: space-between;
}

.dashboard-head p,
.dashboard-head h1 {
  margin: 0;
}

.dashboard-head p {
  font-size: 14px;
  color: #4f6b5f;
}

.dashboard-head h1 {
  margin-top: 4px;
  font-size: 28px;
  color: #14251e;
}

.dashboard-head span {
  color: #64736c;
}

.metric-grid,
.panel-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.metric-grid article,
.panel-grid article {
  padding: 16px;
  background: #fff;
  border: 1px solid #dfe7e2;
  border-radius: 8px;
}

.metric-grid span {
  color: #64736c;
}

.metric-grid strong {
  display: block;
  margin-top: 10px;
  font-size: 28px;
  color: #21483a;
}

.panel-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.panel-grid h2 {
  margin: 0 0 8px;
  font-size: 18px;
  color: #14251e;
}

.panel-grid p {
  margin: 0;
  line-height: 1.7;
  color: #64736c;
}

@media (width <= 960px) {
  .metric-grid,
  .panel-grid {
    grid-template-columns: 1fr;
  }
}
</style>
