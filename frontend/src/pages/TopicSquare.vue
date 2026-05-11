<template>
  <div class="min-h-screen bg-surface font-body text-on-surface">
    <AppTopNav />

    <main class="mx-auto max-w-7xl px-6 pb-32 pt-24">
      <section class="mb-10 overflow-hidden rounded-[2.75rem] bg-gradient-to-br from-teal-950 via-cyan-900 to-sky-700 p-8 text-white shadow-sm md:p-10">
        <div class="grid gap-8 xl:grid-cols-[1.05fr_0.95fr]">
          <div>
            <div class="mb-6 flex flex-wrap items-center gap-3">
              <RouterLink to="/home" class="rounded-full border border-white/15 bg-white/10 px-4 py-2 text-xs font-bold uppercase tracking-[0.22em] text-white transition-colors hover:bg-white/15">
                返回任务首页
              </RouterLink>
              <span class="rounded-full bg-white/10 px-4 py-2 text-xs font-bold uppercase tracking-[0.22em] text-cyan-50">话题帖独立页面</span>
            </div>

            <h1 class="max-w-4xl text-4xl font-extrabold leading-[1.08] tracking-tight md:text-6xl">
              社区话题广场
            </h1>
            <p class="mt-6 max-w-3xl text-lg leading-8 text-cyan-50/82">
              这里集中展示二手闲置、恋爱交友、打听求助和兼职招聘。学习辅导已进入任务接单区，更适合一对一协作。
            </p>
          </div>

          <div class="grid gap-4 sm:grid-cols-2">
            <div class="rounded-[1.8rem] bg-white/10 p-5 ring-1 ring-white/10">
              <p class="text-xs font-bold uppercase tracking-[0.22em] text-cyan-100/70">公开互动</p>
              <p class="mt-3 text-2xl font-extrabold">评论 / 回复</p>
              <p class="mt-2 text-sm leading-6 text-cyan-50/75">适合公开讨论、持续跟进和围绕内容补充信息。</p>
            </div>
            <div class="rounded-[1.8rem] bg-white/10 p-5 ring-1 ring-white/10">
              <p class="text-xs font-bold uppercase tracking-[0.22em] text-cyan-100/70">互动奖励</p>
              <p class="mt-3 text-3xl font-extrabold">+5</p>
              <p class="mt-2 text-sm leading-6 text-cyan-50/75">发布一条评论即可获得 5 积分，鼓励优质交流。</p>
            </div>
          </div>
        </div>
      </section>

      <section class="mb-8 space-y-4">
        <div class="flex flex-wrap gap-3">
          <button
            v-for="category in categories"
            :key="category"
            type="button"
            class="rounded-full px-5 py-2 text-sm font-semibold transition-all"
            :class="activeCategory === category ? 'scale-95 bg-primary text-white shadow-sm' : 'bg-surface-container-high text-on-surface-variant hover:bg-cyan-50/60'"
            @click="setActiveCategory(category)"
          >
            {{ category }}
          </button>
        </div>

        <label class="flex w-full items-center gap-3 rounded-[1.5rem] bg-surface-container-low px-4 py-3 text-sm text-on-surface shadow-sm lg:max-w-xl">
          <span class="material-symbols-outlined text-lg text-on-surface-variant">search</span>
          <input
            v-model.trim="keyword"
            type="text"
            class="min-w-0 flex-1 bg-transparent outline-none placeholder:text-on-surface-variant/60"
            placeholder="搜索标题、内容、分类、地点或发布者"
          />
          <button
            v-if="keyword"
            type="button"
            class="inline-flex h-8 w-8 items-center justify-center rounded-full text-on-surface-variant transition-colors hover:bg-surface-container-high hover:text-on-surface"
            aria-label="清空搜索"
            @click="clearKeyword"
          >
            <span class="material-symbols-outlined text-lg">close</span>
          </button>
        </label>
      </section>

      <section v-if="loading" class="rounded-[2rem] bg-surface-container-low p-10 text-center text-on-surface-variant">
        正在加载话题帖...
      </section>

      <section v-else-if="error" class="rounded-[2rem] border border-rose-200 bg-rose-50 p-10 text-center text-rose-700">
        {{ error }}
      </section>

      <section v-else-if="filteredTopics.length === 0" class="rounded-[2rem] bg-surface-container-low p-10 text-center">
        <div class="mx-auto mb-5 flex h-20 w-20 items-center justify-center rounded-full bg-surface-container-high">
          <span class="material-symbols-outlined text-4xl text-on-surface-variant">forum</span>
        </div>
        <h2 class="text-2xl font-bold text-teal-900">{{ emptyStateTitle }}</h2>
        <p class="mt-3 text-on-surface-variant">{{ emptyStateDescription }}</p>
      </section>

      <section v-else class="space-y-3">
        <article
          v-for="card in pagedTopics"
          :key="card.id"
          class="rounded-[1.25rem] border border-outline-variant/12 bg-surface-container-lowest p-4 shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md md:p-5"
        >
          <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div class="min-w-0 flex-1">
              <div class="mb-2 flex flex-wrap items-center gap-2">
                <span class="rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-[0.16em]" :class="topicCategoryBadgeClass(card.category)">
                  {{ card.category }}
                </span>
                <span class="rounded-full bg-surface-container-low px-3 py-1 text-[11px] font-bold uppercase tracking-[0.16em] text-on-surface-variant">话题帖</span>
                <span class="text-xs font-semibold text-on-surface-variant">{{ card.publisher }}</span>
              </div>

              <h2 class="truncate text-xl font-extrabold text-teal-950">
                {{ card.title }}
              </h2>
              <p class="mt-2 line-clamp-2 text-sm leading-6 text-on-surface-variant">
                {{ card.description }}
              </p>

              <div class="mt-3 flex flex-wrap items-center gap-3 text-xs font-semibold text-on-surface-variant">
                <span class="inline-flex items-center gap-1">
                  <span class="material-symbols-outlined text-sm">place</span>
                  {{ card.locationText || '待补充' }}
                </span>
                <span class="inline-flex items-center gap-1">
                  <span class="material-symbols-outlined text-sm">schedule</span>
                  {{ card.timeText || '待补充' }}
                </span>
                <span class="inline-flex items-center gap-1">
                  <span class="material-symbols-outlined text-sm">favorite</span>
                  {{ card.likeCount || 0 }}
                </span>
                <span class="inline-flex items-center gap-1">
                  <span class="material-symbols-outlined text-sm">chat_bubble</span>
                  {{ card.commentCount || 0 }}
                </span>
              </div>
            </div>

            <RouterLink
              :to="`/detail/${card.id}`"
              class="inline-flex shrink-0 items-center justify-center gap-2 rounded-full bg-teal-900 px-5 py-3 text-sm font-bold text-white transition-all hover:bg-teal-800"
            >
              进入帖子
              <span class="material-symbols-outlined text-base">arrow_forward</span>
            </RouterLink>
          </div>
        </article>

        <div v-if="totalPages > 1" class="flex flex-wrap items-center justify-between gap-3 pt-5">
          <p class="text-sm font-semibold text-on-surface-variant">
            第 {{ currentPage }} / {{ totalPages }} 页，共 {{ filteredTopics.length }} 条
          </p>
          <div class="flex items-center gap-2">
            <button type="button" class="rounded-full bg-surface-container-low px-4 py-2 text-sm font-bold text-teal-900 disabled:opacity-50" :disabled="currentPage === 1" @click="currentPage -= 1">
              上一页
            </button>
            <button type="button" class="rounded-full bg-teal-900 px-4 py-2 text-sm font-bold text-white disabled:opacity-50" :disabled="currentPage === totalPages" @click="currentPage += 1">
              下一页
            </button>
          </div>
        </div>
      </section>
    </main>

    <RouterLink to="/publish" class="group fixed bottom-32 right-8 z-40 flex h-16 w-16 items-center justify-center rounded-full bg-primary text-on-primary shadow-[0_12px_40px_rgba(0,52,57,0.15)] transition-all hover:scale-105 active:scale-95">
      <span class="material-symbols-outlined text-3xl">add</span>
      <span class="absolute right-full mr-4 whitespace-nowrap rounded-xl bg-teal-900 px-4 py-2 text-sm font-headline text-white opacity-0 transition-opacity group-hover:opacity-100">发布话题帖</span>
    </RouterLink>

    <AppBottomNav />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import AppBottomNav from '../components/AppBottomNav.vue'
