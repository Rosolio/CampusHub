<template>
  <div class="h-[100dvh] overflow-hidden bg-surface font-body text-on-surface">
    <AppTopNav :avatar-url="currentUser.avatarUrl || defaultAvatarUrl" />

    <main class="mx-auto flex h-full max-w-7xl flex-col px-4 pb-20 pt-20 sm:px-6 md:pb-6 md:pt-24">
      <div class="mb-3 shrink-0">
        <RouterLink
          to="/"
          class="flex items-center gap-2 text-on-surface-variant font-medium hover:text-primary transition-colors group"
        >
          <span
            class="material-symbols-outlined text-lg group-hover:-translate-x-1 transition-transform"
            data-icon="arrow_back"
          >arrow_back</span>
          <span>返回</span>
        </RouterLink>
      </div>

      <div class="mb-4 flex shrink-0 flex-col gap-2 md:flex-row md:items-end md:justify-between">
        <div>
          <h1 class="font-headline text-2xl font-extrabold text-teal-900 md:text-3xl">消息中心</h1>
          <p class="text-sm leading-relaxed text-on-surface-variant md:text-base">
            按任务查看沟通记录，直接在右侧继续回复。
          </p>
        </div>
        <button
          class="self-start rounded-2xl bg-surface-container-low px-4 py-2 text-sm font-bold text-teal-900 transition-colors hover:bg-surface-container-high"
          type="button"
          :disabled="loading"
          @click="handleRefreshMessages"
        >
          {{ loading ? '刷新中...' : '刷新消息' }}
        </button>
      </div>

      <div v-if="fetchError" class="mb-4 shrink-0 rounded-3xl border border-rose-200 bg-rose-50 px-5 py-4 text-sm font-medium text-rose-700">
        {{ fetchError }}
      </div>

      <div class="grid min-h-0 flex-1 grid-cols-1 grid-rows-[minmax(180px,32vh)_minmax(0,1fr)] gap-4 overflow-hidden lg:grid-cols-[360px_minmax(0,1fr)] lg:grid-rows-1 lg:gap-6">
        <aside class="flex min-h-0 flex-col rounded-[2rem] bg-surface-container-lowest p-4 shadow-sm md:p-5">
          <div class="mb-3 flex shrink-0 items-center justify-between">
            <div>
              <h2 class="text-lg font-extrabold text-teal-900 md:text-xl">会话列表</h2>
              <p class="text-sm text-on-surface-variant">共 {{ filteredConversations.length }} 个会话</p>
            </div>
          </div>

          <div class="mb-3 flex shrink-0 flex-wrap gap-2">
            <button
              class="rounded-full px-4 py-2 text-sm font-bold transition-colors"
              type="button"
              :class="showUnreadOnly ? 'bg-teal-900 text-white' : 'bg-surface-container-low text-teal-900 hover:bg-surface-container-high'"
              @click="showUnreadOnly = !showUnreadOnly"
            >
              {{ showUnreadOnly ? '显示全部' : '仅看未读' }}
            </button>
            <div class="rounded-full bg-cyan-50 px-4 py-2 text-sm font-medium text-teal-900">
              未读 {{ unreadConversationCount }} 个会话
            </div>
          </div>

          <div v-if="filteredConversations.length === 0" class="min-h-0 flex-1 overflow-y-auto rounded-3xl bg-surface-container-low p-6 text-center">
            <p class="text-lg font-bold text-teal-900 mb-2">暂无会话</p>
            <p class="text-on-surface-variant">{{ showUnreadOnly ? '当前没有未读会话。' : '去任务详情页联系需求方后，这里会出现对应聊天。' }}</p>
          </div>

          <div v-else class="min-h-0 flex-1 space-y-3 overflow-y-auto pr-1">
            <button
              v-for="conversation in filteredConversations"
              :key="conversation.key"
              class="w-full rounded-3xl border p-4 text-left transition-all"
              type="button"
              :class="selectedConversation?.key === conversation.key
                ? 'border-teal-300 bg-cyan-50 shadow-sm'
                : 'border-outline-variant/15 bg-surface-container-low hover:border-teal-200 hover:bg-surface-container-high'"
              @click="selectConversation(conversation.key)"
            >
              <div class="mb-3 flex items-start justify-between gap-3">
                <div class="flex items-center gap-3 min-w-0">
                  <img
                    :src="conversation.counterpartAvatarUrl || defaultAvatarUrl"
                    :alt="conversation.counterpartName"
                    class="h-12 w-12 rounded-full object-cover border border-outline-variant/20"
                  />
                  <div class="min-w-0">
                    <p class="truncate font-bold text-teal-900">{{ conversation.counterpartName }}</p>
                    <p class="truncate text-sm text-on-surface-variant">{{ conversation.taskTitle }}</p>
                  </div>
                </div>
                <span v-if="conversation.unreadCount > 0" class="rounded-full bg-primary px-2.5 py-1 text-xs font-bold text-white">
                  {{ conversation.unreadCount }}
                </span>
              </div>
              <div v-if="conversation.hasSystemReminder" class="mb-2">
                <span class="rounded-full bg-amber-100 px-3 py-1 text-[10px] font-bold uppercase tracking-[0.16em] text-amber-800">
                  系统提醒
                </span>
              </div>
              <p class="mb-2 line-clamp-2 text-sm text-on-surface">{{ conversation.lastMessage }}</p>
              <div class="flex items-center justify-between gap-3 text-xs text-on-surface-variant">
                <span>{{ formatCreatedAt(conversation.lastCreatedAt) }}</span>
                <span>{{ conversation.lastDirection === 'outgoing' ? '你发出' : '对方发来' }}</span>
              </div>
            </button>
          </div>
        </aside>

        <section class="flex min-h-0 flex-col overflow-hidden rounded-[2rem] bg-surface-container-lowest shadow-sm">
          <div v-if="!selectedConversation" class="flex-1 flex items-center justify-center p-8">
            <div class="max-w-md rounded-[2rem] bg-surface-container-low p-8 text-center">
              <p class="mb-2 text-xl font-extrabold text-teal-900">选择一个会话</p>
              <p class="text-on-surface-variant">左侧会显示按任务和联系人聚合后的聊天记录。</p>
            </div>
          </div>

          <template v-else>
            <div class="shrink-0 border-b border-outline-variant/15 px-5 py-4 md:px-6 md:py-5">
              <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
                <div class="flex items-center gap-4">
                  <div v-if="selectedConversation.isSystemChannel" class="flex h-14 w-14 items-center justify-center rounded-full border border-amber-200 bg-amber-50 text-amber-700">
                    <span class="material-symbols-outlined text-2xl">notifications_active</span>
                  </div>
                  <img
                    v-else
                    :src="selectedConversation.counterpartAvatarUrl || defaultAvatarUrl"
                    :alt="selectedConversation.counterpartName"
                    class="h-14 w-14 rounded-full object-cover border border-outline-variant/20"
                  />
                  <div class="min-w-0">
                    <h2 class="truncate text-2xl font-extrabold text-teal-900">{{ selectedConversation.counterpartName }}</h2>
                    <p class="truncate text-on-surface-variant">{{ selectedConversation.taskTitle }}</p>
                  </div>
                </div>
                <button
                  v-if="canViewSelectedTaskDetail"
                  type="button"
                  class="inline-flex items-center justify-center gap-2 self-start rounded-2xl bg-surface-container-low px-4 py-2 text-sm font-bold text-teal-900 transition-colors hover:bg-surface-container-high"
                  @click="openSelectedTaskDetail"
                >
                  <span class="material-symbols-outlined text-base">open_in_new</span>
                  查看需求详情
                </button>
              </div>
            </div>

            <div
              ref="messageViewport"
              class="min-h-0 flex-1 space-y-4 overflow-y-auto bg-[linear-gradient(180deg,rgba(240,253,250,0.85),rgba(255,255,255,0.96))] px-5 py-5 md:px-6 md:py-6"
              @scroll="handleMessageScroll"
            >
              <div v-for="group in groupedSelectedMessages" :key="group.label" class="space-y-4">
                <div class="flex justify-center">
                  <span class="rounded-full bg-white/85 px-4 py-1.5 text-xs font-bold text-teal-900 shadow-sm">
                    {{ group.label }}
                  </span>
                </div>
                <div
                  v-for="message in group.messages"
                  :key="message.id"
                  class="flex"
                  :class="message.direction === 'outgoing' ? 'justify-end' : 'justify-start'"
                >
                  <div class="max-w-[85%] md:max-w-[70%]">
                    <div
                      class="rounded-[1.75rem] px-5 py-4 shadow-sm"
                      :class="resolveMessageBubbleClass(message)"
                    >
                      <div v-if="isSystemMessage(message)" class="mb-3 inline-flex items-center gap-2 rounded-full bg-amber-100 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.16em] text-amber-800">
                        <span class="material-symbols-outlined text-sm">notifications_active</span>
                        系统提醒
                      </div>
                      <p class="whitespace-pre-wrap break-words leading-relaxed">{{ message.content }}</p>
                      <button
                        v-if="getMessageAction(message)"
                        type="button"
                        class="mt-4 inline-flex items-center gap-2 rounded-full bg-teal-900 px-4 py-2 text-xs font-bold text-white transition-colors hover:bg-teal-800"
                        @click="handleMessageAction(message)"
                      >
                        <span class="material-symbols-outlined text-sm">{{ getMessageAction(message)?.icon }}</span>
                        {{ getMessageAction(message)?.label }}
                      </button>
                    </div>
                    <p
                      class="mt-2 text-xs"
                      :class="message.direction === 'outgoing' ? 'text-right text-teal-800/70' : 'text-left text-on-surface-variant'"
                    >
                      {{ formatCreatedAt(message.createdAt) }}
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="!selectedConversation.isSystemChannel" class="shrink-0 border-t border-outline-variant/15 bg-white px-5 py-4 md:px-6 md:py-5">
              <div v-if="newMessageNoticeCount > 0" class="mb-4 flex justify-center">
                <button
                  class="rounded-full bg-teal-900 px-4 py-2 text-sm font-bold text-white shadow-lg transition-colors hover:bg-teal-800"
                  type="button"
                  @click="jumpToLatestMessages"
                >
                  有 {{ newMessageNoticeCount }} 条新消息，点击查看
                </button>
              </div>
              <div v-if="sendError" class="mb-4 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-medium text-rose-700">
                {{ sendError }}
              </div>
              <div class="flex flex-col gap-4 md:flex-row md:items-end">
                <div class="flex-1">
                  <label class="mb-2 block text-sm font-bold text-on-surface" for="message-input">
                    发送给 {{ selectedConversation.counterpartName }}
                  </label>
                  <textarea
                    id="message-input"
                    v-model="composer"
                    class="min-h-28 w-full rounded-3xl border border-outline-variant/20 bg-surface px-5 py-4 text-on-surface outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
                    placeholder="输入你想沟通的内容..."
                    @keydown.enter.exact.prevent="handleSendMessage"
                  ></textarea>
                  <p class="mt-2 text-xs text-on-surface-variant">
                    按 Enter 发送，按 Shift + Enter 换行。
                  </p>
                </div>
                <button
                  class="rounded-3xl bg-gradient-to-br from-primary to-primary-dim px-6 py-4 font-bold text-on-primary shadow-lg shadow-primary/20 transition-all hover:scale-[1.01] active:scale-95 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:scale-100"
                  type="button"
                  :disabled="sending"
                  @click="handleSendMessage"
                >
                  {{ sending ? '发送中...' : '发送消息' }}
                </button>
              </div>
            </div>
            <div v-else class="shrink-0 border-t border-outline-variant/15 bg-amber-50/60 px-6 py-5 text-sm text-amber-900">
              系统提醒会集中展示在这里，点击消息卡片内的按钮可直接进入对应详情页或互评页。
            </div>
          </template>
        </section>
      </div>
    </main>

    <nav
      class="md:hidden fixed bottom-0 left-0 w-full z-50 flex justify-around items-center px-6 pb-6 pt-3 bg-white/90 backdrop-blur-lg rounded-t-3xl shadow-[0_-4px_20px_rgba(0,52,57,0.05)]"
    >
      <RouterLink
        to="/"
        class="flex flex-col items-center justify-center text-teal-800/50 dark:text-teal-400/50"
      >
        <span class="material-symbols-outlined mb-1" data-icon="home">home</span>
        <span class="text-[10px] font-semibold uppercase tracking-wider mt-1">{{ t('navHome') }}</span>
      </RouterLink>
      <RouterLink
        to="/publish"
        class="flex flex-col items-center justify-center text-teal-800/50 dark:text-teal-400/50"
      >
        <span class="material-symbols-outlined mb-1" data-icon="add_circle">add_circle</span>
        <span class="text-[10px] font-semibold uppercase tracking-wider mt-1">{{ t('navPublish') }}</span>
      </RouterLink>
      <RouterLink
        to="/messages"
        class="flex flex-col items-center justify-center bg-teal-800 text-white rounded-2xl px-5 py-2 active:scale-90 transition-transform"
      >
        <span class="material-symbols-outlined mb-1" data-icon="chat_bubble">chat_bubble</span>
        <span class="text-[10px] font-semibold uppercase tracking-wider mt-1">{{ t('navMessages') }}</span>
      </RouterLink>
      <RouterLink
        to="/profile"
        class="flex flex-col items-center justify-center text-teal-800/50 dark:text-teal-400/50"
      >
        <span class="material-symbols-outlined mb-1" data-icon="person">person</span>
        <span class="text-[10px] font-semibold uppercase tracking-wider mt-1">{{ t('navProfile') }}</span>
      </RouterLink>
    </nav>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import AppTopNav from '../components/AppTopNav.vue'
