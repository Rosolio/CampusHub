<template>
  <div class="min-h-screen bg-surface font-body text-on-surface pb-24 md:pb-0">

    <main class="mx-auto max-w-3xl px-6 pb-12 pt-24">
      <div class="flex items-center justify-between mb-6">
        <div>
          <h1 class="font-headline text-2xl font-extrabold text-primary md:text-3xl">通知中心</h1>
          <p class="text-sm text-on-surface-variant">查看所有系统通知和动态提醒</p>
        </div>
        <button
          v-if="notifications.some(n => !n.isRead)"
          class="rounded-full bg-primary px-4 py-2 text-sm font-bold text-white hover:bg-primary-dim transition-colors"
          @click="markAllRead"
        >
          全部已读
        </button>
      </div>

      <div v-if="loading" class="text-center py-12">
        <p class="text-on-surface-variant">加载中...</p>
      </div>

      <div v-else-if="notifications.length === 0" class="text-center py-16">
        <span class="material-symbols-outlined text-6xl text-on-surface-variant/30">notifications_off</span>
        <p class="mt-4 text-lg font-semibold text-on-surface-variant">暂无通知</p>
        <p class="mt-1 text-sm text-on-surface-variant/70">当有人回复、接单或系统消息时，会在这里显示</p>
      </div>

      <div v-else class="space-y-2">
        <div
          v-for="(group, idx) in groupedNotifications"
          :key="idx"
        >
          <p class="mt-6 mb-2 text-xs font-bold uppercase tracking-wider text-on-surface-variant">{{ group.label }}</p>
          <div
            v-for="n in group.items"
            :key="n.id"
            class="flex items-start gap-4 rounded-2xl p-4 cursor-pointer transition-all hover:bg-surface-container-lowest"
            :class="n.isRead ? 'bg-surface-container-lowest/50' : 'bg-surface-container-lowest shadow-sm ring-1 ring-primary/10'"
            @click="handleNotificationClick(n)"
          >
            <div
              class="flex h-10 w-10 shrink-0 items-center justify-center rounded-xl"
              :class="iconBgClass(n.type)"
            >
              <span class="material-symbols-outlined text-lg" :class="iconTextClass(n.type)">
                {{ iconForType(n.type) }}
              </span>
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2">
                <p class="text-sm font-bold text-[#1a0033]">{{ n.title }}</p>
                <span v-if="!n.isRead" class="h-2 w-2 rounded-full bg-primary shrink-0"></span>
              </div>
              <p class="mt-1 text-sm text-on-surface-variant line-clamp-2">{{ n.content }}</p>
              <p class="mt-1 text-xs text-on-surface-variant/60">{{ formatDate(n.createdAt) }}</p>
            </div>
            <span class="material-symbols-outlined text-on-surface-variant/40 text-lg shrink-0">chevron_right</span>
          </div>
        </div>
      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { usePreferences } from '../composables/usePreferences'
import { notificationApi } from '../services/api'

const router = useRouter()
const { formatLocaleDateTime } = usePreferences()
const notifications = ref<any[]>([])
const loading = ref(true)

const iconForType = (type: string) => {
  switch (type) {
    case 'TASK_ACCEPTED': return 'check_circle'
    case 'TASK_COMPLETED': return 'task_alt'
    case 'COMMENT_REPLY': return 'chat_bubble'
    case 'REVIEW_RECEIVED': return 'star'
    case 'FEEDBACK_REPLIED': return 'feedback'
    case 'VERIFICATION_RESULT': return 'verified'
    default: return 'notifications'
  }
}

const iconBgClass = (type: string) => {
  switch (type) {
    case 'TASK_ACCEPTED': return 'bg-cyan-100'
    case 'TASK_COMPLETED': return 'bg-emerald-100'
    case 'COMMENT_REPLY': return 'bg-violet-100'
    case 'REVIEW_RECEIVED': return 'bg-amber-100'
    case 'FEEDBACK_REPLIED': return 'bg-blue-100'
    case 'VERIFICATION_RESULT': return 'bg-emerald-100'
    default: return 'bg-slate-100'
  }
}

const iconTextClass = (type: string) => {
  switch (type) {
    case 'TASK_ACCEPTED': return 'text-cyan-700'
    case 'TASK_COMPLETED': return 'text-emerald-700'
    case 'COMMENT_REPLY': return 'text-violet-700'
    case 'REVIEW_RECEIVED': return 'text-amber-700'
    case 'FEEDBACK_REPLIED': return 'text-blue-700'
    case 'VERIFICATION_RESULT': return 'text-emerald-700'
    default: return 'text-slate-700'
  }
}

const groupedNotifications = computed(() => {
  const groups: { label: string; items: any[] }[] = []
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 86400000)

  const todayItems: any[] = []
  const yesterdayItems: any[] = []
  const earlierItems: any[] = []

  for (const n of notifications.value) {
    const d = new Date(n.createdAt)
    if (d >= today) {
      todayItems.push(n)
    } else if (d >= yesterday) {
      yesterdayItems.push(n)
    } else {
      earlierItems.push(n)
    }
  }

  if (todayItems.length) groups.push({ label: '今天', items: todayItems })
  if (yesterdayItems.length) groups.push({ label: '昨天', items: yesterdayItems })
  if (earlierItems.length) groups.push({ label: '更早', items: earlierItems })

  return groups
})

const formatDate = (d: string) => {
  if (!d) return ''
  try {
    return formatLocaleDateTime(d)
  } catch {
    return d
  }
}

const handleNotificationClick = async (n: any) => {
  if (!n.isRead) {
    try {
      await notificationApi.markAsRead(n.id)
      n.isRead = true
    } catch { /* ignore */ }
  }

  if (n.referenceType === 'task' && n.referenceId) {
    router.push(`/detail/${n.referenceId}`)
  } else if (n.referenceType === 'feedback') {
    router.push('/feedback')
  } else if (n.referenceType === 'verification') {
    router.push('/verification')
  }
}

const markAllRead = async () => {
  try {
    await notificationApi.markAllRead()
    notifications.value.forEach(n => { n.isRead = true })
  } catch { /* ignore */ }
}

const fetchNotifications = async () => {
  loading.value = true
  try {
    const data = await notificationApi.getNotifications() as any[]
    notifications.value = Array.isArray(data) ? data : []
  } catch {
    notifications.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchNotifications()
})
</script>
