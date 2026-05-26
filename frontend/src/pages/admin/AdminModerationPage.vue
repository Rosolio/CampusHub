<template>
  <div class="space-y-5">
    <section v-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 px-5 py-4 text-sm font-semibold text-rose-700">
      {{ error }}
    </section>

    <section class="grid gap-4 md:grid-cols-4">
      <article v-for="card in summaryCards" :key="card.label" class="admin-panel p-5">
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

    <section class="admin-panel p-5 md:p-6">
      <div class="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
        <div class="flex-1 space-y-4">
          <div>
            <p class="admin-kicker">内容审核</p>
            <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">内容审核台</h2>
          </div>

          <div class="flex flex-wrap gap-2">
            <button
              v-for="tab in reviewTabs"
              :key="tab.value"
              type="button"
              class="rounded-xl px-4 py-2 text-sm font-bold transition"
              :class="activeReviewTab === tab.value ? 'bg-slate-950 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200 hover:text-slate-900'"
              @click="activeReviewTab = tab.value"
            >
              {{ tab.label }} {{ tab.count }}
            </button>
          </div>
        </div>

        <div class="grid gap-3 sm:grid-cols-[minmax(0,1fr)_auto] xl:w-[28rem]">
          <div class="rounded-2xl border border-slate-200 bg-white px-4 py-3">
            <label class="block text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">搜索</label>
            <input
              v-model.trim="keyword"
              type="text"
              class="mt-2 w-full border-0 bg-transparent text-sm font-semibold text-slate-800 outline-none placeholder:text-slate-400"
              placeholder="搜索标题、正文、分类或发布者"
            />
          </div>
          <button
            type="button"
            class="inline-flex items-center justify-center gap-2 rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm font-bold text-slate-700 transition hover:bg-slate-50"
            @click="loadTasks"
          >
            <span class="material-symbols-outlined text-lg">refresh</span>
            刷新
          </button>
        </div>
      </div>

      <div v-if="filteredTasks.length === 0" class="mt-6 rounded-3xl border border-dashed border-slate-300 bg-slate-50 px-5 py-10 text-center">
        <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-white text-slate-500">
          <span class="material-symbols-outlined">fact_check</span>
        </div>
        <p class="mt-4 text-lg font-extrabold text-slate-900">当前没有匹配的审核内容</p>
        <p class="mt-2 text-sm font-medium text-slate-500">可以切换审核状态或调整搜索关键词继续查看。</p>
      </div>

      <div v-else class="mt-6 grid gap-4">
        <article
          v-for="task in filteredTasks"
          :key="task.id"
          class="overflow-hidden rounded-3xl border border-slate-200 bg-white transition hover:border-slate-300"
        >
          <div class="grid gap-0 xl:grid-cols-[minmax(0,1fr)_17rem]">
            <div class="p-4 md:p-5">
              <div class="flex flex-wrap items-center gap-2">
                <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-extrabold uppercase tracking-[0.14em] text-slate-700">
                  {{ taskModeLabel(task.taskMode) }}
                </span>
                <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-extrabold uppercase tracking-[0.14em] text-slate-700">
                  {{ task.category || '未分类' }}
                </span>
                <span class="rounded-full px-3 py-1 text-xs font-extrabold uppercase tracking-[0.14em]" :class="reviewBadgeClass(task.reviewStatus)">
                  {{ reviewLabel(task.reviewStatus) }}
                </span>
              </div>

              <h3 class="mt-4 text-xl font-extrabold leading-tight text-slate-900">{{ task.title }}</h3>
              <p class="mt-3 text-sm leading-7 text-slate-600">{{ task.description }}</p>

              <div class="mt-4 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
                <div class="rounded-2xl bg-slate-50 px-4 py-3">
                  <p class="text-[11px] font-extrabold uppercase tracking-[0.16em] text-slate-400">发布者</p>
                  <p class="mt-2 text-sm font-bold text-slate-800">{{ task.requesterName || `用户 #${task.requesterId}` }}</p>
                </div>
                <div class="rounded-2xl bg-slate-50 px-4 py-3">
                  <p class="text-[11px] font-extrabold uppercase tracking-[0.16em] text-slate-400">内容状态</p>
                  <p class="mt-2 text-sm font-bold text-slate-800">{{ task.status || 'pending' }}</p>
                </div>
                <div class="rounded-2xl bg-slate-50 px-4 py-3">
                  <p class="text-[11px] font-extrabold uppercase tracking-[0.16em] text-slate-400">评论数</p>
                  <p class="mt-2 text-sm font-bold text-slate-800">{{ task.commentCount ?? 0 }}</p>
                </div>
                <div class="rounded-2xl bg-slate-50 px-4 py-3">
                  <p class="text-[11px] font-extrabold uppercase tracking-[0.16em] text-slate-400">发布时间</p>
                  <p class="mt-2 text-sm font-bold text-slate-800">{{ formatTime(task.createdAt) }}</p>
                </div>
              </div>

              <div class="mt-4">
                <label class="block text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">审核备注</label>
                <textarea
                  v-model.trim="reviewNotes[task.id]"
                  rows="3"
                  class="mt-3 w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-slate-400"
                  placeholder="给发布者留下明确说明，驳回时建议写清需要修改的地方。"
                ></textarea>
              </div>

              <p v-if="task.reviewNote" class="mt-4 rounded-2xl bg-amber-50 px-4 py-3 text-sm font-semibold text-amber-700">
                上次备注：{{ task.reviewNote }}
              </p>
            </div>

            <div class="border-t border-slate-200 bg-slate-50 p-4 xl:border-l xl:border-t-0">
              <p class="text-[11px] font-extrabold uppercase tracking-[0.22em] text-slate-400">处理动作</p>
              <div class="mt-4 grid gap-2.5">
                <button
                  type="button"
                  class="rounded-xl bg-slate-950 px-4 py-3 text-sm font-extrabold text-white transition hover:bg-slate-800 disabled:opacity-45"
                  :disabled="pendingTaskIds.has(task.id)"
                  @click="handleReview(task, 'approved')"
                >
                  {{ pendingTaskIds.has(task.id) ? '处理中...' : '通过' }}
                </button>
                <button
                  type="button"
                  class="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-extrabold text-rose-700 transition hover:bg-rose-100 disabled:opacity-45"
                  :disabled="pendingTaskIds.has(task.id)"
                  @click="handleReview(task, 'rejected')"
                >
                  {{ pendingTaskIds.has(task.id) ? '处理中...' : '驳回' }}
                </button>
                <button
                  type="button"
                  class="rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm font-extrabold text-slate-700 transition hover:bg-slate-100 disabled:opacity-45"
                  :disabled="pendingTaskIds.has(task.id)"
                  @click="handleReview(task, 'pending_review')"
                >
                  {{ pendingTaskIds.has(task.id) ? '处理中...' : '退回待审' }}
                </button>
              </div>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useConfirm } from '../../composables/useConfirm'
