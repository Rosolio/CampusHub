<template>
  <div class="space-y-6">
    <section v-if="error" class="rounded-[1.5rem] border border-rose-200 bg-rose-50 px-5 py-4 text-sm font-semibold text-rose-700">
      {{ error }}
    </section>

    <section class="rounded-[1.9rem] border border-[#ddd6c9] bg-[#fffdf8] p-5 shadow-[0_16px_48px_rgba(15,23,42,0.05)] md:p-6">
      <div class="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-[11px] font-extrabold uppercase tracking-[0.24em] text-slate-500">Moderation Queue</p>
          <h2 class="mt-2 text-2xl font-extrabold tracking-tight text-slate-900">内容审核台</h2>
        </div>
        <div class="flex flex-wrap gap-3">
          <div class="rounded-full bg-[#fef0c7] px-4 py-2 text-sm font-extrabold text-[#a16207]">
            待审核 {{ pendingReviewCount }}
          </div>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-full bg-[#102a33] px-4 py-2 text-sm font-extrabold text-white transition hover:bg-[#163a46]"
            @click="loadTasks"
          >
            <span class="material-symbols-outlined text-lg">refresh</span>
            刷新
          </button>
        </div>
      </div>

      <div class="mt-6 grid gap-4">
        <article
          v-for="task in tasks"
          :key="task.id"
          class="overflow-hidden rounded-[1.5rem] border border-[#ece5d8] bg-[#fcf8f2] transition hover:-translate-y-0.5 hover:shadow-[0_14px_36px_rgba(15,23,42,0.06)]"
        >
          <div class="grid gap-0 xl:grid-cols-[minmax(0,1fr)_15rem]">
            <div class="p-4 md:p-5">
              <div class="flex flex-wrap items-center gap-2">
                <span class="rounded-full bg-white px-3 py-1 text-xs font-extrabold uppercase tracking-[0.14em] text-slate-700">
                  {{ task.category || '未分类' }}
                </span>
                <span
                  class="rounded-full px-3 py-1 text-xs font-extrabold uppercase tracking-[0.14em]"
                  :class="reviewBadgeClass(task.reviewStatus)"
                >
                  {{ reviewLabel(task.reviewStatus) }}
                </span>
              </div>

              <h3 class="mt-4 text-xl font-extrabold leading-tight text-slate-900">{{ task.title }}</h3>
              <p class="mt-3 text-sm leading-7 text-slate-600">{{ task.description }}</p>

              <div class="mt-4 flex flex-wrap gap-x-5 gap-y-2 text-[12px] font-bold uppercase tracking-[0.16em] text-slate-500">
                <span>内容 ID {{ task.id }}</span>
                <span>发布者 {{ task.requesterName || `#${task.requesterId}` }}</span>
              </div>

              <p v-if="task.reviewNote" class="mt-4 rounded-2xl bg-[#fff4d8] px-3 py-2 text-sm font-semibold text-[#9a6700]">
                审核备注：{{ task.reviewNote }}
              </p>
            </div>

            <div class="border-t border-[#ece5d8] bg-[#1f2329] p-4 text-white xl:border-l xl:border-t-0">
              <p class="text-[11px] font-extrabold uppercase tracking-[0.22em] text-white/48">Decision Panel</p>
              <div class="mt-4 grid gap-2.5">
                <button
                  type="button"
                  class="rounded-full bg-[#1c9c8f] px-4 py-3 text-sm font-extrabold text-white transition hover:bg-[#147d72] disabled:opacity-45"
                  :disabled="pendingTaskIds.has(task.id)"
                  @click="reviewTask(task, 'approved')"
                >
                  通过发布
                </button>
                <button
                  type="button"
                  class="rounded-full bg-[#cf4c1f] px-4 py-3 text-sm font-extrabold text-white transition hover:bg-[#ac3a12] disabled:opacity-45"
                  :disabled="pendingTaskIds.has(task.id)"
                  @click="reviewTask(task, 'rejected')"
                >
                  驳回内容
                </button>
                <button
                  type="button"
                  class="rounded-full border border-white/14 bg-white/8 px-4 py-3 text-sm font-extrabold text-white transition hover:bg-white/12 disabled:opacity-45"
                  :disabled="pendingTaskIds.has(task.id)"
                  @click="reviewTask(task, 'pending_review')"
                >
                  挂起待审
                </button>
              </div>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { adminApi } from '../../services/api'
import type { AdminTask } from './adminTypes'

const error = ref('')
const tasks = ref<AdminTask[]>([])
const pendingTaskIds = ref(new Set<number>())

const pendingReviewCount = computed(() => tasks.value.filter((task) => task.reviewStatus === 'pending_review').length)

const loadTasks = async () => {
  error.value = ''
  try {
    tasks.value = await adminApi.getTasks() as unknown as AdminTask[]
  } catch (err: any) {
    error.value = err?.response?.data?.message || '审核数据加载失败'
  }
}

const reviewTask = async (task: AdminTask, reviewStatus: 'approved' | 'rejected' | 'pending_review') => {
  const reviewNote = window.prompt('请输入审核备注（可留空）', task.reviewNote || '') || ''
  pendingTaskIds.value.add(task.id)
  try {
    await adminApi.reviewTask(task.id, { reviewStatus, reviewNote })
    await loadTasks()
  } catch (err: any) {
    error.value = err?.response?.data?.message || '内容审核失败'
  } finally {
    pendingTaskIds.value.delete(task.id)
  }
}

const reviewLabel = (value: string) => {
  if (value === 'approved') return '已通过'
  if (value === 'rejected') return '已驳回'
  return '待审核'
}

const reviewBadgeClass = (value: string) => {
  if (value === 'approved') return 'bg-emerald-100 text-emerald-800'
  if (value === 'rejected') return 'bg-rose-100 text-rose-700'
  return 'bg-amber-100 text-amber-800'
}

onMounted(() => {
  loadTasks()
})
</script>
