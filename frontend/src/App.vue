<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppBottomNav from './components/AppBottomNav.vue'
import AppConfirmDialog from './components/AppConfirmDialog.vue'
import AppToastHost from './components/AppToastHost.vue'
import AppTopNav from './components/AppTopNav.vue'
import { getStoredToken, hasValidAuthToken } from './utils/auth'

const route = useRoute()
const router = useRouter()

const showMainNav = computed(() => {
  const p = route.path
  if (p === '/auth' || p === '/login' || p === '/register') return false
  if (p.startsWith('/admin')) return false
  if (p === '/privacy' || p === '/agreement') return false
  return true
})

const handleKeydown = (e: KeyboardEvent) => {
  if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
    e.preventDefault()
    router.push('/search')
  }
}

// WebSocket online tracking
let ws: WebSocket | null = null
let wsReconnectTimer: ReturnType<typeof setTimeout> | null = null

const connectWebSocket = () => {
  if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) return
  if (!hasValidAuthToken()) return

  const token = getStoredToken()
  if (!token) return

  const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  const wsUrl = `${protocol}//${window.location.host}/ws/online?token=${encodeURIComponent(token)}`

  try {
    ws = new WebSocket(wsUrl)
  } catch {
    return
  }

  ws.onopen = () => {
    // Send heartbeat every 30 seconds
    const heartbeat = setInterval(() => {
      if (ws && ws.readyState === WebSocket.OPEN) {
        ws.send('ping')
      } else {
        clearInterval(heartbeat)
      }
    }, 30000)
  }

  ws.onclose = () => {
    ws = null
    // Reconnect after 5 seconds if still authenticated
    wsReconnectTimer = setTimeout(() => {
      if (hasValidAuthToken()) connectWebSocket()
    }, 5000)
  }

  ws.onerror = () => {
    ws?.close()
  }
}

const disconnectWebSocket = () => {
  if (wsReconnectTimer) {
    clearTimeout(wsReconnectTimer)
    wsReconnectTimer = null
  }
  if (ws) {
    ws.onclose = null  // prevent reconnect
    ws.close()
    ws = null
  }
}

onMounted(() => {
  window.addEventListener('keydown', handleKeydown)
  connectWebSocket()
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
  disconnectWebSocket()
})
</script>

<template>
  <AppTopNav v-if="showMainNav" />
  <RouterView />
  <AppBottomNav v-if="showMainNav" />
  <AppToastHost />
  <AppConfirmDialog />
</template>
