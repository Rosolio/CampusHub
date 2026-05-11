<script setup lang="ts">
import AppConfirmDialog from './components/AppConfirmDialog.vue'
import AppToastHost from './components/AppToastHost.vue'
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
  <AppToastHost />
  <AppConfirmDialog />
</template>
