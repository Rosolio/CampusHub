<template>
  <div class="admin-shell min-h-screen font-body text-slate-900">
    <AdminModeHeader />

    <main class="mx-auto flex max-w-7xl gap-6 px-4 pb-10 pt-6 sm:px-6 lg:px-8">
      <aside class="admin-panel sticky top-[5.5rem] hidden h-[calc(100dvh-7rem)] w-64 shrink-0 flex-col overflow-hidden lg:flex">
        <div class="border-b border-slate-200/80 px-5 py-5">
          <div class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-slate-950 text-white">
              <span class="material-symbols-outlined text-xl">space_dashboard</span>
            </div>
            <div>
              <p class="text-sm font-extrabold text-slate-950">后台工作台</p>
              <p class="text-xs font-semibold text-slate-500">精简导航与操作区</p>
            </div>
          </div>
        </div>

        <nav class="min-h-0 flex-1 space-y-1.5 overflow-y-auto p-3">
          <RouterLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="group flex items-start gap-3 rounded-xl px-3 py-3 text-sm transition"
            :class="isActiveNav(item)
              ? 'bg-slate-950 text-white'
              : 'text-slate-600 hover:bg-slate-100 hover:text-slate-950'"
          >
            <span
              class="material-symbols-outlined mt-0.5 text-xl"
              :class="isActiveNav(item) ? 'text-white' : 'text-slate-400 group-hover:text-slate-700'"
            >
              {{ item.icon }}
            </span>
            <span class="min-w-0">
              <span class="block font-extrabold">{{ item.label }}</span>
              <span class="mt-0.5 block text-xs leading-5" :class="isActiveNav(item) ? 'text-slate-300' : 'text-slate-500'">
                {{ item.description }}
              </span>
            </span>
          </RouterLink>
        </nav>

        <div class="border-t border-slate-200/80 p-3">
          <RouterLink
            to="/home"
            class="flex items-center justify-between rounded-xl border border-slate-200 bg-slate-50 px-3 py-3 text-sm font-bold text-slate-700 transition hover:border-slate-300 hover:bg-white"
          >
            <span class="inline-flex items-center gap-2">
              <span class="material-symbols-outlined text-lg">open_in_new</span>
              社区视角
            </span>
            <span class="material-symbols-outlined text-lg">chevron_right</span>
          </RouterLink>
        </div>
      </aside>

      <section class="min-w-0 flex-1">
        <div class="admin-panel mb-4 px-4 py-4 sm:px-5">
          <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <div class="mb-3 flex flex-wrap items-center gap-2">
                <span class="rounded-lg bg-slate-950 px-2.5 py-1 text-xs font-extrabold text-white">管理模式</span>
                <span class="rounded-lg bg-slate-100 px-2.5 py-1 text-xs font-bold text-slate-600">
                  {{ activeNavItem.eyebrow }}
                </span>
              </div>
              <h1 class="text-2xl font-extrabold tracking-tight text-slate-950 md:text-[2rem]">
                {{ currentPageTitle }}
              </h1>
              <p class="mt-2 max-w-3xl text-sm leading-6 text-slate-600">
                {{ currentPageDescription }}
              </p>
            </div>

            <div class="flex flex-wrap gap-2">
              <RouterLink
                v-for="action in quickActions"
                :key="action.to"
                :to="action.to"
                class="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-700 transition hover:border-slate-300 hover:bg-slate-50"
              >
                <span class="material-symbols-outlined text-lg">{{ action.icon }}</span>
                {{ action.label }}
              </RouterLink>
            </div>
          </div>
        </div>

        <nav class="mb-4 grid grid-cols-2 gap-2 sm:grid-cols-3 lg:hidden">
          <RouterLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="flex items-center gap-2 rounded-xl border px-3 py-2 text-sm font-bold transition"
            :class="isActiveNav(item)
              ? 'border-slate-950 bg-slate-950 text-white'
              : 'border-slate-200 bg-white text-slate-600 hover:bg-slate-50'"
          >
            <span class="material-symbols-outlined text-lg">{{ item.icon }}</span>
            <span class="truncate">{{ item.label }}</span>
          </RouterLink>
        </nav>

        <RouterView />
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import AdminModeHeader from '../../components/AdminModeHeader.vue'

type NavItem = {
  to: string
  label: string
  eyebrow: string
  icon: string
  description: string
}

const route = useRoute()

const navItems: NavItem[] = [
  {
    to: '/admin/overview',
    label: '总览',
    eyebrow: 'Overview',
    icon: 'monitoring',
    description: '核心指标、趋势和结构分布'
  },
  {
    to: '/admin/users',
    label: '用户管理',
    eyebrow: 'Users',
    icon: 'manage_accounts',
    description: '账号状态、禁用与治理操作'
  },
  {
    to: '/admin/moderation',
    label: '内容审核',
    eyebrow: 'Moderation',
    icon: 'gavel',
    description: '处理待审核内容和违规线索'
  },
  {
    to: '/home#feedback-queue',
    label: '反馈处理',
    eyebrow: 'Feedback',
    icon: 'reviews',
    description: '处理用户建议、bug 和追踪状态'
  },
  {
    to: '/admin/community',
    label: '社区动态',
    eyebrow: 'Community',
    icon: 'newspaper',
    description: '在管理模式下查看社区内容'
  },
  {
    to: '/admin/profile',
    label: '个人中心',
    eyebrow: 'Profile',
    icon: 'badge',
    description: '管理员身份信息和常用入口'
  }
]

const isActiveNav = (item: NavItem) => item.to === route.path

const activeNavItem = computed(() => navItems.find(isActiveNav) || navItems[0])

const quickActions = computed(() => {
  const preferred = ['/home#feedback-queue', '/admin/moderation', '/admin/users']
  return preferred
    .map((path) => navItems.find((item) => item.to === path))
    .filter((item): item is NavItem => item !== undefined && item.to !== activeNavItem.value.to)
    .slice(0, 2)
})

const currentPageTitle = computed(() => {
  switch (route.path) {
    case '/admin/community':
      return '社区动态'
    case '/admin/users':
      return '用户管理'
    case '/admin/moderation':
      return '内容审核'
    case '/admin/profile':
      return '管理员个人中心'
    default:
      return '运营总览'
  }
})

const currentPageDescription = computed(() => {
  switch (route.path) {
    case '/admin/community':
      return '在管理上下文中查看最新社区内容，减少来回切换普通用户视角造成的干扰。'
    case '/admin/users':
      return '集中查看账号状态和治理动作，把搜索、状态判断和执行操作放在同一工作面板。'
    case '/admin/moderation':
      return '围绕待处理内容组织审核入口，优先完成需要管理员明确决策的事项。'
    case '/admin/profile':
      return '保留管理员身份信息、核心概况和常用后台入口，作为个人工作面板。'
    default:
      return '将总览监控、用户治理、内容审核和反馈处理拆成清晰的后台工作区，适合日常检查和快速处置。'
  }
})
</script>
