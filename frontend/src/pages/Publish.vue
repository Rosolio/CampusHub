<template>
  <div class="min-h-screen bg-surface font-body text-on-surface pb-24 md:pb-0">

    <main class="mx-auto max-w-4xl px-6 pb-12 pt-24">
      <PageBackHeader to="/" label="返回社区" />

      <section class="mb-8 overflow-hidden rounded-[2rem] bg-gradient-to-br from-primary via-[#3d007a] to-[#5c00b3] p-8 text-white shadow-lg">
        <p class="mb-3 text-xs font-bold uppercase tracking-[0.3em] text-cyan-100/80">发布模式</p>
        <h1 class="mb-3 text-3xl font-extrabold">{{ activeConfig.mode === 'task' ? '发布接单任务' : '发布话题帖' }}</h1>
        <p class="max-w-2xl text-sm leading-7 text-cyan-50/85">
          {{ activeConfig.modeDescription }}
        </p>
        <div class="mt-6 flex flex-wrap gap-3">
          <button
            v-for="category in categories"
            :key="category.value"
            type="button"
            class="rounded-full border px-4 py-2 text-sm font-semibold transition-all"
            :class="form.category === category.value ? 'border-white bg-white text-primary' : 'border-white/20 bg-white/10 text-white hover:bg-white/20'"
            @click="handleCategoryChange(category.value)"
          >
            {{ category.label }}
            <span class="ml-2 text-[11px] opacity-80">{{ category.mode === 'task' ? '接单' : '话题' }}</span>
          </button>
        </div>
      </section>

      <section class="rounded-[2rem] bg-surface-container-lowest p-8 shadow-sm">
        <div v-if="error" class="mb-6 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">
          {{ error }}
        </div>

        <form class="space-y-6" @submit.prevent="submitForm">
          <FormField :label="activeConfig.titleLabel">
            <div class="flex items-center gap-2">
              <input
                ref="titleInput"
                v-model="form.title"
                type="text"
                class="min-w-0 flex-1 rounded-2xl border border-outline-variant/15 bg-surface-container-low px-4 py-3 transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
                :placeholder="activeConfig.titlePlaceholder"
              />
              <EmojiPicker :disabled="loading" align="right" @select="insertTitleEmoji" />
            </div>
          </FormField>

          <FormField :label="activeConfig.descriptionLabel">
            <textarea
              ref="descriptionTextarea"
              v-model="form.description"
              rows="5"
              class="w-full resize-none rounded-2xl border border-outline-variant/15 bg-surface-container-low px-4 py-3 transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
              :placeholder="activeConfig.descriptionPlaceholder"
            ></textarea>
            <div class="mt-3 flex justify-end">
              <EmojiPicker :disabled="loading" align="right" @select="insertDescriptionEmoji" />
            </div>
          </FormField>

          <ImageUploader v-model="form.imageUrls" />

          <div class="grid gap-6 md:grid-cols-2">
            <FormField :label="activeConfig.locationLabel">
              <div class="flex items-center gap-2">
                <input
                  ref="locationInput"
                  v-model="form.location"
                  type="text"
                  class="min-w-0 flex-1 rounded-2xl border border-outline-variant/15 bg-surface-container-low px-4 py-3 transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
                  :placeholder="activeConfig.locationPlaceholder"
                />
                <EmojiPicker :disabled="loading" align="right" @select="insertLocationEmoji" />
              </div>
            </FormField>

            <FormField :label="activeConfig.timeLabel">
              <div v-if="activeConfig.mode === 'topic'" class="mb-3 flex items-center justify-between rounded-2xl border border-cyan-100 bg-cyan-50/60 px-4 py-3 text-sm">
                <div>
                  <p class="font-semibold text-primary">长期有效</p>
                  <p class="mt-1 text-xs leading-5 text-[#3d007a]/75">开启后帖子不会自动截止，也不会因为到期而隐藏。</p>
                </div>
                <button
                  type="button"
                  class="rounded-full px-4 py-2 text-sm font-semibold transition-all"
                  :class="form.topicLongTerm ? 'bg-primary text-white' : 'bg-white text-primary shadow-sm hover:bg-cyan-50'"
                  @click="form.topicLongTerm = !form.topicLongTerm"
                >
                  {{ form.topicLongTerm ? '已开启' : '去开启' }}
                </button>
              </div>
              <input
                v-model="form.deadline"
                type="datetime-local"
                class="w-full rounded-2xl border border-outline-variant/15 bg-surface-container-low px-4 py-3 transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
                :placeholder="activeConfig.timePlaceholder"
                :disabled="activeConfig.mode === 'topic' && form.topicLongTerm"
                :class="{ 'cursor-not-allowed opacity-60': activeConfig.mode === 'topic' && form.topicLongTerm }"
              />
              <p v-if="activeConfig.mode === 'topic' && form.topicLongTerm" class="mt-2 text-xs text-on-surface-variant">
                已设为长期有效，帖子将持续展示，直到你主动删除。
              </p>
            </FormField>
          </div>

          <div v-if="activeConfig.mode === 'task'" class="grid gap-6 md:grid-cols-[minmax(0,1fr)_minmax(0,1fr)]">
            <FormField label="任务奖励" help="跑腿代办保留接单模式，支持明确酬劳与截止时间。" wrapper-class="min-w-0">
              <input
                v-model="form.reward"
                type="number"
                min="1"
                step="1"
                class="box-border min-w-0 w-full rounded-2xl border border-outline-variant/15 bg-surface-container-low px-4 py-3 transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
                placeholder="请输入金额（元）"
              />
            </FormField>

            <FormField label="紧急程度" wrapper-class="min-w-0">
              <div class="grid grid-cols-2 gap-3">
                <button
                  type="button"
                  class="rounded-2xl py-3 text-sm font-semibold transition-all"
                  :class="!form.urgent ? 'bg-primary text-white' : 'bg-surface-container-high text-on-surface-variant hover:bg-cyan-50/60'"
                  @click="form.urgent = false"
                >
                  普通
                </button>
                <button
                  type="button"
                  class="rounded-2xl py-3 text-sm font-semibold transition-all"
                  :class="form.urgent ? 'bg-error text-white' : 'bg-surface-container-high text-on-surface-variant hover:bg-cyan-50/60'"
                  @click="form.urgent = true"
                >
                  紧急
                </button>
              </div>
            </FormField>
          </div>

          <div v-else class="rounded-[1.75rem] border border-cyan-100 bg-cyan-50/60 p-5">
            <div class="flex items-start gap-3">
              <div class="rounded-2xl bg-white p-3 text-primary shadow-sm">
                <span class="material-symbols-outlined">forum</span>
              </div>
              <div>
                    <h2 class="text-lg font-bold text-primary">话题帖互动规则</h2>
                    <p class="mt-2 text-sm leading-6 text-[#3d007a]/80">
                  这类内容不会进入接单流程，而是以帖子形式展开讨论。用户可以评论和回复，发布一条评论即可获得 5 积分；你可以设置截止时间，也可以直接设为长期有效。
                    </p>
                  </div>
                </div>
          </div>

          <FormField v-if="activeConfig.mode === 'topic'" label="联系方式（可选）" help="会展示在话题帖详情里，方便感兴趣的同学直接联系你。">
            <div class="flex items-center gap-2">
              <input
                ref="contactInfoInput"
                v-model="form.contactInfo"
                type="text"
                class="min-w-0 flex-1 rounded-2xl border border-outline-variant/15 bg-surface-container-low px-4 py-3 transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
                placeholder="例如：微信 abc123 / QQ 123456 / 手机尾号 6789"
              />
              <EmojiPicker :disabled="loading" align="right" @select="insertContactInfoEmoji" />
            </div>
          </FormField>

          <button
            type="submit"
            class="w-full rounded-2xl bg-gradient-to-br from-primary to-primary-dim py-4 font-bold text-on-primary shadow-lg transition-all duration-300 hover:scale-[1.01] active:scale-95 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:scale-100"
            :disabled="loading"
          >
            {{ loading ? '发布中...' : activeConfig.submitText }}
          </button>
        </form>
      </section>
    </main>

  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, ref } from 'vue'
