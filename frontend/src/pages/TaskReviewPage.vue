<template>
  <div class="min-h-screen bg-background pb-24 text-on-background md:pb-0">
    <AppTopNav :avatar-url="currentUser.avatarUrl || defaultAvatarUrl" />

    <main class="mx-auto max-w-5xl px-6 pb-12 pt-24">
      <RouterLink
        :to="`/detail/${props.id}`"
        class="mb-8 inline-flex items-center gap-2 font-medium text-on-surface-variant transition-colors hover:text-primary"
      >
        <span class="material-symbols-outlined text-lg">arrow_back</span>
        返回需求详情
      </RouterLink>

      <div
        v-if="feedbackMessage"
        class="mb-6 rounded-2xl border px-4 py-3 text-sm font-medium"
        :class="feedbackType === 'success' ? 'border-emerald-200 bg-emerald-50 text-emerald-700' : 'border-rose-200 bg-rose-50 text-rose-700'"
      >
        {{ feedbackMessage }}
      </div>

      <section class="rounded-[2.25rem] bg-surface-container-lowest p-8 shadow-sm">
        <div class="flex flex-col gap-5 lg:flex-row lg:items-start lg:justify-between">
          <div>
            <p class="text-sm font-bold uppercase tracking-[0.22em] text-on-surface-variant">任务互评</p>
            <h1 class="mt-3 text-4xl font-extrabold tracking-tight text-headline">{{ request.title || '任务评价' }}</h1>
            <p class="mt-4 max-w-2xl text-base leading-8 text-on-surface-variant">
              双方完成任务后，都可以在这里单独提交评价，评价会影响信用分并沉淀到个人资料页。
            </p>
          </div>
          <div class="grid gap-3 sm:grid-cols-3 lg:w-[22rem] lg:grid-cols-1">
            <div class="rounded-3xl bg-amber-50 px-5 py-4">
              <p class="text-xs font-bold uppercase tracking-[0.18em] text-amber-700/70">任务状态</p>
              <p class="mt-2 text-lg font-extrabold text-amber-800">{{ reviewStatusText }}</p>
            </div>
            <div class="rounded-3xl bg-sky-50 px-5 py-4">
              <p class="text-xs font-bold uppercase tracking-[0.18em] text-sky-700/70">评价对象</p>
              <p class="mt-2 text-lg font-extrabold text-sky-800">{{ counterpartName }}</p>
            </div>
            <div class="rounded-3xl bg-emerald-50 px-5 py-4">
              <p class="text-xs font-bold uppercase tracking-[0.18em] text-emerald-700/70">已收到评价</p>
              <p class="mt-2 text-lg font-extrabold text-emerald-800">{{ reviews.length }} 条</p>
            </div>
          </div>
        </div>
      </section>

      <section class="mt-8 grid gap-8 lg:grid-cols-[1.1fr_0.9fr]">
        <article class="rounded-[2rem] bg-surface-container-lowest p-8 shadow-sm">
          <div class="mb-5">
            <p class="text-sm font-bold uppercase tracking-widest text-on-surface-variant">提交评价</p>
            <h2 class="mt-2 text-2xl font-extrabold text-teal-900">你的评价</h2>
          </div>

          <div v-if="canReviewTask" class="rounded-3xl border border-outline-variant/15 bg-surface-container-low p-5">
            <p class="text-sm font-bold text-on-surface">评价对象：{{ counterpartName }}</p>
            <div class="mt-4 flex flex-wrap items-center gap-3">
              <button
                v-for="star in 5"
                :key="star"
                type="button"
                class="flex h-12 w-12 items-center justify-center rounded-2xl border transition-all"
                :class="selectedRating >= star
                  ? 'border-amber-300 bg-amber-100 text-amber-600 shadow-sm'
                  : 'border-outline-variant/20 bg-white text-on-surface-variant hover:border-amber-200 hover:text-amber-500'"
                @click="setRating(star)"
              >
                <span
                  class="material-symbols-outlined text-2xl"
                  style="font-variation-settings:'FILL' 1;"
                >star</span>
              </button>
              <div class="rounded-2xl bg-white px-4 py-3 text-sm">
                <p class="font-bold text-teal-900">{{ selectedRating }} 星</p>
                <p class="mt-1 text-on-surface-variant">{{ ratingHint }}</p>
              </div>
            </div>
            <div class="mt-4 grid gap-3 md:grid-cols-2">
              <div class="rounded-2xl bg-emerald-50 px-4 py-4 text-sm text-emerald-900">
                <p class="font-bold">评价提交奖励</p>
                <p class="mt-1 leading-6">你提交本次评价后会获得 {{ reviewerRewardPoints }} 积分，鼓励双方完成闭环反馈。</p>
              </div>
              <div class="rounded-2xl bg-amber-50 px-4 py-4 text-sm text-amber-900">
                <p class="font-bold">星级积分影响</p>
                <p class="mt-1 leading-6">本次 {{ selectedRating }} 星会给对方结算 {{ revieweeRewardPointsLabel }}，同时继续更新信用分。</p>
              </div>
            </div>
            <textarea
              v-model="reviewForm.content"
              class="mt-4 min-h-32 w-full rounded-3xl border border-outline-variant/20 bg-white px-5 py-4 text-on-surface outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
              placeholder="写下这次协作体验，比如沟通效率、履约情况、完成质量..."
            ></textarea>
            <button
              type="button"
              class="mt-4 w-full rounded-2xl bg-gradient-to-br from-primary to-primary-dim px-6 py-4 font-bold text-on-primary shadow-lg shadow-primary/20 transition-all hover:scale-[1.01] active:scale-95 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:scale-100"
              :disabled="reviewLoading"
              @click="submitReview"
            >
              {{ reviewLoading ? '提交中...' : '提交评价' }}
            </button>
          </div>

          <div v-else-if="currentUserReview" class="rounded-3xl bg-emerald-50 px-5 py-4 text-emerald-800">
            <p class="text-sm font-bold">你已完成评价</p>
            <p class="mt-2 text-sm">你的评分：{{ currentUserReview.rating }} 星</p>
            <p class="mt-2 text-sm leading-7">{{ currentUserReview.content || '未填写文字评价。' }}</p>
          </div>

          <div v-else class="rounded-3xl bg-surface-container-low px-5 py-4 text-on-surface-variant">
            {{ reviewBlockedReason }}
          </div>
        </article>

        <article class="rounded-[2rem] bg-surface-container-lowest p-8 shadow-sm">
          <div class="mb-5">
            <p class="text-sm font-bold uppercase tracking-widest text-on-surface-variant">评价概览</p>
            <h2 class="mt-2 text-2xl font-extrabold text-teal-900">双方互评情况</h2>
          </div>

          <div v-if="reviews.length > 0" class="grid gap-3 md:grid-cols-3 lg:grid-cols-1">
            <div class="rounded-3xl bg-amber-50 px-5 py-4">
              <p class="text-xs font-bold uppercase tracking-[0.18em] text-amber-700/70">平均评分</p>
              <p class="mt-2 text-3xl font-extrabold text-amber-800">{{ reviewAverageText }}</p>
            </div>
            <div class="rounded-3xl bg-sky-50 px-5 py-4">
              <p class="text-xs font-bold uppercase tracking-[0.18em] text-sky-700/70">评价总数</p>
              <p class="mt-2 text-3xl font-extrabold text-sky-800">{{ reviews.length }}</p>
            </div>
            <div class="rounded-3xl bg-emerald-50 px-5 py-4">
              <p class="text-xs font-bold uppercase tracking-[0.18em] text-emerald-700/70">当前状态</p>
              <p class="mt-2 text-lg font-extrabold text-emerald-800">{{ reviewSentimentLabel }}</p>
            </div>
          </div>

          <div v-if="reviewsLoading" class="mt-5 rounded-3xl bg-surface-container-low px-5 py-6 text-center text-on-surface-variant">
            正在加载评价...
          </div>
          <div v-else-if="reviews.length === 0" class="mt-5 rounded-3xl bg-surface-container-low px-5 py-6 text-center text-on-surface-variant">
            当前还没有评价，双方提交后会显示在这里。
          </div>
          <div v-else class="mt-5 space-y-4">
            <div
              v-for="review in reviews"
              :key="review.id"
              class="rounded-3xl border border-outline-variant/12 bg-white px-5 py-5 shadow-sm"
            >
              <div class="flex items-start justify-between gap-4">
                <div>
                  <p class="font-bold text-on-surface">{{ review.reviewerName || `用户 #${review.reviewerId}` }}</p>
                  <p class="mt-1 text-xs text-on-surface-variant">{{ review.reviewerRole === 'requester' ? '需求方评价' : '服务方评价' }}</p>
                </div>
                <div class="rounded-full bg-amber-50 px-4 py-2 text-sm font-bold text-amber-700">
                  {{ review.rating }} 星
                </div>
              </div>
              <p class="mt-4 whitespace-pre-wrap text-sm leading-7 text-on-surface-variant">{{ review.content || '对方未填写文字评价。' }}</p>
            </div>
          </div>
        </article>
      </section>
    </main>

    <AppBottomNav />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import AppBottomNav from '../components/AppBottomNav.vue'
