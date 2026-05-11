<template>
  <AdminCommunityHome v-if="isAdmin" />
  <Discovery v-else />
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Discovery from './Discovery.vue'
import AdminCommunityHome from './AdminCommunityHome.vue'
import { isAdminUser, storedUser } from '../utils/auth'

const isAdmin = computed(() => isAdminUser() || String(storedUser.value?.role || '').toUpperCase() === 'ADMIN')
</script>