import { showToast } from '../../composables/useToast'
import { adminApi } from '../../services/api'
import type { AdminTask } from './adminTypes'

type ReviewStatus = 'pending_review' | 'approved' | 'rejected'
type ReviewTab = ReviewStatus | 'all'

const error = ref('')
const tasks = ref<AdminTask[]>([])
const pendingTaskIds = ref(new Set<number>())
const keyword = ref('')
const activeReviewTab = ref<ReviewTab>('pending_review')
const reviewNotes = ref<Record<number, string>>({})
const { openConfirm } = useConfirm()

const reviewTabs = computed(() => {
  const counts = {
    all: tasks.value.length,
    pending_review: tasks.value.filter((task) => task.reviewStatus === 'pending_review').length,
    approved: tasks.value.filter((task) => task.reviewStatus === 'approved').length,
    rejected: tasks.value.filter((task) => task.reviewStatus === 'rejected').length
  }

  return [
    { value: 'pending_review' as ReviewTab, label: '待审核', count: counts.pending_review },
    { value: 'rejected' as ReviewTab, label: '已驳回', count: counts.rejected },
    { value: 'approved' as ReviewTab, label: '已通过', count: counts.approved },
    { value: 'all' as ReviewTab, label: '全部', count: counts.all }
  ]
})

const summaryCards = computed(() => [
  { label: '待审核', value: reviewTabs.value[0].count, hint: '优先处理自动命中审核规则或重新提交的内容。', icon: 'fact_check' },
  { label: '已驳回', value: reviewTabs.value[1].count, hint: '检查驳回备注是否足够明确、便于用户修改。', icon: 'block' },
  { label: '已通过', value: reviewTabs.value[2].count, hint: '已通过内容会进入公开社区流。', icon: 'verified' },
  { label: '全部内容', value: reviewTabs.value[3].count, hint: '包含任务帖和话题帖的完整审核视图。', icon: 'library_books' }
])

