<template>
  <header class="sticky top-0 z-50 border-b border-slate-200/70 bg-[rgba(248,250,252,0.9)] backdrop-blur-xl">
    <div class="mx-auto flex max-w-7xl items-center justify-between gap-4 px-4 py-4 sm:px-6 lg:px-8">
      <div class="flex min-w-0 items-center gap-3">
        <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl bg-slate-950 text-white">
          <span class="material-symbols-outlined text-[22px]">shield_person</span>
        </div>
        <div class="min-w-0">
          <p class="admin-kicker">Admin Console</p>
          <div class="mt-1 flex flex-wrap items-center gap-x-3 gap-y-1">
            <h1 class="truncate text-lg font-extrabold tracking-tight text-slate-950">CampusHub 管理后台</h1>
            <span class="hidden text-sm text-slate-400 sm:inline">独立工作区</span>
          </div>
        </div>
      </div>

      <div class="flex flex-wrap items-center justify-end gap-2">
        <RouterLink
          to="/home"
          class="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-700 transition hover:border-slate-300 hover:bg-slate-50"
        >
          <span class="material-symbols-outlined text-[18px]">arrow_back</span>
          社区视角
        </RouterLink>
        <RouterLink
          to="/admin/profile"
          class="inline-flex items-center gap-2 rounded-xl border border-slate-900 bg-slate-900 px-3 py-2 text-sm font-bold text-white transition hover:bg-slate-800"
        >
          <span class="material-symbols-outlined text-[18px]">badge</span>
          {{ displayName }}
        </RouterLink>
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-xl border border-rose-200 bg-rose-50 px-3 py-2 text-sm font-bold text-rose-700 transition hover:bg-rose-100"
          @click="logout"
        >
          <span class="material-symbols-outlined text-[18px]">logout</span>
          退出登录
        </button>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { clearAuthStorage, getStoredUser } from '../utils/auth'

const router = useRouter()

const displayName = computed(() => getStoredUser()?.name || '管理员')

const logout = async () => {
  clearAuthStorage()
  await router.push('/auth?tab=login')
}
</script>