import AppTopNav from '../components/AppTopNav.vue'
import { taskApi, userApi } from '../services/api'

type FeedbackType = 'success' | 'error'

const props = defineProps<{ id: string }>()

const defaultAvatarUrl = 'https://lh3.googleusercontent.com/aida-public/AB6AXuDeXWwurmf7TZNlFaxpQ4N9cUqjIOp0LS96VhYcYf185KeqTd4xoDC5zDnZXuyz0rPWpKhC4ba_hynr8lnO8q6p3XV7x3xNlMa2DSut8QJvZfUVM2qf5PC2-N0AYVms42RiY4_P94jUh4mT59Hebcq7dghdwvFEuHsNZnEE-dIvmt6o_lbkR6PbC5eBwdRyiJQDuuP4OpAAMsyQrL-AHsU8Gt5aUDTvEzoe_LNthLkmawY2jV4fB5Kx0E0sooeg65eOCnt0Ldnv1HnC'
const currentUser = ref<any>(JSON.parse(localStorage.getItem('user') || '{}'))
const request = ref<any>({})
const reviews = ref<any[]>([])
const reviewsLoading = ref(false)
const reviewLoading = ref(false)
const reviewForm = ref({ rating: 5, content: '' })
const selectedRating = ref(5)
const feedbackMessage = ref('')
const feedbackType = ref<FeedbackType>('success')
const reviewerRewardPoints = 3

