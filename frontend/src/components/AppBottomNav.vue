<template>
  <nav class="app-bottom-nav fixed bottom-0 left-0 z-50 flex w-full items-center justify-around rounded-t-[2rem] border-t border-white/70 bg-[rgba(255,255,255,0.88)] px-6 pb-6 pt-3 shadow-[0_-10px_36px_rgba(15,23,42,0.08)] backdrop-blur-xl md:hidden">
    <RouterLink
      v-for="item in items"
      :key="item.to"
      :to="item.to"
      class="flex flex-col items-center justify-center rounded-2xl px-5 py-2 text-[11px] font-semibold uppercase tracking-[0.2em] transition-all duration-200"
      :class="isActive(item.to) ? 'scale-95 bg-teal-900 text-white shadow-sm' : 'text-teal-900/45'"
    >
      <span class="material-symbols-outlined mb-1" :data-weight="isActive(item.to) ? 'fill' : 'regular'">
        {{ item.icon }}
      </span>
      <span>{{ resolveItemLabel(item) }}</span>
    </RouterLink>
  </nav>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { usePreferences } from '../composables/usePreferences'
import { isAdminUser } from '../utils/auth'

const route = useRoute()
const { t } = usePreferences()
const isAdmin = isAdminUser()

const items = computed(() => (
  isAdmin
    ? [
        { to: '/', label: '社区', icon: 'home' },
        { to: '/admin', label: '后台', icon: 'shield_person' },
        { to: '/admin/profile', label: '我的', icon: 'person' }
      ]
    : [
        { to: '/', labelKey: 'navHome', icon: 'home' },
        { to: '/publish', labelKey: 'navPublish', icon: 'add_circle' },
        { to: '/messages', labelKey: 'navMessages', icon: 'chat_bubble' },
        { to: '/profile', labelKey: 'navProfile', icon: 'person' }
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
</script>
