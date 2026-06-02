<script setup lang="ts">
import { onBeforeUnmount, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppConfirmDialog from './components/AppConfirmDialog.vue'
import AppToastHost from './components/AppToastHost.vue'

const router = useRouter()

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
  <RouterView />
  <AppToastHost />
  <AppConfirmDialog />
</template>