import { usePreferences } from '../composables/usePreferences'
import { DEFAULT_AVATAR_URL } from '../constants/assets'
import { messageApi } from '../services/api'
import { storedUser } from '../utils/auth'

type RawMessage = {
  id: number
  senderId: number
  receiverId: number
  taskId?: number | null
  content: string
  status?: string
  createdAt?: string
  senderName?: string
  senderAvatarUrl?: string
  receiverName?: string
  receiverAvatarUrl?: string
  taskTitle?: string
}

type ConversationMessage = RawMessage & {
  direction: 'incoming' | 'outgoing'
}

type Conversation = {
  key: string
  counterpartId: number
  counterpartName: string
  counterpartAvatarUrl: string
  taskId: number | null
  taskTitle: string
  unreadCount: number
  lastCreatedAt?: string
  lastMessage: string
  lastDirection: 'incoming' | 'outgoing'
  messages: ConversationMessage[]
  isDraft?: boolean
  hasSystemReminder?: boolean
  isSystemChannel?: boolean
}

type MessageGroup = {
  label: string
  messages: ConversationMessage[]
}

const route = useRoute()
const router = useRouter()
const { formatLocaleDateLabel, formatLocaleDateTime, t } = usePreferences()
const messages = ref<RawMessage[]>([])
const selectedKey = ref('')
const composer = ref('')
const loading = ref(false)
const sending = ref(false)
const showUnreadOnly = ref(false)
const fetchError = ref('')
const sendError = ref('')
const messageViewport = ref<HTMLElement | null>(null)
const isNearBottom = ref(true)
const newMessageNoticeCount = ref(0)
const currentUser = computed(() => storedUser.value || {})
const defaultAvatarUrl = DEFAULT_AVATAR_URL
let pollingTimer: number | null = null

