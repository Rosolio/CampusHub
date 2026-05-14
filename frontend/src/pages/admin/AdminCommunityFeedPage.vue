<template>
  <div class="space-y-5">
    <section v-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 px-5 py-4 text-sm font-semibold text-rose-700">
      {{ error }}
    </section>

    <section class="grid gap-4 md:grid-cols-3">
      <article v-for="card in workspaceCards" :key="card.label" class="admin-panel p-5">
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

    <section class="admin-panel p-4 sm:p-5">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div class="flex flex-wrap gap-2">
          <RouterLink
            v-for="tab in workspaceTabs"
            :key="tab.value"
            :to="{ path: '/admin/community', query: { tab: tab.value }, hash: tab.hash }"
            class="inline-flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-bold transition"
            :class="activeTab === tab.value
              ? 'bg-slate-950 text-white'
              : 'bg-slate-100 text-slate-600 hover:bg-slate-200 hover:text-slate-900'"
          >
            <span class="material-symbols-outlined text-lg">{{ tab.icon }}</span>
            {{ tab.label }}
          </RouterLink>
        </div>

        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-700 transition hover:bg-slate-50"
          @click="loadWorkspace"
        >
          <span class="material-symbols-outlined text-lg">refresh</span>
          刷新工作台
        </button>
      </div>
    </section>

    <section v-if="activeTab === 'announcements'" id="announcement-desk" class="grid gap-5 xl:grid-cols-[0.95fr_1.05fr]">
      <article class="admin-panel p-5 md:p-6">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p class="admin-kicker">Announcements</p>
            <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">发布社区公告</h2>
          </div>
          <span class="rounded-lg bg-amber-50 px-3 py-1.5 text-xs font-extrabold uppercase tracking-[0.18em] text-amber-700">
            普通用户首页可见
          </span>
        </div>

        <form class="mt-6 space-y-4" @submit.prevent="handleCreateAnnouncement">
          <input
            v-model.trim="announcementForm.title"
            type="text"
            class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-slate-900 outline-none transition focus:border-slate-400"
            placeholder="公告标题，例如：系统维护通知 / 社区规则调整"
          />
          <textarea
            v-model.trim="announcementForm.content"
            rows="6"
            class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-slate-900 outline-none transition focus:border-slate-400"
            placeholder="公告内容会展示在所有普通用户首页顶部。"
          ></textarea>
          <div class="flex flex-wrap items-center justify-between gap-3">
            <label class="inline-flex items-center gap-3 rounded-xl bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-700">
              <input v-model="announcementForm.pinned" type="checkbox" class="h-4 w-4 accent-slate-900" />
              置顶展示
            </label>
            <button
              type="submit"
              class="rounded-xl bg-slate-950 px-5 py-3 text-sm font-extrabold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="creatingAnnouncement"
            >
              {{ creatingAnnouncement ? '发布中...' : '发布公告' }}
            </button>
          </div>
        </form>
      </article>

      <article class="admin-panel p-5 md:p-6">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p class="admin-kicker">Recent Posts</p>
            <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">最新公告记录</h2>
          </div>
          <span class="rounded-lg bg-slate-100 px-3 py-1.5 text-xs font-extrabold uppercase tracking-[0.18em] text-slate-600">
            最近 {{ announcementList.length }} 条
          </span>
        </div>

        <div class="mt-6 space-y-4">
          <article v-for="announcement in announcementList.slice(0, 6)" :key="announcement.id" class="admin-panel-soft p-4">
            <div class="flex flex-wrap items-center justify-between gap-3">
              <div class="flex items-center gap-2">
                <span v-if="announcement.pinned" class="rounded-full bg-amber-100 px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em] text-amber-800">置顶公告</span>
                <span class="text-xs font-semibold text-slate-500">{{ formatTime(announcement.createdAt) }}</span>
              </div>
              <span class="text-xs font-semibold text-slate-500">{{ announcement.authorName || '管理员' }}</span>
            </div>
            <h3 class="mt-3 text-lg font-extrabold text-slate-900">{{ announcement.title }}</h3>
            <p class="mt-2 text-sm leading-7 text-slate-600">{{ announcement.content }}</p>
          </article>
          <div v-if="announcementList.length === 0" class="admin-panel-soft px-4 py-5 text-sm text-slate-500">
            当前还没有公告记录。
          </div>
        </div>
      </article>
    </section>

    <section v-else-if="activeTab === 'feedback'" id="feedback-queue" class="admin-panel p-5 md:p-6">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p class="admin-kicker">Feedback Queue</p>
          <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">用户反馈处理</h2>
        </div>
        <span class="rounded-lg bg-rose-50 px-3 py-1.5 text-xs font-extrabold uppercase tracking-[0.18em] text-rose-700">
          待处理 {{ pendingFeedbackCount }} 条
        </span>
      </div>

      <div class="mt-6 space-y-4">
        <article
          v-for="item in feedbackList"
          :key="item.id"
          class="admin-panel-soft p-5"
        >
          <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <div class="flex flex-wrap items-center gap-2">
                <span class="rounded-full px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em]" :class="feedbackTypeClass(item.type)">
                  {{ feedbackTypeLabel(item.type) }}
                </span>
                <span class="rounded-full px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em]" :class="feedbackStatusClass(item.status)">
                  {{ feedbackStatusLabel(item.status) }}
                </span>
              </div>
              <h3 class="mt-3 text-lg font-extrabold text-slate-900">{{ item.title }}</h3>
              <p class="mt-2 text-sm text-slate-500">{{ item.userName }} · {{ item.userStudentId }}</p>
            </div>
            <span class="text-xs font-semibold text-slate-500">{{ formatTime(item.createdAt) }}</span>
          </div>

          <p class="mt-4 text-sm leading-7 text-slate-700">{{ item.content }}</p>

          <textarea
            v-model.trim="feedbackReplies[item.id]"
            rows="3"
            class="mt-4 w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-slate-400"
            placeholder="填写给用户的回复，回复后会通过系统提醒通知对方。"
          ></textarea>

          <div class="mt-4 flex flex-wrap gap-2">
            <button
              type="button"
              class="rounded-xl bg-amber-100 px-4 py-2 text-sm font-bold text-amber-800 transition hover:bg-amber-200"
              :disabled="updatingFeedbackId === item.id"
              @click="handleUpdateFeedback(item, 'in_progress')"
            >
              标记处理中
            </button>
            <button
              type="button"
              class="rounded-xl bg-emerald-100 px-4 py-2 text-sm font-bold text-emerald-800 transition hover:bg-emerald-200"
              :disabled="updatingFeedbackId === item.id"
              @click="handleUpdateFeedback(item, 'resolved')"
            >
              回复并关闭
            </button>
          </div>

          <div v-if="item.adminReply" class="mt-4 rounded-2xl bg-white px-4 py-3 text-sm text-slate-700">
            <p class="font-bold text-slate-900">当前回复</p>
            <p class="mt-2 leading-7 text-slate-600">{{ item.adminReply }}</p>
          </div>
        </article>
        <div v-if="feedbackList.length === 0" class="admin-panel-soft px-4 py-5 text-sm text-slate-500">
          当前没有用户反馈。
        </div>
      </div>
    </section>

    <section v-else class="grid gap-5 xl:grid-cols-[1.05fr_0.95fr]">
      <article class="admin-panel p-5 md:p-6">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p class="admin-kicker">Community Feed</p>
            <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">最新社区动态</h2>
          </div>
          <RouterLink
            :to="{ path: '/admin/community', query: { tab: 'announcements' }, hash: '#announcement-desk' }"
            class="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-700 transition hover:bg-slate-50"
          >
            发布公告
            <span class="material-symbols-outlined text-lg">campaign</span>
          </RouterLink>
        </div>

        <div class="mt-6 grid gap-4 xl:grid-cols-2">
          <article
            v-for="item in contentList"
            :key="item.id"
            class="admin-panel-soft p-5"
          >
            <div class="flex flex-wrap items-center gap-2">
              <span class="rounded-full bg-white px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em] text-slate-800">
                {{ item.taskMode === 'topic' ? '话题帖' : '任务' }}
              </span>
              <span class="rounded-full bg-white px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em] text-slate-500">
                {{ item.category || '未分类' }}
              </span>
              <span class="rounded-full px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em]" :class="item.reviewStatus === 'approved' ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-700'">
                {{ item.reviewStatus === 'approved' ? '已展示' : '未展示' }}
              </span>
            </div>

            <h3 class="mt-4 text-xl font-extrabold text-slate-900">{{ item.title }}</h3>
            <p class="mt-3 line-clamp-4 text-sm leading-7 text-slate-600">{{ item.description }}</p>

            <div class="mt-5 grid gap-3 sm:grid-cols-2">
              <div class="rounded-2xl bg-white px-4 py-3">
                <p class="text-[11px] font-extrabold uppercase tracking-[0.18em] text-slate-500">发布者</p>
                <p class="mt-2 text-sm font-bold text-slate-800">{{ item.requesterName || `用户 #${item.requesterId}` }}</p>
              </div>
              <div class="rounded-2xl bg-white px-4 py-3">
                <p class="text-[11px] font-extrabold uppercase tracking-[0.18em] text-slate-500">发布时间</p>
                <p class="mt-2 text-sm font-bold text-slate-800">{{ formatTime(item.createdAt) }}</p>
              </div>
            </div>
          </article>
          <div v-if="contentList.length === 0" class="admin-panel-soft px-4 py-5 text-sm text-slate-500 xl:col-span-2">
            当前没有社区内容。
          </div>
        </div>
      </article>

      <div class="space-y-5">
        <article class="admin-panel p-5 md:p-6">
          <div class="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p class="admin-kicker">Pinned Notices</p>
              <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">公告概览</h2>
            </div>
            <RouterLink
              :to="{ path: '/admin/community', query: { tab: 'announcements' }, hash: '#announcement-desk' }"
              class="text-sm font-bold text-slate-700 transition hover:text-slate-950"
            >
              管理公告
            </RouterLink>
          </div>

          <div class="mt-6 space-y-3">
            <article v-for="announcement in announcementList.slice(0, 3)" :key="announcement.id" class="admin-panel-soft p-4">
              <div class="flex flex-wrap items-center justify-between gap-2">
                <span v-if="announcement.pinned" class="rounded-full bg-amber-100 px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em] text-amber-800">置顶</span>
                <span class="text-xs font-semibold text-slate-500">{{ formatTime(announcement.createdAt) }}</span>
              </div>
              <h3 class="mt-3 text-base font-extrabold text-slate-900">{{ announcement.title }}</h3>
              <p class="mt-2 text-sm leading-6 text-slate-600">{{ announcement.content }}</p>
            </article>
            <div v-if="announcementList.length === 0" class="admin-panel-soft px-4 py-5 text-sm text-slate-500">
              当前没有公告。
            </div>
          </div>
        </article>

        <article class="admin-panel p-5 md:p-6">
          <div class="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p class="admin-kicker">Feedback Snapshot</p>
              <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">反馈概览</h2>
            </div>
            <RouterLink
              :to="{ path: '/admin/community', query: { tab: 'feedback' }, hash: '#feedback-queue' }"
              class="text-sm font-bold text-slate-700 transition hover:text-slate-950"
            >
              进入处理
            </RouterLink>
          </div>

          <div class="mt-6 space-y-3">
            <div class="admin-panel-soft flex items-center justify-between px-4 py-4">
              <div>
                <p class="text-sm font-extrabold text-slate-900">待处理反馈</p>
                <p class="mt-1 text-sm text-slate-500">优先看 bug 和高频建议。</p>
              </div>
              <span class="text-2xl font-extrabold tracking-tight text-rose-700">{{ pendingFeedbackCount }}</span>
            </div>
            <div class="admin-panel-soft flex items-center justify-between px-4 py-4">
              <div>
                <p class="text-sm font-extrabold text-slate-900">已发布公告</p>
                <p class="mt-1 text-sm text-slate-500">包含置顶和普通公告。</p>
              </div>
              <span class="text-2xl font-extrabold tracking-tight text-slate-900">{{ announcementList.length }}</span>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { adminApi, taskApi } from '../../services/api'

