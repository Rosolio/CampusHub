<template>
  <div class="min-h-screen bg-surface font-body text-on-surface">
    <AppTopNav :show-avatar="false" />

    <main class="mx-auto max-w-7xl px-6 pb-16 pt-24">
      <section class="overflow-hidden rounded-[2.25rem] bg-[linear-gradient(135deg,rgba(11,47,64,0.96),rgba(17,94,89,0.9)_42%,rgba(217,119,6,0.82)_100%)] p-8 text-white shadow-[0_22px_60px_rgba(15,23,42,0.16)]">
        <div class="flex flex-col gap-6 lg:flex-row lg:items-end lg:justify-between">
          <div>
            <span class="rounded-full border border-white/15 bg-white/10 px-4 py-2 text-xs font-bold uppercase tracking-[0.24em]">CampusAid Admin</span>
            <h1 class="mt-4 text-4xl font-extrabold tracking-tight">平台管理后台</h1>
            <p class="mt-3 max-w-2xl text-sm leading-7 text-white/78">管理用户状态、审核社区内容，并查看平台日活、订单与分类分布趋势。</p>
          </div>
          <button type="button" class="rounded-full border border-white/20 bg-white/10 px-5 py-3 text-sm font-bold text-white transition hover:bg-white/15" @click="loadAll">
            刷新数据
          </button>
        </div>
      </section>

      <section v-if="error" class="mt-6 rounded-[1.6rem] border border-rose-200 bg-rose-50 px-5 py-4 text-sm font-medium text-rose-700">
        {{ error }}
      </section>

      <section class="mt-8 grid gap-5 md:grid-cols-2 xl:grid-cols-4">
        <article v-for="card in overviewCards" :key="card.label" class="rounded-[1.7rem] border border-outline-variant/10 bg-surface-container-lowest p-5 shadow-sm">
          <p class="text-xs font-bold uppercase tracking-[0.2em] text-teal-800/60">{{ card.label }}</p>
          <p class="mt-4 text-4xl font-extrabold text-teal-950">{{ card.value }}</p>
          <p class="mt-2 text-sm text-on-surface-variant">{{ card.hint }}</p>
        </article>
      </section>

      <section class="mt-8 grid gap-6 xl:grid-cols-2">
        <article class="rounded-[1.8rem] border border-outline-variant/10 bg-surface-container-lowest p-6 shadow-sm">
          <div class="flex items-center justify-between gap-3">
            <div>
              <p class="text-xs font-bold uppercase tracking-[0.2em] text-teal-800/60">日活趋势</p>
              <h2 class="mt-2 text-2xl font-extrabold text-teal-950">近 7 天活跃用户</h2>
            </div>
            <span class="rounded-full bg-teal-50 px-3 py-1.5 text-xs font-bold text-teal-900">DAU</span>
          </div>
          <div class="mt-8 grid h-64 grid-cols-7 items-end gap-3">
            <div v-for="point in dailyActiveTrend" :key="point.label" class="flex h-full flex-col justify-end gap-3">
              <div class="relative flex-1 rounded-[1.2rem] bg-surface-container-low px-2 py-3">
                <div class="absolute inset-x-2 bottom-2 rounded-xl bg-[linear-gradient(180deg,#14b8a6,#0f766e)]" :style="{ height: `${barHeight(point.value, dailyActiveTrend)}%` }"></div>
                <div class="relative z-10 text-center text-xs font-bold text-teal-950">{{ point.value }}</div>
              </div>
              <p class="text-center text-xs font-semibold text-on-surface-variant">{{ formatShortDate(point.label) }}</p>
            </div>
          </div>
        </article>

        <article class="rounded-[1.8rem] border border-outline-variant/10 bg-surface-container-lowest p-6 shadow-sm">
          <div class="flex items-center justify-between gap-3">
            <div>
              <p class="text-xs font-bold uppercase tracking-[0.2em] text-amber-700/70">订单趋势</p>
              <h2 class="mt-2 text-2xl font-extrabold text-teal-950">近 7 天完成订单</h2>
            </div>
            <span class="rounded-full bg-amber-50 px-3 py-1.5 text-xs font-bold text-amber-800">Orders</span>
          </div>
          <div class="mt-8 rounded-[1.5rem] bg-surface-container-low p-4">
            <svg viewBox="0 0 420 180" class="h-56 w-full">
              <polyline fill="none" stroke="#d97706" stroke-width="4" stroke-linecap="round" stroke-linejoin="round" :points="linePoints(orderTrend)" />
              <circle v-for="point in pointCoordinates(orderTrend)" :key="point.key" :cx="point.x" :cy="point.y" r="5" fill="#f59e0b" />
            </svg>
            <div class="mt-4 grid grid-cols-7 gap-2 text-center text-xs font-semibold text-on-surface-variant">
              <span v-for="point in orderTrend" :key="point.label">{{ formatShortDate(point.label) }}</span>
            </div>
          </div>
        </article>
      </section>

      <section class="mt-8 grid gap-6 xl:grid-cols-[1.15fr_0.85fr]">
        <article class="rounded-[1.8rem] border border-outline-variant/10 bg-surface-container-lowest p-6 shadow-sm">
          <div>
            <p class="text-xs font-bold uppercase tracking-[0.2em] text-teal-800/60">分类分布</p>
            <h2 class="mt-2 text-2xl font-extrabold text-teal-950">已通过内容分类占比</h2>
          </div>
          <div class="mt-6 space-y-4">
            <div v-for="item in categoryDistribution" :key="item.label">
              <div class="mb-2 flex items-center justify-between gap-3 text-sm">
                <span class="font-semibold text-teal-950">{{ item.label }}</span>
                <span class="font-bold text-teal-900">{{ item.value }}</span>
              </div>
              <div class="h-3 rounded-full bg-surface-container-low">
                <div class="h-full rounded-full bg-[linear-gradient(90deg,#0f766e,#14b8a6,#67e8f9)]" :style="{ width: `${distributionWidth(item.value, categoryDistribution)}%` }"></div>
              </div>
            </div>
          </div>
        </article>

        <article class="rounded-[1.8rem] border border-outline-variant/10 bg-surface-container-lowest p-6 shadow-sm">
          <p class="text-xs font-bold uppercase tracking-[0.2em] text-teal-800/60">审核总览</p>
          <h2 class="mt-2 text-2xl font-extrabold text-teal-950">用户与内容状态</h2>
          <div class="mt-6 grid gap-4">
            <div class="rounded-[1.3rem] bg-surface-container-low p-4">
              <p class="text-sm font-bold text-teal-950">内容审核状态</p>
              <div class="mt-3 flex flex-wrap gap-3">
                <span v-for="item in reviewDistribution" :key="item.label" class="rounded-full bg-white px-3 py-2 text-sm font-semibold text-teal-900">
                  {{ reviewLabel(item.label) }} {{ item.value }}
                </span>
              </div>
            </div>
            <div class="rounded-[1.3rem] bg-surface-container-low p-4">
              <p class="text-sm font-bold text-teal-950">用户状态</p>
              <div class="mt-3 flex flex-wrap gap-3">
                <span v-for="item in userStatusDistribution" :key="item.label" class="rounded-full bg-white px-3 py-2 text-sm font-semibold text-teal-900">
                  {{ userStatusLabel(item.label) }} {{ item.value }}
                </span>
              </div>
            </div>
          </div>
        </article>
      </section>

      <section class="mt-8 grid gap-6 2xl:grid-cols-[1fr_1fr]">
        <article class="rounded-[1.8rem] border border-outline-variant/10 bg-surface-container-lowest p-6 shadow-sm">
          <div>
            <p class="text-xs font-bold uppercase tracking-[0.2em] text-teal-800/60">用户管理</p>
            <h2 class="mt-2 text-2xl font-extrabold text-teal-950">查看与禁用用户</h2>
          </div>
          <div class="mt-6 space-y-4">
            <div v-for="user in users" :key="user.id" class="rounded-[1.35rem] border border-outline-variant/10 bg-surface-container-low p-4">
              <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
                <div>
                  <div class="flex flex-wrap items-center gap-2">
                    <h3 class="text-lg font-extrabold text-teal-950">{{ user.name }}</h3>
                    <span class="rounded-full px-3 py-1 text-xs font-bold" :class="user.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-700'">
                      {{ userStatusLabel(user.status) }}
                    </span>
                    <span v-if="user.role === 'ADMIN'" class="rounded-full bg-amber-100 px-3 py-1 text-xs font-bold text-amber-800">管理员</span>
                  </div>
                  <p class="mt-2 text-sm text-on-surface-variant">学号 {{ user.studentId }} · {{ user.email }}</p>
                  <p class="mt-1 text-sm text-on-surface-variant">{{ user.major || '未填写专业' }}</p>
                  <p v-if="user.disabledReason" class="mt-2 text-sm font-medium text-rose-700">禁用原因：{{ user.disabledReason }}</p>
                </div>
                <div v-if="user.role !== 'ADMIN'" class="flex flex-wrap gap-2">
                  <button type="button" class="rounded-full bg-emerald-600 px-4 py-2 text-sm font-bold text-white transition hover:bg-emerald-700 disabled:opacity-50" :disabled="pendingUserIds.has(user.id) || user.status === 'ACTIVE'" @click="changeUserStatus(user, 'ACTIVE')">启用</button>
                  <button type="button" class="rounded-full bg-rose-600 px-4 py-2 text-sm font-bold text-white transition hover:bg-rose-700 disabled:opacity-50" :disabled="pendingUserIds.has(user.id) || user.status === 'DISABLED'" @click="changeUserStatus(user, 'DISABLED')">禁用</button>
                </div>
              </div>
            </div>
          </div>
        </article>

        <article class="rounded-[1.8rem] border border-outline-variant/10 bg-surface-container-lowest p-6 shadow-sm">
          <div class="flex items-center justify-between gap-3">
            <div>
              <p class="text-xs font-bold uppercase tracking-[0.2em] text-teal-800/60">内容审核</p>
              <h2 class="mt-2 text-2xl font-extrabold text-teal-950">需求内容审核</h2>
            </div>
            <span class="rounded-full bg-amber-50 px-3 py-1.5 text-xs font-bold text-amber-800">待审核 {{ pendingReviewCount }}</span>
          </div>
          <div class="mt-6 space-y-4">
            <div v-for="task in tasks" :key="task.id" class="rounded-[1.35rem] border border-outline-variant/10 bg-surface-container-low p-4">
              <div class="flex flex-wrap items-center gap-2">
                <span class="rounded-full bg-white px-3 py-1 text-xs font-bold text-teal-900">{{ task.category || '未分类' }}</span>
                <span class="rounded-full px-3 py-1 text-xs font-bold" :class="reviewBadgeClass(task.reviewStatus)">{{ reviewLabel(task.reviewStatus) }}</span>
              </div>
              <h3 class="mt-3 text-lg font-extrabold text-teal-950">{{ task.title }}</h3>
              <p class="mt-2 text-sm leading-7 text-on-surface-variant">{{ task.description }}</p>
              <p class="mt-3 text-xs font-semibold uppercase tracking-[0.18em] text-teal-800/60">发布者 {{ task.requesterName || `#${task.requesterId}` }}</p>
              <p v-if="task.reviewNote" class="mt-2 text-sm text-amber-700">审核备注：{{ task.reviewNote }}</p>
              <div class="mt-4 flex flex-wrap gap-2">
                <button type="button" class="rounded-full bg-emerald-600 px-4 py-2 text-sm font-bold text-white transition hover:bg-emerald-700 disabled:opacity-50" :disabled="pendingTaskIds.has(task.id)" @click="reviewTask(task, 'approved')">通过</button>
                <button type="button" class="rounded-full bg-rose-600 px-4 py-2 text-sm font-bold text-white transition hover:bg-rose-700 disabled:opacity-50" :disabled="pendingTaskIds.has(task.id)" @click="reviewTask(task, 'rejected')">驳回</button>
                <button type="button" class="rounded-full bg-slate-800 px-4 py-2 text-sm font-bold text-white transition hover:bg-slate-900 disabled:opacity-50" :disabled="pendingTaskIds.has(task.id)" @click="reviewTask(task, 'pending_review')">退回待审</button>
              </div>
            </div>
          </div>
        </article>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AppTopNav from '../components/AppTopNav.vue'
