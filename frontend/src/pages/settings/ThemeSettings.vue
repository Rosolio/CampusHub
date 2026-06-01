<template>
  <div class="page-shell bg-surface font-body text-on-surface">
    <AppTopNav />

    <main class="page-shell-main page-shell-main--narrow">
      <PageBackHeader />

      <div class="page-card">
        <div class="page-card-header">
          <p class="page-kicker">Theme Settings</p>
          <h1 class="page-title">{{ t('themeTitle') }}</h1>
          <p class="page-description">统一控制浅色、深色与跟随系统三种模式，立即预览页面变化。</p>
        </div>

        <div class="space-y-6">
          <div class="space-y-4 page-prose">
            <h2>{{ t('themeMode') }}</h2>

            <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div
                v-for="option in themeOptions"
                :key="option.value"
                class="bg-surface-container-high rounded-xl p-4 cursor-pointer hover:bg-surface-container-low transition-colors border-2"
                :class="selectedTheme === option.value ? 'border-primary' : 'border-transparent'"
                @click="selectedTheme = option.value"
              >
                <div class="flex items-center justify-between mb-4">
                  <h3 class="font-medium text-on-surface">{{ t(option.labelKey) }}</h3>
                  <div class="w-6 h-6 rounded-full flex items-center justify-center" :class="selectedTheme === option.value ? 'bg-primary' : 'bg-gray-300'">
                    <span class="text-white text-xs">✓</span>
                  </div>
                </div>

                <div
                  class="h-24 rounded-lg border flex items-center justify-center"
                  :class="option.value === 'light'
                    ? 'bg-white border-gray-200 text-gray-800'
                    : option.value === 'dark'
                      ? 'bg-gray-900 border-gray-700 text-white'
                      : 'bg-gradient-to-r from-white to-gray-900 border-gray-200 text-gray-800'"
                >
                  <span>{{ t(option.labelKey) }}</span>
                </div>
              </div>
            </div>
          </div>

          <div class="rounded-2xl bg-surface-container-high p-5 text-sm leading-6 text-on-surface-variant">
            主题会立即应用到导航、设置页和全局背景。选择“跟随系统”时，会根据系统深浅色自动切换。
          </div>

          <div v-if="message" class="rounded-2xl border px-4 py-3 text-sm font-medium" :class="messageType === 'success' ? 'border-emerald-200 bg-emerald-50 text-emerald-700' : 'border-rose-200 bg-rose-50 text-rose-700'">
            {{ message }}
          </div>

          <button
            type="button"
            class="w-full bg-gradient-to-br from-primary to-primary-dim text-on-primary font-bold py-4 rounded-xl shadow-lg hover:scale-[1.02] active:scale-95 transition-all duration-300 disabled:cursor-not-allowed disabled:opacity-60"
            :disabled="saving"
            @click="handleSave"
          >
            {{ saving ? t('themeSaving') : t('themeSave') }}
          </button>
        </div>
      </div>
    </main>

    <AppBottomNav />
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import AppBottomNav from '../../components/AppBottomNav.vue'
import AppTopNav from '../../components/AppTopNav.vue'
import PageBackHeader from '../../components/PageBackHeader.vue'
import { themeOptions, usePreferences } from '../../composables/usePreferences'

const { theme, t, savePreferences } = usePreferences()

const selectedTheme = ref(theme.value)
const saving = ref(false)
const message = ref('')
const messageType = ref<'success' | 'error'>('success')

watch(theme, (value) => {
  selectedTheme.value = value
})

const handleSave = async () => {
  saving.value = true
  message.value = ''

  try {
    await savePreferences({ theme: selectedTheme.value })
    messageType.value = 'success'
    message.value = t('themeSaved')
  } catch (error) {
    console.error('保存主题设置失败:', error)
    messageType.value = 'error'
    message.value = t('saveFailed')
  } finally {
    saving.value = false
  }
}
</script>
