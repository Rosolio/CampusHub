<template>
  <div class="bg-surface font-body text-on-surface min-h-screen pb-24 md:pb-0">
    <AppTopNav />

    <main class="pt-24 px-6 max-w-5xl mx-auto">
      <PageBackHeader />

      <div class="bg-surface-container-lowest rounded-[2rem] p-8 shadow-sm">
        <h1 class="text-3xl font-extrabold font-headline text-on-surface mb-6">{{ t('languageTitle') }}</h1>

        <div class="space-y-6">
          <div class="space-y-4">
            <h2 class="text-xl font-bold text-on-surface">{{ t('languageApp') }}</h2>

            <div class="space-y-2">
              <div
                v-for="option in languageOptions"
                :key="option.value"
                class="flex items-center justify-between p-4 bg-surface-container-high rounded-xl cursor-pointer border-2 transition-colors"
                :class="selectedLanguage === option.value ? 'border-primary' : 'border-transparent hover:border-primary/30'"
                @click="selectedLanguage = option.value"
              >
                <div class="flex items-center gap-4">
                  <div class="w-10 h-10 rounded-full flex items-center justify-center font-bold" :class="option.value === 'zh-CN' ? 'bg-red-100 text-red-600' : option.value === 'en-US' ? 'bg-blue-100 text-blue-600' : 'bg-yellow-100 text-yellow-600'">
                    <span>{{ option.value === 'zh-CN' ? '中' : option.value === 'en-US' ? 'EN' : '繁' }}</span>
                  </div>
                  <div>
                    <h3 class="font-medium text-on-surface">{{ t(option.labelKey) }}</h3>
                    <p class="text-sm text-on-surface-variant">{{ option.note }}</p>
                  </div>
                </div>
                <div class="w-6 h-6 rounded-full flex items-center justify-center" :class="selectedLanguage === option.value ? 'bg-primary' : 'bg-gray-300'">
                  <span class="text-white text-xs">✓</span>
                </div>
              </div>
            </div>
          </div>

          <div class="space-y-4">
            <h2 class="text-xl font-bold text-on-surface">{{ t('languageRegion') }}</h2>

            <FormField :label="t('languageRegionLabel')">
              <select class="w-full px-4 py-3 rounded-xl bg-surface-container-low border border-outline-variant/15 focus:ring-2 focus:ring-primary/30 focus:border-primary transition-all">
                <option>中国大陆</option>
                <option>中国香港</option>
                <option>中国台湾</option>
                <option>美国</option>
                <option>其他</option>
              </select>
            </FormField>

            <FormField :label="t('languageTimezoneLabel')">
              <select class="w-full px-4 py-3 rounded-xl bg-surface-container-low border border-outline-variant/15 focus:ring-2 focus:ring-primary/30 focus:border-primary transition-all">
                <option>(GMT+8:00) 北京时间</option>
                <option>(GMT-5:00) 东部标准时间</option>
                <option>(GMT+0:00) 格林威治标准时间</option>
                <option>(GMT+9:00) 东京时间</option>
              </select>
            </FormField>
          </div>

          <div class="rounded-2xl bg-surface-container-high p-5 text-sm leading-6 text-on-surface-variant">
            语言切换会立即更新导航、设置页和时间日期格式，便于演示多语言支持。
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
            {{ saving ? t('languageSaving') : t('languageSave') }}
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
import FormField from '../../components/FormField.vue'
import PageBackHeader from '../../components/PageBackHeader.vue'
import { languageOptions, usePreferences } from '../../composables/usePreferences'

const { language, t, savePreferences } = usePreferences()

const selectedLanguage = ref(language.value)
const saving = ref(false)
const message = ref('')
const messageType = ref<'success' | 'error'>('success')

watch(language, (value) => {
  selectedLanguage.value = value
})

const handleSave = async () => {
  saving.value = true
  message.value = ''

  try {
    await savePreferences({ language: selectedLanguage.value })
    messageType.value = 'success'
    message.value = t('languageSaved')
  } catch (error) {
    console.error('保存语言设置失败:', error)
    messageType.value = 'error'
    message.value = t('saveFailed')
  } finally {
    saving.value = false
  }
}
</script>
