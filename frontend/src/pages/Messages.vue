<template>
  <div class="h-[100dvh] overflow-hidden bg-surface font-body text-on-surface">
    <main class="mx-auto flex h-full max-w-4xl flex-col">
      <!-- Header -->
      <div class="flex shrink-0 items-center justify-between px-5 py-4">
        <div>
          <h1 class="text-xl font-extrabold text-teal-900">消息</h1>
          <p class="text-xs text-on-surface-variant">{{ filteredConversations.length }} 个会话</p>
        </div>
        <div class="flex items-center gap-2">
          <button
            class="rounded-full px-3 py-1.5 text-xs font-bold transition-colors"
            :class="showUnreadOnly ? 'bg-teal-900 text-white' : 'bg-surface-container-low text-teal-900 hover:bg-surface-container-high'"
            @click="showUnreadOnly = !showUnreadOnly"
          >{{ showUnreadOnly ? '显示全部' : '未读' }}</button>
          <button
            class="flex items-center gap-1 rounded-full bg-surface-container-low px-3 py-1.5 text-xs font-bold text-teal-900 hover:bg-surface-container-high"
            :disabled="loading"
            @click="handleRefreshMessages"
          >
            <span class="material-symbols-outlined text-sm">refresh</span>
          </button>
        </div>
      </div>

      <div v-if="fetchError" class="mx-5 mb-2 shrink-0 rounded-2xl border border-rose-200 bg-rose-50 px-4 py-2 text-xs font-medium text-rose-700">
        {{ fetchError }}
      </div>

      <!-- Content: List or Chat -->
      <div class="flex min-h-0 flex-1">
        <!-- Conversation List -->
        <div class="flex w-full min-w-0 flex-col overflow-hidden" :class="{ 'hidden sm:flex': !!selectedConversation }">
          <div v-if="filteredConversations.length === 0 && !loading" class="flex-1 flex items-center justify-center p-8">
            <div class="text-center">
              <span class="material-symbols-outlined text-5xl text-on-surface-variant/25">chat</span>
              <p class="mt-4 text-sm font-semibold text-on-surface-variant">暂无消息</p>
              <p class="mt-1 text-xs text-on-surface-variant/60">发布或参与任务后，消息会显示在这里</p>
            </div>
          </div>
          <div v-else class="flex-1 overflow-y-auto px-4">
            <div
              v-for="conv in filteredConversations"
              :key="conv.key"
              class="flex cursor-pointer items-center gap-3 rounded-2xl px-3 py-3 transition-colors hover:bg-surface-container-lowest"
              :class="{ 'bg-surface-container-lowest shadow-sm': selectedConversation?.key === conv.key }"
              @click="selectConversation(conv.key); mobileShowChat = true"
            >
              <!-- Avatar -->
              <div class="relative shrink-0">
                <img
                  :src="conv.counterpartAvatarUrl || defaultAvatarUrl"
                  :alt="conv.counterpartName"
                  class="h-12 w-12 rounded-full object-cover"
                />
                <span
                  v-if="conv.unreadCount > 0"
                  class="absolute -right-1 -top-1 flex h-5 w-5 items-center justify-center rounded-full bg-primary text-[10px] font-extrabold text-white"
                >{{ conv.unreadCount > 9 ? '9+' : conv.unreadCount }}</span>
              </div>

              <!-- Info -->
              <div class="min-w-0 flex-1">
                <div class="flex items-center justify-between gap-2">
                  <p class="truncate text-sm font-extrabold text-teal-900">{{ conv.counterpartName }}</p>
                  <span class="shrink-0 text-[11px] text-on-surface-variant/60">{{ formatTime(conv.lastCreatedAt) }}</span>
                </div>
                <p class="truncate text-xs text-on-surface-variant/70">{{ conv.taskTitle }}</p>
                <p class="mt-0.5 truncate text-[13px] leading-relaxed" :class="conv.unreadCount > 0 ? 'font-semibold text-on-surface' : 'text-on-surface-variant'">
                  <span v-if="conv.lastDirection === 'outgoing'" class="mr-1 text-on-surface-variant/50">你:</span>
                  {{ conv.lastMessage }}
                </p>
              </div>

              <!-- System badge -->
              <span v-if="conv.isSystemChannel" class="shrink-0 rounded-full bg-amber-100 px-2 py-0.5 text-[9px] font-extrabold uppercase tracking-wider text-amber-700">系统</span>
            </div>
          </div>
        </div>

        <!-- Chat View -->
        <div
          v-if="selectedConversation"
          class="flex w-full min-w-0 flex-col border-l border-outline-variant/10 bg-surface-container-lowest/60"
          :class="{ 'hidden sm:flex': !mobileShowChat }"
        >
          <!-- Chat Header -->
          <div class="flex shrink-0 items-center gap-3 border-b border-outline-variant/10 px-4 py-3">
            <button
              class="rounded-full p-1.5 text-on-surface-variant hover:bg-surface-container-low sm:hidden"
              @click="mobileShowChat = false"
            >
              <span class="material-symbols-outlined">arrow_back</span>
            </button>
            <img
              v-if="!selectedConversation.isSystemChannel"
              :src="selectedConversation.counterpartAvatarUrl || defaultAvatarUrl"
              :alt="selectedConversation.counterpartName"
              class="h-10 w-10 rounded-full object-cover"
            />
            <div class="min-w-0 flex-1">
              <p class="truncate text-sm font-extrabold text-teal-900">{{ selectedConversation.counterpartName }}</p>
              <p class="truncate text-xs text-on-surface-variant">{{ selectedConversation.taskTitle }}</p>
            </div>
            <RouterLink
              v-if="canViewSelectedTaskDetail"
              :to="`/detail/${selectedConversation!.taskId}`"
              class="rounded-full bg-teal-900 px-4 py-1.5 text-xs font-bold text-white hover:bg-teal-800"
            >查看帖子</RouterLink>
          </div>

          <!-- Messages -->
          <div
            ref="messageViewport"
            class="flex-1 overflow-y-auto px-4 py-3"
            @scroll="handleScroll"
          >
            <!-- New messages notice -->
            <div
              v-if="newMessageNoticeCount > 0"
              class="mb-3 cursor-pointer rounded-full bg-primary px-4 py-2 text-center text-xs font-extrabold text-white shadow-lg"
              @click="newMessageNoticeCount = 0; scrollMessagesToBottom('smooth')"
            >{{ newMessageNoticeCount }} 条新消息</div>

            <div v-for="(group, idx) in chatMessageGroups" :key="idx">
              <p class="my-3 text-center text-[11px] font-semibold text-on-surface-variant/50">{{ group.label }}</p>
              <div
                v-for="msg in group.items"
                :key="msg.id"
                class="mb-2 flex"
                :class="msg.direction === 'outgoing' ? 'justify-end' : 'justify-start'"
              >
                <!-- System message -->
                <div
                  v-if="msg.direction === 'system'"
                  class="mx-auto max-w-sm rounded-2xl bg-amber-50 px-4 py-2.5 text-center"
                >
                  <p class="text-xs leading-relaxed text-amber-800">{{ msg.content }}</p>
                  <button
                    v-if="msg.taskId"
                    class="mt-2 rounded-full bg-amber-200 px-3 py-1 text-[10px] font-extrabold text-amber-900 hover:bg-amber-300"
                    @click="router.push(`/detail/${msg.taskId}`)"
                  >查看详情</button>
                </div>

                <!-- User message -->
                <div v-else class="max-w-[75%]">
                  <div
                    class="rounded-2xl px-4 py-2.5 text-sm leading-relaxed"
                    :class="msg.direction === 'outgoing'
                      ? 'bg-teal-900 text-white rounded-br-md'
                      : 'bg-white text-on-surface rounded-bl-md shadow-sm border border-outline-variant/10'"
                  >{{ msg.content }}</div>
                  <p class="mt-0.5 text-[10px] text-on-surface-variant/50" :class="msg.direction === 'outgoing' ? 'text-right' : 'text-left'">
                    {{ formatMessageTime(msg.createdAt) }}
                  </p>
                </div>
              </div>
            </div>

            <div v-if="selectedConversation.messages.length === 0" class="flex-1 flex items-center justify-center py-16">
              <p class="text-sm text-on-surface-variant/50">暂无消息，发送第一条开始沟通</p>
            </div>
          </div>

          <!-- Composer -->
          <div class="shrink-0 border-t border-outline-variant/10 bg-surface-container-lowest px-4 py-3">
            <div v-if="sendError" class="mb-2 text-[11px] font-medium text-rose-600">{{ sendError }}</div>
            <div class="flex items-end gap-2">
              <textarea
                v-model="composer"
                rows="1"
                class="min-h-[42px] max-h-32 flex-1 resize-none rounded-2xl border border-outline-variant/15 bg-surface-container-low px-4 py-2.5 text-sm outline-none transition focus:border-primary/50"
                placeholder="输入消息..."
                :disabled="sending"
                @keydown.enter.exact.prevent="handleSendMessage"
                @input="autoResize"
              ></textarea>
              <button
                class="flex h-[42px] w-[42px] shrink-0 items-center justify-center rounded-full bg-teal-900 text-white transition hover:bg-teal-800 disabled:opacity-40"
                :disabled="sending || !composer.trim()"
                @click="handleSendMessage"
              >
                <span class="material-symbols-outlined text-lg">{{ sending ? 'progress_activity' : 'send' }}</span>
              </button>
            </div>
          </div>
        </div>

        <!-- Empty chat state -->
        <div v-if="!selectedConversation && !mobileShowChat" class="hidden flex-1 items-center justify-center border-l border-outline-variant/10 sm:flex">
          <div class="text-center px-8">
            <span class="material-symbols-outlined text-6xl text-on-surface-variant/15">forum</span>
            <p class="mt-4 text-lg font-extrabold text-teal-900">选择一个会话</p>
            <p class="mt-1 text-sm text-on-surface-variant/60">左侧选择会话后开始聊天</p>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
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
  createdAt: string
  senderName?: string
  senderAvatarUrl?: string
  receiverName?: string
  receiverAvatarUrl?: string
  taskTitle?: string
}

