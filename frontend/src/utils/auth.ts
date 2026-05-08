const TOKEN_KEY = 'token'
const REFRESH_TOKEN_KEY = 'refreshToken'
const USER_KEY = 'user'

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

export const clearAuthStorage = () => {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

export const getStoredToken = () => localStorage.getItem(TOKEN_KEY)

export const getStoredUser = () => {
  try {
    const raw = localStorage.getItem(USER_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

export const isAdminUser = () => {
  const user = getStoredUser()
  return String(user?.role || '').toUpperCase() === 'ADMIN'
}

export const hasValidAuthToken = () => {
  const token = getStoredToken()
  if (!token) return false

  const payload = decodeJwtPayload(token)
  const exp = Number(payload?.exp)

  if (!Number.isFinite(exp)) {
    clearAuthStorage()
    return false
  }

  if (exp * 1000 <= Date.now()) {
    clearAuthStorage()
    return false
  }

  return true
}
