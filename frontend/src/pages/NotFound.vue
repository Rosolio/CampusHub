<template>
  <div class="page-shell min-h-screen bg-background text-on-background">
    <AppTopNav />

    <main class="page-shell-main flex flex-col items-center justify-center text-center">
      <div class="mx-auto mb-8 flex h-24 w-24 items-center justify-center rounded-full bg-surface-container-high">
        <span class="material-symbols-outlined text-5xl text-on-surface-variant">search_off</span>
      </div>

      <h1 class="font-headline text-6xl font-extrabold tracking-tight text-headline">404</h1>
      <p class="mt-4 text-xl font-semibold text-on-surface">页面未找到</p>
      <p class="mt-3 max-w-md text-base leading-7 text-on-surface-variant">
        你访问的页面不存在或已被移除，请检查链接是否正确。
      </p>

      <div class="mt-10 flex flex-wrap items-center justify-center gap-4">
        <RouterLink
          to="/"
          class="inline-flex items-center gap-2 rounded-full bg-teal-900 px-6 py-3 text-sm font-bold text-white transition-colors hover:bg-teal-800"
        >
          <span class="material-symbols-outlined text-base">home</span>
          返回首页
        </RouterLink>
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-full bg-surface-container-low px-6 py-3 text-sm font-bold text-teal-900 transition-colors hover:bg-surface-container-high"
          @click="goBack"
        >
          <span class="material-symbols-outlined text-base">arrow_back</span>
          返回上一页
        </button>
      </div>
    </main>

    <AppBottomNav />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import AppBottomNav from '../components/AppBottomNav.vue'
import AppTopNav from '../components/AppTopNav.vue'

const router = useRouter()

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}
</script>