const router = useRouter()
const { formatLocaleDateLabel } = usePreferences()

const messages = ref<RawMessage[]>([])
const selectedKey = ref('')
const composer = ref('')
const loading = ref(false)
const sending = ref(false)
const showUnreadOnly = ref(false)
const mobileShowChat = ref(false)
const fetchError = ref('')
const sendError = ref('')
const messageViewport = ref<HTMLElement | null>(null)
const newMessageNoticeCount = ref(0)

const currentUser = computed(() => storedUser.value || {})
const defaultAvatarUrl = DEFAULT_AVATAR_URL

// === Conversation Aggregation ===

const toConversationKey = (taskId: number | null, counterpartId: number) =>
  `${taskId ?? 'no-task'}-${counterpartId}`

const conversations = computed(() => {
  const map = new Map<string, {
    key: string
    counterpartId: number
    counterpartName: string
    counterpartAvatarUrl: string
    taskId: number | null
    taskTitle: string
    unreadCount: number
    lastMessage: string
    lastDirection: string
    lastCreatedAt: string
    hasSystemReminder: boolean
    isSystemChannel: boolean
    messages: any[]
  }>()

  for (const raw of messages.value) {
    const isOutgoing = Number(raw.senderId) === Number(currentUser.value?.id)
    const counterpartId = isOutgoing ? Number(raw.receiverId) : Number(raw.senderId)
    const taskId = raw.taskId == null ? null : Number(raw.taskId)
    const isSystem = (raw.content || '').startsWith('【')

    const key = isSystem ? 'system-channel' : toConversationKey(taskId, counterpartId)

    let conv = map.get(key)
    if (!conv) {
      conv = {
        key,
        counterpartId: isSystem ? 0 : counterpartId,
        counterpartName: isSystem ? '系统通知' : (isOutgoing ? raw.receiverName : raw.senderName) || `用户 #${counterpartId}`,
        counterpartAvatarUrl: isSystem ? '' : (isOutgoing ? raw.receiverAvatarUrl : raw.senderAvatarUrl) || '',
        taskId: isSystem ? null : taskId,
        taskTitle: isSystem ? '系统消息与提醒' : raw.taskTitle || '未关联任务',
        unreadCount: 0,
        lastMessage: '',
        lastDirection: '',
        lastCreatedAt: '',
        hasSystemReminder: false,
        isSystemChannel: isSystem,
        messages: []
      }
      map.set(key, conv)
    }

    conv.messages.push({
      ...raw,
      direction: isSystem ? 'system' : isOutgoing ? 'outgoing' : 'incoming',
      taskId: raw.taskId == null ? null : Number(raw.taskId)
    })

    if (!isOutgoing && raw.status !== 'read') conv.unreadCount++
    if (isSystem) conv.hasSystemReminder = true
  }

  const result = Array.from(map.values())
  for (const conv of result) {
    conv.messages.sort((a, b) => new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime())
    const last = conv.messages[conv.messages.length - 1]
    if (last) {
      conv.lastMessage = last.content
      conv.lastDirection = last.direction
      conv.lastCreatedAt = last.createdAt
    }
  }

  result.sort((a, b) => new Date(b.lastCreatedAt || 0).getTime() - new Date(a.lastCreatedAt || 0).getTime())

  // System channel always at top
  const systemChannel = result.find(c => c.isSystemChannel)
  const regularChannels = result.filter(c => !c.isSystemChannel)
  return systemChannel ? [systemChannel, ...regularChannels] : regularChannels
})

