<template>
  <div class="min-h-screen bg-[#f3f1ea] font-body text-slate-900">
    <AppTopNav :show-avatar="false" />

    <main class="relative overflow-hidden px-4 pb-14 pt-22 sm:px-6 lg:px-8">
      <div class="pointer-events-none absolute inset-x-0 top-0 h-90 bg-[radial-gradient(circle_at_top_left,rgba(11,61,73,0.16),transparent_32%),radial-gradient(circle_at_80%_18%,rgba(202,138,4,0.18),transparent_24%),linear-gradient(180deg,rgba(255,251,235,0.92),rgba(243,241,234,0))]"></div>

      <div class="relative mx-auto max-w-7xl">
        <section class="overflow-hidden rounded-[2rem] border border-white/65 bg-[linear-gradient(135deg,#16323b_0%,#0d4f57_48%,#bf7c1d_100%)] text-white shadow-[0_24px_90px_rgba(15,23,42,0.14)]">
          <div class="grid gap-8 px-6 py-7 lg:grid-cols-[1.1fr_0.9fr] lg:px-8 lg:py-8">
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
                  校园互助平台管理后台
                </h1>

                <p class="mt-4 max-w-2xl text-sm leading-7 text-white/76 md:text-base">
                  将总览监控、用户治理和内容审核拆成独立工作面板，降低切换成本，让后台真正按任务流转。
                </p>
              </div>
            </div>

            <div class="grid gap-3 sm:grid-cols-3">
              <RouterLink
                v-for="item in navItems"
                :key="item.to"
                :to="item.to"
                class="rounded-[1.35rem] border p-4 backdrop-blur-sm transition-all"
                :class="isActive(item.to)
                  ? 'border-white/30 bg-white text-slate-900 shadow-[0_14px_40px_rgba(15,23,42,0.14)]'
                  : 'border-white/12 bg-white/10 text-white hover:bg-white/14'"
              >
                <div class="flex items-start justify-between gap-3">
                  <div>
                    <p class="text-[11px] font-extrabold uppercase tracking-[0.22em]" :class="isActive(item.to) ? 'text-slate-500' : 'text-white/58'">
                      {{ item.eyebrow }}
                    </p>
                    <h2 class="mt-3 text-xl font-extrabold tracking-tight">{{ item.label }}</h2>
                  </div>
                  <span class="material-symbols-outlined text-2xl">{{ item.icon }}</span>
                </div>
                <p class="mt-4 text-sm leading-6" :class="isActive(item.to) ? 'text-slate-600' : 'text-white/72'">
                  {{ item.description }}
                </p>
              </RouterLink>
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
import { RouterLink, RouterView, useRoute } from 'vue-router'
import AppTopNav from '../../components/AppTopNav.vue'

const route = useRoute()

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
  }
]

const isActive = (path: string) => route.path === path
</script>