const normalizeMessages = (response: any): RawMessage[] => {
  if (Array.isArray(response)) return response
  if (Array.isArray(response?.data)) return response.data
  return []
}

const isSystemRawMessage = (message: RawMessage) => /^【/.test(message.content || '')

const toConversationKey = (taskId: number | null, counterpartId: number) => `${taskId ?? 'no-task'}-${counterpartId}`

const draftConversation = computed<Conversation | null>(() => {
  const counterpartId = Number(route.query.userId)
  if (!counterpartId) return null

  const taskIdValue = route.query.taskId
  const taskId = taskIdValue ? Number(taskIdValue) : null
  const key = toConversationKey(Number.isFinite(taskId as number) ? taskId : null, counterpartId)

  if (messages.value.some((message) => {
    const isOutgoing = Number(message.senderId) === Number(currentUser.value?.id)
    const otherUserId = isOutgoing ? Number(message.receiverId) : Number(message.senderId)
    const messageTaskId = message.taskId == null ? null : Number(message.taskId)
    return otherUserId === counterpartId && messageTaskId === (Number.isFinite(taskId as number) ? taskId : null)
  })) {
    return null
  }

  return {
    key,
    counterpartId,
    counterpartName: String(route.query.userName || `用户 #${counterpartId}`),
    counterpartAvatarUrl: defaultAvatarUrl,
    taskId: Number.isFinite(taskId as number) ? taskId : null,
    taskTitle: String(route.query.taskTitle || '未关联任务'),
    unreadCount: 0,
    lastCreatedAt: undefined,
    lastMessage: '还没有消息，发一条开始沟通吧。',
    lastDirection: 'outgoing',
    messages: [],
    isDraft: true
  }
})

