<template>
  <div class="relative inline-flex">
    <button
      type="button"
      class="flex h-10 w-10 items-center justify-center rounded-full bg-surface-container-low text-lg transition hover:bg-surface-container-high disabled:cursor-not-allowed disabled:opacity-45"
      :disabled="disabled"
      :aria-expanded="open"
      aria-label="选择 emoji"
      @click="open = !open"
    >
      <span aria-hidden="true">😊</span>
    </button>

    <div
      v-if="open"
      class="absolute bottom-full mb-2 grid w-64 grid-cols-8 gap-1 rounded-2xl border border-outline-variant/15 bg-white p-3 shadow-xl"
      :class="align === 'right' ? 'right-0' : 'left-0'"
    >
      <button
        v-for="emoji in emojis"
        :key="emoji"
        type="button"
        class="flex h-8 w-8 items-center justify-center rounded-xl text-lg transition hover:bg-surface-container-low"
        :aria-label="`插入 ${emoji}`"
        @click="selectEmoji(emoji)"
      >
        {{ emoji }}
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

withDefaults(defineProps<{
  align?: 'left' | 'right'
  disabled?: boolean
}>(), {
  align: 'left',
  disabled: false
})

const emit = defineEmits<{
  select: [emoji: string]
}>()

const open = ref(false)

const emojis = [
  '😊', '😂', '😍', '🥰', '😎', '😭', '😅', '👍',
  '👏', '🙏', '💪', '🤝', '🔥', '✨', '🎉', '💡',
  '❤️', '💚', '⭐', '🌟', '📌', '📚', '☕', '🍜',
  '🏃', '🧭', '🕒', '📦', '📝', '✅', '❓', '💬'
]

const selectEmoji = (emoji: string) => {
  emit('select', emoji)
  open.value = false
}
</script>