import { adminApi } from '../services/api'

type StatPoint = { label: string; value: number }
type AdminUser = { id: number; studentId: string; name: string; email: string; major?: string; role: string; status: string; disabledReason?: string }
type AdminTask = { id: number; title: string; description: string; category?: string; requesterId: number; requesterName?: string; reviewStatus: string; reviewNote?: string }

const error = ref('')
const users = ref<AdminUser[]>([])
const tasks = ref<AdminTask[]>([])
const dashboard = ref<any>({
  overview: { dailyActiveUsers: 0, todayOrderCount: 0, totalUsers: 0, totalTasks: 0 },
  dailyActiveTrend: [],
  orderTrend: [],
  categoryDistribution: [],
  reviewDistribution: [],
  userStatusDistribution: []
})
const pendingUserIds = ref(new Set<number>())
const pendingTaskIds = ref(new Set<number>())

const dailyActiveTrend = computed<StatPoint[]>(() => dashboard.value.dailyActiveTrend || [])
const orderTrend = computed<StatPoint[]>(() => dashboard.value.orderTrend || [])
const categoryDistribution = computed<StatPoint[]>(() => dashboard.value.categoryDistribution || [])
const reviewDistribution = computed<StatPoint[]>(() => dashboard.value.reviewDistribution || [])
const userStatusDistribution = computed<StatPoint[]>(() => dashboard.value.userStatusDistribution || [])
const pendingReviewCount = computed(() => tasks.value.filter((task) => task.reviewStatus === 'pending_review').length)

