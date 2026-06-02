<template>
  <div class="space-y-3">
    <p class="text-sm font-bold text-teal-950">图片上传</p>

    <div
      class="rounded-2xl border-2 border-dashed p-6 text-center transition-all cursor-pointer"
      :class="isDragging ? 'border-primary bg-primary/5' : 'border-outline-variant/25 hover:border-primary/50 hover:bg-surface-container-low'"
      @click="openFilePicker"
      @dragover.prevent="isDragging = true"
      @dragleave.prevent="isDragging = false"
      @drop.prevent="handleDrop"
    >
      <span class="material-symbols-outlined text-4xl text-on-surface-variant/40">add_photo_alternate</span>
      <p class="mt-3 text-sm font-semibold text-on-surface-variant">拖拽图片到此处或点击上传</p>
      <p class="mt-1 text-xs text-on-surface-variant/60">支持 JPG、PNG、GIF、WebP，单张最大 5MB，最多 9 张</p>
    </div>

    <input
      ref="fileInput"
      type="file"
      multiple
      accept="image/*"
      class="hidden"
      @change="handleFileSelect"
    />

    <div v-if="uploadingCount > 0" class="text-xs text-on-surface-variant">
      正在上传 {{ uploadingCount }} 张...
    </div>
    <div v-if="uploadError" class="rounded-xl border border-rose-200 bg-rose-50 px-4 py-2 text-xs font-medium text-rose-700">
      {{ uploadError }}
    </div>

    <div v-if="images.length > 0" class="grid grid-cols-3 gap-3 sm:grid-cols-4 md:grid-cols-5">
      <div
        v-for="(img, idx) in images"
        :key="idx"
        class="group relative aspect-square overflow-hidden rounded-2xl bg-surface-container-low"
      >
        <img
          v-if="img.preview || img.url"
          :src="img.preview || img.url"
          class="h-full w-full object-cover"
          :alt="'上传图片 ' + (idx + 1)"
        />
        <div
          v-if="img.uploading"
          class="absolute inset-0 flex items-center justify-center bg-slate-900/50"
        >
          <span class="material-symbols-outlined animate-spin text-2xl text-white">progress_activity</span>
        </div>
        <button
          v-else
          type="button"
          class="absolute right-1.5 top-1.5 flex h-7 w-7 items-center justify-center rounded-full bg-rose-600 text-white opacity-0 shadow-lg transition-opacity group-hover:opacity-100"
          @click.stop="removeImage(idx)"
        >
          <span class="material-symbols-outlined text-sm">close</span>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { fileApi } from '../services/api'

const props = withDefaults(defineProps<{
  modelValue?: string[]
}>(), {
  modelValue: () => []
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string[]): void
}>()

const fileInput = ref<HTMLInputElement | null>(null)
const isDragging = ref(false)
const uploadingCount = ref(0)
const uploadError = ref('')

interface ImageItem {
  file?: File
  preview?: string
  url?: string
  uploading?: boolean
}

const images = ref<ImageItem[]>(
  (props.modelValue || []).map(url => ({ url }))
)

const ALLOWED_TYPES = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
const MAX_SIZE = 5 * 1024 * 1024
const MAX_COUNT = 9

const openFilePicker = () => {
  fileInput.value?.click()
}

const handleDrop = (e: DragEvent) => {
  isDragging.value = false
  const files = e.dataTransfer?.files
  if (files) processFiles(files)
}

const handleFileSelect = (e: Event) => {
  const target = e.target as HTMLInputElement
  const files = target.files
  if (files) processFiles(files)
  target.value = ''
}

const processFiles = (fileList: FileList) => {
  uploadError.value = ''

  const validFiles: File[] = []
  for (let i = 0; i < fileList.length; i++) {
    const file = fileList[i]
    if (!ALLOWED_TYPES.includes(file.type)) {
      uploadError.value = `${file.name} 格式不支持，仅支持 JPG/PNG/GIF/WebP`
      return
    }
    if (file.size > MAX_SIZE) {
      uploadError.value = `${file.name} 超过 5MB 限制`
      return
    }
    validFiles.push(file)
  }

  const remaining = MAX_COUNT - images.value.length
  if (validFiles.length > remaining) {
    uploadError.value = `最多上传 ${MAX_COUNT} 张，还可添加 ${remaining} 张`
    return
  }

  for (const file of validFiles) {
    const preview = URL.createObjectURL(file)
    const item: ImageItem = { file, preview, uploading: true }
    images.value.push(item)
    uploadFile(item)
  }
}

const uploadFile = async (item: ImageItem) => {
  if (!item.file) return
  uploadingCount.value++
  try {
    const result = await fileApi.upload(item.file) as { url: string }
    item.url = result.url
    item.uploading = false
    URL.revokeObjectURL(item.preview!)
    item.preview = undefined
    emitUpdate()
  } catch (err: any) {
    uploadError.value = err?.response?.data?.message || '上传失败'
    images.value = images.value.filter(i => i !== item)
  } finally {
    uploadingCount.value--
  }
}

const removeImage = (idx: number) => {
  const item = images.value[idx]
  if (item?.preview) {
    URL.revokeObjectURL(item.preview)
  }
  images.value.splice(idx, 1)
  emitUpdate()
}

const emitUpdate = () => {
  const urls = images.value
    .filter(i => i.url && !i.uploading)
    .map(i => i.url!)
  emit('update:modelValue', urls)
}

</script>