const filteredTasks = computed(() => {
  const query = keyword.value.toLowerCase()

  return tasks.value.filter((task) => {
    const matchesTab = activeReviewTab.value === 'all' || task.reviewStatus === activeReviewTab.value
    if (!matchesTab) {
      return false
    }

    if (!query) {
      return true
    }

    return [
      task.title,
      task.description,
      task.category,
      task.requesterName,
      task.status,
      task.taskMode,
      String(task.requesterId)
    ].some((value) => String(value || '').toLowerCase().includes(query))
  })
})

const syncReviewNotes = (list: AdminTask[]) => {
  reviewNotes.value = list.reduce<Record<number, string>>((acc, task) => {
    acc[task.id] = reviewNotes.value[task.id] ?? task.reviewNote ?? ''
    return acc
  }, {})
}

const loadTasks = async () => {
  error.value = ''
  try {
    const response = await adminApi.getTasks() as unknown as AdminTask[]
    tasks.value = Array.isArray(response) ? response : []
    syncReviewNotes(tasks.value)
  } catch (err: any) {
    error.value = err?.response?.data?.message || '审核数据加载失败'
  }
}

const handleReview = async (task: AdminTask, reviewStatus: ReviewStatus) => {
  const reviewNote = reviewNotes.value[task.id] || ''

  const confirmed = await openConfirm({
    title: confirmTitle(reviewStatus),
    message: confirmMessage(task, reviewStatus, reviewNote),
    confirmText: confirmActionText(reviewStatus),
    tone: reviewStatus === 'rejected' ? 'danger' : 'default'
  })

  if (!confirmed) {
    return
  }

  pendingTaskIds.value.add(task.id)
  error.value = ''

  try {
    const updated = await adminApi.reviewTask(task.id, {
      reviewStatus,
      reviewNote
    }) as unknown as AdminTask

    tasks.value = tasks.value.map((item) => item.id === task.id ? { ...item, ...updated } : item)
    reviewNotes.value[task.id] = updated.reviewNote || reviewNote
    showToast(successMessage(reviewStatus), 'success')
  } catch (err: any) {
    error.value = err?.response?.data?.message || '审核操作失败'
  } finally {
    pendingTaskIds.value.delete(task.id)
  }
}

const confirmTitle = (reviewStatus: ReviewStatus) => {
  if (reviewStatus === 'approved') return '确认通过这条内容？'
  if (reviewStatus === 'rejected') return '确认驳回这条内容？'
  return '确认将这条内容退回待审核队列？'
}

const confirmActionText = (reviewStatus: ReviewStatus) => {
  if (reviewStatus === 'approved') return '通过'
  if (reviewStatus === 'rejected') return '驳回'
  return '退回待审'
}

const confirmMessage = (task: AdminTask, reviewStatus: ReviewStatus, reviewNote: string) => {
  const noteLine = reviewNote ? `审核备注：${reviewNote}` : '当前还没有填写审核备注。'
  if (reviewStatus === 'approved') {
    return `《${task.title}》通过后会进入公开社区流。\n${noteLine}`
  }
  if (reviewStatus === 'rejected') {
    return `《${task.title}》驳回后将不会在公开社区展示。\n${noteLine}`
  }
  return `《${task.title}》会重新进入待审核队列。\n${noteLine}`
}

const successMessage = (reviewStatus: ReviewStatus) => {
  if (reviewStatus === 'approved') return '内容已通过审核'
  if (reviewStatus === 'rejected') return '内容已驳回'
  return '内容已退回待审核队列'
}

const reviewLabel = (value?: string) => {
  if (value === 'approved') return '已通过'
  if (value === 'rejected') return '已驳回'
  return '待审核'
}

const reviewBadgeClass = (value?: string) => {
  if (value === 'approved') return 'bg-emerald-100 text-emerald-800'
  if (value === 'rejected') return 'bg-rose-100 text-rose-700'
  return 'bg-amber-100 text-amber-800'
}

const taskModeLabel = (value?: string) => value === 'topic' ? '话题帖' : '任务帖'

const formatTime = (value?: string) => {
  if (!value) return '刚刚'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '刚刚'
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  loadTasks()
})
</script>
