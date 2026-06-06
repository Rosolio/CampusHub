<template>
  <div class="min-h-screen bg-surface pb-24 font-body text-on-surface md:pb-0">

    <main class="mx-auto max-w-5xl px-6 pb-12 pt-24">
      <PageBackHeader to="/home" label="返回社区" />

      <section class="mt-6 overflow-hidden rounded-[2.2rem] bg-[linear-gradient(135deg,#0f3c44_0%,#0d5d68_55%,#66b7ac_100%)] p-7 text-white shadow-[0_24px_70px_rgba(15,23,42,0.12)]">
        <p class="text-[11px] font-extrabold uppercase tracking-[0.28em] text-white/70">Community Feedback</p>
        <h1 class="mt-4 text-4xl font-extrabold tracking-[-0.04em]">向管理员反馈问题或争议</h1>
        <p class="mt-4 max-w-2xl text-sm leading-7 text-white/80 md:text-base">
          这里不走普通用户私信。你可以直接提交社区 Bug、任务纠纷、账号举报、内容举报或功能建议，管理员处理后会通过系统消息回复你。
        </p>
      </section>

      <div class="mt-8 grid gap-8 xl:grid-cols-[0.95fr_1.05fr]">
        <section class="rounded-[2rem] bg-surface-container-lowest p-6 shadow-sm">
          <h2 class="text-2xl font-extrabold text-teal-950">提交反馈</h2>

          <div v-if="error" class="mt-5 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">
            {{ error }}
          </div>
          <div v-if="successMessage" class="mt-5 rounded-2xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-700">
            {{ successMessage }}
          </div>

          <form class="mt-5 space-y-5" @submit.prevent="handleSubmit">
            <div>
              <p class="text-sm font-bold text-teal-950">反馈类型</p>
              <div class="mt-3 grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
                <button
                  v-for="item in feedbackTypes"
                  :key="item.value"
                  type="button"
                  class="rounded-2xl px-4 py-3 text-left text-sm transition"
                  :class="form.type === item.value ? 'bg-teal-900 text-white shadow-lg shadow-teal-900/15' : 'bg-surface-container-low text-teal-900 hover:bg-surface-container-high'"
                  @click="selectFeedbackType(item.value)"
                >
                  <p class="font-bold">{{ item.label }}</p>
                  <p class="mt-1 text-xs leading-5" :class="form.type === item.value ? 'text-white/75' : 'text-on-surface-variant'">
                    {{ item.hint }}
                  </p>
                </button>
              </div>
            </div>

            <div>
              <button type="button" class="flex items-center gap-2 text-sm font-bold text-teal-900 hover:text-teal-700 transition-colors" @click="showAdvancedOptions = !showAdvancedOptions">
                <span class="material-symbols-outlined text-base">{{ showAdvancedOptions ? 'expand_less' : 'expand_more' }}</span>
                高级选项
                <span class="text-xs font-medium text-on-surface-variant ml-1">（优先级: {{ feedbackPriorityLabel(form.priority) }}）</span>
              </button>
              <div v-if="showAdvancedOptions" class="mt-3 grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
                <button
                  v-for="item in feedbackPriorities"
                  :key="item.value"
                  type="button"
                  class="rounded-2xl border px-4 py-3 text-left text-sm transition"
                  :class="form.priority === item.value
                    ? `${feedbackPrioritySelectionClass(item.value)} border-transparent shadow-lg`
                    : 'border-outline-variant/20 bg-surface-container-low text-slate-700 hover:bg-surface-container-high'"
                  @click="form.priority = item.value"
                >
                  <p class="font-bold">{{ item.label }}</p>
                  <p class="mt-1 text-xs leading-5" :class="form.priority === item.value ? 'text-current/75' : 'text-on-surface-variant'">
                    {{ item.hint }}
                  </p>
                </button>
              </div>
            </div>

            <input
              v-model.trim="form.title"
              type="text"
              class="w-full rounded-2xl border border-outline-variant/15 bg-surface-container-low px-4 py-3 outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
              placeholder="反馈标题，例如：任务被恶意放鸽子 / 私信页面加载异常"
            />

            <textarea
              v-model.trim="form.content"
              rows="6"
              class="w-full rounded-2xl border border-outline-variant/15 bg-surface-container-low px-4 py-3 outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/20"
              placeholder="请尽量写清楚发生了什么、涉及哪个页面或对象、你已经做过什么尝试，以及希望管理员如何协助。"
            ></textarea>

            <button
              type="submit"
              class="rounded-full bg-teal-900 px-5 py-3 text-sm font-extrabold text-white transition hover:bg-teal-800 disabled:opacity-60"
              :disabled="submitting"
            >
              {{ submitting ? '提交中...' : '提交反馈' }}
            </button>
          </form>
        </section>

        <section class="rounded-[2rem] bg-surface-container-lowest p-6 shadow-sm">
          <div class="flex items-center justify-between gap-3">
            <div>
              <h2 class="text-2xl font-extrabold text-teal-950">我的反馈记录</h2>
              <p class="mt-2 text-sm text-on-surface-variant">管理员处理进度、回复和优先级调整都会同步展示在这里。</p>
            </div>
            <button
              type="button"
              class="rounded-full bg-surface-container-low px-4 py-2 text-sm font-bold text-teal-900 transition hover:bg-surface-container-high"
              :disabled="loading"
              @click="loadFeedback"
            >
              刷新
            </button>
          </div>

          <div v-if="loading" class="mt-6 rounded-2xl bg-surface-container-low px-4 py-5 text-sm text-on-surface-variant">
            正在加载反馈记录...
          </div>

          <div v-else-if="feedbackList.length === 0" class="mt-6 rounded-2xl bg-surface-container-low px-4 py-5 text-sm text-on-surface-variant">
            你还没有提交过反馈。
          </div>

          <div v-else class="mt-6 space-y-4">
            <article
              v-for="item in feedbackList"
              :key="item.id"
              class="rounded-[1.5rem] border border-outline-variant/12 bg-surface-container-low p-4"
            >
              <div class="flex flex-wrap items-center gap-2">
                <span class="rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em]" :class="feedbackTypeClass(item.type)">
                  {{ feedbackTypeLabel(item.type) }}
                </span>
                <span class="rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em]" :class="feedbackPriorityClass(item.priority)">
                  {{ feedbackPriorityLabel(item.priority) }}
                </span>
                <span class="rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-[0.18em]" :class="feedbackStatusClass(item.status)">
                  {{ feedbackStatusLabel(item.status) }}
                </span>
                <span class="text-xs font-semibold text-on-surface-variant">{{ formatTime(item.createdAt) }}</span>
              </div>

              <h3 class="mt-3 text-lg font-extrabold text-teal-950">{{ item.title }}</h3>
              <p class="mt-2 text-sm leading-7 text-on-surface">{{ item.content }}</p>

              <div v-if="item.adminReply" class="mt-4 rounded-2xl bg-white px-4 py-3">
                <p class="text-sm font-bold text-teal-900">管理员回复</p>
                <p class="mt-2 text-sm leading-7 text-on-surface-variant">{{ item.adminReply }}</p>
              </div>

              <div v-if="canWithdrawFeedback(item)" class="mt-4 flex justify-end">
                <button
                  type="button"
                  class="rounded-full bg-rose-50 px-4 py-2 text-sm font-bold text-rose-600 transition hover:bg-rose-100 disabled:opacity-60"
                  :disabled="withdrawingId === item.id"
                  @click="handleWithdrawFeedback(item)"
                >
                  {{ withdrawingId === item.id ? '撤回中...' : '撤回反馈' }}
                </button>
              </div>
            </article>
          </div>
        </section>
      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import PageBackHeader from '../components/PageBackHeader.vue'
