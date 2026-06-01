<template>
  <div class="page-shell bg-surface font-body text-on-surface">
    <AppTopNav :avatar-url="form.avatarUrl || defaultAvatarUrl" />

    <main class="page-shell-main page-shell-main--narrow">
      <PageBackHeader />

      <div class="page-card">
        <div class="page-card-header">
          <p class="page-kicker">Profile Settings</p>
          <h1 class="page-title">个人资料设置</h1>
          <p class="page-description">支持更换头像，并同步更新个人中心与资料页展示。</p>
        </div>

        <div
          v-if="feedback.message"
          class="mb-6 rounded-2xl border px-4 py-3 text-sm font-medium"
          :class="feedback.type === 'success' ? 'border-emerald-200 bg-emerald-50 text-emerald-700' : 'border-rose-200 bg-rose-50 text-rose-700'"
        >
          {{ feedback.message }}
        </div>

        <form class="space-y-6" @submit.prevent="handleSave">
          <div class="flex flex-col items-center gap-4">
            <div class="h-24 w-24 rounded-full overflow-hidden border-4 border-primary-container">
              <img
                alt="个人头像"
                class="h-full w-full object-cover"
                :src="form.avatarUrl || defaultAvatarUrl"
              />
            </div>
            <input
              ref="avatarInputRef"
              type="file"
              accept="image/*"
              class="hidden"
              @change="handleAvatarChange"
            />
            <div class="flex flex-wrap items-center justify-center gap-3">
              <button
                type="button"
                class="px-4 py-2 rounded-full bg-primary text-white text-sm font-medium hover:bg-primary/90 transition-colors"
                @click="triggerAvatarPicker"
              >
                更换头像
              </button>
              <button
                v-if="form.avatarUrl && form.avatarUrl !== initialAvatarUrl"
                type="button"
                class="px-4 py-2 rounded-full bg-surface-container-high text-on-surface-variant text-sm font-medium hover:bg-surface-container-low transition-colors"
                @click="resetAvatar"
              >
                撤销头像修改
              </button>
            </div>
            <p class="text-xs text-on-surface-variant">建议使用方形图片，保存后会同步显示到个人中心。</p>
          </div>

          <div class="space-y-5">
            <FormField label="昵称">
              <input
                v-model="form.name"
                type="text"
                class="w-full px-4 py-3 rounded-xl bg-surface-container-low border border-outline-variant/15 focus:ring-2 focus:ring-primary/30 focus:border-primary transition-all"
                placeholder="请输入昵称"
              />
            </FormField>

            <FormField label="学号">
              <input
                :value="currentUser.studentId || '未设置学号'"
                type="text"
                disabled
                class="w-full px-4 py-3 rounded-xl bg-surface-container-low border border-outline-variant/15 text-on-surface-variant cursor-not-allowed"
              />
            </FormField>

            <FormField label="专业">
              <select
                v-model="form.major"
                class="w-full px-4 py-3 rounded-xl bg-surface-container-low border border-outline-variant/15 focus:ring-2 focus:ring-primary/30 focus:border-primary transition-all"
              >
                <option value="">请选择专业</option>
                <option v-for="major in majorOptions" :key="major" :value="major">{{ major }}</option>
              </select>
            </FormField>

            <FormField label="电子邮箱">
              <input
                v-model="form.email"
                type="email"
                class="w-full px-4 py-3 rounded-xl bg-surface-container-low border border-outline-variant/15 focus:ring-2 focus:ring-primary/30 focus:border-primary transition-all"
                placeholder="请输入电子邮箱"
              />
            </FormField>
          </div>

          <button
            type="submit"
            class="w-full bg-gradient-to-br from-primary to-primary-dim text-on-primary font-bold py-4 rounded-xl shadow-lg hover:scale-[1.02] active:scale-95 transition-all duration-300 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:scale-100"
            :disabled="saving"
          >
            {{ saving ? '保存中...' : '保存修改' }}
          </button>
        </form>
      </div>
    </main>

    <AppBottomNav />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import AppBottomNav from '../../components/AppBottomNav.vue'
