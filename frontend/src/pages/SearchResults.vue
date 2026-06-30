<template>
  <div class="min-h-screen bg-surface font-body text-on-surface pb-24 md:pb-0">

    <main class="mx-auto max-w-4xl px-6 pb-12 pt-24">
      <PageBackHeader to="/home" label="返回社区" />

      <div class="mt-6">
        <div class="relative">
          <span class="material-symbols-outlined absolute left-4 top-1/2 -translate-y-1/2 text-on-surface-variant">search</span>
          <input
            ref="searchInput"
            v-model="query"
            type="text"
            class="w-full rounded-2xl border border-outline-variant/20 bg-surface-container-lowest py-4 pl-12 pr-4 text-lg font-medium transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
            placeholder="搜索任务、话题、关键词..."
            @input="onQueryChange"
          />
        </div>

        <div class="mt-4 flex flex-wrap items-center gap-2">
          <button
            v-for="opt in modeOptions"
            :key="opt.value"
            type="button"
            class="rounded-full px-4 py-2 text-sm font-semibold transition-all"
            :class="mode === opt.value ? 'bg-primary text-white shadow-sm' : 'bg-surface-container-low text-primary hover:bg-surface-container-high'"
            @click="mode = opt.value; doSearch()"
          >
            {{ opt.label }}
          </button>
        </div>
      </div>

      <div v-if="loading" class="mt-10 text-center">
        <p class="text-on-surface-variant text-sm">搜索中...</p>
      </div>

      <div v-else-if="error" class="mt-10 rounded-2xl border border-rose-200 bg-rose-50 px-5 py-4 text-sm font-medium text-rose-700">
        {{ error }}
      </div>

      <div v-else-if="!hasSearched" class="mt-16 text-center">
        <span class="material-symbols-outlined text-6xl text-on-surface-variant/30">search</span>
        <p class="mt-4 text-lg font-semibold text-on-surface-variant">输入关键词开始搜索</p>
        <p class="mt-1 text-sm text-on-surface-variant/70">可搜索标题、描述、分类和地点</p>
      </div>

      <div v-else-if="results.length === 0" class="mt-16 text-center">
        <span class="material-symbols-outlined text-6xl text-on-surface-variant/30">search_off</span>
        <p class="mt-4 text-lg font-semibold text-on-surface-variant">未找到 "{{ query }}" 的相关结果</p>
        <p class="mt-1 text-sm text-on-surface-variant/70">尝试使用其他关键词或修改筛选条件</p>
      </div>

      <div v-else class="mt-8">
        <p class="mb-4 text-sm font-medium text-on-surface-variant">共找到 {{ total }} 条结果</p>

        <div class="grid gap-4">
          <RouterLink
            v-for="task in results"
            :key="task.id"
            :to="`/detail/${task.id}`"
            class="group block rounded-[1.5rem] bg-surface-container-lowest p-5 shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md"
          >
            <div class="flex items-start gap-4">
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-2">
                  <span
                    class="rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-wider"
                    :class="task.taskMode === 'task' ? 'bg-cyan-100 text-cyan-800' : 'bg-amber-100 text-amber-800'"
                  >
                    {{ task.taskMode === 'task' ? '任务' : '话题' }}
                  </span>
                  <span class="rounded-full bg-surface-container-low px-3 py-1 text-[11px] font-semibold text-on-surface-variant">
                    {{ task.category }}
                  </span>
                </div>
                <h3 class="text-lg font-extrabold text-[#1a0033] group-hover:text-[#3d007a] transition-colors" v-html="highlightText(task.title)"></h3>
                <p class="mt-2 line-clamp-2 text-sm text-on-surface-variant" v-html="highlightText(task.description || '')"></p>
                <div class="mt-3 flex flex-wrap items-center gap-3 text-xs text-on-surface-variant">
                  <span v-if="task.locationText" class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm">location_on</span>
                    {{ task.locationText }}
                  </span>
                  <span v-if="task.rewardText" class="flex items-center gap-1 font-semibold text-amber-700">
                    <span class="material-symbols-outlined text-sm">redeem</span>
                    {{ task.rewardText }}
                  </span>
                  <span class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm">person</span>
                    {{ task.requesterName || '匿名' }}
                  </span>
                  <span class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm">thumb_up</span>
                    {{ task.likeCount || 0 }}
                  </span>
                  <span class="flex items-center gap-1">
                    <span class="material-symbols-outlined text-sm">chat_bubble</span>
                    {{ task.commentCount || 0 }}
                  </span>
                </div>
              </div>
            </div>
          </RouterLink>
        </div>

        <!-- Pagination -->
        <div v-if="totalPages > 1" class="mt-8 flex items-center justify-center gap-2">
          <button
            :disabled="page <= 1"
            class="rounded-full px-4 py-2 text-sm font-bold transition-all disabled:opacity-30"
            :class="page <= 1 ? 'bg-surface-container-low text-on-surface-variant' : 'bg-primary text-white hover:bg-primary-dim'"
            @click="page--; doSearch()"
          >
            上一页
          </button>
          <span class="px-4 py-2 text-sm font-semibold text-on-surface-variant">{{ page }} / {{ totalPages }}</span>
          <button
            :disabled="page >= totalPages"
            class="rounded-full px-4 py-2 text-sm font-bold transition-all disabled:opacity-30"
            :class="page >= totalPages ? 'bg-surface-container-low text-on-surface-variant' : 'bg-primary text-white hover:bg-primary-dim'"
            @click="page++; doSearch()"
          >
            下一页
          </button>
        </div>
      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import PageBackHeader from '../components/PageBackHeader.vue'