const conversations = computed<Conversation[]>(() => {
  const map = new Map<string, Conversation>()
  const currentUserId = Number(currentUser.value?.id)
  const systemMessages: ConversationMessage[] = []

  for (const rawMessage of [...messages.value].sort((a, b) => new Date(a.createdAt || 0).getTime() - new Date(b.createdAt || 0).getTime())) {
    const isOutgoing = Number(rawMessage.senderId) === currentUserId
    if (!isOutgoing && isSystemRawMessage(rawMessage)) {
      systemMessages.push({
        ...rawMessage,
        direction: 'incoming'
      })
      continue
    }
    const counterpartId = isOutgoing ? Number(rawMessage.receiverId) : Number(rawMessage.senderId)
    const counterpartName = isOutgoing
      ? rawMessage.receiverName || `用户 #${rawMessage.receiverId}`
      : rawMessage.senderName || `用户 #${rawMessage.senderId}`
    const counterpartAvatarUrl = isOutgoing ? rawMessage.receiverAvatarUrl || defaultAvatarUrl : rawMessage.senderAvatarUrl || defaultAvatarUrl
    const taskId = rawMessage.taskId == null ? null : Number(rawMessage.taskId)
    const key = toConversationKey(taskId, counterpartId)

    if (!map.has(key)) {
      map.set(key, {
        key,
        counterpartId,
        counterpartName,
        counterpartAvatarUrl,
        taskId,
        taskTitle: rawMessage.taskTitle || '未关联任务',
        unreadCount: 0,
        lastCreatedAt: rawMessage.createdAt,
        lastMessage: rawMessage.content,
        lastDirection: isOutgoing ? 'outgoing' : 'incoming',
        messages: [],
        hasSystemReminder: false,
        isSystemChannel: false
      })
    }

    const conversation = map.get(key)!
    conversation.messages.push({
      ...rawMessage,
      direction: isOutgoing ? 'outgoing' : 'incoming'
    })
    conversation.lastCreatedAt = rawMessage.createdAt
    conversation.lastMessage = rawMessage.content
    conversation.lastDirection = isOutgoing ? 'outgoing' : 'incoming'

    if (!isOutgoing && rawMessage.status === 'unread') {
      conversation.unreadCount += 1
    }
    if (!isOutgoing && /^【/.test(rawMessage.content || '')) {
      conversation.hasSystemReminder = true
    }
  }

  const list = Array.from(map.values())
    .map((conversation) => ({
      ...conversation,
      messages: conversation.messages.sort((a, b) => new Date(a.createdAt || 0).getTime() - new Date(b.createdAt || 0).getTime())
    }))
    .sort((a, b) => new Date(b.lastCreatedAt || 0).getTime() - new Date(a.lastCreatedAt || 0).getTime())

  if (systemMessages.length > 0) {
    const unreadCount = systemMessages.filter((message) => message.status === 'unread').length
    const lastMessage = systemMessages[systemMessages.length - 1]
    list.unshift({
      key: 'system-channel',
      counterpartId: 0,
      counterpartName: '系统提醒',
      counterpartAvatarUrl: defaultAvatarUrl,
      taskId: null,
      taskTitle: '任务、帖子与互评提醒',
      unreadCount,
      lastCreatedAt: lastMessage?.createdAt,
      lastMessage: lastMessage?.content || '暂无系统提醒',
      lastDirection: 'incoming',
      messages: systemMessages,
      hasSystemReminder: true,
      isSystemChannel: true
    })
  }

  if (draftConversation.value) {
    const insertIndex = list.findIndex((conversation) => !conversation.isSystemChannel)
    if (insertIndex === -1) {
      list.push(draftConversation.value)
    } else {
      list.splice(insertIndex, 0, draftConversation.value)
    }
  }

  return list
})

