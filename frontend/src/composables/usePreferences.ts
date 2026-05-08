import { computed, ref } from 'vue'
import { userApi } from '../services/api'
import { hasValidAuthToken } from '../utils/auth'

export type ThemeMode = 'light' | 'dark' | 'system'
export type LanguageCode = 'zh-CN' | 'en-US' | 'zh-TW'

type PreferenceState = {
  notificationEnabled: boolean
  theme: ThemeMode
  language: LanguageCode
}

const STORAGE_KEY = 'campushub.preferences'
const DEFAULT_PREFERENCES: PreferenceState = {
  notificationEnabled: true,
  theme: 'light',
  language: 'zh-CN'
}

const state = ref<PreferenceState>({ ...DEFAULT_PREFERENCES })
let pendingLoad: Promise<void> | null = null
let mediaQueryBound = false

const messages: Record<LanguageCode, Record<string, string>> = {
  'zh-CN': {
    navHome: '社区',
    navPublish: '发布',
    navMessages: '消息',
    navProfile: '个人中心',
    navNotifications: '消息中心',
    navSettings: '设置',
    navProfileLabel: '进入个人中心',
    settingsTitle: '设置',
    settingsAccount: '账户设置',
    settingsApp: '应用设置',
    settingsAbout: '关于',
    settingsProfile: '个人资料',
    settingsNotifications: '通知设置',
    settingsTheme: '主题设置',
    settingsLanguage: '语言设置',
    settingsAboutCampusHub: '关于 CampusHub',
    settingsPrivacy: '隐私政策',
    settingsAgreement: '用户协议',
    settingsLogout: '退出登录',
    backToSettings: '返回设置',
    themeTitle: '主题设置',
    themeMode: '主题模式',
    themeLight: '浅色模式',
    themeDark: '深色模式',
    themeSystem: '跟随系统',
    themeSave: '保存设置',
    themeSaving: '保存中...',
    themeSaved: '主题设置已保存并立即生效。',
    languageTitle: '语言设置',
    languageApp: '应用语言',
    languageSimplified: '简体中文',
    languageEnglish: 'English',
    languageTraditional: '繁体中文',
    languageSave: '保存设置',
    languageSaving: '保存中...',
    languageSaved: '语言设置已保存并立即生效。',
    languageRegion: '地区设置',
    languageRegionLabel: '地区',
    languageTimezoneLabel: '时区',
    saveFailed: '保存失败，请稍后重试。'
  },
  'en-US': {
    navHome: 'Home',
    navPublish: 'Post',
    navMessages: 'Messages',
    navProfile: 'Profile',
    navNotifications: 'Inbox',
    navSettings: 'Settings',
    navProfileLabel: 'Open profile',
    settingsTitle: 'Settings',
    settingsAccount: 'Account',
    settingsApp: 'App',
    settingsAbout: 'About',
    settingsProfile: 'Profile',
    settingsNotifications: 'Notifications',
    settingsTheme: 'Theme',
    settingsLanguage: 'Language',
    settingsAboutCampusHub: 'About CampusHub',
    settingsPrivacy: 'Privacy Policy',
    settingsAgreement: 'User Agreement',
    settingsLogout: 'Log out',
    backToSettings: 'Back to settings',
    themeTitle: 'Theme Settings',
    themeMode: 'Theme Mode',
    themeLight: 'Light',
    themeDark: 'Dark',
    themeSystem: 'System',
    themeSave: 'Save Settings',
    themeSaving: 'Saving...',
    themeSaved: 'Theme updated and applied.',
    languageTitle: 'Language Settings',
    languageApp: 'App Language',
    languageSimplified: 'Simplified Chinese',
    languageEnglish: 'English',
    languageTraditional: 'Traditional Chinese',
    languageSave: 'Save Settings',
    languageSaving: 'Saving...',
    languageSaved: 'Language updated and applied.',
    languageRegion: 'Region',
    languageRegionLabel: 'Region',
    languageTimezoneLabel: 'Time Zone',
    saveFailed: 'Save failed. Please try again later.'
  },
  'zh-TW': {
    navHome: '社群',
    navPublish: '發布',
    navMessages: '訊息',
    navProfile: '個人中心',
    navNotifications: '訊息中心',
    navSettings: '設定',
    navProfileLabel: '進入個人中心',
    settingsTitle: '設定',
    settingsAccount: '帳戶設定',
    settingsApp: '應用設定',
    settingsAbout: '關於',
    settingsProfile: '個人資料',
    settingsNotifications: '通知設定',
    settingsTheme: '主題設定',
    settingsLanguage: '語言設定',
    settingsAboutCampusHub: '關於 CampusHub',
    settingsPrivacy: '隱私政策',
    settingsAgreement: '使用者協議',
    settingsLogout: '登出',
    backToSettings: '返回設定',
    themeTitle: '主題設定',
    themeMode: '主題模式',
    themeLight: '淺色模式',
    themeDark: '深色模式',
    themeSystem: '跟隨系統',
    themeSave: '儲存設定',
    themeSaving: '儲存中...',
    themeSaved: '主題設定已儲存並立即生效。',
    languageTitle: '語言設定',
    languageApp: '應用語言',
    languageSimplified: '簡體中文',
    languageEnglish: 'English',
    languageTraditional: '繁體中文',
    languageSave: '儲存設定',
    languageSaving: '儲存中...',
    languageSaved: '語言設定已儲存並立即生效。',
    languageRegion: '地區設定',
    languageRegionLabel: '地區',
    languageTimezoneLabel: '時區',
    saveFailed: '儲存失敗，請稍後再試。'
  }
}