import { searchApi } from '../services/api'

const route = useRoute()

const query = ref('')
const mode = ref('all')
const page = ref(1)
const size = 20
const results = ref<any[]>([])
const total = ref(0)
const loading = ref(false)
const error = ref('')
const hasSearched = ref(false)
const searchInput = ref<HTMLInputElement | null>(null)

const modeOptions = [
  { value: 'all', label: '全部' },
  { value: 'task', label: '任务' },
  { value: 'topic', label: '话题' }
]

const totalPages = ref(1)

let debounceTimer: number | null = null

const doSearch = async () => {
  const q = query.value.trim()
  if (!q) {
    results.value = []
    total.value = 0
    hasSearched.value = false
    return
  }

  hasSearched.value = true
  loading.value = true
  error.value = ''

  try {
    const data = await searchApi.search({
      q,
      mode: mode.value === 'all' ? undefined : mode.value,
      page: page.value,
      size
    }) as any

    const tasks = data?.results || []
    results.value = tasks.map((t: any) => ({
      ...t,
      taskMode: t.taskMode || (t.task_mode === 'topic' ? 'topic' : 'task')
    }))
    total.value = data?.total || 0
    totalPages.value = Math.max(1, Math.ceil(total.value / size))
  } catch (err: any) {
    error.value = err?.response?.data?.message || '搜索失败，请稍后重试'
    results.value = []
  } finally {
    loading.value = false
  }
}

const onQueryChange = () => {
  if (debounceTimer) window.clearTimeout(debounceTimer)
  page.value = 1
  debounceTimer = window.setTimeout(() => {
    doSearch()
  }, 300)
}

const highlightText = (text: string) => {
  if (!query.value.trim() || !text) return escapeHtml(text)
  const escaped = escapeHtml(text)
  const keyword = escapeHtml(query.value.trim())
  const regex = new RegExp(`(${keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')})`, 'gi')
  return escaped.replace(regex, '<mark class="bg-amber-200 text-amber-900 rounded px-0.5">$1</mark>')
}

const escapeHtml = (str: string) => {
  return str
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
}

onMounted(async () => {
  const q = (route.query.q as string) || ''
  if (q) {
    query.value = q
    await nextTick()
    doSearch()
  }
  searchInput.value?.focus()
})
</script>