const unreadConversationCount = computed(() => conversations.value.filter((conversation) => conversation.unreadCount > 0).length)

const filteredConversations = computed(() => {
  if (!showUnreadOnly.value) return conversations.value
  return conversations.value.filter((conversation) => conversation.unreadCount > 0)
})

const selectedConversation = computed(() => conversations.value.find((conversation) => conversation.key === selectedKey.value) || null)
const canViewSelectedTaskDetail = computed(() => (
  !selectedConversation.value?.isSystemChannel && selectedConversation.value?.taskId != null
))

const groupedSelectedMessages = computed<MessageGroup[]>(() => {
  if (!selectedConversation.value) return []

  const groups: MessageGroup[] = []
  for (const message of selectedConversation.value.messages) {
    const label = formatMessageGroupLabel(message.createdAt)
    const lastGroup = groups[groups.length - 1]
    if (!lastGroup || lastGroup.label !== label) {
      groups.push({ label, messages: [message] })
      continue
    }
    lastGroup.messages.push(message)
  }

  return groups
})

const formatCreatedAt = (value?: string) => {
  return formatLocaleDateTime(value, {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }, '刚刚')
}

const formatMessageGroupLabel = (value?: string) => {
  if (!value) return '刚刚'

  const date = new Date(value)
  const today = new Date()
  const dateOnly = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  const todayOnly = new Date(today.getFullYear(), today.getMonth(), today.getDate())
  const diffDays = Math.round((todayOnly.getTime() - dateOnly.getTime()) / 86400000)

  if (diffDays === 0) return '今天'
  if (diffDays === 1) return '昨天'

  return formatLocaleDateLabel(value, {
    month: 'long',
    day: 'numeric',
    weekday: 'long'
  }, '刚刚')
}

