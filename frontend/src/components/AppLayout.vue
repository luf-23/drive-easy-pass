<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuth } from '../stores/auth'

const router = useRouter()
const { user, isLoggedIn, logout } = useAuth()

function handleLogout() {
  logout()
  router.push('/login')
}
</script>

<template>
  <main class="app-shell">
    <aside class="sidebar">
      <div class="brand-block">
        <p class="eyebrow">Drive Easy Pass</p>
        <h1>驾考一点通</h1>
        <p class="subtle">练题、模拟考试、错题复习，一套流程直接跑通。</p>
      </div>

      <nav class="nav-list" aria-label="功能导航">
        <RouterLink to="/home">首页</RouterLink>
        <RouterLink to="/practice">顺序练习</RouterLink>
        <RouterLink to="/exam">模拟考试</RouterLink>
        <RouterLink to="/wrong">错题本</RouterLink>
      </nav>

      <section class="user-panel">
        <div v-if="isLoggedIn" class="user-card">
          <span>当前用户</span>
          <strong>{{ user?.nickname }}</strong>
          <button class="sidebar-action" @click="handleLogout">退出登录</button>
        </div>
        <div v-else class="guest-card">
          <RouterLink class="sidebar-action" to="/login">登录</RouterLink>
          <RouterLink class="ghost-link" to="/register">注册账号</RouterLink>
        </div>
      </section>
    </aside>

    <section class="workspace">
      <slot />
    </section>
  </main>
</template>
