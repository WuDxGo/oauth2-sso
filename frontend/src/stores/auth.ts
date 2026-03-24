import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/api/auth'

const ACCESS_TOKEN_KEY = 'access_token'
const TOKEN_TYPE_KEY = 'token_type'
const EXPIRES_IN_KEY = 'expires_in'
const USER_INFO_KEY = 'user_info'

function getFromStorage<T>(key: string, defaultValue: T): T {
  const item = localStorage.getItem(key)
  if (item) {
    try {
      return JSON.parse(item)
    } catch {
      return defaultValue
    }
  }
  return defaultValue
}

function setToStorage<T>(key: string, value: T) {
  localStorage.setItem(key, JSON.stringify(value))
}

function removeFromStorage(key: string) {
  localStorage.removeItem(key)
}

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string>(getFromStorage(ACCESS_TOKEN_KEY, ''))
  const tokenType = ref<string>(getFromStorage(TOKEN_TYPE_KEY, 'Bearer'))
  const expiresIn = ref<number>(getFromStorage(EXPIRES_IN_KEY, 0))
  const user = ref<UserInfo | null>(getFromStorage<UserInfo | null>(USER_INFO_KEY, null))

  const isAuthenticated = computed(() => !!accessToken.value)
  const username = computed(() => user.value?.username || '')
  const nickname = computed(() => user.value?.nickname || '')
  const roles = computed(() => user.value?.roles || [])
  const hasRole = (role: string) => roles.value.includes(role)

  function setToken(token: string, type: string = 'Bearer', expires: number = 7200) {
    accessToken.value = token
    tokenType.value = type
    expiresIn.value = expires
    setToStorage(ACCESS_TOKEN_KEY, token)
    setToStorage(TOKEN_TYPE_KEY, type)
    setToStorage(EXPIRES_IN_KEY, expires)
  }

  function setUser(userInfo: UserInfo) {
    user.value = userInfo
    setToStorage(USER_INFO_KEY, userInfo)
  }

  function logout() {
    accessToken.value = ''
    tokenType.value = ''
    expiresIn.value = 0
    user.value = null
    removeFromStorage(ACCESS_TOKEN_KEY)
    removeFromStorage(TOKEN_TYPE_KEY)
    removeFromStorage(EXPIRES_IN_KEY)
    removeFromStorage(USER_INFO_KEY)
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