const scrollMessagesToBottom = async (behavior: ScrollBehavior = 'smooth') => {
  await nextTick()
  if (!messageViewport.value) return
  messageViewport.value.scrollTo({
    top: messageViewport.value.scrollHeight,
    behavior
  })
  isNearBottom.value = true
  newMessageNoticeCount.value = 0
}

const updateNearBottomState = () => {
  if (!messageViewport.value) {
    isNearBottom.value = true
    return
  }

  const distanceToBottom =
    messageViewport.value.scrollHeight - messageViewport.value.scrollTop - messageViewport.value.clientHeight
  isNearBottom.value = distanceToBottom < 96
  if (isNearBottom.value) {
    newMessageNoticeCount.value = 0
  }
}

const handleMessageScroll = () => {
  updateNearBottomState()
}

const jumpToLatestMessages = () => {
  scrollMessagesToBottom()
}

const openSelectedTaskDetail = () => {
  if (!canViewSelectedTaskDetail.value || selectedConversation.value?.taskId == null) return
  router.push(`/detail/${selectedConversation.value.taskId}`)
}

const isSystemMessage = (message: ConversationMessage) => (
  message.direction === 'incoming' && /^【/.test(message.content || '')
)

const resolveMessageBubbleClass = (message: ConversationMessage) => {
  if (message.direction === 'outgoing') {
    return 'bg-teal-900 text-white rounded-br-md'
  }
  if (isSystemMessage(message)) {
    return 'rounded-bl-md border border-amber-200 bg-gradient-to-br from-amber-50 to-orange-50 text-amber-950'
  }
  return 'bg-white text-on-surface rounded-bl-md border border-outline-variant/15'
}