type WorkspaceTab = 'content' | 'announcements' | 'feedback'

const route = useRoute()
const error = ref('')
const dashboard = ref<any>({ overview: {} })
const announcementList = ref<any[]>([])
const feedbackList = ref<any[]>([])
const contentList = ref<any[]>([])
const creatingAnnouncement = ref(false)
const updatingFeedbackId = ref<number | null>(null)
const feedbackReplies = ref<Record<number, string>>({})

const announcementForm = ref({
  title: '',
  content: '',
  pinned: true
})

const workspaceTabs: Array<{ value: WorkspaceTab; label: string; icon: string; hash: string }> = [
  { value: 'content', label: '社区动态', icon: 'newspaper', hash: '' },
  { value: 'announcements', label: '公告发布', icon: 'campaign', hash: '#announcement-desk' },
  { value: 'feedback', label: '反馈处理', icon: 'reviews', hash: '#feedback-queue' }
]

const activeTab = computed<WorkspaceTab>(() => {
  const value = String(route.query.tab || 'content')
  return value === 'announcements' || value === 'feedback' ? value : 'content'
})

const workspaceCards = computed(() => [
  { label: '置顶公告', value: announcementList.value.filter((item) => item.pinned).length, hint: '普通用户首页顶部展示。', icon: 'campaign' },
  { label: '待处理反馈', value: pendingFeedbackCount.value, hint: '优先处理 bug 与高频建议。', icon: 'reviews' },
  { label: '今日订单', value: dashboard.value?.overview?.todayOrderCount ?? 0, hint: '后台只做观察和治理。', icon: 'inventory_2' }
])