const counterpartName = computed(() => {
  const currentUserId = Number(currentUser.value?.id)
  const requesterId = Number(request.value?.requesterId)
  return currentUserId === requesterId
    ? request.value?.helperName || '接单同学'
    : request.value?.requesterName || '需求方'
})

const counterpartId = computed(() => {
  const currentUserId = Number(currentUser.value?.id)
  const requesterId = Number(request.value?.requesterId)
  return currentUserId === requesterId
    ? Number(request.value?.helperId || 0)
    : Number(request.value?.requesterId || 0)
})

const canReviewTask = computed(() => {
  if (String(request.value?.taskMode || 'task') !== 'task') return false
  if (request.value?.status !== 'completed') return false
  if (!counterpartId.value) return false
  return !reviews.value.some((review) => Number(review.reviewerId) === Number(currentUser.value?.id))
})

const currentUserReview = computed(() => (
  reviews.value.find((review) => Number(review.reviewerId) === Number(currentUser.value?.id)) || null
))

const reviewAverage = computed(() => {
  if (!reviews.value.length) return 0
  const total = reviews.value.reduce((sum, review) => sum + Number(review.rating || 0), 0)
  return total / reviews.value.length
})

const reviewAverageText = computed(() => reviewAverage.value ? reviewAverage.value.toFixed(1) : '0.0')

const reviewSentimentLabel = computed(() => {
  if (!reviews.value.length) return '待评价'
  if (reviewAverage.value >= 4.5) return '表现优秀'
  if (reviewAverage.value >= 4.0) return '表现稳定'
  if (reviewAverage.value >= 3.0) return '仍可提升'
  return '需重点改进'
})

const reviewStatusText = computed(() => {
  if (request.value?.status === 'completed') return '已完成，可互评'
  if (request.value?.status === 'completion_pending') return '待双方确认'
  if (request.value?.status === 'accepted') return '进行中，暂不可评'
  return '当前不可评'
})

