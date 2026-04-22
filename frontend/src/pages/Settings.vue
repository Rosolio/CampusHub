<template>
  <div class="bg-background text-on-surface min-h-screen pb-24 md:pb-0 md:pt-20">
    <!-- Top Navigation Bar -->
    <AppTopNav :show-avatar="false" />

    <main class="max-w-7xl mx-auto px-6 pt-8 pb-12">
      <h1 class="text-3xl font-bold text-on-surface mb-8">{{ t('settingsTitle') }}</h1>
      
      <div class="bg-surface-container-lowest p-8 rounded-[2rem] space-y-6">
        <SettingsSection :title="t('settingsAccount')">
          <SettingsItem :title="t('settingsProfile')" type="button" :interactive="true" :show-chevron="true" @click="router.push('/settings/profile')" />
          <SettingsItem :title="t('settingsNotifications')" type="button" :interactive="true" :show-chevron="true" @click="router.push('/settings/notification')" />
        </SettingsSection>
        
        <SettingsSection :title="t('settingsApp')">
          <SettingsItem :title="t('settingsTheme')" type="button" :interactive="true" :show-chevron="true" @click="router.push('/settings/theme')" />
          <SettingsItem :title="t('settingsLanguage')" type="button" :interactive="true" :show-chevron="true" @click="router.push('/settings/language')" />
        </SettingsSection>
        
        <SettingsSection :title="t('settingsAbout')">
          <SettingsItem :title="t('settingsAboutCampusAid')" type="button" :interactive="true" :show-chevron="true" @click="router.push('/settings/about')" />
          <SettingsItem :title="t('settingsPrivacy')" type="button" :interactive="true" :show-chevron="true" @click="router.push('/settings/privacy')" />
          <SettingsItem :title="t('settingsAgreement')" type="button" :interactive="true" :show-chevron="true" @click="router.push('/settings/agreement')" />
        </SettingsSection>

        <div class="pt-4 border-t border-outline-variant/20">
          <button
            class="w-full flex items-center justify-center gap-2 rounded-xl bg-red-50 text-red-600 font-bold py-4 hover:bg-red-100 transition-colors"
            type="button"
            @click="handleLogout"
          >
            <span class="material-symbols-outlined text-lg">logout</span>
            {{ t('settingsLogout') }}
          </button>
        </div>
      </div>
    </main>

    <!-- Bottom Navigation Bar (Mobile) -->
    <AppBottomNav />
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import AppBottomNav from '../components/AppBottomNav.vue'
import SettingsItem from '../components/SettingsItem.vue'
import SettingsSection from '../components/SettingsSection.vue'
import AppTopNav from '../components/AppTopNav.vue'
import { usePreferences } from '../composables/usePreferences'

const router = useRouter()
const { t } = usePreferences()

const handleLogout = () => {
  localStorage.removeItem('token')
  localStorage.removeItem('refreshToken')
  localStorage.removeItem('user')
  router.push('/auth?tab=login')
}
</script>
