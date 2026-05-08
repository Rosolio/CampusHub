<template>
  <component
    :is="tag"
    class="flex w-full items-center justify-between gap-4 rounded-xl bg-surface-container-high p-4 text-left transition-colors"
    :class="interactive ? 'cursor-pointer hover:bg-surface-container-low' : ''"
    v-bind="componentProps"
  >
    <div class="min-w-0">
      <p class="font-medium text-on-surface">{{ title }}</p>
      <p v-if="description" class="mt-1 text-sm text-on-surface-variant">{{ description }}</p>
    </div>
    <slot>
      <span
        v-if="showChevron"
        class="material-symbols-outlined shrink-0 text-on-surface-variant"
      >
        chevron_right
      </span>
    </slot>
  </component>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  title: string
  description?: string
  interactive?: boolean
  showChevron?: boolean
  type?: 'button' | 'div'
}>(), {
  description: '',
  interactive: false,
  showChevron: false,
  type: 'div'
})

const tag = computed(() => props.type)
const componentProps = computed(() => props.type === 'button' ? { type: 'button' } : {})
</script>