const getMessageAction = (message: ConversationMessage) => {
  if (!message.taskId || !isSystemMessage(message)) {
    return null
  }

  const content = String(message.content || '')
  if (content.includes('互评') || content.includes('评价')) {
    return { label: '进入互评页', icon: 'rate_review' }
  }
  if (content.includes('帖子') || content.includes('评论') || content.includes('回复')) {
    return { label: '查看帖子详情', icon: 'forum' }
  }
  return { label: '查看需求详情', icon: 'open_in_new' }
}

const handleMessageAction = (message: ConversationMessage) => {
  if (!message.taskId) return
  const action = getMessageAction(message)
  if (!action) return

  if (action.label === '进入互评页') {
    router.push(`/detail/${message.taskId}/review`)
    return
  }

  router.push(`/detail/${message.taskId}`)
}

const syncSelectedConversation = () => {
  const counterpartId = Number(route.query.userId)
  const taskIdValue = route.query.taskId
  const parsedTaskId = taskIdValue ? Number(taskIdValue) : null
  const taskId = Number.isFinite(parsedTaskId as number) ? parsedTaskId : null

  if (counterpartId) {
    const key = toConversationKey(taskId, counterpartId)
    const matched = conversations.value.find((conversation) => conversation.key === key)
    if (matched) {
      selectedKey.value = matched.key
      return
    }
  }

  if (!selectedKey.value && filteredConversations.value.length > 0) {
    selectedKey.value = filteredConversations.value[0].key
    return
  }

  if (selectedKey.value && !filteredConversations.value.some((conversation) => conversation.key === selectedKey.value)) {
    selectedKey.value = filteredConversations.value[0]?.key || conversations.value[0]?.key || ''
  }
}

const markConversationAsRead = async (conversation: Conversation | null) => {
  if (!conversation) return

  const unreadIncoming = conversation.messages.filter((message) => message.direction === 'incoming' && message.status === 'unread')
  if (unreadIncoming.length === 0) return

  for (const message of unreadIncoming) {
    try {
      await messageApi.markAsRead(message.id)
      const target = messages.value.find((item) => item.id === message.id)
      if (target) target.status = 'read'
    } catch (error) {
      console.error('标记已读失败:', error)
    }
  }
}

const selectConversation = (key: string) => {
  selectedKey.value = key
}

