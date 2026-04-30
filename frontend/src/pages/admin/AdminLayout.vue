<template>
  <div class="min-h-screen bg-slate-50 font-body text-slate-900">
    <AdminModeHeader />

    <main class="mx-auto flex max-w-7xl gap-5 px-4 pb-10 pt-5 sm:px-6 lg:px-8">
      <aside class="sticky top-5 hidden h-[calc(100dvh-2.5rem)] w-72 shrink-0 flex-col rounded-lg border border-slate-200 bg-white shadow-sm lg:flex">
        <div class="border-b border-slate-200 px-5 py-5">
          <div class="flex items-center gap-3">
            <div class="flex h-10 w-10 items-center justify-center rounded-lg bg-teal-900 text-white">
              <span class="material-symbols-outlined text-xl">admin_panel_settings</span>
            </div>
            <div>
              <p class="text-sm font-extrabold text-slate-950">CampusHub 管理后台</p>
              <p class="text-xs font-semibold text-slate-500">Admin Workspace</p>
            </div>
          </div>
        </div>

        <nav class="min-h-0 flex-1 space-y-1 overflow-y-auto p-3">
          <RouterLink
            v-for="item in navItems"
            :key="item.to"
            :to="item.to"
            class="group flex items-start gap-3 rounded-lg px-3 py-3 text-sm transition"
            :class="isActiveNav(item)
              ? 'bg-teal-50 text-teal-950 ring-1 ring-teal-100'
              : 'text-slate-600 hover:bg-slate-100 hover:text-slate-950'"
          >
            <span
              class="material-symbols-outlined mt-0.5 text-xl"
              :class="isActiveNav(item) ? 'text-teal-800' : 'text-slate-400 group-hover:text-slate-700'"
            >
              {{ item.icon }}
            </span>
            <span class="min-w-0">
              <span class="block font-extrabold">{{ item.label }}</span>
              <span class="mt-0.5 block text-xs leading-5 text-slate-500">{{ item.description }}</span>
            </span>
          </RouterLink>
        </nav>

        <div class="border-t border-slate-200 p-3">
          <RouterLink
            to="/home"
            class="flex items-center justify-between rounded-lg border border-slate-200 bg-slate-50 px-3 py-3 text-sm font-bold text-slate-700 transition hover:border-teal-200 hover:bg-teal-50 hover:text-teal-950"
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
        <div class="mb-4 rounded-lg border border-slate-200 bg-white px-4 py-4 shadow-sm sm:px-5">
          <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <div class="mb-2 flex flex-wrap items-center gap-2">
                <span class="rounded-md bg-teal-50 px-2.5 py-1 text-xs font-extrabold text-teal-800">
                  管理模式
                </span>
                <span class="rounded-md bg-slate-100 px-2.5 py-1 text-xs font-bold text-slate-600">
                  {{ activeNavItem.eyebrow }}
                </span>
              </div>
              <h1 class="text-2xl font-extrabold tracking-tight text-slate-950 md:text-3xl">
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
                class="inline-flex items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-700 transition hover:border-teal-200 hover:bg-teal-50 hover:text-teal-950"
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
            class="flex items-center gap-2 rounded-lg border px-3 py-2 text-sm font-bold transition"
            :class="isActiveNav(item)
              ? 'border-teal-200 bg-teal-50 text-teal-950'
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
      return '社区内容作为后台中的独立工作区呈现，便于管理员观察最新动态并保持管理导航连续。'
    case '/admin/users':
      return '集中查看用户状态和账号治理动作，减少与普通社区浏览混在一起造成的操作干扰。'
    case '/admin/moderation':
      return '围绕待处理内容组织审核入口，优先完成需要管理员决策的事项。'
    case '/admin/profile':
      return '保留身份信息、后台入口和管理相关操作，作为管理员自己的工作面板。'
    default:
      return '将总览监控、用户治理、内容审核和反馈处理拆成清晰的后台工作区，适合日常检查和快速处置。'
  }
})
</script>
