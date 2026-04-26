<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { request } from '../services/api'
import { setAuth } from '../stores/auth'
import type { AuthResponse } from '../types'

const props = defineProps<{
  mode: 'login' | 'register'
}>()

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const error = ref('')
const form = ref({
  username: '',
  password: '',
  nickname: '',
})

const isRegister = computed(() => props.mode === 'register')
const title = computed(() => (isRegister.value ? '注册账号' : '登录账号'))
const submitText = computed(() => (isRegister.value ? '注册并登录' : '登录'))
const switchText = computed(() => (isRegister.value ? '已有账号？去登录' : '没有账号？去注册'))
const switchPath = computed(() => (isRegister.value ? '/login' : '/register'))

watch(
  () => props.mode,
  () => {
    error.value = ''
    form.value.password = ''
    form.value.nickname = ''
  },
)

async function submitAuth() {
  loading.value = true
  error.value = ''

  try {
    const result = await request<AuthResponse>(`/auth/${props.mode}`, {
      method: 'POST',
      body: JSON.stringify(form.value),
    })
    setAuth(result.token, result.user)
    router.push((route.query.redirect as string) || '/home')
  } catch (err) {
    error.value = err instanceof Error ? err.message : '认证失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="auth-page">
    <section class="auth-card">
      <RouterLink class="brand-link" to="/home">Drive Easy Pass</RouterLink>
      <h1>{{ title }}</h1>
      <p>{{ isRegister ? '创建账号后，错题会保存到你的个人错题本。' : '登录后继续练习、考试和复习错题。' }}</p>

      <form class="auth-page-form" @submit.prevent="submitAuth">
        <label>
          用户名
          <input v-model="form.username" autocomplete="username" placeholder="3-20 位字母、数字或下划线" />
        </label>
        <label>
          密码
          <input v-model="form.password" autocomplete="current-password" placeholder="6-32 位密码" type="password" />
        </label>
        <label v-if="isRegister">
          昵称
          <input v-model="form.nickname" placeholder="可不填，默认使用用户名" />
        </label>

        <div v-if="error" class="message error">{{ error }}</div>
        <button class="primary" :disabled="loading" type="submit">{{ loading ? '处理中...' : submitText }}</button>
      </form>

      <RouterLink class="auth-switch" :to="switchPath">{{ switchText }}</RouterLink>
    </section>
  </main>
</template>
