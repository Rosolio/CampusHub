import { computed, ref } from 'vue'

const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_KEY = 'user'
const AUTH_EVENT = 'campushub:auth-changed'

const decodeJwtPayload = (token: string) => {
  try {
    const parts = token.split('.')
    if (parts.length < 2) return null

    const base64 = parts[1].replace(/-/g, '+').replace(/_/g, '/')
    const padded = base64.padEnd(Math.ceil(base64.length / 4) * 4, '=')
    return JSON.parse(window.atob(padded))
  } catch {
    return null
  }
}

export const getStoredUser = () => {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

const readStorage = () => ({
  token: typeof window === 'undefined' ? null : window.localStorage.getItem(TOKEN_KEY),
  refreshToken: typeof window === 'undefined' ? null : window.localStorage.getItem(REFRESH_TOKEN_KEY),
  user: typeof window === 'undefined' ? null : getStoredUser()
})

const authState = ref(readStorage())

const emitAuthChanged = () => {
  authState.value = readStorage()
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new CustomEvent(AUTH_EVENT))
  }
}

export const syncAuthStateFromStorage = () => {
  authState.value = readStorage()
}

export const clearAuthStorage = () => {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
  emitAuthChanged()
}

export const redirectToLogin = () => {
  if (typeof window !== 'undefined') {
    window.location.replace('/auth?tab=login')
  }
}

export const logoutAndRedirect = () => {
  clearAuthStorage()
  redirectToLogin()
}

export const setStoredToken = (token: string) => {
  localStorage.setItem(TOKEN_KEY, token)
  emitAuthChanged()
}

export const getStoredToken = () => authState.value.token

export const setStoredRefreshToken = (refreshToken: string) => {
  localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  emitAuthChanged()
}

export const getStoredRefreshToken = () => authState.value.refreshToken

export const setStoredUser = (user: unknown) => {
  localStorage.setItem(USER_KEY, JSON.stringify(user))
  emitAuthChanged()
}

export const setAuthSession = (payload: { token: string; refreshToken?: string | null; user: unknown }) => {
  localStorage.setItem(TOKEN_KEY, payload.token)
  if (payload.refreshToken) {
    localStorage.setItem(REFRESH_TOKEN_KEY, payload.refreshToken)
  } else {
    localStorage.removeItem(REFRESH_TOKEN_KEY)
  }
  localStorage.setItem(USER_KEY, JSON.stringify(payload.user))
  emitAuthChanged()
}

export const storedUser = computed(() => authState.value.user)

export const isAdminUser = () => {
  const user = authState.value.user
  return String(user?.role || '').toUpperCase() === 'ADMIN'
}

export const isTokenExpired = (token?: string | null) => {
  if (!token) return true

  const payload = decodeJwtPayload(token)
  const exp = Number(payload?.exp)

  return !Number.isFinite(exp) || exp * 1000 <= Date.now()
}

export const hasValidAuthToken = () => {
  const token = getStoredToken()
  if (isTokenExpired(token)) {
    clearAuthStorage()
    return false
  }

  return true
}

if (typeof window !== 'undefined') {
  window.addEventListener('storage', (event) => {
    if ([TOKEN_KEY, REFRESH_TOKEN_KEY, USER_KEY].includes(event.key || '')) {
      syncAuthStateFromStorage()
    }
  })
}

export const AUTH_STORAGE_EVENT = AUTH_EVENT
