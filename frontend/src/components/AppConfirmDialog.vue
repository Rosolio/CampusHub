<template>
  <Teleport to="body">
    <div
      v-if="confirmState"
      class="fixed inset-0 z-[95] flex items-center justify-center bg-slate-950/45 px-6"
      @click.self="resolveConfirm(false)"
    >
      <div class="w-full max-w-md rounded-[2rem] bg-white p-6 shadow-[0_24px_80px_rgba(15,23,42,0.22)]">
        <div class="flex items-start gap-4">
          <div
            class="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl"
            :class="confirmState.tone === 'danger' ? 'bg-rose-100 text-rose-700' : 'bg-slate-100 text-slate-700'"
          >
            <span class="material-symbols-outlined">{{ confirmState.tone === 'danger' ? 'warning' : 'help' }}</span>
          </div>
          <div class="min-w-0 flex-1">
            <h2 class="text-xl font-extrabold text-slate-950">{{ confirmState.title }}</h2>
            <p class="mt-2 text-sm leading-6 text-slate-600">{{ confirmState.message }}</p>
          </div>
        </div>

        <div class="mt-6 flex justify-end gap-3">
          <button
            type="button"
            class="rounded-xl px-4 py-2.5 text-sm font-bold text-slate-600 transition hover:bg-slate-100"
            @click="resolveConfirm(false)"
          >
            {{ confirmState.cancelText || '取消' }}
          </button>
          <button
            type="button"
            class="rounded-xl px-4 py-2.5 text-sm font-bold text-white transition"
            :class="confirmState.tone === 'danger' ? 'bg-rose-600 hover:bg-rose-700' : 'bg-slate-950 hover:bg-slate-800'"
            @click="resolveConfirm(true)"
          >
            {{ confirmState.confirmText || '确认' }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { resolveConfirm, useConfirm } from '../composables/useConfirm'

const { confirmState } = useConfirm()
</script>
