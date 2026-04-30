<template>
  <div class="space-y-5">
    <section v-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 px-5 py-4 text-sm font-semibold text-rose-700">
      {{ error }}
    </section>

    <section class="admin-panel p-5 md:p-6">
      <div class="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="admin-kicker">Moderation</p>
          <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">内容审核台</h2>
        </div>
        <div class="flex flex-wrap gap-3">
          <div class="rounded-xl bg-amber-50 px-4 py-2 text-sm font-extrabold text-amber-700">
            待审核 {{ pendingReviewCount }}
          </div>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-700 transition hover:bg-slate-50"
            @click="loadTasks"
          >
            <span class="material-symbols-outlined text-lg">refresh</span>
            刷新
          </button>
        </div>
      </div>

      <div class="admin-panel-soft mt-6 p-4">
        <label class="block text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">关键词搜索</label>
        <div class="mt-3 flex items-center gap-3 rounded-2xl border border-slate-200 bg-white px-4 py-3">
          <span class="material-symbols-outlined text-slate-400">search</span>
          <input
            v-model.trim="keyword"
            type="text"
            class="w-full border-0 bg-transparent text-sm font-semibold text-slate-800 outline-none placeholder:text-slate-400"
            placeholder="搜索标题、正文、分类或发布者"
          />
        </div>
      </div>

      <div v-if="filteredTasks.length === 0" class="mt-6 rounded-3xl border border-dashed border-slate-300 bg-slate-50 px-5 py-10 text-center">
        <div class="mx-auto flex h-14 w-14 items-center justify-center rounded-full bg-white text-slate-500">
          <span class="material-symbols-outlined">fact_check</span>
        </div>
        <p class="mt-4 text-lg font-extrabold text-slate-900">当前没有待审核内容</p>
        <p class="mt-2 text-sm font-medium text-slate-500">已审核内容不会继续显示在这里。你也可以尝试调整搜索关键词。</p>
      </div>

      <div v-else class="mt-6 grid gap-4">
        <article
          v-for="task in filteredTasks"
          :key="task.id"
          class="overflow-hidden rounded-3xl border border-slate-200 bg-white transition hover:border-slate-300"
        >
          <div class="grid gap-0 xl:grid-cols-[minmax(0,1fr)_14rem]">
            <div class="p-4 md:p-5">
              <div class="flex flex-wrap items-center gap-2">
                <span class="rounded-full bg-slate-100 px-3 py-1 text-xs font-extrabold uppercase tracking-[0.14em] text-slate-700">
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

              <p v-if="task.reviewNote" class="mt-4 rounded-2xl bg-amber-50 px-3 py-2 text-sm font-semibold text-amber-700">
                审核备注：{{ task.reviewNote }}
              </p>
            </div>

            <div class="border-t border-slate-200 bg-slate-50 p-4 xl:border-l xl:border-t-0">
              <p class="text-[11px] font-extrabold uppercase tracking-[0.22em] text-slate-400">Decision</p>
              <div class="mt-4 grid gap-2.5">
                <button
                  type="button"
                  class="rounded-xl bg-slate-950 px-4 py-3 text-sm font-extrabold text-white transition hover:bg-slate-800 disabled:opacity-45"
                  :disabled="pendingTaskIds.has(task.id)"
                  @click="reviewTask(task, 'approved')"
                >
                  通过发布
                </button>
                <button
                  type="button"
                  class="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3 text-sm font-extrabold text-rose-700 transition hover:bg-rose-100 disabled:opacity-45"
                  :disabled="pendingTaskIds.has(task.id)"
                  @click="reviewTask(task, 'rejected')"
                >
                  驳回内容
                </button>
                <button
                  type="button"
                  class="rounded-xl border border-slate-200 bg-white px-4 py-3 text-sm font-extrabold text-slate-700 transition hover:bg-slate-100 disabled:opacity-45"
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
const keyword = ref('')

const pendingTasks = computed(() => tasks.value.filter((task) => task.reviewStatus === 'pending_review'))
const pendingReviewCount = computed(() => pendingTasks.value.length)
const filteredTasks = computed(() => {
  const query = keyword.value.toLowerCase()
  if (!query) {
    return pendingTasks.value
  }

  return pendingTasks.value.filter((task) => [
    task.title,
    task.description,
    task.category,
    task.requesterName,
    String(task.requesterId)
  ].some((value) => String(value || '').toLowerCase().includes(query)))
})

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
    tasks.value = tasks.value.filter((item) => item.id !== task.id)
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