const pendingFeedbackCount = computed(() => feedbackList.value.filter((item) => item.status !== 'resolved').length)

const normalizeList = (response: any) => Array.isArray(response) ? response : Array.isArray(response?.data) ? response.data : []

const loadWorkspace = async () => {
  error.value = ''
  try {
    const [dashboardResponse, announcementResponse, feedbackResponse, tasksResponse] = await Promise.all([
      adminApi.getDashboard(),
      adminApi.getAnnouncements(),
      adminApi.getFeedback(),
      taskApi.getTasks()
    ]) as [any, any, any, any]

    dashboard.value = dashboardResponse || { overview: {} }
    announcementList.value = normalizeList(announcementResponse)
    feedbackList.value = normalizeList(feedbackResponse)
    contentList.value = normalizeList(tasksResponse)
    feedbackReplies.value = feedbackList.value.reduce((acc: Record<number, string>, item: any) => {
      acc[item.id] = item.adminReply || ''
      return acc
    }, {})
  } catch (err: any) {
    error.value = err?.response?.data?.message || '社区工作台加载失败'
  }
}

const scrollToHashIfNeeded = async () => {
  if (!route.hash) return
  await nextTick()
  document.querySelector(route.hash)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const handleCreateAnnouncement = async () => {
  if (!announcementForm.value.title || !announcementForm.value.content) {
    error.value = '公告标题和内容不能为空'
    return
  }

  creatingAnnouncement.value = true
  error.value = ''
  try {
    await adminApi.createAnnouncement({
      title: announcementForm.value.title,
      content: announcementForm.value.content,
      pinned: announcementForm.value.pinned
    })
    announcementForm.value = { title: '', content: '', pinned: true }
    await loadWorkspace()
  } catch (err: any) {
    error.value = err?.response?.data?.message || '公告发布失败'
  } finally {
    creatingAnnouncement.value = false
  }
}

const handleUpdateFeedback = async (item: any, status: 'in_progress' | 'resolved') => {
  updatingFeedbackId.value = item.id
  error.value = ''
  try {
    await adminApi.updateFeedback(item.id, {
      status,
      adminReply: feedbackReplies.value[item.id]
    })
    await loadWorkspace()
  } catch (err: any) {
    error.value = err?.response?.data?.message || '反馈处理失败'
  } finally {
    updatingFeedbackId.value = null
  }
}

const formatTime = (value?: string) => {
  if (!value) return '刚刚'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '刚刚'
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const feedbackTypeLabel = (value?: string) => {
  if (value === 'BUG') return 'Bug'
  if (value === 'SUGGESTION') return '建议'
  return '其他'
}

const feedbackTypeClass = (value?: string) => {
  if (value === 'BUG') return 'bg-rose-100 text-rose-700'
  if (value === 'SUGGESTION') return 'bg-cyan-100 text-cyan-800'
  return 'bg-slate-100 text-slate-700'
}

const feedbackStatusLabel = (value?: string) => {
  if (value === 'resolved') return '已解决'
  if (value === 'in_progress') return '处理中'
  return '待处理'
}

const feedbackStatusClass = (value?: string) => {
  if (value === 'resolved') return 'bg-emerald-100 text-emerald-700'
  if (value === 'in_progress') return 'bg-amber-100 text-amber-800'
  return 'bg-slate-100 text-slate-700'
}

watch(() => route.fullPath, () => {
  scrollToHashIfNeeded()
})

onMounted(async () => {
  await loadWorkspace()
  scrollToHashIfNeeded()
})
</script>
