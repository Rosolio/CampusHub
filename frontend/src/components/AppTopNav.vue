<template>
  <header class="app-top-nav fixed top-0 z-50 w-full border-b border-white/55 bg-[rgba(244,247,243,0.82)] shadow-[0_8px_30px_rgba(15,23,42,0.06)] backdrop-blur-xl">
    <nav class="mx-auto flex max-w-7xl items-center justify-between px-6 py-3">
      <div class="flex items-center gap-8">
        <RouterLink to="/" class="font-headline text-xl font-bold tracking-tight text-teal-950">
          CampusHub
        </RouterLink>
        <div class="app-top-nav-links hidden items-center gap-2 rounded-full bg-white/70 p-1 shadow-[inset_0_1px_0_rgba(255,255,255,0.7)] md:flex">
          <RouterLink
            v-for="item in desktopItems"
            :key="item.to"
            :to="item.to"
            class="relative rounded-full px-4 py-2 text-sm font-bold tracking-tight transition-all duration-200"
            :class="isActive(item.to) ? 'bg-teal-900 text-white shadow-sm' : 'text-teal-900/65 hover:bg-teal-50 hover:text-teal-950'"
          >
            {{ resolveItemLabel(item) }}
            <span
              v-if="item.to === '/messages' && unreadCount > 0"
              class="absolute right-2 top-1.5 h-2.5 w-2.5 rounded-full bg-rose-500 ring-2 ring-white"
            ></span>
          </RouterLink>
        </div>
      </div>

      <div class="flex items-center gap-2">
        <RouterLink
          v-if="isAdmin"
          to="/admin"
          class="rounded-full px-3 py-2 text-sm font-bold text-teal-900/72 transition-all hover:bg-white hover:text-teal-950"
        >
          管理后台
        </RouterLink>
        <RouterLink
          v-if="!isAdmin"
          to="/messages"
          class="relative rounded-full p-2 text-teal-900/72 transition-all hover:bg-white hover:text-teal-950"
          :aria-label="t('navNotifications')"
        >
          <span class="material-symbols-outlined">notifications</span>
          <span
            v-if="unreadCount > 0"
            class="absolute right-1.5 top-1.5 h-2.5 w-2.5 rounded-full bg-rose-500 ring-2 ring-[rgba(244,247,243,0.82)]"
          ></span>
        </RouterLink>
        <RouterLink
          to="/settings"
          class="rounded-full p-2 text-teal-900/72 transition-all hover:bg-white hover:text-teal-950"
          :aria-label="t('navSettings')"
        >
          <span class="material-symbols-outlined">settings</span>
        </RouterLink>
        <RouterLink
          v-if="showAvatar"
          :to="profileLink"
          class="block transition-transform hover:scale-105 active:scale-95"
          :aria-label="t('navProfileLabel')"
        >
          <div class="h-10 w-10 overflow-hidden rounded-full border-2 border-primary-container bg-surface-container-highest shadow-sm">
            <img :src="resolvedAvatarUrl" alt="当前用户头像" class="h-full w-full object-cover" />
          </div>
        </RouterLink>
      </div>
    </nav>
  </header>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { DEFAULT_AVATAR_URL } from '../constants/assets'
import { usePreferences } from '../composables/usePreferences'
import { messageApi } from '../services/api'
import { hasValidAuthToken, storedUser } from '../utils/auth'

const props = withDefaults(defineProps<{
  avatarUrl?: string
  showAvatar?: boolean
}>(), {
  avatarUrl: '',
  showAvatar: true
})

const route = useRoute()
const { t } = usePreferences()
const unreadCount = ref(0)
let unreadTimer: number | null = null

const resolvedAvatarUrl = computed(() => {
  if (props.avatarUrl) return props.avatarUrl

  return storedUser.value?.avatarUrl || DEFAULT_AVATAR_URL
})

const isAdmin = computed(() => String(storedUser.value?.role || '').toUpperCase() === 'ADMIN')
const profileLink = computed(() => isAdmin.value ? '/admin/profile' : '/profile')
const desktopItems = computed(() => (
  isAdmin.value
    ? [
        { to: '/', label: '社区视角' },
        { to: '/admin', label: '管理后台' },
        { to: '/admin/profile', label: '个人中心' }
      ]
    : [
        { to: '/', labelKey: 'navHome' },
        { to: '/publish', labelKey: 'navPublish' },
        { to: '/messages', labelKey: 'navMessages' },
        { to: '/profile', labelKey: 'navProfile' }
      ]
))

const isActive = (path: string) => {
  if (path === '/admin') {
    return route.path.startsWith('/admin')
  }

  if (path === '/admin/profile') {
    return route.path === '/admin/profile'
  }

  if (path === '/') {
    return route.path === '/' || route.path === '/home' || route.path === '/topics' || route.path.startsWith('/detail/')
  }

  if (path === '/profile') {
    return route.path === '/profile' || route.path === '/tasks' || route.path.startsWith('/settings')
  }

  return route.path === path
}

const resolveItemLabel = (item: { label?: string; labelKey?: string }) => item.label || t(item.labelKey || '')

const fetchUnreadCount = async () => {
  if (!hasValidAuthToken()) {
    unreadCount.value = 0
    return
  }

  try {
    const response = await messageApi.getUnreadCount() as { count?: number }
    unreadCount.value = Number(response?.count ?? 0)
  } catch (error) {
    console.error('获取未读消息数量失败:', error)
  }
}

onMounted(() => {
  if (!isAdmin.value) {
    fetchUnreadCount()
    unreadTimer = window.setInterval(() => {
      fetchUnreadCount()
    }, 60000)
  }
})

onBeforeUnmount(() => {
  if (unreadTimer !== null) {
    window.clearInterval(unreadTimer)
    unreadTimer = null
  }
})
</script>