const fetchMessages = async (options?: { silent?: boolean }) => {
  const silent = options?.silent ?? false
  if (!silent) {
    loading.value = true
  }
  if (!silent) {
    fetchError.value = ''
  }

  try {
    const response = await messageApi.getMessages() as RawMessage[]
    const previousSelectedKey = selectedKey.value
    const previousMessageCount = selectedConversation.value?.messages.length ?? 0
    const previousSelectedConversation = selectedConversation.value
    const previousIncomingIds = new Set(
      (previousSelectedConversation?.messages || [])
        .filter((message) => message.direction === 'incoming')
        .map((message) => message.id)
    )
    messages.value = normalizeMessages(response)
    syncSelectedConversation()
    const nextSelectedConversation =
      conversations.value.find((conversation) => conversation.key === (selectedKey.value || previousSelectedKey)) || null
    const nextMessageCount = nextSelectedConversation?.messages.length ?? 0
    const newIncomingCount = (nextSelectedConversation?.messages || []).filter(
      (message) => message.direction === 'incoming' && !previousIncomingIds.has(message.id)
    ).length
    if (nextMessageCount > previousMessageCount) {
      if (newIncomingCount > 0 && !isNearBottom.value) {
        newMessageNoticeCount.value += newIncomingCount
      } else {
        scrollMessagesToBottom(silent ? 'auto' : 'smooth')
      }
    }
  } catch (error) {
    console.error('获取消息失败:', error)
    if (!silent) {
      fetchError.value = '获取消息失败，请稍后重试。'
      messages.value = []
    }
  } finally {
    if (!silent) {
      loading.value = false
    }
  }
}

const handleRefreshMessages = () => {
  fetchMessages()
}

const handleSendMessage = async () => {
  if (!selectedConversation.value) return
  if (!composer.value.trim()) {
    sendError.value = '请输入消息内容。'
    return
  }

  sending.value = true
  sendError.value = ''

  try {
    const payload = await messageApi.sendMessage({
      receiverId: selectedConversation.value.counterpartId,
      taskId: selectedConversation.value.taskId ?? undefined,
      content: composer.value.trim()
    }) as Record<string, any>
    messages.value.push({
      ...payload,
      id: Number(payload.id ?? Date.now()),
      senderId: Number(payload.senderId ?? currentUser.value?.id),
      receiverId: Number(payload.receiverId ?? selectedConversation.value.counterpartId),
      taskId: payload.taskId ?? selectedConversation.value.taskId,
      content: payload.content ?? composer.value.trim(),
      status: payload.status ?? 'read',
      createdAt: payload.createdAt ?? new Date().toISOString(),
      senderName: payload.senderName ?? currentUser.value?.name,
      senderAvatarUrl: payload.senderAvatarUrl ?? currentUser.value?.avatarUrl,
      receiverName: payload.receiverName ?? selectedConversation.value.counterpartName,
      receiverAvatarUrl: payload.receiverAvatarUrl ?? selectedConversation.value.counterpartAvatarUrl,
      taskTitle: payload.taskTitle ?? selectedConversation.value.taskTitle
    })
    composer.value = ''
    syncSelectedConversation()
    scrollMessagesToBottom()
  } catch (error) {
    console.error('发送消息失败:', error)
    sendError.value = '发送消息失败，请稍后重试。'
  } finally {
    sending.value = false
  }
}

watch(
  () => [route.query.userId, route.query.taskId, messages.value.length, showUnreadOnly.value],
  () => {
    syncSelectedConversation()
  }
)

watch(
  selectedConversation,
  (conversation) => {
    markConversationAsRead(conversation)
    sendError.value = ''
    newMessageNoticeCount.value = 0
    if (conversation) {
      scrollMessagesToBottom('auto')
    }
  },
  { immediate: true }
)

onMounted(() => {
  fetchMessages()
  pollingTimer = window.setInterval(() => {
    fetchMessages({ silent: true })
  }, 5000)
})

onBeforeUnmount(() => {
  if (pollingTimer !== null) {
    window.clearInterval(pollingTimer)
    pollingTimer = null
  }
})
</script>