import { feedbackApi } from '../services/api'

type FeedbackType = 'BUG' | 'SUGGESTION' | 'TASK_DISPUTE' | 'ACCOUNT_REPORT' | 'CONTENT_REPORT' | 'OTHER'
type FeedbackPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'

const loading = ref(false)
const submitting = ref(false)
const showAdvancedOptions = ref(false)
const withdrawingId = ref<number | null>(null)
const error = ref('')
const successMessage = ref('')
const feedbackList = ref<any[]>([])

const feedbackTypes: Array<{ value: FeedbackType; label: string; hint: string }> = [
  { value: 'BUG', label: '功能异常', hint: '页面报错、按钮失效、状态显示不正确。' },
  { value: 'TASK_DISPUTE', label: '任务纠纷', hint: '接单争议、放鸽子、奖励与描述不符。' },
  { value: 'ACCOUNT_REPORT', label: '账号举报', hint: '冒名顶替、骚扰、恶意行为或异常账号。' },
  { value: 'CONTENT_REPORT', label: '内容举报', hint: '违规帖子、虚假信息、引战或不当内容。' },
  { value: 'SUGGESTION', label: '功能建议', hint: '希望增加筛选、优化流程、补充能力。' },
  { value: 'OTHER', label: '其他反馈', hint: '不属于以上类型，但需要管理员介入。' }
]

