<template>
  <div class="min-h-screen bg-surface font-body text-on-surface">
    <AppTopNav />

    <main class="mx-auto max-w-7xl px-6 pb-32 pt-24">
      <section class="mb-8 grid gap-4 xl:grid-cols-[1.05fr_0.95fr]">
        <article class="overflow-hidden rounded-[2rem] bg-[linear-gradient(135deg,rgba(15,60,68,0.98),rgba(13,93,104,0.92),rgba(102,183,172,0.82))] p-6 text-white shadow-sm">
          <div class="flex flex-wrap items-center gap-3">
            <span class="rounded-full border border-white/16 bg-white/10 px-4 py-2 text-[11px] font-bold uppercase tracking-[0.26em] text-white/76">Pinned Announcements</span>
            <span class="rounded-full bg-white/12 px-4 py-2 text-xs font-bold uppercase tracking-[0.2em] text-white/78">管理员发布</span>
          </div>
          <h2 class="mt-4 text-3xl font-extrabold tracking-tight">社区公告</h2>
          <p class="mt-3 max-w-2xl text-sm leading-7 text-white/78">
            重要通知会固定展示在这里。系统维护、规则调整和功能变更会优先通过公告同步。
          </p>

          <div class="mt-5 space-y-3">
            <article
              v-for="announcement in pinnedAnnouncements"
              :key="announcement.id"
              class="rounded-[1.4rem] border border-white/14 bg-white/10 px-4 py-4 backdrop-blur-sm"
            >
              <div class="flex flex-wrap items-center justify-between gap-3">
                <h3 class="text-lg font-extrabold text-white">{{ announcement.title }}</h3>
                <span class="text-xs font-semibold text-white/66">{{ formatAnnouncementTime(announcement.createdAt) }}</span>
              </div>
              <p class="mt-2 text-sm leading-7 text-white/76">{{ announcement.content }}</p>
            </article>
            <div v-if="pinnedAnnouncements.length === 0" class="rounded-[1.4rem] border border-white/14 bg-white/10 px-4 py-4 text-sm text-white/72">
              当前没有公告。
            </div>
          </div>
        </article>

        <article class="rounded-[2rem] bg-surface-container-lowest p-6 shadow-sm">
          <p class="text-[11px] font-extrabold uppercase tracking-[0.24em] text-teal-700/65">Community Feedback</p>
          <h2 class="mt-3 text-3xl font-extrabold text-teal-950">发现 bug 或有建议？</h2>
          <p class="mt-3 text-sm leading-7 text-on-surface-variant">
            现在支持直接向管理员提交社区反馈。这里不走普通私信，反馈会进入管理员处理队列，回复后会通过系统提醒通知你。
          </p>
          <div class="mt-5 grid gap-3 sm:grid-cols-3">
            <div class="rounded-[1.3rem] bg-surface-container-low px-4 py-4">
              <p class="text-[11px] font-extrabold uppercase tracking-[0.2em] text-on-surface-variant">问题类型</p>
              <p class="mt-3 text-lg font-extrabold text-teal-900">Bug / 建议</p>
            </div>
            <div class="rounded-[1.3rem] bg-surface-container-low px-4 py-4">
              <p class="text-[11px] font-extrabold uppercase tracking-[0.2em] text-on-surface-variant">处理方式</p>
              <p class="mt-3 text-lg font-extrabold text-teal-900">管理员回复</p>
            </div>
            <div class="rounded-[1.3rem] bg-surface-container-low px-4 py-4">
              <p class="text-[11px] font-extrabold uppercase tracking-[0.2em] text-on-surface-variant">通知渠道</p>
              <p class="mt-3 text-lg font-extrabold text-teal-900">系统提醒</p>
            </div>
          </div>
          <RouterLink
            to="/feedback"
            class="mt-5 inline-flex items-center gap-2 rounded-full bg-teal-900 px-5 py-3 text-sm font-extrabold text-white transition hover:bg-teal-800"
          >
            提交社区反馈
            <span class="material-symbols-outlined text-base">arrow_forward</span>
          </RouterLink>
        </article>
      </section>

      <section class="mb-8 overflow-hidden rounded-[2.4rem] bg-[radial-gradient(circle_at_top_left,rgba(255,255,255,0.96),rgba(236,253,245,0.96)_36%,rgba(223,245,255,0.92)_68%,rgba(248,250,252,0.95)_100%)] p-6 shadow-sm md:p-8">
        <div class="grid gap-6 xl:grid-cols-[1.05fr_0.95fr] xl:items-start">
          <div>
            <div class="mb-4 flex flex-wrap items-center gap-3">
              <span class="rounded-full bg-teal-900 px-4 py-2 text-xs font-bold uppercase tracking-[0.28em] text-white">校园需求广场</span>
              <span class="rounded-full border border-teal-200 bg-white/70 px-4 py-2 text-xs font-bold uppercase tracking-[0.22em] text-teal-900">两种参与方式</span>
            </div>

            <h1 class="max-w-3xl text-4xl font-extrabold leading-[1.04] tracking-tight text-teal-950 md:text-5xl">
              社区内容分区进入
            </h1>

            <p class="mt-4 max-w-2xl text-base leading-7 text-slate-600">
              任务型内容保留在首页处理，公开讨论型内容进入独立话题页。右侧热榜会同步展示当前最热的话题帖。
            </p>
          </div>

          <div class="relative">
            <div class="absolute inset-0 translate-x-4 translate-y-4 rounded-[2.2rem] bg-gradient-to-br from-teal-900/15 to-cyan-500/10 blur-2xl"></div>
            <div class="relative grid gap-4 md:grid-cols-2">
              <button
                type="button"
                class="group rounded-[1.9rem] bg-teal-950 p-5 text-left text-white shadow-[0_24px_60px_rgba(15,23,42,0.18)] ring-1 ring-teal-900/20 transition-all duration-300 hover:-translate-y-1 hover:scale-[1.01] hover:shadow-[0_30px_70px_rgba(15,23,42,0.22)]"
                @click="handleTaskEntry"
              >
                <div class="flex items-start justify-between gap-4">
                  <div>
                    <p class="text-xs font-bold uppercase tracking-[0.22em] text-cyan-100/70">入口一</p>
                    <h2 class="mt-2 text-2xl font-extrabold">任务接单</h2>
                  </div>
                  <div class="rounded-2xl bg-white/10 p-3 text-cyan-50 ring-1 ring-white/10">
                    <span class="material-symbols-outlined text-3xl">directions_run</span>
                  </div>
                </div>
                <p class="mt-4 text-sm leading-7 text-cyan-50/78">
                  跑腿代办、学习辅导等明确履约内容。
                </p>
                <div class="mt-4 flex flex-wrap items-center gap-2">
                  <span class="rounded-full border border-white/12 bg-white/10 px-3 py-1.5 text-xs font-semibold text-white">跑腿代办</span>
                  <span class="rounded-full border border-white/12 bg-white/10 px-3 py-1.5 text-xs font-semibold text-white">学习辅导</span>
                  <span class="rounded-full border border-white/12 bg-white/10 px-3 py-1.5 text-xs font-semibold text-white">待接单 / 进行中</span>
                </div>
                <div class="mt-5 inline-flex items-center gap-2 text-sm font-bold text-cyan-50 transition-all group-hover:gap-3">
                  查看任务列表
                  <span class="material-symbols-outlined text-base">arrow_forward</span>
                </div>
              </button>

              <button
                type="button"
                class="group rounded-[1.9rem] bg-white/92 p-5 text-left shadow-[0_24px_60px_rgba(15,23,42,0.08)] ring-1 ring-teal-100/90 transition-all duration-300 hover:-translate-y-1 hover:scale-[1.01] hover:shadow-[0_30px_70px_rgba(15,23,42,0.12)]"
                @click="goToTopicSquare()"
              >
                <div class="flex items-start justify-between gap-4">
                  <div>
                    <p class="text-xs font-bold uppercase tracking-[0.22em] text-teal-700/60">入口二</p>
                    <h2 class="mt-2 text-2xl font-extrabold text-teal-950">帖子互动</h2>
                  </div>
                  <div class="rounded-2xl bg-teal-50 p-3 text-teal-900 ring-1 ring-teal-100">
                    <span class="material-symbols-outlined text-3xl">forum</span>
                  </div>
                </div>
                <p class="mt-4 text-sm leading-7 text-slate-600">
                  二手、交友、求助、兼职等公开互动内容。
                </p>
                <div class="mt-4 flex flex-wrap gap-2">
                  <button
                    v-for="category in topicPreviewCategories"
                    :key="category"
                    type="button"
                    class="rounded-full px-3 py-1.5 text-xs font-semibold transition-all"
                    :class="selectedTopicEntryCategory === category
                      ? 'bg-teal-900 text-white shadow-sm'
                      : 'bg-surface-container-low text-teal-900 hover:bg-teal-50'"
                    @click.stop="selectedTopicEntryCategory = category"
                  >
                    {{ category }}
                  </button>
                </div>
                <div class="mt-5 inline-flex items-center gap-2 text-sm font-bold text-teal-900 transition-all group-hover:gap-3">
                  进入全部话题页
                  <span class="material-symbols-outlined text-base">arrow_forward</span>
                </div>
              </button>
            </div>
          </div>
        </div>
      </section>

      <div class="grid gap-8 xl:grid-cols-[minmax(0,1fr)_18rem]">
        <section>
          <section ref="taskSection" class="mb-8 flex gap-3 overflow-x-auto pb-2">
            <button
              v-for="category in categories"
              :key="category"
              type="button"
              class="flex shrink-0 items-center gap-2 rounded-full px-5 py-2 text-sm font-semibold transition-all"
              :class="activeCategory === category ? 'scale-95 bg-primary text-white shadow-sm' : 'bg-surface-container-high text-on-surface-variant hover:bg-cyan-50/60'"
              @click="activeCategory = category"
            >
              <span class="material-symbols-outlined text-lg">{{ iconForCategory(category) }}</span>
              {{ category }}
            </button>
          </section>

          <section v-if="loading" class="rounded-[2rem] bg-surface-container-low p-10 text-center text-on-surface-variant">
            正在加载社区内容...
          </section>

          <section v-else-if="error" class="rounded-[2rem] border border-rose-200 bg-rose-50 p-10 text-center text-rose-700">
            {{ error }}
          </section>

          <section v-else-if="filteredCards.length === 0" class="rounded-[2rem] bg-surface-container-low p-10 text-center">
            <div class="mx-auto mb-5 flex h-20 w-20 items-center justify-center rounded-full bg-surface-container-high">
              <span class="material-symbols-outlined text-4xl text-on-surface-variant">inbox</span>
            </div>
            <h2 class="text-2xl font-bold text-teal-900">当前没有可接单任务</h2>
            <p class="mt-3 text-on-surface-variant">稍后再来看看，或者自己先发一条跑腿代办或学习辅导。</p>
          </section>

          <section v-else class="grid gap-8 lg:grid-cols-2">
            <article
              v-for="card in filteredCards"
              :key="card.id"
              class="overflow-hidden rounded-[2rem] border border-outline-variant/10 bg-surface-container-lowest shadow-sm transition-all hover:-translate-y-1 hover:shadow-lg"
            >
              <div class="p-8">
                <div class="mb-5 flex flex-wrap items-start justify-between gap-3">
                  <div class="flex flex-wrap gap-2">
                    <span class="rounded-full bg-surface-container-high px-3 py-1 text-[11px] font-bold uppercase tracking-[0.2em] text-on-surface-variant">
                      {{ card.category }}
                    </span>
                    <span
                      class="rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-[0.2em]"
                      :class="card.badgePrimary === '紧急' ? 'border border-rose-200 bg-rose-50 text-rose-700' : 'border border-sky-200 bg-sky-50 text-sky-700'"
                    >
                      {{ card.badgePrimary }}
                    </span>
                    <span class="rounded-full border border-amber-200 bg-amber-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.2em] text-amber-700">
                      {{ taskStatusBadge(card.status) }}
                    </span>
                  </div>
                  <span class="text-sm font-bold text-secondary">
                    {{ card.rewardText }}
                  </span>
                </div>

                <h2 class="text-2xl font-extrabold leading-tight text-teal-900">
                  {{ card.title }}
                </h2>
                <p class="mt-4 line-clamp-3 text-sm leading-7 text-on-surface-variant">
                  {{ card.description }}
                </p>

                <div class="mt-6 grid gap-3 sm:grid-cols-2">
                  <div class="rounded-2xl bg-surface-container-low px-4 py-3 text-on-surface">
                    <p class="text-[11px] font-bold uppercase tracking-[0.18em] text-on-surface-variant">地点</p>
                    <p class="mt-2 text-sm font-medium">{{ card.locationText || '待补充' }}</p>
                  </div>
                  <div class="rounded-2xl bg-surface-container-low px-4 py-3 text-on-surface">
                    <p class="text-[11px] font-bold uppercase tracking-[0.18em] text-on-surface-variant">截止时间</p>
                    <p class="mt-2 text-sm font-medium">{{ card.timeText || '待补充' }}</p>
                  </div>
                </div>

                <div class="mt-8 flex items-center justify-between gap-3">
                  <div>
                    <p class="text-sm font-semibold text-teal-900">{{ card.publisher }}</p>
                    <p class="mt-1 text-xs text-on-surface-variant">{{ taskStatusText(card) }}</p>
                  </div>
                  <RouterLink
                    :to="`/detail/${card.id}`"
                    class="inline-flex items-center gap-2 rounded-full bg-primary px-5 py-3 text-sm font-bold text-white transition-all hover:gap-3"
                  >
                    查看任务
                    <span class="material-symbols-outlined text-base">arrow_forward</span>
                  </RouterLink>
                </div>
              </div>
            </article>
          </section>
        </section>

        <aside class="space-y-6 xl:sticky xl:top-24 self-start">
          <section class="rounded-[1.8rem] border border-outline-variant/12 bg-surface-container-lowest p-5 shadow-sm">
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="text-xs font-bold uppercase tracking-[0.24em] text-teal-700/65">社区热榜</p>
                <h2 class="mt-2 text-xl font-extrabold text-teal-950">热度最高的话题帖</h2>
              </div>
              <span class="rounded-full bg-amber-100 px-3 py-1.5 text-[11px] font-bold uppercase tracking-[0.2em] text-amber-800">Top 10</span>
            </div>

            <div v-if="hotTopicRanking.length === 0" class="mt-6 rounded-2xl bg-surface-container-low px-4 py-5 text-sm text-on-surface-variant">
              还没有话题帖进入热榜。
            </div>

            <div v-else class="mt-5 space-y-2.5">
              <RouterLink
                v-for="(topic, index) in hotTopicRanking"
                :key="topic.id"
                :to="`/detail/${topic.id}`"
                class="flex items-start gap-3 rounded-[1.2rem] bg-surface-container-low px-3.5 py-3 transition-all hover:-translate-y-0.5 hover:bg-cyan-50/80"
              >
                <span
                  class="mt-0.5 inline-flex h-6 min-w-6 items-center justify-center rounded-full text-[11px] font-extrabold"
                  :class="index < 3 ? 'bg-teal-900 text-white' : 'bg-white text-teal-900'"
                >
                  {{ index + 1 }}
                </span>
                <span class="line-clamp-2 text-sm font-semibold leading-6 text-teal-950">{{ topic.title }}</span>
              </RouterLink>
            </div>
          </section>
        </aside>
      </div>
    </main>

    <RouterLink to="/publish" class="group fixed bottom-32 right-8 z-40 flex h-16 w-16 items-center justify-center rounded-full bg-primary text-on-primary shadow-[0_12px_40px_rgba(0,52,57,0.15)] transition-all hover:scale-105 active:scale-95">
      <span class="material-symbols-outlined text-3xl">add</span>
      <span class="absolute right-full mr-4 whitespace-nowrap rounded-xl bg-teal-900 px-4 py-2 text-sm font-headline text-white opacity-0 transition-opacity group-hover:opacity-100">发布需求</span>
    </RouterLink>

    <AppBottomNav />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import AppBottomNav from '../components/AppBottomNav.vue'
