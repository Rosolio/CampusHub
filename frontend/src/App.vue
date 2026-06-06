<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppBottomNav from './components/AppBottomNav.vue'
import AppConfirmDialog from './components/AppConfirmDialog.vue'
import AppToastHost from './components/AppToastHost.vue'
import AppTopNav from './components/AppTopNav.vue'

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

onMounted(() => window.addEventListener('keydown', handleKeydown))
onBeforeUnmount(() => window.removeEventListener('keydown', handleKeydown))
</script>

<template>
  <AppTopNav v-if="showMainNav" />
  <RouterView />
  <AppBottomNav v-if="showMainNav" />
  <AppToastHost />
  <AppConfirmDialog />
</template>