import { useRouter } from 'vue-router'
import EmojiPicker from '../components/EmojiPicker.vue'
import FormField from '../components/FormField.vue'
import ImageUploader from '../components/ImageUploader.vue'
import PageBackHeader from '../components/PageBackHeader.vue'
import { usePreferences } from '../composables/usePreferences'
import { showToast } from '../composables/useToast'
import { taskApi } from '../services/api'

type Mode = 'task' | 'topic'

const categories = [
  { label: '跑腿代办', value: '跑腿代办', mode: 'task' as Mode, badgeSecondary: '校园配送', impactText: 'errand' },
  { label: '学习辅导', value: '学习辅导', mode: 'task' as Mode, badgeSecondary: '学业辅导', impactText: 'study' },
  { label: '二手闲置', value: '二手闲置', mode: 'topic' as Mode, badgeSecondary: '闲置交换', impactText: 'secondhand' },
  { label: '恋爱交友', value: '恋爱交友', mode: 'topic' as Mode, badgeSecondary: '社交互助', impactText: 'social' },
  { label: '打听求助', value: '打听求助', mode: 'topic' as Mode, badgeSecondary: '信息求助', impactText: 'help' },
  { label: '兼职招聘', value: '兼职招聘', mode: 'topic' as Mode, badgeSecondary: '兼职机会', impactText: 'job' }
]