import AppTopNav from '../components/AppTopNav.vue'
import { announcementApi, taskApi } from '../services/api'

const router = useRouter()
const categories = ['全部任务', '跑腿代办', '学习辅导']
const topicPreviewCategories = ['二手闲置', '恋爱交友', '打听求助', '兼职招聘']
const activeCategory = ref('全部任务')
const selectedTopicEntryCategory = ref('二手闲置')
const tasks = ref<any[]>([])
const announcements = ref<any[]>([])
const loading = ref(false)
const error = ref('')
const taskSection = ref<HTMLElement | null>(null)

const pinnedAnnouncements = computed(() => announcements.value.filter((item) => item.pinned).slice(0, 3))

const iconForCategory = (category: string) => {
  const iconMap: Record<string, string> = {
    全部任务: 'dashboard',
    跑腿代办: 'directions_run',
    学习辅导: 'school'
  }
  return iconMap[category] || 'widgets'
}

const mapTaskTypeToCategory = (task: any) => {
  if (task.category) return task.category
  const typeMap: Record<string, string> = {
    errand: '跑腿代办',
    study: '学习辅导',
    secondhand: '二手闲置',
    help: '打听求助',
    social: '恋爱交友',
    job: '兼职招聘',
    校园配送: '跑腿代办',
    学业辅导: '学习辅导',
    闲置交换: '二手闲置',
    信息求助: '打听求助',
    社交互助: '恋爱交友',
    兼职机会: '兼职招聘'
  }
  return typeMap[task.impactText || task.badgeSecondary || task.taskType] || '跑腿代办'
}

