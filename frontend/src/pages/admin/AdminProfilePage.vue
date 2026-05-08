<template>
  <div class="space-y-5">
    <section v-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 px-5 py-4 text-sm font-semibold text-rose-700">
      {{ error }}
    </section>

    <section class="grid gap-5 xl:grid-cols-[1.05fr_0.95fr]">
      <article class="admin-panel overflow-hidden">
        <div class="border-b border-slate-200 bg-slate-950 px-6 py-6 text-white md:px-7">
          <p class="text-[11px] font-extrabold uppercase tracking-[0.28em] text-white/58">Identity</p>
          <div class="mt-5 flex flex-col gap-5 md:flex-row md:items-center">
            <div class="flex h-22 w-22 items-center justify-center rounded-[1.8rem] border border-white/15 bg-white/10 text-3xl font-extrabold uppercase">
              {{ initials }}
            </div>
            <div class="min-w-0 flex-1">
              <h2 class="text-3xl font-extrabold tracking-[-0.04em]">{{ profile.name || '管理员' }}</h2>
              <p class="mt-2 text-sm text-white/78">{{ profile.email || '未设置邮箱' }}</p>
              <div class="mt-4 flex flex-wrap gap-2">
                <span class="rounded-full border border-white/18 bg-white/12 px-3 py-1.5 text-xs font-extrabold uppercase tracking-[0.18em] text-white">
                  {{ profile.role || 'ADMIN' }}
                </span>
                <span class="rounded-full bg-[#fff6de] px-3 py-1.5 text-xs font-extrabold uppercase tracking-[0.18em] text-[#8a5300]">
                  {{ profile.status === 'DISABLED' ? '账号受限' : '后台已授权' }}
                </span>
              </div>
            </div>
          </div>
        </div>

        <div class="grid gap-4 px-6 py-6 md:grid-cols-3 md:px-7">
          <div class="admin-panel-soft p-4">
            <p class="text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">学号</p>
            <p class="mt-3 text-lg font-extrabold text-slate-900">{{ profile.studentId || '未配置' }}</p>
          </div>
          <div class="admin-panel-soft p-4">
            <p class="text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">专业</p>
            <p class="mt-3 text-lg font-extrabold text-slate-900">{{ profile.major || '未填写' }}</p>
          </div>
          <div class="admin-panel-soft p-4">
            <p class="text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">账号状态</p>
            <p class="mt-3 text-lg font-extrabold" :class="profile.status === 'DISABLED' ? 'text-rose-700' : 'text-emerald-700'">
              {{ profile.status === 'DISABLED' ? '已禁用' : '正常可用' }}
            </p>
          </div>
        </div>
      </article>

      <article class="admin-panel p-6 md:p-7">
        <p class="admin-kicker">Snapshot</p>
        <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">管理面概况</h2>

        <div class="mt-6 grid gap-4 sm:grid-cols-2">
          <div class="rounded-3xl bg-slate-950 p-5 text-white">
            <p class="text-[11px] font-extrabold uppercase tracking-[0.2em] text-white/55">今日日活</p>
            <p class="mt-3 text-4xl font-extrabold">{{ overview.dailyActiveUsers ?? 0 }}</p>
          </div>
          <div class="admin-panel-soft p-5 text-slate-900">
            <p class="text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">今日订单</p>
            <p class="mt-3 text-4xl font-extrabold">{{ overview.todayOrderCount ?? 0 }}</p>
          </div>
          <div class="admin-panel-soft p-5 text-slate-900">
            <p class="text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">用户总数</p>
            <p class="mt-3 text-4xl font-extrabold">{{ overview.totalUsers ?? 0 }}</p>
          </div>
          <div class="admin-panel-soft p-5 text-slate-900">
            <p class="text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">内容总量</p>
            <p class="mt-3 text-4xl font-extrabold">{{ overview.totalTasks ?? 0 }}</p>
          </div>
        </div>
      </article>
    </section>

    <section class="grid gap-5 xl:grid-cols-[0.95fr_1.05fr]">
      <article class="admin-panel p-6 md:p-7">
        <p class="admin-kicker">Quick Actions</p>
        <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">后台常用入口</h2>
        <div class="mt-6 grid gap-3">
          <RouterLink
            v-for="item in quickActions"
            :key="item.to"
            :to="item.to"
            class="admin-panel-soft flex items-center justify-between px-5 py-4 transition hover:border-slate-300 hover:bg-white"
          >
            <div class="flex items-center gap-4">
              <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-white text-slate-700">
                <span class="material-symbols-outlined">{{ item.icon }}</span>
              </div>
              <div>
                <p class="text-sm font-extrabold text-slate-900">{{ item.label }}</p>
                <p class="mt-1 text-sm text-slate-500">{{ item.description }}</p>
              </div>
            </div>
            <span class="material-symbols-outlined text-slate-400">arrow_forward</span>
          </RouterLink>
        </div>
      </article>

      <article class="admin-panel p-6 md:p-7">
        <p class="admin-kicker">Operating Rules</p>
        <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">后台工作原则</h2>

        <div class="mt-6 space-y-4">
          <div v-for="rule in rules" :key="rule.title" class="admin-panel-soft p-5">
            <div class="flex items-center gap-3">
              <div class="flex h-10 w-10 items-center justify-center rounded-2xl bg-slate-950 text-white">
                <span class="material-symbols-outlined text-lg">{{ rule.icon }}</span>
              </div>
              <p class="text-sm font-extrabold text-slate-900">{{ rule.title }}</p>
            </div>
            <p class="mt-3 text-sm leading-7 text-slate-600">{{ rule.description }}</p>
          </div>
        </div>

        <div class="mt-6 rounded-3xl bg-slate-950 p-5 text-white">
          <p class="text-[11px] font-extrabold uppercase tracking-[0.22em] text-white/55">社区访问说明</p>
          <p class="mt-3 text-sm leading-7 text-white/80">
            管理模式下不再展示社区首页、发布流和个人内容流。如果需要回到社区，只通过顶部的“社区入口”进入，减少后台处理被内容浏览打断。
          </p>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { adminApi, userApi } from '../../services/api'

