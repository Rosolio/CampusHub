<template>
  <div class="min-h-screen bg-[#f3f1ea] font-body text-slate-900">
    <header class="sticky top-0 z-50 border-b border-white/70 bg-[rgba(247,244,237,0.88)] backdrop-blur-xl">
      <div class="mx-auto flex max-w-7xl flex-col gap-4 px-4 py-4 sm:px-6 lg:px-8">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
          <div class="flex items-start gap-4">
            <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-[#102a33] text-white shadow-[0_14px_30px_rgba(16,42,51,0.22)]">
              <span class="material-symbols-outlined text-[26px]">shield_person</span>
            </div>
            <div>
              <p class="text-[11px] font-extrabold uppercase tracking-[0.28em] text-slate-500">Admin Console</p>
              <h1 class="mt-2 text-2xl font-extrabold tracking-[-0.04em] text-slate-950">CampusAid 管理模式</h1>
              <p class="mt-1 text-sm text-slate-600">后台视图已与社区浏览解耦，社区内容只保留一个独立入口。</p>
            </div>
          </div>

          <div class="flex flex-wrap items-center gap-3">
            <RouterLink
              to="/home"
              class="inline-flex items-center gap-2 rounded-full border border-[#d9d1c3] bg-white px-4 py-2.5 text-sm font-extrabold text-slate-800 transition hover:bg-[#f6f1e7]"
            >
              <span class="material-symbols-outlined text-lg">storefront</span>
              社区入口
            </RouterLink>
            <RouterLink
              to="/messages"
              class="inline-flex items-center gap-2 rounded-full border border-[#d9d1c3] bg-white px-4 py-2.5 text-sm font-extrabold text-slate-800 transition hover:bg-[#f6f1e7]"
            >
              <span class="material-symbols-outlined text-lg">notifications</span>
              系统消息
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
            :class="isActive(item.to)
              ? 'bg-[#102a33] text-white'
              : 'bg-white text-slate-700 hover:bg-[#efe8d7]'"
          >
            <span class="material-symbols-outlined text-base">{{ item.icon }}</span>
            {{ item.label }}
          </RouterLink>
        </div>
      </div>
    </header>

    <main class="relative overflow-hidden px-4 pb-14 pt-6 sm:px-6 lg:px-8">
      <div class="pointer-events-none absolute inset-x-0 top-0 h-90 bg-[radial-gradient(circle_at_top_left,rgba(11,61,73,0.16),transparent_32%),radial-gradient(circle_at_80%_18%,rgba(202,138,4,0.18),transparent_24%),linear-gradient(180deg,rgba(255,251,235,0.92),rgba(243,241,234,0))]"></div>

      <div class="relative mx-auto max-w-7xl">
        <section class="overflow-hidden rounded-[2rem] border border-white/65 bg-[linear-gradient(135deg,#16323b_0%,#0d4f57_48%,#bf7c1d_100%)] text-white shadow-[0_24px_90px_rgba(15,23,42,0.14)]">
          <div class="grid gap-8 px-6 py-7 lg:grid-cols-[minmax(0,1.15fr)_minmax(320px,0.85fr)] lg:px-8 lg:py-8">
            <div class="relative">
              <div class="absolute -left-10 top-0 h-42 w-42 rounded-full bg-white/8 blur-3xl"></div>
              <div class="relative">
                <div class="flex flex-wrap items-center gap-3">
                  <span class="rounded-full border border-white/16 bg-white/10 px-4 py-2 text-[11px] font-extrabold uppercase tracking-[0.3em] text-white/82">
                    CampusAid Control
                  </span>
                  <span class="rounded-full bg-white/12 px-4 py-2 text-sm font-semibold text-white/86">
                    Admin Zone
                  </span>
                </div>

                <h1 class="mt-5 max-w-3xl text-4xl font-extrabold leading-[1.02] tracking-[-0.04em] md:text-5xl">
                  {{ currentPageTitle }}
                </h1>

                <p class="mt-4 max-w-2xl text-sm leading-7 text-white/76 md:text-base">
                  {{ currentPageDescription }}
                </p>

                <div class="mt-8 grid gap-3 sm:grid-cols-3">
                  <div
                    v-for="signal in heroSignals"
                    :key="signal.label"
                    class="rounded-[1.35rem] border border-white/12 bg-white/8 px-4 py-4 backdrop-blur-sm"
                  >
                    <p class="text-[11px] font-extrabold uppercase tracking-[0.2em] text-white/55">{{ signal.label }}</p>
                    <p class="mt-3 text-2xl font-extrabold tracking-tight text-white">{{ signal.value }}</p>
                    <p class="mt-2 text-sm leading-6 text-white/72">{{ signal.hint }}</p>
                  </div>
                </div>

                <div class="mt-6 flex flex-wrap items-center gap-3">
                  <div class="inline-flex items-center gap-2 rounded-full border border-white/16 bg-white/10 px-4 py-2 text-sm font-bold text-white/82">
                    <span class="material-symbols-outlined text-base">verified_user</span>
                    当前模式：纯管理视图
                  </div>
                  <RouterLink
                    to="/home"
                    class="inline-flex items-center gap-2 rounded-full bg-white px-4 py-2 text-sm font-extrabold text-slate-900 transition hover:bg-[#f7f1e6]"
                  >
                    <span class="material-symbols-outlined text-base">arrow_outward</span>
                    单独进入社区
                  </RouterLink>
                </div>
              </div>
            </div>

            <div class="flex flex-col gap-4">
              <RouterLink
                :to="activeNavItem.to"
                class="group relative overflow-hidden rounded-[1.7rem] border border-white/24 bg-white px-5 py-5 text-slate-900 shadow-[0_18px_48px_rgba(15,23,42,0.16)] transition hover:-translate-y-0.5"
              >
                <div class="absolute right-0 top-0 h-28 w-28 rounded-full bg-[radial-gradient(circle,rgba(255,208,140,0.5),transparent_70%)] blur-2xl"></div>
                <div class="relative">
                  <div class="flex items-start justify-between gap-3">
                    <div>
                      <p class="text-[11px] font-extrabold uppercase tracking-[0.24em] text-slate-500">{{ activeNavItem.eyebrow }}</p>
                      <h2 class="mt-3 text-3xl font-extrabold tracking-tight">{{ activeNavItem.label }}</h2>
                    </div>
                    <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-[#102a33] text-white shadow-[0_12px_28px_rgba(16,42,51,0.2)]">
                      <span class="material-symbols-outlined text-2xl">{{ activeNavItem.icon }}</span>
                    </div>
                  </div>
                  <p class="mt-4 max-w-md text-sm leading-7 text-slate-600">
                    {{ activeNavItem.description }}
                  </p>
                  <div class="mt-5 flex items-center justify-between gap-4 rounded-[1.2rem] bg-[#f6f0e3] px-4 py-3">
                    <div>
                      <p class="text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">Current Focus</p>
                      <p class="mt-1 text-sm font-bold text-slate-800">{{ focusLine }}</p>
                    </div>
                    <span class="material-symbols-outlined text-slate-500 transition group-hover:translate-x-1">arrow_forward</span>
                  </div>
                </div>
              </RouterLink>

              <div class="grid gap-3 sm:grid-cols-3 lg:grid-cols-1">
                <RouterLink
                  v-for="item in secondaryNavItems"
                  :key="item.to"
                  :to="item.to"
                  class="rounded-[1.35rem] border border-white/12 bg-white/8 p-4 text-white backdrop-blur-sm transition hover:bg-white/14"
                >
                  <div class="flex items-start justify-between gap-3">
                    <div>
                      <p class="text-[11px] font-extrabold uppercase tracking-[0.22em] text-white/58">{{ item.eyebrow }}</p>
                      <h3 class="mt-3 text-lg font-extrabold tracking-tight">{{ item.label }}</h3>
                    </div>
                    <span class="material-symbols-outlined text-xl text-white/80">{{ item.icon }}</span>
                  </div>
                  <p class="mt-3 text-sm leading-6 text-white/72">
                    {{ item.description }}
                  </p>
                </RouterLink>
              </div>
            </div>
          </div>
        </section>

        <div class="mt-8 grid gap-6 xl:grid-cols-[14rem_minmax(0,1fr)]">
          <aside class="xl:sticky xl:top-24 xl:self-start">
            <div class="rounded-[1.8rem] border border-[#d8d2c6] bg-[#fffaf0] p-4 shadow-[0_16px_48px_rgba(15,23,42,0.06)]">
              <p class="px-2 text-[11px] font-extrabold uppercase tracking-[0.28em] text-slate-500">Workspace</p>
              <nav class="mt-4 space-y-2">
                <RouterLink
                  v-for="item in navItems"
                  :key="item.to"
                  :to="item.to"
                  class="flex items-center justify-between rounded-[1.1rem] px-3 py-3 text-sm font-bold transition"
                  :class="isActive(item.to)
                    ? 'bg-[#102a33] text-white'
                    : 'text-slate-700 hover:bg-[#efe8d7]'"
                >
                  <span>{{ item.label }}</span>
                  <span class="material-symbols-outlined text-base" :class="isActive(item.to) ? 'text-white/72' : 'text-slate-400'">
                    {{ item.icon }}
                  </span>
                </RouterLink>
              </nav>

              <div class="mt-5 rounded-[1.3rem] bg-[#102a33] p-4 text-white">
                <p class="text-xs font-extrabold uppercase tracking-[0.22em] text-white/55">工作建议</p>
                <p class="mt-3 text-sm leading-7 text-white/78">
                  先在总览页观察异常，再进入用户管理或内容审核进行处置，避免盲目操作。
                </p>
              </div>
            </div>
          </aside>

          <RouterView />
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, RouterView, useRoute, useRouter } from 'vue-router'
import { clearAuthStorage, getStoredUser } from '../../utils/auth'

const route = useRoute()
const router = useRouter()

const navItems = [
  {
    to: '/admin/overview',
    label: '总览',
    eyebrow: 'Overview',
    icon: 'monitoring',
    description: '查看核心指标、日活、订单趋势和结构分布。'
  },
  {
    to: '/admin/users',
    label: '用户管理',
    eyebrow: 'Users',
    icon: 'manage_accounts',
    description: '查看用户状态并执行启用、禁用等治理操作。'
  },
  {
    to: '/admin/moderation',
    label: '内容审核',
    eyebrow: 'Moderation',
    icon: 'gavel',
    description: '处理待审核内容，完成通过、驳回或挂起。'
  },
  {
    to: '/admin/profile',
    label: '个人中心',
    eyebrow: 'Profile',
    icon: 'badge',
    description: '查看管理员身份信息、快捷动作和后台使用入口。'
  }
]

const displayName = computed(() => getStoredUser()?.name || '管理员')
const activeNavItem = computed(() => navItems.find((item) => item.to === route.path) || navItems[0])
const secondaryNavItems = computed(() => navItems.filter((item) => item.to !== activeNavItem.value.to))

const currentPageTitle = computed(() => {
  switch (route.path) {
    case '/admin/users':
      return '纯净管理模式下的用户治理面板'
    case '/admin/moderation':
      return '只处理待办事项的内容审核工作区'
    case '/admin/profile':
      return '面向管理者的专属个人中心'
    default:
      return '与社区浏览分离的运营总览指挥台'
  }
})

const heroSignals = computed(() => [
  { label: '当前页面', value: activeNavItem.value.label, hint: '正在处理的后台任务面板。' },
  { label: '管理模式', value: 'Pure Admin', hint: '社区内容被收敛为单独入口。' },
  { label: '建议流程', value: '总览 -> 处置', hint: '先观察，再下钻到治理或审核。' }
])

const focusLine = computed(() => {
  switch (route.path) {
    case '/admin/users':
      return '优先检查异常账号与禁用状态变更。'
    case '/admin/moderation':
      return '保持审核队列干净，处理完立即退出列表。'
    case '/admin/profile':
      return '核对管理员身份信息与常用后台入口。'
    default:
      return '先看趋势和结构，再决定进入哪个工作区。'
  }
})

const currentPageDescription = computed(() => {
  switch (route.path) {
    case '/admin/users':
      return '用户管理只保留治理相关动作，社区流量入口已收敛，避免后台操作被内容浏览打断。'
    case '/admin/moderation':
      return '审核视图只承载待处理内容，完成后立即退出队列，保持后台界面始终围绕决策任务。'
    case '/admin/profile':
      return '个人中心改为管理员工作首页，保留身份信息、风险提示和常用操作，不再混入社区内容流。'
    default:
      return '将总览监控、用户治理和内容审核拆成独立工作面板，并把社区首页等浏览入口压缩为一个独立出口。'
  }
})

const isActive = (path: string) => route.path === path

const logout = async () => {
  clearAuthStorage()
  await router.push('/auth?tab=login')
}
</script>
