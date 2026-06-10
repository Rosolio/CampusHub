<template>
  <div class="page-shell bg-surface font-body text-on-surface">

    <main class="page-shell-main page-shell-main--narrow">
      <PageBackHeader />

      <div class="page-card">
        <div class="page-card-header">
          <p class="page-kicker">Notifications</p>
          <h1 class="page-title">通知设置</h1>
          <p class="page-description">把通知入口、通知类型和接收方式放在同一张表单里，减少理解成本。</p>
        </div>
        
        <div class="space-y-6">
          <SettingsToggleItem v-model="notificationState.enabled" title="接收通知" description="开启后将接收所有通知" />

          <div class="space-y-4 page-prose">
            <h2>通知类型</h2>
            
            <SettingsToggleItem v-model="notificationState.task" title="任务相关通知" description="接收任务发布、接受、完成等通知" :disabled="!notificationState.enabled" />
            <SettingsToggleItem v-model="notificationState.message" title="消息通知" description="接收新消息通知" :disabled="!notificationState.enabled" />
            <SettingsToggleItem v-model="notificationState.system" title="系统通知" description="接收系统更新、活动等通知" :disabled="!notificationState.enabled" />
            <SettingsToggleItem v-model="notificationState.community" title="社区通知" description="接收社区动态、热门话题等通知" :disabled="!notificationState.enabled" />
          </div>

          <div class="space-y-4 page-prose">
            <h2>通知方式</h2>
            
            <SettingsToggleItem v-model="notificationState.inApp" title="应用内通知" description="在应用内接收通知" :disabled="!notificationState.enabled" />
            <SettingsToggleItem v-model="notificationState.sms" title="短信通知" description="通过短信接收重要通知" :disabled="!notificationState.enabled" />
            <SettingsToggleItem v-model="notificationState.email" title="邮件通知" description="通过邮件接收通知" :disabled="!notificationState.enabled" />
          </div>

          <button
            type="button"
            class="w-full bg-gradient-to-br from-primary to-primary-dim text-on-primary font-bold py-4 rounded-xl shadow-lg hover:scale-[1.02] active:scale-95 transition-all duration-300"
          >
            保存设置
          </button>
        </div>
      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import PageBackHeader from '../../components/PageBackHeader.vue'
import SettingsToggleItem from '../../components/SettingsToggleItem.vue'

const notificationState = reactive({
  enabled: true,
  task: true,
  message: true,
  system: true,
  community: true,
  inApp: true,
  sms: false,
  email: false
})
</script>
