<template>
  <div class="page-shell bg-background text-on-surface md:pt-20">

    <main class="page-shell-main page-shell-main--narrow">
      <div class="page-card-header">
        <p class="page-kicker">Settings Center</p>
        <h1 class="page-title">{{ t('settingsTitle') }}</h1>
        <p class="page-description">统一管理账户资料、通知偏好、主题语言与平台说明，减少不同设置页之间的跳转成本。</p>
      </div>
      
      <div class="page-card space-y-6">
        <SettingsSection :title="t('settingsAccount')">
          <SettingsItem :title="t('settingsProfile')" type="button" :interactive="true" :show-chevron="true" @click="router.push('/settings/profile')" />
          <SettingsItem :title="t('settingsNotifications')" type="button" :interactive="true" :show-chevron="true" @click="router.push('/settings/notification')" />
        </SettingsSection>
        
        <SettingsSection :title="t('settingsApp')">
          <SettingsItem :title="t('settingsTheme')" type="button" :interactive="true" :show-chevron="true" @click="router.push('/settings/theme')" />
          <SettingsItem :title="t('settingsLanguage')" type="button" :interactive="true" :show-chevron="true" @click="router.push('/settings/language')" />
        </SettingsSection>
        
        <SettingsSection :title="t('settingsAbout')">
          <SettingsItem :title="t('settingsAboutCampusHub')" type="button" :interactive="true" :show-chevron="true" @click="router.push('/settings/about')" />
          <SettingsItem :title="t('settingsPrivacy')" type="button" :interactive="true" :show-chevron="true" @click="router.push('/settings/privacy')" />
          <SettingsItem :title="t('settingsAgreement')" type="button" :interactive="true" :show-chevron="true" @click="router.push('/settings/agreement')" />
        </SettingsSection>

        <div class="pt-4 border-t border-outline-variant/20">
          <button
            class="w-full flex items-center justify-center gap-2 rounded-xl bg-red-50 py-4 font-bold text-red-600 transition-colors hover:bg-red-100"
            type="button"
            @click="handleLogout"
          >
            <span class="material-symbols-outlined text-lg">logout</span>
            {{ t('settingsLogout') }}
          </button>
        </div>
      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import SettingsItem from '../components/SettingsItem.vue'
import SettingsSection from '../components/SettingsSection.vue'
import { useConfirm } from '../composables/useConfirm'
import { usePreferences } from '../composables/usePreferences'
import { logoutAndRedirect } from '../utils/auth'

const router = useRouter()
const { t } = usePreferences()
const { openConfirm } = useConfirm()

const handleLogout = async () => {
  const confirmed = await openConfirm({
    title: '退出登录',
    message: '确定要退出当前账号吗？',
    confirmText: '退出',
    cancelText: '取消',
    tone: 'danger'
  })
  if (confirmed) {
    logoutAndRedirect()
  }
}
</script>