const inferTaskMode = (task: any) => {
  const category = mapTaskTypeToCategory(task)
  const resolvedTaskMode = ['跑腿代办', '学习辅导'].includes(category) ? 'task' : 'topic'
  return task.taskMode === resolvedTaskMode ? task.taskMode : resolvedTaskMode
}

const mapTaskToCard = (task: any) => ({
  id: task.id,
  category: mapTaskTypeToCategory(task),
  taskMode: inferTaskMode(task),
  status: task.status,
  expiresAt: task.expiresAt,
  badgePrimary: task.badgePrimary || '普通',
  title: task.title,
  description: task.description,
  rewardText: task.rewardText || task.rewardTitle || '待补充',
  locationText: task.locationText,
  timeText: task.timeText,
  likeCount: Number(task.likeCount || 0),
  commentCount: Number(task.commentCount || 0),
  publisher: task.requesterName || task.publisher || `用户 #${task.requesterId ?? ''}`
})

const fetchTasks = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await taskApi.getTasks() as any
    const rawTasks = Array.isArray(response) ? response : Array.isArray(response?.data) ? response.data : []
    tasks.value = rawTasks.map(mapTaskToCard)
  } catch (err: any) {
    error.value = err?.response?.data?.message || '获取任务列表失败，请稍后重试'
    tasks.value = []
  } finally {
    loading.value = false
  }
}