const error = ref('')
const profile = ref<any>({})
const overview = ref({
  dailyActiveUsers: 0,
  todayOrderCount: 0,
  totalUsers: 0,
  totalTasks: 0
})

const initials = computed(() => {
  const name = String(profile.value.name || '管理员').trim()
  return name.slice(0, 2).toUpperCase()
})

const quickActions = [
  { to: '/admin/overview', label: '查看总览', description: '回到核心指标和趋势图表，先判断整体运行状态。', icon: 'monitoring' },
  { to: '/admin/users', label: '用户治理', description: '按学号搜索用户，处理启用和禁用操作。', icon: 'manage_accounts' },
  { to: '/admin/moderation', label: '内容审核', description: '集中处理待审核内容和驳回通知。', icon: 'gavel' },
  { to: '/settings/profile', label: '账号资料设置', description: '更新管理员昵称、邮箱、头像与基础信息。', icon: 'tune' }
]

const rules = [
  {
    title: '先看总览，再下钻处理',
    description: '先从趋势和结构分布定位异常，再进入用户管理或内容审核，避免在局部页面盲目操作。',
    icon: 'query_stats'
  },
  {
    title: '后台只做管理动作',
    description: '后台不再承载社区内容消费和发布浏览，所有社区访问统一走单一入口，保持管理路径纯净。',
    icon: 'shield'
  },
  {
    title: '审核反馈要闭环',
    description: '驳回内容会同步系统提醒给发布者，审核备注应直接说明原因，减少重复申诉和沟通成本。',
    icon: 'campaign'
  }
]

const loadPage = async () => {
  error.value = ''

  try {
    const [userResponse, dashboardResponse] = await Promise.all([
      userApi.getCurrentUser(),
      adminApi.getDashboard()
    ]) as [any, any]

    profile.value = userResponse || {}
    overview.value = dashboardResponse?.overview || overview.value
  } catch (err: any) {
    error.value = err?.response?.data?.message || '管理员个人中心加载失败'
  }
}

onMounted(() => {
  loadPage()
})
</script>
