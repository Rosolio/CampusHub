<template>
  <div class="min-h-screen bg-surface font-body text-on-surface pb-16">
    <AdminModeHeader />

    <main class="mx-auto max-w-7xl px-6 pb-12 pt-8">
      <section class="overflow-hidden rounded-[2.2rem] bg-[linear-gradient(135deg,#102a33_0%,#12555f_48%,#cf8428_100%)] p-7 text-white shadow-[0_24px_80px_rgba(15,23,42,0.14)] md:p-8">
        <div class="grid gap-8 xl:grid-cols-[1.05fr_0.95fr]">
          <div>
            <div class="flex flex-wrap items-center gap-3">
              <span class="rounded-full border border-white/16 bg-white/10 px-4 py-2 text-[11px] font-extrabold uppercase tracking-[0.28em] text-white/82">Admin Community View</span>
              <span class="rounded-full bg-white/12 px-4 py-2 text-sm font-semibold text-white/84">只做治理，不做互动</span>
            </div>
            <h1 class="mt-5 max-w-3xl text-4xl font-extrabold leading-[1.02] tracking-[-0.04em] md:text-5xl">
              管理员专属社区视角
            </h1>
            <p class="mt-4 max-w-2xl text-sm leading-7 text-white/76 md:text-base">
              管理员不参与普通私信、发布需求或帖子。这里聚焦公告发布、反馈处理和社区整体动态，避免混入普通用户操作流。
            </p>

            <div class="mt-7 grid gap-3 sm:grid-cols-3">
              <div v-for="card in heroCards" :key="card.label" class="rounded-[1.35rem] border border-white/12 bg-white/10 px-4 py-4 backdrop-blur-sm">
                <p class="text-[11px] font-extrabold uppercase tracking-[0.22em] text-white/55">{{ card.label }}</p>
                <p class="mt-3 text-3xl font-extrabold tracking-tight">{{ card.value }}</p>
                <p class="mt-2 text-sm leading-6 text-white/72">{{ card.hint }}</p>
              </div>
            </div>
          </div>

          <div class="rounded-[1.8rem] border border-white/16 bg-white/10 p-5 backdrop-blur-sm">
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="text-[11px] font-extrabold uppercase tracking-[0.24em] text-white/55">Actions</p>
                <h2 class="mt-2 text-2xl font-extrabold">快捷入口</h2>
              </div>
              <RouterLink to="/admin/overview" class="inline-flex items-center gap-2 rounded-full bg-white px-4 py-2 text-sm font-extrabold text-slate-900 transition hover:bg-[#f6f1e7]">
                进入后台
                <span class="material-symbols-outlined text-base">arrow_forward</span>
              </RouterLink>
            </div>

            <div class="mt-5 grid gap-3">
              <RouterLink
                v-for="item in quickLinks"
                :key="item.to"
                :to="item.to"
                class="flex items-center justify-between rounded-[1.3rem] border border-white/12 bg-white/8 px-4 py-4 transition hover:bg-white/14"
              >
                <div class="flex items-center gap-3">
                  <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-white/12 text-white">
                    <span class="material-symbols-outlined">{{ item.icon }}</span>
                  </div>
                  <div>
                    <p class="text-sm font-extrabold text-white">{{ item.label }}</p>
                    <p class="mt-1 text-sm text-white/68">{{ item.description }}</p>
                  </div>
                </div>
                <span class="material-symbols-outlined text-white/72">arrow_forward</span>
              </RouterLink>
            </div>
          </div>
        </div>
      </section>

      <div class="mt-8 grid gap-8 xl:grid-cols-[1.05fr_0.95fr]">
        <section class="space-y-8">
          <section class="rounded-[2rem] bg-surface-container-lowest p-6 shadow-sm">
            <div class="flex flex-wrap items-center justify-between gap-3">
              <div>
                <p class="text-[11px] font-extrabold uppercase tracking-[0.24em] text-teal-700/65">Announcement Desk</p>
                <h2 class="mt-2 text-2xl font-extrabold text-teal-950">发布社区公告</h2>
              </div>
              <button
                type="button"
                class="inline-flex items-center gap-2 rounded-full bg-surface-container-low px-4 py-2 text-sm font-bold text-teal-900 transition hover:bg-surface-container-high"
                :disabled="creatingAnnouncement"
                @click="loadHome"
              >
                <span class="material-symbols-outlined text-base">refresh</span>
                刷新
              </button>
            </div>

            <div v-if="error" class="mt-5 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">
              {{ error }}
            </div>

            <form class="mt-5 space-y-4" @submit.prevent="handleCreateAnnouncement">
              <input
                v-model.trim="announcementForm.title"
                type="text"
                class="w-full rounded-2xl border border-outline-variant/15 bg-surface-container-low px-4 py-3 text-on-surface outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
                placeholder="公告标题，例如：系统维护通知 / 社区规则调整"
              />
              <textarea
                v-model.trim="announcementForm.content"
                rows="4"
                class="w-full rounded-2xl border border-outline-variant/15 bg-surface-container-low px-4 py-3 text-on-surface outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
                placeholder="公告内容会展示在所有普通用户首页顶部。"
              ></textarea>
              <div class="flex flex-wrap items-center justify-between gap-3">
                <label class="inline-flex items-center gap-3 rounded-full bg-surface-container-low px-4 py-2 text-sm font-medium text-teal-900">
                  <input v-model="announcementForm.pinned" type="checkbox" class="h-4 w-4 accent-teal-900" />
                  置顶展示
                </label>
                <button
                  type="submit"
                  class="rounded-full bg-teal-900 px-5 py-3 text-sm font-extrabold text-white transition hover:bg-teal-800 disabled:opacity-60"
                  :disabled="creatingAnnouncement"
                >
                  {{ creatingAnnouncement ? '发布中...' : '发布公告' }}
                </button>
              </div>
            </form>

            <div class="mt-6 space-y-3">
              <article
                v-for="announcement in announcements.slice(0, 4)"
                :key="announcement.id"
                class="rounded-[1.45rem] border border-outline-variant/12 bg-surface-container-low p-4"
              >
                <div class="flex flex-wrap items-center justify-between gap-3">
                  <div class="flex items-center gap-2">
                    <span v-if="announcement.pinned" class="rounded-full bg-amber-100 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-amber-800">置顶公告</span>
                    <span class="text-xs font-semibold text-on-surface-variant">{{ formatTime(announcement.createdAt) }}</span>
                  </div>
                  <span class="text-xs font-semibold text-on-surface-variant">{{ announcement.authorName || '管理员' }}</span>
                </div>
                <h3 class="mt-3 text-lg font-extrabold text-teal-950">{{ announcement.title }}</h3>
                <p class="mt-2 text-sm leading-7 text-on-surface-variant">{{ announcement.content }}</p>
              </article>
            </div>
          </section>

          <section class="rounded-[2rem] bg-surface-container-lowest p-6 shadow-sm">
            <div class="flex flex-wrap items-center justify-between gap-3">
              <div>
                <p class="text-[11px] font-extrabold uppercase tracking-[0.24em] text-teal-700/65">Community Pulse</p>
                <h2 class="mt-2 text-2xl font-extrabold text-teal-950">最新社区动态</h2>
              </div>
              <RouterLink to="/admin/community" class="inline-flex items-center gap-2 rounded-full bg-cyan-50 px-4 py-2 text-sm font-bold text-teal-900 transition hover:bg-cyan-100">
                进入独立页面
                <span class="material-symbols-outlined text-base">arrow_forward</span>
              </RouterLink>
            </div>

            <div class="mt-5 grid gap-4 md:grid-cols-2">
              <article
                v-for="item in latestContent"
                :key="item.id"
                class="rounded-[1.5rem] border border-outline-variant/12 bg-surface-container-low p-4 transition hover:-translate-y-0.5 hover:bg-cyan-50/70"
              >
                <div class="flex flex-wrap items-center gap-2">
                  <span class="rounded-full bg-white px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-teal-900">{{ item.modeLabel }}</span>
                  <span class="rounded-full bg-white px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em] text-on-surface-variant">{{ item.category }}</span>
                </div>
                <h3 class="mt-3 text-lg font-extrabold text-teal-950">{{ item.title }}</h3>
                <p class="mt-2 line-clamp-3 text-sm leading-7 text-on-surface-variant">{{ item.description }}</p>
                <div class="mt-4 flex items-center justify-between text-xs font-semibold text-on-surface-variant">
                  <span>{{ item.requesterName || '社区用户' }}</span>
                  <span>{{ formatTime(item.createdAt) }}</span>
                </div>
              </article>
            </div>
          </section>
        </section>

        <section id="feedback-queue" class="rounded-[2rem] bg-surface-container-lowest p-6 shadow-sm">
          <div class="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p class="text-[11px] font-extrabold uppercase tracking-[0.24em] text-teal-700/65">Feedback Queue</p>
              <h2 class="mt-2 text-2xl font-extrabold text-teal-950">用户反馈处理</h2>
            </div>
            <div class="rounded-full bg-rose-50 px-4 py-2 text-sm font-bold text-rose-700">
              待处理 {{ pendingFeedbackCount }} 条
            </div>
          </div>

          <div class="mt-5 space-y-4">
            <article
              v-for="item in feedbackList.slice(0, 8)"
              :key="item.id"
              class="rounded-[1.55rem] border border-outline-variant/12 bg-surface-container-low p-4"
            >
              <div class="flex flex-wrap items-start justify-between gap-3">
                <div>
                  <div class="flex flex-wrap items-center gap-2">
                    <span class="rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em]" :class="feedbackTypeClass(item.type)">
                      {{ feedbackTypeLabel(item.type) }}
                    </span>
                    <span class="rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em]" :class="feedbackStatusClass(item.status)">
                      {{ feedbackStatusLabel(item.status) }}
                    </span>
                  </div>
                  <h3 class="mt-3 text-lg font-extrabold text-teal-950">{{ item.title }}</h3>
                  <p class="mt-2 text-sm text-on-surface-variant">{{ item.userName }} · {{ item.userStudentId }}</p>
                </div>
                <span class="text-xs font-semibold text-on-surface-variant">{{ formatTime(item.createdAt) }}</span>
              </div>

              <p class="mt-4 text-sm leading-7 text-on-surface">{{ item.content }}</p>

              <textarea
                v-model.trim="feedbackReplies[item.id]"
                rows="3"
                class="mt-4 w-full rounded-2xl border border-outline-variant/15 bg-white px-4 py-3 text-sm text-on-surface outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
                placeholder="填写给用户的回复，回复后会通过系统提醒通知对方。"
              ></textarea>

              <div class="mt-4 flex flex-wrap gap-2">
                <button
                  type="button"
                  class="rounded-full bg-amber-100 px-4 py-2 text-sm font-bold text-amber-800 transition hover:bg-amber-200"
                  :disabled="updatingFeedbackId === item.id"
                  @click="handleUpdateFeedback(item, 'in_progress')"
                >
                  标记处理中
                </button>
                <button
                  type="button"
                  class="rounded-full bg-emerald-100 px-4 py-2 text-sm font-bold text-emerald-800 transition hover:bg-emerald-200"
                  :disabled="updatingFeedbackId === item.id"
                  @click="handleUpdateFeedback(item, 'resolved')"
                >
                  回复并关闭
                </button>
              </div>

              <div v-if="item.adminReply" class="mt-4 rounded-2xl bg-white px-4 py-3 text-sm text-on-surface">
                <p class="font-bold text-teal-900">当前回复</p>
                <p class="mt-2 leading-7 text-on-surface-variant">{{ item.adminReply }}</p>
              </div>
            </article>
          </div>
        </section>
      </div>
    </main>

    <AppBottomNav />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import AppBottomNav from '../components/AppBottomNav.vue'