const filteredConversations = computed(() => {
  if (showUnreadOnly.value) return conversations.value.filter(c => c.unreadCount > 0)
  return conversations.value
})

const selectedConversation = computed(() =>
  conversations.value.find(c => c.key === selectedKey.value) || null
)

const canViewSelectedTaskDetail = computed(() => {
  const c = selectedConversation.value
  return c && c.taskId && !c.isSystemChannel
})

// === Message grouping for chat ===

const chatMessageGroups = computed(() => {
  const msgs = selectedConversation.value?.messages || []
  if (!msgs.length) return []

  const groups: { label: string; items: any[] }[] = []
  let currentLabel = ''
  let currentItems: any[] = []

  for (const msg of msgs) {
    const label = formatLocaleDateLabel(msg.createdAt) || ''

    if (label !== currentLabel) {
      if (currentItems.length) groups.push({ label: currentLabel, items: currentItems })
      currentLabel = label
      currentItems = []
    }
    currentItems.push(msg)
  }
  if (currentItems.length) groups.push({ label: currentLabel, items: currentItems })

  return groups
})

// === Helpers ===

const formatTime = (d?: string) => {
  if (!d) return ''
  const date = new Date(d)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  if (diff < 86400000 && date.getDate() === now.getDate()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  }
  if (diff < 172800000 && date.getDate() === now.getDate() - 1) return '昨天'
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

const formatMessageTime = (d?: string) => {
  if (!d) return ''
  const date = new Date(d)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const autoResize = (e: Event) => {
  const el = e.target as HTMLTextAreaElement
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 140) + 'px'
}

// === Data Fetching ===

const fetchMessages = async (options?: { silent?: boolean }) => {
  const silent = options?.silent ?? false
  if (!silent) { loading.value = true; fetchError.value = '' }

  try {
    const response = await messageApi.getMessages() as RawMessage[]

    if (silent && messages.value.length > 0) {
      const existingIds = new Set(messages.value.map(m => m.id))
      const newMessages = response.filter(m => !existingIds.has(m.id))
      if (newMessages.length > 0) {
        messages.value = [...messages.value, ...newMessages]
        if (selectedConversation.value && !mobileShowChat.value) {
          newMessageNoticeCount.value += newMessages.filter(
            m => Number(m.receiverId) === Number(currentUser.value?.id)
          ).length
        }
      }
    } else {
      const previousCount = messages.value.length
      messages.value = response
      if (silent && response.length !== previousCount && selectedConversation.value && !mobileShowChat.value) {
        newMessageNoticeCount.value += Math.max(0, response.length - previousCount)
      }
    }
  } catch (error) {
    console.error('获取消息失败:', error)
    if (!silent) fetchError.value = '获取消息失败，请稍后重试。'
  } finally {
    if (!silent) loading.value = false
  }
}

const handleRefreshMessages = () => fetchMessages()

const handleSendMessage = async () => {
  if (!selectedConversation.value) return
  if (!composer.value.trim()) { sendError.value = '请输入消息内容。'; return }

  sending.value = true
  sendError.value = ''

  try {
    const payload = await messageApi.sendMessage({
      receiverId: selectedConversation.value.counterpartId,
      taskId: selectedConversation.value.taskId ?? undefined,
      content: composer.value.trim()
    }) as Record<string, any>

    messages.value.push({
      id: payload.id || Date.now(),
      senderId: Number(currentUser.value?.id),
      receiverId: selectedConversation.value.counterpartId,
      taskId: selectedConversation.value.taskId ?? null,
      content: composer.value.trim(),
      createdAt: payload.createdAt || new Date().toISOString(),
      status: 'read'
    })

    composer.value = ''
    scrollMessagesToBottom('smooth')
  } catch (err: any) {
    sendError.value = err?.response?.data?.message || '发送失败，请稍后重试。'
  } finally {
    sending.value = false
  }
}

const markConversationAsRead = async (conv: any) => {
  if (!conv) return
  const unreadIds = (conv.messages || [])
    .filter((m: any) => m.direction === 'incoming' && m.status !== 'read')
    .map((m: any) => Number(m.id))
  if (unreadIds.length > 0) {
    try {
      await messageApi.markAsReadBatch(unreadIds)
      for (const id of unreadIds) {
        const msg = messages.value.find(m => m.id === id)
        if (msg) msg.status = 'read'
      }
    } catch { /* ignore */ }
  }
}

const selectConversation = (key: string) => {
  selectedKey.value = key
  mobileShowChat.value = true
  newMessageNoticeCount.value = 0
}

const scrollMessagesToBottom = (behavior: ScrollBehavior = 'auto') => {
  nextTick(() => {
    const el = messageViewport.value
    if (el) el.scrollTo({ top: el.scrollHeight, behavior })
  })
}

const handleScroll = () => {
  const el = messageViewport.value
  if (!el) return
  const threshold = 60
  if (el.scrollHeight - el.scrollTop - el.clientHeight < threshold) {
    newMessageNoticeCount.value = 0
  }
}

// === Watch & Lifecycle ===

watch(selectedConversation, (conv) => {
  markConversationAsRead(conv)
  sendError.value = ''
  if (conv) scrollMessagesToBottom('auto')
}, { immediate: true })

let pollingTimer: number | null = null
let lastUnreadCount = 0

onMounted(async () => {
  await fetchMessages()

  try {
    const res = await messageApi.getUnreadCount() as any
    lastUnreadCount = res?.count ?? 0
  } catch { /* ignore */ }

  pollingTimer = window.setInterval(async () => {
    try {
      const res = await messageApi.getUnreadCount() as any
      const currentCount = res?.count ?? 0
      if (currentCount !== lastUnreadCount) {
        lastUnreadCount = currentCount
        await fetchMessages({ silent: true })
      }
    } catch { /* ignore */ }
  }, 5000)
})

onBeforeUnmount(() => {
  if (pollingTimer !== null) {
    window.clearInterval(pollingTimer)
    pollingTimer = null
  }
})
</script>