const fetchAnnouncements = async () => {
  try {
    const response = await announcementApi.getAnnouncements() as any
    const rows = Array.isArray(response) ? response : Array.isArray(response?.data) ? response.data : []
    announcements.value = rows
  } catch (err) {
    console.error('获取公告失败:', err)
    announcements.value = []
  }
}

const taskCards = computed(() => (
  tasks.value.filter((card) => card.taskMode === 'task' && (card.status === 'pending' || card.status === 'accepted'))
))

const filteredCards = computed(() => {
  if (activeCategory.value === '全部任务') return taskCards.value
  return taskCards.value.filter((card) => card.category === activeCategory.value)
})

const hotTopicRanking = computed(() => (
  tasks.value
    .filter((card) => card.taskMode === 'topic')
    .slice()
    .sort((a, b) => {
      const heatDiff = (Number(b.commentCount || 0) * 3 + Number(b.likeCount || 0) * 2)
        - (Number(a.commentCount || 0) * 3 + Number(a.likeCount || 0) * 2)
      if (heatDiff !== 0) return heatDiff
      return Number(b.id || 0) - Number(a.id || 0)
    })
    .slice(0, 10)
))

const scrollToTaskSection = async () => {
  await nextTick()
  taskSection.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const handleTaskEntry = async () => {
  activeCategory.value = '全部任务'
  await scrollToTaskSection()
}

const goToTopicSquare = (category = '全部话题') => {
  const query = category === '全部话题' ? {} : { category }
  router.push({
    path: '/topics',
    query
  })
}

const taskStatusText = (card: any) => {
  if (card.status === 'accepted') return '任务进行中'
  return '等待接单中'
}

const taskStatusBadge = (status?: string) => {
  if (status === 'accepted') return '任务进行中'
  return '待接单'
}

const formatAnnouncementTime = (value?: string) => {
  if (!value) return '刚刚'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '刚刚'
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  fetchTasks()
  fetchAnnouncements()
})
</script>