import AdminModeHeader from '../components/AdminModeHeader.vue'
import { adminApi, taskApi } from '../services/api'

const error = ref('')
const dashboard = ref<any>({ overview: {} })
const announcements = ref<any[]>([])
const feedbackList = ref<any[]>([])
const communityContent = ref<any[]>([])
const creatingAnnouncement = ref(false)
const updatingFeedbackId = ref<number | null>(null)
const feedbackReplies = ref<Record<number, string>>({})

const announcementForm = ref({
  title: '',
  content: '',
  pinned: true
})

const heroCards = computed(() => [
  { label: '置顶公告', value: announcements.value.filter((item) => item.pinned).length, hint: '所有普通用户首页顶部可见。' },
  { label: '待处理反馈', value: pendingFeedbackCount.value, hint: '优先处理 bug 与高频建议。' },
  { label: '今日订单', value: dashboard.value?.overview?.todayOrderCount ?? 0, hint: '这里只做运营观察，不参与履约。' }
])

const pendingFeedbackCount = computed(() => feedbackList.value.filter((item) => item.status !== 'resolved').length)

const latestContent = computed(() => communityContent.value.slice(0, 6))

const quickLinks = [
  { to: '/admin/overview', label: '运营总览', description: '查看日活、订单量和结构分布。', icon: 'monitoring' },
  { to: '/admin/community', label: '社区动态', description: '进入独立页面查看最新社区内容。', icon: 'newspaper' },
  { to: '/admin/users', label: '用户治理', description: '处理账号状态和用户搜索。', icon: 'manage_accounts' },
  { to: '/admin/moderation', label: '内容审核', description: '管理待审核内容与驳回通知。', icon: 'gavel' },
  { to: '/home#feedback-queue', label: '反馈处理', description: '集中回复用户 bug 与产品建议。', icon: 'reviews' },
  { to: '/admin/profile', label: '个人中心', description: '查看管理员身份信息与后台工作入口。', icon: 'badge' }
]

const normalizeList = (response: any) => Array.isArray(response) ? response : Array.isArray(response?.data) ? response.data : []

const loadHome = async () => {
  error.value = ''
  try {
    const [dashboardResponse, announcementResponse, feedbackResponse, tasksResponse] = await Promise.all([
      adminApi.getDashboard(),
      adminApi.getAnnouncements(),
      adminApi.getFeedback(),
      taskApi.getTasks()
    ]) as [any, any, any, any]

    dashboard.value = dashboardResponse || { overview: {} }
    announcements.value = normalizeList(announcementResponse)
    feedbackList.value = normalizeList(feedbackResponse)
    communityContent.value = normalizeList(tasksResponse).map((item: any) => ({
      ...item,
      modeLabel: item.taskMode === 'topic' ? '话题帖' : '任务'
    }))
    feedbackReplies.value = feedbackList.value.reduce((acc: Record<number, string>, item: any) => {
      acc[item.id] = item.adminReply || ''
      return acc
    }, {})
  } catch (err: any) {
    error.value = err?.response?.data?.message || '管理员社区视角加载失败'
  }
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
    await loadHome()
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
    await loadHome()
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

onMounted(() => {
  loadHome()
})
</script>