import AppTopNav from '../../components/AppTopNav.vue'
import FormField from '../../components/FormField.vue'
import PageBackHeader from '../../components/PageBackHeader.vue'
import { DEFAULT_AVATAR_URL } from '../../constants/assets'
import { userApi } from '../../services/api'
import { setStoredUser, storedUser } from '../../utils/auth'

type FeedbackState = {
  message: string
  type: 'success' | 'error'
}

const defaultAvatarUrl = DEFAULT_AVATAR_URL
const currentUser = computed(() => storedUser.value || {})
const initialAvatarUrl = ref('')
const avatarInputRef = ref<HTMLInputElement | null>(null)
const saving = ref(false)
const feedback = ref<FeedbackState>({ message: '', type: 'success' })
const form = ref({
  name: '',
  email: '',
  major: '',
  avatarUrl: ''
})

const majorOptions = [
  '软件工程',
  '计算机科学与技术',
  '人工智能',
  '数据科学与大数据技术',
  '网络空间安全',
  '电子信息工程',
  '自动化',
  '数学与应用数学',
  '工商管理',
  '会计学',
  '英语',
  '法学',
  '其他'
]

const fillForm = (user: any) => {
  initialAvatarUrl.value = user?.avatarUrl || ''
  form.value = {
    name: user?.name || '',
    email: user?.email || '',
    major: user?.major || '',
    avatarUrl: user?.avatarUrl || ''
  }
}

const loadCurrentUser = async () => {
  try {
    const user = await userApi.getCurrentUser() as Record<string, any>
    fillForm(user)
    setStoredUser(user)
  } catch (error) {
    console.error('加载个人资料失败:', error)
    fillForm(currentUser.value)
    feedback.value = { message: '加载个人资料失败，请稍后重试。', type: 'error' }
  }
}

const triggerAvatarPicker = () => {
  avatarInputRef.value?.click()
}

const resetAvatar = () => {
  form.value.avatarUrl = initialAvatarUrl.value
  if (avatarInputRef.value) {
    avatarInputRef.value.value = ''
  }
}

const handleAvatarChange = (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  if (!file.type.startsWith('image/')) {
    feedback.value = { message: '请选择图片文件作为头像。', type: 'error' }
    input.value = ''
    return
  }

  const reader = new FileReader()
  reader.onload = () => {
    form.value.avatarUrl = typeof reader.result === 'string' ? reader.result : form.value.avatarUrl
    feedback.value = { message: '', type: 'success' }
  }
  reader.onerror = () => {
    feedback.value = { message: '头像读取失败，请换一张图片重试。', type: 'error' }
  }
  reader.readAsDataURL(file)
}

const handleSave = async () => {
  if (!form.value.name.trim()) {
    feedback.value = { message: '昵称不能为空。', type: 'error' }
    return
  }

  if (!form.value.email.trim()) {
    feedback.value = { message: '邮箱不能为空。', type: 'error' }
    return
  }

  saving.value = true
  feedback.value = { message: '', type: 'success' }

  try {
    const updatedUser = await userApi.updateUser({
      name: form.value.name.trim(),
      email: form.value.email.trim(),
      major: form.value.major.trim(),
      avatarUrl: form.value.avatarUrl || ''
    }) as Record<string, any>
    fillForm(updatedUser)
    setStoredUser(updatedUser)
    feedback.value = { message: '个人资料已保存，头像展示已同步更新。', type: 'success' }
  } catch (error: any) {
    console.error('保存个人资料失败:', error)
    feedback.value = {
      message: error?.response?.data?.message || '保存失败，请稍后重试。',
      type: 'error'
    }
  } finally {
    saving.value = false
  }
}

onMounted(loadCurrentUser)
</script>
