<template>
  <div class="page-shell bg-background text-on-surface md:pt-20">
    <AppTopNav />
    <main class="page-shell-main page-shell-main--narrow max-w-2xl">
      <PageBackHeader to="/profile" :label="t('backToProfile')" />

      <section class="page-card space-y-6">
        <div class="page-card-header">
          <p class="page-kicker">{{ t('verification') }}</p>
          <h1 class="page-title">{{ t('campusVerification') }}</h1>
          <p class="page-description">{{ t('verificationDescription') }}</p>
        </div>

        <!-- Error/Success messages -->
        <div v-if="errorMessage" class="rounded-2xl border border-rose-200 bg-rose-50 px-5 py-4 text-sm font-semibold text-rose-700">
          {{ errorMessage }}
        </div>
        <div v-if="successMessage" class="rounded-2xl border border-emerald-200 bg-emerald-50 px-5 py-4 text-sm font-semibold text-emerald-700">
          {{ successMessage }}
        </div>

        <!-- PENDING state -->
        <div v-if="verification && verification.status === 'PENDING'" class="space-y-6">
          <div class="flex flex-col items-center gap-4 rounded-2xl bg-amber-50 p-8 text-center">
            <span class="material-symbols-outlined text-5xl text-amber-500">hourglass_top</span>
            <h2 class="text-xl font-extrabold text-amber-800">{{ t('reviewPending') }}</h2>
            <p class="text-sm text-amber-700">{{ t('reviewPendingHint') }}</p>
          </div>
          <div v-if="parsedImageUrls.length > 0" class="grid grid-cols-2 gap-3">
            <img
              v-for="(blobUrl, idx) in myImageBlobUrls"
              :key="idx"
              :src="blobUrl"
              class="w-full rounded-xl border border-slate-200 object-cover"
              style="aspect-ratio: 3/2"
              :alt="t('materialImage') + (idx + 1)"
            />
          </div>
        </div>

        <!-- VERIFIED state -->
        <div v-else-if="verification && verification.status === 'APPROVED'" class="space-y-6">
          <div class="flex flex-col items-center gap-4 rounded-2xl bg-emerald-50 p-8 text-center">
            <span class="material-symbols-outlined text-5xl text-emerald-500" style="font-variation-settings:'FILL' 1;">verified</span>
            <h2 class="text-xl font-extrabold text-emerald-800">{{ t('verificationApproved') }}</h2>
            <p class="text-sm text-emerald-700">{{ t('verificationApprovedHint') }}</p>
          </div>
        </div>

        <!-- REJECTED state -->
        <div v-else-if="verification && verification.status === 'REJECTED'" class="space-y-6">
          <div class="flex flex-col items-center gap-4 rounded-2xl bg-rose-50 p-8 text-center">
            <span class="material-symbols-outlined text-5xl text-rose-500">gpp_bad</span>
            <h2 class="text-xl font-extrabold text-rose-800">{{ t('verificationRejected') }}</h2>
            <p class="text-sm text-rose-700">{{ t('rejectReason') }}：{{ verification.rejectReason || t('unknownReason') }}</p>
            <button
              type="button"
              class="mt-2 rounded-xl bg-primary px-6 py-3 text-sm font-extrabold text-white transition hover:brightness-110"
              @click="resetForm"
            >
              {{ t('resubmit') }}
            </button>
          </div>
        </div>

        <!-- REVOKED state -->
        <div v-else-if="verification && verification.status === 'REVOKED'" class="space-y-6">
          <div class="flex flex-col items-center gap-4 rounded-2xl bg-slate-100 p-8 text-center">
            <span class="material-symbols-outlined text-5xl text-slate-500">remove_moderator</span>
            <h2 class="text-xl font-extrabold text-slate-800">{{ t('verificationRevoked') }}</h2>
            <p class="text-sm text-slate-600">{{ t('verificationRevokedHint') }}</p>
            <button
              type="button"
              class="mt-2 rounded-xl bg-primary px-6 py-3 text-sm font-extrabold text-white transition hover:brightness-110"
              @click="resetForm"
            >
              {{ t('resubmit') }}
            </button>
          </div>
        </div>

        <!-- NONE state: Submit form -->
        <form v-else class="space-y-6" @submit.prevent="handleSubmit">
          <FormField :label="t('realName')" help="">
            <input
              v-model.trim="form.realName"
              type="text"
              required
              class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-800 outline-none focus:border-primary"
              :placeholder="t('realNamePlaceholder')"
            />
          </FormField>

          <FormField :label="t('studentId')" help="">
            <input
              v-model.trim="form.studentId"
              type="text"
              required
              class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm font-semibold text-slate-800 outline-none focus:border-primary"
              :placeholder="t('studentIdPlaceholder')"
            />
          </FormField>

          <FormField :label="t('uploadMaterials')" :help="t('uploadHint')">
            <div class="space-y-3">
              <div
                v-for="(file, idx) in form.images"
                :key="idx"
                class="flex items-center gap-3 rounded-2xl border border-slate-200 bg-white p-3"
              >
                <img
                  v-if="file.preview"
                  :src="file.preview"
                  class="h-16 w-24 rounded-xl object-cover"
                  :alt="t('preview')"
                />
                <div v-else class="flex h-16 w-24 items-center justify-center rounded-xl bg-slate-100">
                  <span class="material-symbols-outlined text-slate-400">image</span>
                </div>
                <div class="flex-1 min-w-0">
                  <p class="text-sm font-semibold text-slate-700 truncate">{{ file.file?.name || t('imageSlot') + (idx + 1) }}</p>
                  <p v-if="file.file" class="text-xs text-slate-500">{{ formatFileSize(file.file.size) }}</p>
                </div>
                <button type="button" class="rounded-full p-1 text-slate-400 hover:text-rose-500" @click="removeImage(idx)">
                  <span class="material-symbols-outlined">close</span>
                </button>
              </div>

              <label
                v-if="form.images.length < 3"
                class="flex cursor-pointer items-center justify-center gap-2 rounded-2xl border-2 border-dashed border-slate-300 bg-slate-50 px-4 py-6 text-sm font-semibold text-slate-500 transition hover:border-primary hover:text-primary"
              >
                <span class="material-symbols-outlined">add_photo_alternate</span>
                {{ t('addImage') }}
                <input type="file" accept="image/jpeg,image/png" class="hidden" @change="handleImageChange" />
              </label>
            </div>
          </FormField>

          <button
            type="submit"
            class="w-full rounded-xl bg-primary px-6 py-4 text-sm font-extrabold text-white transition hover:brightness-110 disabled:opacity-50"
            :disabled="submitting || !form.realName.trim() || !form.studentId.trim() || form.images.length === 0"
          >
            {{ submitting ? t('submitting') : t('submitVerification') }}
          </button>
        </form>
      </section>
    </main>
    <AppBottomNav />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AppBottomNav from '../components/AppBottomNav.vue'