export const themeOptions: Array<{ value: ThemeMode, labelKey: string }> = [
  { value: 'light', labelKey: 'themeLight' },
  { value: 'dark', labelKey: 'themeDark' },
  { value: 'system', labelKey: 'themeSystem' }
]

export const languageOptions: Array<{ value: LanguageCode, labelKey: string, note: string }> = [
  { value: 'zh-CN', labelKey: 'languageSimplified', note: 'Simplified Chinese' },
  { value: 'en-US', labelKey: 'languageEnglish', note: 'English (US)' },
  { value: 'zh-TW', labelKey: 'languageTraditional', note: 'Traditional Chinese' }
]

const normalizeTheme = (value?: string): ThemeMode => {
  if (value === 'dark' || value === 'system') return value
  return 'light'
}

const normalizeLanguage = (value?: string): LanguageCode => {
  if (value === 'en-US' || value === 'zh-TW') return value
  return 'zh-CN'
}

const getStoredPreferences = (): PreferenceState => {
  if (typeof window === 'undefined') {
    return { ...DEFAULT_PREFERENCES }
  }

  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    if (!raw) return { ...DEFAULT_PREFERENCES }
    return { ...DEFAULT_PREFERENCES, ...JSON.parse(raw) }
  } catch {
    return { ...DEFAULT_PREFERENCES }
  }
}

const persistPreferences = () => {
  if (typeof window === 'undefined') return
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state.value))
}

const resolveTheme = (theme: ThemeMode) => {
  if (theme !== 'system' || typeof window === 'undefined') {
    return theme === 'system' ? 'light' : theme
  }
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

const applyPreferences = () => {
  if (typeof document === 'undefined') return

  document.documentElement.lang = state.value.language
  document.documentElement.dataset.theme = state.value.theme
  document.documentElement.dataset.resolvedTheme = resolveTheme(state.value.theme)
}

const mergePreferences = (partial: Partial<PreferenceState>) => {
  state.value = {
    ...state.value,
    ...partial,
    theme: normalizeTheme(partial.theme || state.value.theme),
    language: normalizeLanguage(partial.language || state.value.language)
  }
  persistPreferences()
  applyPreferences()
}

const bindSystemThemeListener = () => {
  if (mediaQueryBound || typeof window === 'undefined') return
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  mediaQuery.addEventListener('change', () => {
    if (state.value.theme === 'system') {
      applyPreferences()
    }
  })
  mediaQueryBound = true
}

export const initializePreferences = async () => {
  if (!pendingLoad) {
    state.value = getStoredPreferences()
    persistPreferences()
    applyPreferences()
    bindSystemThemeListener()

    pendingLoad = (async () => {
      if (!hasValidAuthToken()) return

      try {
        const response = await userApi.getUserSettings() as any
        const remote = response?.data ?? response ?? {}
        mergePreferences({
          notificationEnabled: typeof remote.notificationEnabled === 'boolean'
            ? remote.notificationEnabled
            : state.value.notificationEnabled,
          theme: normalizeTheme(remote.theme),
          language: normalizeLanguage(remote.language)
        })
      } catch (error) {
        console.warn('加载用户偏好失败，已使用本地设置。', error)
      }
    })()
  }

  await pendingLoad
}

export const usePreferences = () => {
  const t = (key: string) => messages[state.value.language]?.[key] || messages['zh-CN'][key] || key

  const savePreferences = async (partial: Partial<PreferenceState>) => {
    mergePreferences(partial)

    if (!hasValidAuthToken()) return

    await userApi.updateUserSettings({
      notificationEnabled: state.value.notificationEnabled,
      theme: state.value.theme,
      language: state.value.language
    })
  }

  const formatLocaleDateTime = (
    value?: string,
    options: Intl.DateTimeFormatOptions = {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    },
    fallback = '刚刚'
  ) => {
    if (!value) return fallback
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return value
    return date.toLocaleString(state.value.language, options)
  }

  const formatLocaleDateLabel = (
    value?: string,
    options: Intl.DateTimeFormatOptions = {
      month: 'long',
      day: 'numeric',
      weekday: 'long'
    },
    fallback = '刚刚'
  ) => {
    if (!value) return fallback
    const date = new Date(value)
    if (Number.isNaN(date.getTime())) return value
    return date.toLocaleDateString(state.value.language, options)
  }

  return {
    preferences: computed(() => state.value),
    theme: computed(() => state.value.theme),
    language: computed(() => state.value.language),
    t,
    savePreferences,
    formatLocaleDateTime,
    formatLocaleDateLabel
  }
}