const router = useRouter()
const { formatLocaleDateTime } = usePreferences()
const loading = ref(false)
const error = ref('')
const titleInput = ref<HTMLInputElement | null>(null)
const descriptionTextarea = ref<HTMLTextAreaElement | null>(null)
const locationInput = ref<HTMLInputElement | null>(null)
const contactInfoInput = ref<HTMLInputElement | null>(null)

const form = ref({
  title: '',
  category: '跑腿代办',
  description: '',
  reward: 10,
  location: '',
  deadline: '',
  urgent: false,
  contactInfo: '',
  topicLongTerm: false,
  imageUrls: [] as string[]
})

const insertAtCursor = (
  target: HTMLInputElement | HTMLTextAreaElement | null,
  currentValue: string,
  value: string
) => {
  const start = target?.selectionStart ?? currentValue.length
  const end = target?.selectionEnd ?? currentValue.length
  const nextValue = currentValue.slice(0, start) + value + currentValue.slice(end)
  const nextCursor = start + value.length
  return { nextValue, nextCursor }
}

const focusAfterInsert = (
  target: HTMLInputElement | HTMLTextAreaElement | null,
  cursor: number
) => {
  nextTick(() => {
    if (!target) return
    target.focus()
    target.setSelectionRange(cursor, cursor)
  })
}

const insertTitleEmoji = (emoji: string) => {
  const { nextValue, nextCursor } = insertAtCursor(titleInput.value, form.value.title, emoji)
  form.value.title = nextValue
  focusAfterInsert(titleInput.value, nextCursor)
}

const insertDescriptionEmoji = (emoji: string) => {
  const { nextValue, nextCursor } = insertAtCursor(descriptionTextarea.value, form.value.description, emoji)
  form.value.description = nextValue
  focusAfterInsert(descriptionTextarea.value, nextCursor)
}

const insertLocationEmoji = (emoji: string) => {
  const { nextValue, nextCursor } = insertAtCursor(locationInput.value, form.value.location, emoji)
  form.value.location = nextValue
  focusAfterInsert(locationInput.value, nextCursor)
}

const insertContactInfoEmoji = (emoji: string) => {
  const { nextValue, nextCursor } = insertAtCursor(contactInfoInput.value, form.value.contactInfo, emoji)
  form.value.contactInfo = nextValue
  focusAfterInsert(contactInfoInput.value, nextCursor)
}

const activeCategory = computed(() => categories.find((category) => category.value === form.value.category) || categories[0])

