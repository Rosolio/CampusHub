<template>
  <div :class="wrapperClass">
    <label v-if="label" class="mb-2 block text-sm font-bold text-on-surface">
      {{ label }}
    </label>
    <slot />
    <p v-if="help" class="mt-2 text-xs text-on-surface-variant">
      {{ help }}
    </p>
  </div>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  label?: string
  help?: string
  wrapperClass?: string
}>(), {
  label: '',
  help: '',
  wrapperClass: ''
})
</script>
