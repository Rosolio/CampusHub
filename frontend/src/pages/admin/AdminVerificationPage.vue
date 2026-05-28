<template>
  <div class="space-y-5">
    <section v-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 px-5 py-4 text-sm font-semibold text-rose-700">
      {{ error }}
    </section>

    <section class="admin-panel p-5 md:p-6">
      <div class="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="admin-kicker">Verification</p>
          <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">认证审核</h2>
        </div>
        <div class="flex flex-wrap gap-3">
          <div class="rounded-xl bg-slate-100 px-4 py-2 text-sm font-bold text-slate-700">
            共 {{ filteredVerifications.length }} 条记录
          </div>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-700 transition hover:bg-slate-50"
            @click="loadVerifications"
          >
            <span class="material-symbols-outlined text-lg">refresh</span>
            刷新
          </button>
        </div>
      </div>

      <div class="mt-6 flex flex-wrap gap-2">
        <button
          v-for="f in statusFilters"
          :key="f.value"
          type="button"
          class="rounded-full px-4 py-2 text-sm font-bold transition"
          :class="activeFilter === f.value ? 'bg-slate-950 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200'"
          @click="activeFilter = f.value"
        >
          {{ f.label }}
        </button>
      </div>

      <div class="mt-6 overflow-hidden rounded-3xl border border-slate-200 bg-white">
        <div class="hidden grid-cols-[1fr_0.8fr_1fr_0.7fr_1fr] gap-4 border-b border-slate-200 bg-slate-50 px-5 py-4 text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500 lg:grid">
          <span>申请人</span>
          <span>学号</span>
          <span>申请时间</span>
          <span>状态</span>
          <span>操作</span>
        </div>

        <div v-if="filteredVerifications.length === 0" class="px-5 py-10 text-center text-sm font-medium text-slate-500">
          暂无认证申请记录。
        </div>

        <div v-else>
          <div
            v-for="item in filteredVerifications"
            :key="item.id"
            class="border-b border-slate-200 last:border-b-0"
          >
            <div
              class="cursor-pointer px-5 py-4 transition hover:bg-slate-50"
              @click="toggleExpand(item.id)"
            >
              <div class="grid gap-4 lg:grid-cols-[1fr_0.8fr_1fr_0.7fr_1fr] lg:items-center">
                <div class="min-w-0">
                  <p class="text-sm font-extrabold text-slate-900">{{ item.realName || item.userName }}</p>
                  <p class="text-xs text-slate-500">{{ item.userName || '' }}</p>
                </div>
                <div class="text-sm font-bold text-slate-700">
                  {{ item.studentId || item.userStudentId }}
                </div>
                <div class="text-sm text-slate-600">
                  {{ formatDate(item.createdAt) }}
                </div>
                <div>
                  <span class="rounded-full px-3 py-1 text-xs font-extrabold uppercase tracking-[0.14em]" :class="statusBadgeClass(item.status)">
                    {{ statusLabel(item.status) }}
                  </span>
                </div>
                <div class="flex items-center gap-2">
                  <span class="material-symbols-outlined text-sm text-slate-400 transition-transform" :class="{ 'rotate-180': expandedId === item.id }">
                    expand_more
                  </span>
                  <span class="text-xs text-slate-500">{{ expandedId === item.id ? '收起' : '展开详情' }}</span>
                </div>
              </div>
            </div>

            <!-- Expanded detail panel -->
            <div v-if="expandedId === item.id" class="border-t border-slate-100 bg-slate-50 px-5 py-5">
              <div class="space-y-5">
                <div v-if="parseImageUrls(item.imageUrls).length > 0">
                  <p class="text-xs font-extrabold uppercase tracking-[0.18em] text-slate-500 mb-3">证件照片</p>
                  <div v-if="loadingImages.has(item.id)" class="text-sm text-slate-500 py-4">加载图片中...</div>
                  <div v-else class="grid grid-cols-2 gap-3 max-w-xl">
                    <img
                      v-for="(blobUrl, idx) in loadedImages.get(item.id) || []"
                      :key="idx"
                      :src="blobUrl"
                      class="w-full rounded-xl border border-slate-200 object-cover"
                      style="aspect-ratio: 3/2"
                      :alt="'证件照 ' + (idx + 1)"
                    />
                  </div>
                </div>

                <div v-if="item.rejectReason" class="rounded-xl border border-rose-200 bg-rose-50 px-4 py-3">
                  <p class="text-xs font-extrabold text-rose-700">驳回原因</p>
                  <p class="mt-1 text-sm text-rose-800">{{ item.rejectReason }}</p>
                </div>

                <div v-if="item.reviewerName" class="text-xs text-slate-500">
                  审核人：{{ item.reviewerName }} | 审核时间：{{ formatDate(item.reviewedAt) }}
                </div>

                <!-- Review actions -->
                <div v-if="item.status === 'PENDING'" class="flex flex-wrap gap-3">
                  <button
                    type="button"
                    class="rounded-xl bg-emerald-600 px-5 py-2.5 text-sm font-extrabold text-white transition hover:bg-emerald-700 disabled:opacity-50"
                    :disabled="pendingIds.has(item.id)"
                    @click="handleReview(item.id, 'APPROVED')"
                  >
                    {{ pendingIds.has(item.id) && pendingAction === 'APPROVED' ? '处理中...' : '通过' }}
                  </button>
                  <button
                    type="button"
                    class="rounded-xl border border-rose-200 bg-rose-50 px-5 py-2.5 text-sm font-extrabold text-rose-700 transition hover:bg-rose-100 disabled:opacity-50"
                    :disabled="pendingIds.has(item.id)"
                    @click="showRejectPrompt(item.id)"
                  >
                    驳回
                  </button>
                </div>

                <div v-if="item.status === 'APPROVED'" class="flex flex-wrap gap-3">
                  <button
                    type="button"
                    class="rounded-xl border border-amber-200 bg-amber-50 px-5 py-2.5 text-sm font-extrabold text-amber-700 transition hover:bg-amber-100 disabled:opacity-50"
                    :disabled="pendingIds.has(item.id)"
                    @click="handleRevoke(item.id)"
                  >
                    {{ pendingIds.has(item.id) ? '处理中...' : '撤销认证' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { verificationApi } from '../../services/api'
import { getStoredToken } from '../../utils/auth'

const error = ref('')
const verifications = ref<any[]>([])
const pendingIds = ref(new Set<number>())
const pendingAction = ref('')
const activeFilter = ref('ALL')
const expandedId = ref<number | null>(null)
const loadedImages = ref(new Map<number, string[]>())
const loadingImages = ref(new Set<number>())

const statusFilters = [
  { label: '全部', value: 'ALL' },
  { label: '待审核', value: 'PENDING' },
  { label: '已通过', value: 'APPROVED' },
  { label: '已驳回', value: 'REJECTED' },
  { label: '已撤销', value: 'REVOKED' }
]

const filteredVerifications = computed(() => {
  return activeFilter.value === 'ALL'
    ? verifications.value
    : verifications.value.filter(v => v.status === activeFilter.value)
})

const statusLabel = (status: string) => {
  const map: Record<string, string> = {
    PENDING: '待审核',
    APPROVED: '已通过',
    REJECTED: '已驳回',
    REVOKED: '已撤销'
  }
  return map[status] || status
}

const statusBadgeClass = (status: string) => {
  const map: Record<string, string> = {
    PENDING: 'bg-amber-100 text-amber-800',
    APPROVED: 'bg-emerald-100 text-emerald-800',
    REJECTED: 'bg-rose-100 text-rose-700',
    REVOKED: 'bg-slate-200 text-slate-700'
  }
  return map[status] || 'bg-slate-100 text-slate-600'
}

const formatDate = (value?: string) => {
  if (!value) return '-'
  const d = new Date(value)
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
}

const parseImageUrls = (imageUrls?: string) => {
  if (!imageUrls) return []
  try {
    const urls = typeof imageUrls === 'string' ? JSON.parse(imageUrls) : imageUrls
    return Array.isArray(urls) ? urls : []
  } catch {
    return []
  }
}

const fetchImages = async (verificationId: number, filenames: string[]) => {
  if (loadedImages.value.has(verificationId)) return
  loadingImages.value.add(verificationId)
  try {
    const token = getStoredToken()
    const blobUrls = await Promise.all(
      filenames.map(async (filename) => {
        const url = verificationApi.getAdminVerificationImageUrl(verificationId, filename)
        const response = await fetch(url, {
          headers: { Authorization: `Bearer ${token}` }
        })
        if (!response.ok) throw new Error('Failed to load')
        const blob = await response.blob()
        return URL.createObjectURL(blob)
      })
    )
    loadedImages.value.set(verificationId, blobUrls)
  } catch {
    loadedImages.value.set(verificationId, [])
  } finally {
    loadingImages.value.delete(verificationId)
  }
}

const toggleExpand = (id: number) => {
  if (expandedId.value === id) {
    expandedId.value = null
  } else {
    expandedId.value = id
    const item = verifications.value.find(v => v.id === id)
    if (item) {
      const filenames = parseImageUrls(item.imageUrls)
      if (filenames.length > 0) {
        fetchImages(id, filenames)
      }
    }
  }
}

const loadVerifications = async () => {
  error.value = ''
  try {
    verifications.value = await verificationApi.getVerifications() as any[]
  } catch (err: any) {
    error.value = err?.response?.data?.message || '加载认证列表失败'
  }
}

const handleReview = async (id: number, status: string, rejectReason?: string) => {
  pendingIds.value.add(id)
  pendingAction.value = status
  try {
    await verificationApi.reviewVerification(id, { status, rejectReason })
    await loadVerifications()
    expandedId.value = null
  } catch (err: any) {
    error.value = err?.response?.data?.message || '审核操作失败'
  } finally {
    pendingIds.value.delete(id)
    pendingAction.value = ''
  }
}

const showRejectPrompt = (id: number) => {
  const reason = window.prompt('请输入驳回原因', '资料不清晰，请重新上传') || ''
  if (!reason.trim()) return
  handleReview(id, 'REJECTED', reason.trim())
}

const handleRevoke = async (id: number) => {
  if (!window.confirm('确认撤销该用户的认证吗？')) return
  pendingIds.value.add(id)
  try {
    await verificationApi.revokeVerification(id)
    await loadVerifications()
    expandedId.value = null
  } catch (err: any) {
    error.value = err?.response?.data?.message || '撤销操作失败'
  } finally {
    pendingIds.value.delete(id)
  }
}

onMounted(() => {
  loadVerifications()
})
</script>