const reviewBlockedReason = computed(() => {
  if (request.value?.status !== 'completed') {
    return '任务尚未正式完成，需双方确认完成后才可以进入互评。'
  }
  if (!counterpartId.value) {
    return '当前还没有可评价对象，通常是因为任务接单信息尚未完成同步。'
  }
  return '你已经完成评价，当前只保留查看结果。'
})

const ratingHint = computed(() => {
  const labelMap: Record<number, string> = {
    1: '严重不符合预期，会触发扣分。',
    2: '体验较差，不加分但会保留记录。',
    3: '基本完成，给予少量积分。',
    4: '协作顺畅，会获得正常积分奖励。',
    5: '表现优秀，会获得最高积分奖励。'
  }
  return labelMap[selectedRating.value] || '请选择评分。'
})

const revieweeRewardPoints = computed(() => {
  const rewardMap: Record<number, number> = {
    1: -3,
    2: 0,
    3: 2,
    4: 5,
    5: 8
  }
  return rewardMap[selectedRating.value] ?? 0
})

const revieweeRewardPointsLabel = computed(() => (
  revieweeRewardPoints.value > 0
    ? `+${revieweeRewardPoints.value} 积分`
    : revieweeRewardPoints.value < 0
      ? `${revieweeRewardPoints.value} 积分`
      : '0 积分'
))

const setFeedback = (message: string, type: FeedbackType) => {
  feedbackMessage.value = message
  feedbackType.value = type
}

const setRating = (star: number) => {
  selectedRating.value = star
  reviewForm.value = {
    ...reviewForm.value,
    rating: star
  }
}

const refreshCurrentUser = async () => {
  try {
    const response = await userApi.getCurrentUser() as any
    const latestUser = response?.data ?? response ?? null
    if (latestUser) {
      currentUser.value = latestUser
      localStorage.setItem('user', JSON.stringify(latestUser))
    }
  } catch (error) {
    console.error('刷新当前用户积分失败:', error)
  }
}

const inferCategory = (task: any) => {
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
  return typeMap[task?.impactText || task?.badgeSecondary || task?.taskType] || '跑腿代办'
}

const normalizeTask = (task: any) => {
  const category = task?.category || inferCategory(task)
  const resolvedTaskMode = ['跑腿代办', '学习辅导'].includes(category) ? 'task' : 'topic'
  return {
    ...task,
    category,
    taskMode: task?.taskMode === resolvedTaskMode ? task.taskMode : resolvedTaskMode
  }
}

const fetchTaskDetail = async () => {
  try {
    const response = await taskApi.getTaskById(Number(props.id)) as any
    request.value = normalizeTask(response?.data ?? response ?? {})
  } catch (error: any) {
    console.error('获取互评页面任务详情失败:', error)
    request.value = {}
    setFeedback(error?.response?.data?.message || '任务详情加载失败，请稍后重试。', 'error')
  }
}

const fetchReviews = async () => {
  reviewsLoading.value = true
  try {
    const response = await taskApi.getTaskReviews(Number(props.id)) as any
    reviews.value = Array.isArray(response) ? response : Array.isArray(response?.data) ? response.data : []
  } catch (error: any) {
    console.error('获取评价失败:', error)
    reviews.value = []
    setFeedback(error?.response?.data?.message || '评价信息加载失败，请稍后刷新重试。', 'error')
  } finally {
    reviewsLoading.value = false
  }
}

const submitReview = async () => {
  if (!canReviewTask.value) {
    setFeedback('当前状态暂时不能提交评价。', 'error')
    return
  }
  reviewLoading.value = true
  try {
    const submittedRating = Number(selectedRating.value)
    await taskApi.createTaskReview(Number(props.id), {
      rating: submittedRating,
      content: reviewForm.value.content.trim()
    })
    await refreshCurrentUser()
    reviewForm.value = { rating: 5, content: '' }
    selectedRating.value = 5
    setFeedback(`评价已提交。你获得 ${reviewerRewardPoints} 积分，对方将按 ${submittedRating} 星规则结算积分与信用分。`, 'success')
    await fetchReviews()
  } catch (error: any) {
    console.error('提交评价失败:', error)
    setFeedback(error?.response?.data?.message || '提交评价失败，请稍后重试。', 'error')
  } finally {
    reviewLoading.value = false
  }
}

onMounted(async () => {
  await refreshCurrentUser()
  await fetchTaskDetail()
  await fetchReviews()
  setRating(5)
})
</script>