import AppTopNav from '../components/AppTopNav.vue'
import { taskApi } from '../services/api'

const route = useRoute()
const router = useRouter()
const categories = ['全部话题', '二手闲置', '恋爱交友', '打听求助', '兼职招聘']
const activeCategory = ref('全部话题')
const keyword = ref('')
const topics = ref<any[]>([])
const loading = ref(false)
const error = ref('')
const currentPage = ref(1)
const pageSize = 8

const mapTaskTypeToCategory = (task: any) => {
  if (task.category) return task.category
  const typeMap: Record<string, string> = {
    study: '学习辅导',
    secondhand: '二手闲置',
    help: '打听求助',
    social: '恋爱交友',
    job: '兼职招聘',
    学业辅导: '学习辅导',
    闲置交换: '二手闲置',
    信息求助: '打听求助',
    社交互助: '恋爱交友',
    兼职机会: '兼职招聘'
  }
  return typeMap[task.impactText || task.badgeSecondary || task.taskType] || '二手闲置'
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
  expiresAt: task.expiresAt,
  title: task.title,
  description: task.description,
  rewardText: task.rewardText || task.rewardTitle || '待补充',
  locationText: task.locationText,
  timeText: task.timeText,
  likeCount: Number(task.likeCount || 0),
  commentCount: Number(task.commentCount || 0),
  publisher: task.requesterName || task.publisher || `用户 #${task.requesterId ?? ''}`
})

