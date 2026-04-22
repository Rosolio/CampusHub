<template>
  <div class="mb-8">
    <RouterLink
      :to="to"
      class="group inline-flex items-center gap-2 text-sm font-semibold text-on-surface-variant transition-colors hover:text-primary"
    >
      <span class="material-symbols-outlined text-lg transition-transform group-hover:-translate-x-1">
        arrow_back
      </span>
      <span>{{ props.label === '返回设置' ? t('backToSettings') : props.label }}</span>
    </RouterLink>
  </div>
</template>

<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { usePreferences } from '../composables/usePreferences'

const props = withDefaults(defineProps<{
  to?: string
  label?: string
}>(), {
  to: '/settings',
  label: '返回设置'
})

const { t } = usePreferences()
</script>
