<template>
  <Teleport to="body">
    <transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="translate-y-4 opacity-0"
      enter-to-class="translate-y-0 opacity-100"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="translate-y-0 opacity-100"
      leave-to-class="translate-y-2 opacity-0"
    >
      <div
        v-if="toast"
        class="fixed bottom-24 left-1/2 z-[90] w-[calc(100%-2rem)] max-w-md -translate-x-1/2 rounded-2xl border px-4 py-3 shadow-xl backdrop-blur"
        :class="toneClass"
      >
        <div class="flex items-start gap-3">
          <span class="material-symbols-outlined text-lg">{{ iconName }}</span>
          <p class="flex-1 text-sm font-semibold leading-6">{{ toast.message }}</p>
          <button type="button" class="rounded-full p-1 transition hover:bg-black/5" @click="closeToast">
            <span class="material-symbols-outlined text-base">close</span>
          </button>
        </div>
      </div>
    </transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { closeToast, useToast } from '../composables/useToast'

const { toast } = useToast()

const toneClass = computed(() => {
  if (toast.value?.type === 'success') return 'border-emerald-200 bg-emerald-50/95 text-emerald-900'
  if (toast.value?.type === 'error') return 'border-rose-200 bg-rose-50/95 text-rose-900'
  return 'border-slate-200 bg-white/95 text-slate-900'
})

const iconName = computed(() => {
  if (toast.value?.type === 'success') return 'check_circle'
  if (toast.value?.type === 'error') return 'error'
  return 'info'
})
</script>
