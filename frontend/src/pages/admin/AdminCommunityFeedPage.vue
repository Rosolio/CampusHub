<template>
  <div class="space-y-6">
    <section v-if="error" class="rounded-[1.5rem] border border-rose-200 bg-rose-50 px-5 py-4 text-sm font-semibold text-rose-700">
      {{ error }}
    </section>

    <section class="rounded-[1.9rem] border border-[#ddd6c9] bg-[#fffdf8] p-5 shadow-[0_16px_48px_rgba(15,23,42,0.05)] md:p-6">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <p class="text-[11px] font-extrabold uppercase tracking-[0.24em] text-slate-500">Community Feed</p>
          <h2 class="mt-2 text-2xl font-extrabold tracking-tight text-slate-900">最新社区动态</h2>
        </div>
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-full bg-[#102a33] px-4 py-2 text-sm font-extrabold text-white transition hover:bg-[#163a46]"
          @click="loadContent"
        >
          <span class="material-symbols-outlined text-lg">refresh</span>
          刷新
        </button>
      </div>

      <div class="mt-6 grid gap-4 xl:grid-cols-2">
        <article
          v-for="item in contentList"
          :key="item.id"
          class="rounded-[1.5rem] border border-[#e8e0d3] bg-[#faf7f0] p-5"
        >
          <div class="flex flex-wrap items-center gap-2">
            <span class="rounded-full bg-white px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em] text-slate-800">
              {{ item.taskMode === 'topic' ? '话题帖' : '任务' }}
            </span>
            <span class="rounded-full bg-white px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em] text-slate-500">
              {{ item.category || '未分类' }}
            </span>
            <span class="rounded-full px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em]" :class="item.reviewStatus === 'approved' ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-700'">
              {{ item.reviewStatus === 'approved' ? '已展示' : '未展示' }}
            </span>
          </div>

          <h3 class="mt-4 text-xl font-extrabold text-slate-900">{{ item.title }}</h3>
          <p class="mt-3 line-clamp-4 text-sm leading-7 text-slate-600">{{ item.description }}</p>

          <div class="mt-5 grid gap-3 sm:grid-cols-2">
            <div class="rounded-2xl bg-white px-4 py-3">
              <p class="text-[11px] font-extrabold uppercase tracking-[0.18em] text-slate-500">发布者</p>
              <p class="mt-2 text-sm font-bold text-slate-800">{{ item.requesterName || `用户 #${item.requesterId}` }}</p>
            </div>
            <div class="rounded-2xl bg-white px-4 py-3">
              <p class="text-[11px] font-extrabold uppercase tracking-[0.18em] text-slate-500">发布时间</p>
              <p class="mt-2 text-sm font-bold text-slate-800">{{ formatTime(item.createdAt) }}</p>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { taskApi } from '../../services/api'

const error = ref('')
const contentList = ref<any[]>([])

const loadContent = async () => {
  error.value = ''
  try {
    const response = await taskApi.getTasks() as any
    contentList.value = Array.isArray(response) ? response : Array.isArray(response?.data) ? response.data : []
  } catch (err: any) {
    error.value = err?.response?.data?.message || '社区动态加载失败'
  }
}

const formatTime = (value?: string) => {
  if (!value) return '刚刚'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '刚刚'
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

onMounted(() => {
  loadContent()
})
</script>