const feedbackPriorities: Array<{ value: FeedbackPriority; label: string; hint: string }> = [
  { value: 'LOW', label: '低优先级', hint: '不影响当前使用，可稍后跟进。' },
  { value: 'NORMAL', label: '普通', hint: '需要处理，但没有明显时效压力。' },
  { value: 'HIGH', label: '高优先级', hint: '明显影响体验，建议优先排查。' },
  { value: 'URGENT', label: '紧急', hint: '存在较高风险，需要尽快介入。' }
]

const form = ref({
  type: 'BUG' as FeedbackType,
  priority: 'HIGH' as FeedbackPriority,
  title: '',
  content: ''
})

const normalizeList = (response: any) => Array.isArray(response) ? response : Array.isArray(response?.data) ? response.data : []

const defaultPriorityForType = (type: FeedbackType): FeedbackPriority => {
  if (type === 'SUGGESTION' || type === 'OTHER') {
    return 'NORMAL'
  }
  return 'HIGH'
}

const selectFeedbackType = (type: FeedbackType) => {
  form.value.type = type
  form.value.priority = defaultPriorityForType(type)
}

const loadFeedback = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await feedbackApi.getMyFeedback() as any
    feedbackList.value = normalizeList(response)
  } catch (err: any) {
    error.value = err?.response?.data?.message || '反馈记录加载失败'
  } finally {
    loading.value = false
  }
}

const handleSubmit = async () => {
  if (!form.value.title.trim() || !form.value.content.trim()) {
    error.value = '标题和内容不能为空'
    return
  }

  submitting.value = true
  error.value = ''
  successMessage.value = ''
  try {
    await feedbackApi.createFeedback({
      type: form.value.type,
      priority: form.value.priority,
      title: form.value.title,
      content: form.value.content
    })
    form.value = { type: 'BUG', priority: 'HIGH', title: '', content: '' }
    successMessage.value = '反馈已提交，管理员处理后会通过系统消息回复你。'
    await loadFeedback()
  } catch (err: any) {
    error.value = err?.response?.data?.message || '反馈提交失败'
  } finally {
    submitting.value = false
  }
}

const canWithdrawFeedback = (item: any) => item?.status !== 'resolved'

const handleWithdrawFeedback = async (item: any) => {
  if (!canWithdrawFeedback(item)) return

  withdrawingId.value = item.id
  error.value = ''
  successMessage.value = ''
  try {
    await feedbackApi.withdrawFeedback(item.id)
    successMessage.value = '反馈已撤回。'
    await loadFeedback()
  } catch (err: any) {
    error.value = err?.response?.data?.message || '撤回反馈失败'
  } finally {
    withdrawingId.value = null
  }
}

const formatTime = (value?: string) => {
  if (!value) return '刚刚'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '刚刚'
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const feedbackTypeLabel = (value?: string) => {
  if (value === 'BUG') return '功能异常'
  if (value === 'TASK_DISPUTE') return '任务纠纷'
  if (value === 'ACCOUNT_REPORT') return '账号举报'
  if (value === 'CONTENT_REPORT') return '内容举报'
  if (value === 'SUGGESTION') return '功能建议'
  return '其他反馈'
}

const feedbackTypeClass = (value?: string) => {
  if (value === 'BUG') return 'bg-rose-100 text-rose-700'
  if (value === 'TASK_DISPUTE') return 'bg-amber-100 text-amber-800'
  if (value === 'ACCOUNT_REPORT') return 'bg-fuchsia-100 text-fuchsia-700'
  if (value === 'CONTENT_REPORT') return 'bg-orange-100 text-orange-700'
  if (value === 'SUGGESTION') return 'bg-cyan-100 text-cyan-800'
  return 'bg-slate-100 text-slate-700'
}

const feedbackPriorityLabel = (value?: string) => {
  if (value === 'LOW') return '低优先级'
  if (value === 'HIGH') return '高优先级'
  if (value === 'URGENT') return '紧急'
  return '普通'
}

const feedbackPriorityClass = (value?: string) => {
  if (value === 'LOW') return 'bg-slate-100 text-slate-600'
  if (value === 'HIGH') return 'bg-amber-100 text-amber-800'
  if (value === 'URGENT') return 'bg-rose-100 text-rose-700'
  return 'bg-sky-100 text-sky-700'
}

const feedbackPrioritySelectionClass = (value: FeedbackPriority) => {
  if (value === 'LOW') return 'bg-slate-200 text-slate-800'
  if (value === 'HIGH') return 'bg-amber-200 text-amber-900'
  if (value === 'URGENT') return 'bg-rose-200 text-rose-800'
  return 'bg-sky-200 text-sky-800'
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
  loadFeedback()
})
</script>