const applyRouteCategory = () => {
  const nextCategory = String(route.query.category || '')
  activeCategory.value = categories.includes(nextCategory) ? nextCategory : '全部话题'
}

const applyRouteKeyword = () => {
  keyword.value = String(route.query.keyword || '').trim()
}

const updateTopicQuery = () => {
  const nextQuery: Record<string, string> = {}
  if (activeCategory.value !== '全部话题') {
    nextQuery.category = activeCategory.value
  }
  if (keyword.value) {
    nextQuery.keyword = keyword.value
  }

  const currentCategory = String(route.query.category || '')
  const currentKeyword = String(route.query.keyword || '').trim()
  if (currentCategory === (nextQuery.category || '') && currentKeyword === (nextQuery.keyword || '')) {
    return
  }

  router.replace({
    path: route.path,
    query: nextQuery
  })
}

const fetchTopics = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await taskApi.getTasks() as any
    const rawTasks = Array.isArray(response) ? response : Array.isArray(response?.data) ? response.data : []
    topics.value = rawTasks
      .map(mapTaskToCard)
      .filter((card: any) => card.taskMode === 'topic')
  } catch (err: any) {
    error.value = err?.response?.data?.message || '获取话题帖失败，请稍后重试'
    topics.value = []
  } finally {
    loading.value = false
  }
}

const filteredTopics = computed(() => {
  const normalizedKeyword = keyword.value.trim().toLowerCase()

  return topics.value.filter((card: any) => {
    const categoryMatched = activeCategory.value === '全部话题' || card.category === activeCategory.value
    if (!categoryMatched) return false
    if (!normalizedKeyword) return true

    const searchText = [
      card.title,
      card.description,
      card.category,
      card.locationText,
      card.publisher
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()

    return searchText.includes(normalizedKeyword)
  })
})

const totalPages = computed(() => Math.max(Math.ceil(filteredTopics.value.length / pageSize), 1))

const pagedTopics = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredTopics.value.slice(start, start + pageSize)
})

const setActiveCategory = (category: string) => {
  activeCategory.value = category
  currentPage.value = 1
}

const clearKeyword = () => {
  keyword.value = ''
  currentPage.value = 1
}

const emptyStateTitle = computed(() => (
  keyword.value ? '没有找到匹配的话题帖' : '这个分类还没有话题帖'
))

const emptyStateDescription = computed(() => (
  keyword.value
    ? '试试更换关键词、切换分类，或者直接发布一条新帖子。'
    : '可以切换别的分类，或者直接发布一条新帖子。'
))

const topicCategoryBadgeClass = (category: string) => {
  const map: Record<string, string> = {
    二手闲置: 'bg-amber-100 text-amber-800',
    恋爱交友: 'bg-rose-100 text-rose-700',
    打听求助: 'bg-sky-100 text-sky-700',
    兼职招聘: 'bg-emerald-100 text-emerald-700'
  }
  return map[category] || 'bg-slate-100 text-slate-700'
}

watch(() => route.query.category, applyRouteCategory)
watch(() => route.query.keyword, applyRouteKeyword)
watch([activeCategory, keyword], updateTopicQuery)
watch(filteredTopics, () => {
  if (currentPage.value > totalPages.value) {
    currentPage.value = totalPages.value
  }
})

onMounted(async () => {
  applyRouteCategory()
  applyRouteKeyword()
  await fetchTopics()
})
</script>
