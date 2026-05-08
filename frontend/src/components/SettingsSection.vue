<template>
  <section class="space-y-4">
    <h2 class="text-xl font-bold text-on-surface">{{ title }}</h2>
    <div class="space-y-3">
      <slot />
    </div>
  </section>
</template>

<script setup lang="ts">
defineProps<{
  title: string
}>()
</script>
