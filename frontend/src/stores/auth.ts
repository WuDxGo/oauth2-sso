import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string>('')
  const tokenType = ref<string>('Bearer')
  const expiresIn = ref<number>(0)
  const user = ref<UserInfo | null>(null)

  const isAuthenticated = computed(() => !!accessToken.value)
  const username = computed(() => user.value?.username || '')
  const nickname = computed(() => user.value?.nickname || '')
  const roles = computed(() => user.value?.roles || [])
  const hasRole = (role: string) => roles.value.includes(role)

  function setToken(token: string, type: string = 'Bearer', expires: number = 7200) {
    accessToken.value = token
    tokenType.value = type
    expiresIn.value = expires
  }

  function setUser(userInfo: UserInfo) {
    user.value = userInfo
  }

  function logout() {
    accessToken.value = ''
    tokenType.value = ''
    expiresIn.value = 0
    user.value = null
  }

  return {
    accessToken,
    tokenType,
    expiresIn,
    user,
    isAuthenticated,
    username,
    nickname,
    roles,
    hasRole,
    setToken,
    setUser,
    logout
  }
})
