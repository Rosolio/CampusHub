<template>
  <div class="space-y-5">
    <section v-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 px-5 py-4 text-sm font-semibold text-rose-700">
      {{ error }}
    </section>

    <section class="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      <article
        v-for="card in overviewCards"
        :key="card.label"
        class="admin-panel p-5"
      >
        <div class="flex items-start justify-between gap-4">
          <div>
            <p class="admin-kicker">{{ card.label }}</p>
            <p class="mt-3 text-3xl font-extrabold tracking-tight text-slate-900">{{ card.value }}</p>
          </div>
          <div class="rounded-2xl bg-slate-100 p-3 text-slate-700">
            <span class="material-symbols-outlined text-[22px]">{{ card.icon }}</span>
          </div>
        </div>
        <p class="mt-3 text-sm leading-6 text-slate-600">{{ card.hint }}</p>
      </article>
    </section>

    <section class="grid gap-5 2xl:grid-cols-[1.2fr_0.8fr]">
      <article class="admin-panel p-5 md:p-6">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p class="admin-kicker">Activity</p>
            <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">近 7 天活跃趋势</h2>
          </div>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-700 transition hover:bg-slate-50"
            @click="loadDashboard"
          >
            <span class="material-symbols-outlined text-lg">refresh</span>
            刷新
          </button>
        </div>

        <div class="admin-panel-soft mt-6 p-4">
          <div class="grid h-72 grid-cols-7 items-end gap-3">
            <div v-for="point in dailyActiveTrend" :key="point.label" class="flex h-full flex-col justify-end gap-3">
              <div class="relative flex-1 overflow-hidden rounded-2xl bg-white px-2 py-3">
                <div
                  class="absolute inset-x-2 bottom-2 rounded-xl bg-[linear-gradient(180deg,#334155,#0f172a)]"
                  :style="{ height: `${barHeight(point.value, dailyActiveTrend)}%` }"
                ></div>
                <div class="relative z-10 text-center text-xs font-extrabold text-slate-800">{{ point.value }}</div>
              </div>
              <p class="text-center text-xs font-bold text-slate-500">{{ formatShortDate(point.label) }}</p>
            </div>
          </div>
        </div>
      </article>

      <article class="admin-panel p-5 md:p-6">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p class="admin-kicker">Orders</p>
            <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">订单完成轨迹</h2>
          </div>
          <span class="rounded-lg bg-amber-50 px-3 py-1.5 text-xs font-extrabold uppercase tracking-[0.18em] text-amber-700">
            近 7 天
          </span>
        </div>

        <div class="mt-6 rounded-3xl bg-slate-950 p-4 text-white">
          <svg viewBox="0 0 420 180" class="h-58 w-full">
            <defs>
              <linearGradient id="admin-order-line" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#94a3b8" />
                <stop offset="100%" stop-color="#ffffff" />
              </linearGradient>
            </defs>
            <polyline
              fill="none"
              stroke="url(#admin-order-line)"
              stroke-width="5"
              stroke-linecap="round"
              stroke-linejoin="round"
              :points="linePoints(orderTrend)"
            />
            <circle
              v-for="point in pointCoordinates(orderTrend)"
              :key="point.key"
              :cx="point.x"
              :cy="point.y"
              r="5.5"
              fill="#ffffff"
              stroke="#cbd5e1"
              stroke-width="3"
            />
          </svg>
          <div class="mt-2 grid grid-cols-7 gap-2 text-center text-[11px] font-bold uppercase tracking-[0.12em] text-white/58">
            <span v-for="point in orderTrend" :key="point.label">{{ formatShortDate(point.label) }}</span>
          </div>
        </div>
      </article>
    </section>

    <section class="grid gap-5 xl:grid-cols-[1.1fr_0.9fr]">
      <article class="admin-panel p-5 md:p-6">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p class="admin-kicker">Distribution</p>
            <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">分类分布</h2>
          </div>
          <span class="rounded-lg bg-slate-100 px-3 py-1.5 text-xs font-extrabold uppercase tracking-[0.18em] text-slate-600">
            已审核内容
          </span>
        </div>

        <div class="mt-6 grid gap-4">
          <div
            v-for="(item, index) in categoryDistribution"
            :key="item.label"
            class="admin-panel-soft p-4"
          >
            <div class="mb-3 flex items-center justify-between gap-3">
              <div class="flex items-center gap-3">
                <span class="flex h-8 w-8 items-center justify-center rounded-full bg-white text-xs font-extrabold text-slate-700">{{ index + 1 }}</span>
                <span class="text-sm font-bold text-slate-900">{{ item.label }}</span>
              </div>
              <span class="text-sm font-extrabold text-slate-700">{{ item.value }}</span>
            </div>
            <div class="h-3 overflow-hidden rounded-full bg-white">
              <div
                class="h-full rounded-full bg-[linear-gradient(90deg,#0f172a,#334155,#94a3b8)]"
                :style="{ width: `${distributionWidth(item.value, categoryDistribution)}%` }"
              ></div>
            </div>
          </div>
        </div>
      </article>

      <article class="admin-panel p-5 md:p-6">
        <div>
          <p class="admin-kicker">System State</p>
          <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">审核与用户状态</h2>
        </div>

        <div class="mt-6 grid gap-4">
          <div class="rounded-3xl bg-slate-950 p-4 text-white">
            <div class="flex items-center justify-between gap-3">
              <p class="text-sm font-extrabold">内容审核池</p>
              <span class="text-xs font-bold uppercase tracking-[0.18em] text-white/55">Moderation</span>
            </div>
            <div class="mt-4 flex flex-wrap gap-3">
              <span
                v-for="item in reviewDistribution"
                :key="item.label"
                class="rounded-full border border-white/10 bg-white/8 px-3 py-2 text-sm font-bold text-white/84"
              >
                {{ reviewLabel(item.label) }} {{ item.value }}
              </span>
            </div>
          </div>

          <div class="admin-panel-soft p-4">
            <div class="flex items-center justify-between gap-3">
              <p class="text-sm font-extrabold text-slate-900">账号池</p>
              <span class="text-xs font-bold uppercase tracking-[0.18em] text-slate-500">Users</span>
            </div>
            <div class="mt-4 flex flex-wrap gap-3">
              <span
                v-for="item in userStatusDistribution"
                :key="item.label"
                class="rounded-full bg-white px-3 py-2 text-sm font-bold text-slate-700"
              >
                {{ userStatusLabel(item.label) }} {{ item.value }}
              </span>
            </div>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { adminApi } from '../../services/api'