import AppTopNav from '../components/AppTopNav.vue'
import PageBackHeader from '../components/PageBackHeader.vue'
import FormField from '../components/FormField.vue'
import { verificationApi } from '../services/api'
import { getStoredToken } from '../utils/auth'

const myImageBlobUrls = ref<string[]>([])

const t = (key: string) => {
  const map: Record<string, string> = {
    backToProfile: '返回个人主页',
    verification: '校园认证',
    campusVerification: '学生身份认证',
    verificationDescription: '上传学生证或校园卡照片，审核通过后获得认证标识，提升社区可信度。',
    reviewPending: '审核中',
    reviewPendingHint: '您的认证申请已提交，管理员正在审核，请耐心等待。',
    verificationApproved: '认证已通过',
    verificationApprovedHint: '恭喜，您的校园身份认证已通过！',
    verificationRejected: '认证未通过',
    rejectReason: '驳回原因',
    unknownReason: '未提供原因',
    verificationRevoked: '认证已撤销',
    verificationRevokedHint: '您的认证已被管理员撤销，可重新提交申请。',
    resubmit: '重新申请',
    realName: '真实姓名',
    realNamePlaceholder: '请输入学生证上的真实姓名',
    studentId: '学号',
    studentIdPlaceholder: '请输入学生证上的学号',
    uploadMaterials: '上传证件照片',
    uploadHint: '请上传学生证或校园卡正反面照片，支持 JPG/PNG，单张不超过 5MB。可打码遮挡身份证号等非学籍信息。',
    addImage: '添加图片',
    imageSlot: '证件照片',
    submitting: '提交中...',
    submitVerification: '提交认证申请',
    preview: '预览',
    materialImage: '认证材料图片',
  }
  return map[key] || key
}