const overviewCards = computed(() => [
  { label: '今日日活', value: dashboard.value.overview?.dailyActiveUsers ?? 0, hint: '按登录日志去重统计' },
  { label: '今日订单量', value: dashboard.value.overview?.todayOrderCount ?? 0, hint: '按已完成需求统计' },
  { label: '用户总数', value: dashboard.value.overview?.totalUsers ?? 0, hint: '含普通用户与管理员' },
  { label: '内容总量', value: dashboard.value.overview?.totalTasks ?? 0, hint: '含需求帖与话题帖' }
])

const loadAll = async () => {
  error.value = ''
  try {
    const [dashboardResponse, usersResponse, tasksResponse] = await Promise.all([
      adminApi.getDashboard(),
      adminApi.getUsers(),
      adminApi.getTasks()
    ])

    dashboard.value = dashboardResponse as any
    users.value = usersResponse as unknown as AdminUser[]
    tasks.value = tasksResponse as unknown as AdminTask[]
  } catch (err: any) {
    error.value = err?.response?.data?.message || '管理后台数据加载失败'
  }
}

const changeUserStatus = async (user: AdminUser, status: 'ACTIVE' | 'DISABLED') => {
  const reason = status === 'DISABLED' ? window.prompt('请输入禁用原因', user.disabledReason || '违规内容发布') || '' : ''
  pendingUserIds.value.add(user.id)
  try {
    await adminApi.updateUserStatus(user.id, { status, disabledReason: reason })
    await loadAll()
  } catch (err: any) {
    error.value = err?.response?.data?.message || '用户状态更新失败'
  } finally {
    pendingUserIds.value.delete(user.id)
  }
}

const reviewTask = async (task: AdminTask, reviewStatus: 'approved' | 'rejected' | 'pending_review') => {
  const reviewNote = window.prompt('请输入审核备注（可留空）', task.reviewNote || '') || ''
  pendingTaskIds.value.add(task.id)
  try {
    await adminApi.reviewTask(task.id, { reviewStatus, reviewNote })
    await loadAll()
  } catch (err: any) {
    error.value = err?.response?.data?.message || '内容审核失败'
  } finally {
    pendingTaskIds.value.delete(task.id)
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
const reviewLabel = (value: string) => value === 'approved' ? '已通过' : value === 'rejected' ? '已驳回' : '待审核'
const userStatusLabel = (value: string) => value === 'DISABLED' ? '已禁用' : '正常'
const reviewBadgeClass = (value: string) => value === 'approved' ? 'bg-emerald-100 text-emerald-800' : value === 'rejected' ? 'bg-rose-100 text-rose-700' : 'bg-amber-100 text-amber-800'

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
  loadAll()
})
</script>
