import { computed, ref } from 'vue'
import type { UserProfile } from '../types'

const token = ref(localStorage.getItem('drive_easy_pass_token') ?? '')
const user = ref<UserProfile | null>(readStoredUser())

export const isLoggedIn = computed(() => !!token.value && !!user.value)

export function useAuth() {
  return {
    token,
    user,
    isLoggedIn,
    setAuth,
    logout,
  }
}

function readStoredUser() {
  const raw = localStorage.getItem('drive_easy_pass_user')
  if (!raw) return null

  try {
    return JSON.parse(raw) as UserProfile
  } catch {
    return null
  }
}

export function setAuth(nextToken: string, nextUser: UserProfile) {
  token.value = nextToken
  user.value = nextUser
  localStorage.setItem('drive_easy_pass_token', nextToken)
  localStorage.setItem('drive_easy_pass_user', JSON.stringify(nextUser))
}

export function logout() {
  token.value = ''
  user.value = null
  localStorage.removeItem('drive_easy_pass_token')
  localStorage.removeItem('drive_easy_pass_user')
}