const verification = ref<any>(null)
const errorMessage = ref('')
const successMessage = ref('')
const submitting = ref(false)

const form = ref<{
  realName: string
  studentId: string
  images: Array<{ file: File | null; preview: string }>
}>({
  realName: '',
  studentId: '',
  images: []
})

const parsedImageUrls = computed(() => {
  if (!verification.value?.imageUrls) return []
  try {
    const urls = typeof verification.value.imageUrls === 'string'
      ? JSON.parse(verification.value.imageUrls)
      : verification.value.imageUrls
    return Array.isArray(urls) ? urls : []
  } catch {
    return []
  }
})

const fetchMyImages = async (filenames: string[]) => {
  if (filenames.length === 0) return
  try {
    const token = getStoredToken()
    const blobUrls = await Promise.all(
      filenames.map(async (filename) => {
        const url = verificationApi.getMyVerificationImageUrl(filename)
        const response = await fetch(url, {
          headers: { Authorization: `Bearer ${token}` }
        })
        if (!response.ok) throw new Error('Failed to load')
        const blob = await response.blob()
        return URL.createObjectURL(blob)
      })
    )
    myImageBlobUrls.value = blobUrls
  } catch {
    myImageBlobUrls.value = []
  }
}

const formatFileSize = (bytes: number) => {
  if (bytes < 1024) return bytes + ' B'
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB'
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB'
}

const handleImageChange = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!['image/jpeg', 'image/png'].includes(file.type)) {
    errorMessage.value = '仅支持 JPG 和 PNG 格式的图片'
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    errorMessage.value = '图片大小不能超过 5MB'
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    form.value.images.push({
      file,
      preview: typeof reader.result === 'string' ? reader.result : ''
    })
    errorMessage.value = ''
  }
  reader.readAsDataURL(file)
  input.value = ''
}

const removeImage = (idx: number) => {
  form.value.images.splice(idx, 1)
}

const resetForm = () => {
  verification.value = null
  form.value = { realName: '', studentId: '', images: [] }
  errorMessage.value = ''
  successMessage.value = ''
}

const loadVerification = async () => {
  try {
    verification.value = await verificationApi.getMyVerification()
    if (verification.value) {
      form.value.realName = verification.value.realName || ''
      form.value.studentId = verification.value.studentId || ''
      if (verification.value.status === 'PENDING') {
        fetchMyImages(parsedImageUrls.value as string[])
      }
    }
  } catch {
    verification.value = null
  }
}

const handleSubmit = async () => {
  errorMessage.value = ''
  successMessage.value = ''

  if (!form.value.realName.trim() || !form.value.studentId.trim()) {
    errorMessage.value = '请填写真实姓名和学号'
    return
  }
  if (form.value.images.length === 0) {
    errorMessage.value = '请至少上传一张证件照片'
    return
  }

  submitting.value = true
  try {
    const fd = new FormData()
    fd.append('realName', form.value.realName.trim())
    fd.append('studentId', form.value.studentId.trim())
    form.value.images.forEach((img) => {
      if (img.file) fd.append('images', img.file)
    })
    verification.value = await verificationApi.submitVerification(fd)
    successMessage.value = '认证申请已提交，请耐心等待审核。'
  } catch (err: any) {
    errorMessage.value = err?.response?.data?.message || err?.message || '提交失败，请稍后重试'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  loadVerification()
})
</script>
