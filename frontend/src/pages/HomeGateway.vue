<template>
  <AdminCommunityHome v-if="isAdmin" />
  <Discovery v-else />
</template>

<script setup lang="ts">
import Discovery from './Discovery.vue'
import AdminCommunityHome from './AdminCommunityHome.vue'
import { isAdminUser } from '../utils/auth'

const isAdmin = isAdminUser()
</script>