import type { StatPoint } from './adminTypes'

const error = ref('')
const dashboard = ref<any>({
  overview: { dailyActiveUsers: 0, todayOrderCount: 0, totalUsers: 0, totalTasks: 0 },
  dailyActiveTrend: [],
  orderTrend: [],
  categoryDistribution: [],
  reviewDistribution: [],
  userStatusDistribution: []
})

const dailyActiveTrend = computed<StatPoint[]>(() => dashboard.value.dailyActiveTrend || [])
const orderTrend = computed<StatPoint[]>(() => dashboard.value.orderTrend || [])
const categoryDistribution = computed<StatPoint[]>(() => dashboard.value.categoryDistribution || [])
const reviewDistribution = computed<StatPoint[]>(() => dashboard.value.reviewDistribution || [])
const userStatusDistribution = computed<StatPoint[]>(() => dashboard.value.userStatusDistribution || [])

const overviewCards = computed(() => [
  { label: '今日日活', value: dashboard.value.overview?.dailyActiveUsers ?? 0, hint: '按登录日志去重，适合观察活跃波峰。', icon: 'stacked_line_chart' },
  { label: '今日订单', value: dashboard.value.overview?.todayOrderCount ?? 0, hint: '只统计已完成需求，反映履约效率。', icon: 'inventory_2' },
  { label: '用户总数', value: dashboard.value.overview?.totalUsers ?? 0, hint: '覆盖普通用户与管理员账号。', icon: 'group' },
  { label: '内容总量', value: dashboard.value.overview?.totalTasks ?? 0, hint: '含需求帖与话题帖。', icon: 'newspaper' }
])

const loadDashboard = async () => {
  error.value = ''
  try {
    dashboard.value = await adminApi.getDashboard()
  } catch (err: any) {
    error.value = err?.response?.data?.message || '总览数据加载失败'
  }
}

const barHeight = (value: number, series: StatPoint[]) => {
  const max = Math.max(...series.map((item) => item.value), 1)
  return Math.max((value / max) * 100, value > 0 ? 12 : 4)
}

const distributionWidth = (value: number, series: StatPoint[]) => {
  const total = series.reduce((sum, item) => sum + item.value, 0)
  return total === 0 ? 0 : Math.max((value / total) * 100, 8)
}

const formatShortDate = (value: string) => value.slice(5).replace('-', '/')

const reviewLabel = (value: string) => {
  if (value === 'approved') return '已通过'
  if (value === 'rejected') return '已驳回'
  return '待审核'
}

const userStatusLabel = (value: string) => value === 'DISABLED' ? '已禁用' : '正常'

const pointCoordinates = (series: StatPoint[]) => {
  const max = Math.max(...series.map((item) => item.value), 1)
  const width = 360
  return series.map((item, index) => {
    const x = 30 + (index * width) / Math.max(series.length - 1, 1)
    const y = 150 - ((item.value / max) * 110 + 10)
    return { key: item.label, x, y }
  })
}

const linePoints = (series: StatPoint[]) => pointCoordinates(series).map((point) => `${point.x},${point.y}`).join(' ')

onMounted(() => {
  loadDashboard()
})
</script>