const activeConfig = computed(() => {
  const mode = activeCategory.value.mode
  if (mode === 'task') {
    return {
      mode,
      modeDescription: activeCategory.value.value === '学习辅导'
        ? '学习辅导保留“任务接单”逻辑，适合一对一答疑、课程辅导、作业讲解等需要明确时间和协作安排的需求。'
        : '跑腿代办保留“任务接单”逻辑，适合明确时间、地点、酬劳和履约责任的需求。',
      titleLabel: '任务标题',
      titlePlaceholder: activeCategory.value.value === '学习辅导' ? '例如：高数期中前求一对一辅导' : '例如：今晚帮我取快递送到宿舍',
      descriptionLabel: '任务描述',
      descriptionPlaceholder: activeCategory.value.value === '学习辅导'
        ? '写清楚课程、年级、辅导内容、期望时长和你的基础情况'
        : '描述代办事项、注意事项、楼栋信息等',
      locationLabel: '任务地点',
      locationPlaceholder: activeCategory.value.value === '学习辅导' ? '例如：图书馆三楼 / 腾讯会议线上' : '例如：南区菜鸟驿站',
      timeLabel: '截止时间',
      timePlaceholder: '',
      submitText: '发布接单任务'
    }
  }

  return {
    mode,
    modeDescription: '二手闲置、恋爱交友、打听求助、兼职招聘都以“话题帖”形式发布，更适合围绕评论和回复展开交流；你可以设置帖子截止时间，也可以直接设为长期有效。',
    titleLabel: '帖子标题',
    titlePlaceholder: '例如：转让九成新电饭锅 / 想找周末一起看展的搭子',
    descriptionLabel: '帖子内容',
    descriptionPlaceholder: '写清楚你希望讨论、交流或发布的信息，让大家更容易参与',
    locationLabel: '补充信息',
    locationPlaceholder: '例如：宿舍自提 / 线上交流 / 工作地点在校门口',
    timeLabel: form.value.topicLongTerm ? '有效期设置' : '截止时间',
    timePlaceholder: '',
    submitText: '发布话题帖'
  }
})

const handleCategoryChange = (category: string) => {
  const previousMode = activeCategory.value.mode
  const newMode = categories.find((c) => c.value === category)?.mode || 'task'

  form.value.category = category

  if (previousMode !== newMode) {
    form.value.title = ''
    form.value.description = ''
    form.value.location = ''
    form.value.deadline = ''
    form.value.contactInfo = ''
  }

  if (newMode === 'topic') {
    form.value.urgent = false
    return
  }
  form.value.topicLongTerm = false
}

const validateForm = () => {
  if (!form.value.title.trim()) return '请填写标题'
  if (!form.value.description.trim()) return '请填写内容描述'
  if (!form.value.location.trim()) return activeConfig.value.mode === 'task' ? '请填写任务地点' : '请填写补充信息'
  if (activeConfig.value.mode === 'topic' && form.value.topicLongTerm) return ''
  if (!form.value.deadline.trim()) return '请填写截止时间'
  const deadline = new Date(form.value.deadline.trim())
  if (Number.isNaN(deadline.getTime())) return '截止时间格式不合法'
  if (deadline.getTime() <= Date.now()) return '截止时间必须晚于当前时间'
  return ''
}

const formatDeadlineText = (deadline: string) => {
  return formatLocaleDateTime(deadline, {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }, deadline)
}

const submitForm = async () => {
  const validationMessage = validateForm()
  if (validationMessage) {
    error.value = validationMessage
    return
  }

  loading.value = true
  error.value = ''

  try {
    const rewardAmount = String(Math.max(1, form.value.reward || 10))
    const isTaskMode = activeCategory.value.mode === 'task'
    const deadlineText = !isTaskMode && form.value.topicLongTerm
      ? '长期有效'
      : formatDeadlineText(form.value.deadline.trim())
    const payload = {
      title: form.value.title.trim(),
      description: form.value.description.trim(),
      category: activeCategory.value.value,
      taskMode: activeCategory.value.mode,
      badgePrimary: isTaskMode ? (form.value.urgent ? '紧急' : '普通') : '话题帖',
      badgeSecondary: activeCategory.value.badgeSecondary,
      locationText: form.value.location.trim(),
      timeText: deadlineText,
      rewardTitle: isTaskMode ? '任务奖励' : '互动奖励',
      rewardText: isTaskMode ? `${rewardAmount} 元` : '发布评论即可获得 5 积分',
      impactTitle: isTaskMode ? '任务类型' : '帖子类型',
      impactText: activeCategory.value.impactText,
      mapImageUrl: '',
      contactInfo: isTaskMode ? '' : form.value.contactInfo.trim(),
      expiresAt: !isTaskMode && form.value.topicLongTerm ? undefined : form.value.deadline.trim(),
      imageUrls: form.value.imageUrls.length > 0 ? JSON.stringify(form.value.imageUrls) : undefined
    }

    const response = await taskApi.createTask(payload) as Record<string, any>
    if (!response?.id) {
      throw new Error('服务器返回了无效结果')
    }

    showToast(activeCategory.value.mode === 'task' ? '接单任务发布成功！' : '话题帖发布成功！', 'success')
    router.push(`/detail/${response.id}`)
  } catch (err: any) {
    error.value = err?.response?.data?.message || err?.message || '发布失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>
