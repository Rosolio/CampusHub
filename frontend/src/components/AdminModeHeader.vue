<template>
  <header class="sticky top-0 z-50 border-b border-white/70 bg-[rgba(247,244,237,0.88)] backdrop-blur-xl">
    <div class="mx-auto flex max-w-7xl flex-col gap-4 px-4 py-4 sm:px-6 lg:px-8">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div class="flex items-start gap-4">
          <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-[#102a33] text-white shadow-[0_14px_30px_rgba(16,42,51,0.22)]">
            <span class="material-symbols-outlined text-[26px]">shield_person</span>
          </div>
          <div>
            <p class="text-[11px] font-extrabold uppercase tracking-[0.28em] text-slate-500">Admin Console</p>
            <h1 class="mt-2 text-2xl font-extrabold tracking-[-0.04em] text-slate-950">CampusHub 管理模式</h1>
            <p class="mt-1 text-sm text-slate-600">管理员视图统一使用独立导航，不混入普通社区入口。</p>
          </div>
        </div>

        <div class="flex flex-wrap items-center gap-3">
          <RouterLink
            to="/home"
            class="inline-flex items-center gap-2 rounded-full border border-[#d9d1c3] bg-white px-4 py-2.5 text-sm font-extrabold text-slate-800 transition hover:bg-[#f6f1e7]"
          >
            <span class="material-symbols-outlined text-lg">dashboard</span>
            社区视角
          </RouterLink>
          <RouterLink
            to="/admin/profile"
            class="inline-flex items-center gap-2 rounded-full bg-[#102a33] px-4 py-2.5 text-sm font-extrabold text-white transition hover:bg-[#163a46]"
          >
            <span class="material-symbols-outlined text-lg">badge</span>
            {{ displayName }}
          </RouterLink>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-full bg-[#d96b2b] px-4 py-2.5 text-sm font-extrabold text-white transition hover:bg-[#be5a21]"
            @click="logout"
          >
            <span class="material-symbols-outlined text-lg">logout</span>
            退出登录
          </button>
        </div>
      </div>

      <div class="flex flex-wrap gap-2">
        <RouterLink
          v-for="item in navItems"
          :key="item.to"
          :to="item.to"
          class="inline-flex items-center gap-2 rounded-full px-4 py-2 text-sm font-extrabold transition"
          :class="isActive(item.to) ? 'bg-[#102a33] text-white' : 'bg-white text-slate-700 hover:bg-[#efe8d7]'"
        >
          <span class="material-symbols-outlined text-base">{{ item.icon }}</span>
          {{ item.label }}
        </RouterLink>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { clearAuthStorage, getStoredUser } from '../utils/auth'

const route = useRoute()
const router = useRouter()

const navItems = [
  { to: '/home', label: '社区视角', icon: 'dashboard' },
  { to: '/admin/community', label: '社区动态', icon: 'newspaper' },
  { to: '/admin/overview', label: '总览', icon: 'monitoring' },
  { to: '/admin/users', label: '用户管理', icon: 'manage_accounts' },
  { to: '/admin/moderation', label: '内容审核', icon: 'gavel' },
  { to: '/admin/profile', label: '个人中心', icon: 'badge' }
]

const displayName = computed(() => getStoredUser()?.name || '管理员')

const isActive = (path: string) => {
  if (path === '/home') {
    return route.path === '/home'
  }
  return route.path === path
}

const logout = async () => {
  clearAuthStorage()
  await router.push('/auth?tab=login')
}
</script>
