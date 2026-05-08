<template>
  <div class="bg-surface font-body text-on-surface min-h-screen pb-24 md:pb-0">
    <AppTopNav :show-avatar="false" />

    <main class="pt-24 px-6 max-w-5xl mx-auto">
      <PageBackHeader />

      <div class="bg-surface-container-lowest rounded-[2rem] p-8 shadow-sm">
        <h1 class="text-3xl font-extrabold font-headline text-teal-900 mb-6">通知设置</h1>
        
        <div class="space-y-6">
          <!-- 通知开关 -->
          <SettingsToggleItem v-model="notificationState.enabled" title="接收通知" description="开启后将接收所有通知" />

          <!-- 通知类型设置 -->
          <div class="space-y-4">
            <h2 class="text-xl font-bold text-teal-900">通知类型</h2>
            
            <SettingsToggleItem v-model="notificationState.task" title="任务相关通知" description="接收任务发布、接受、完成等通知" :disabled="!notificationState.enabled" />
            <SettingsToggleItem v-model="notificationState.message" title="消息通知" description="接收新消息通知" :disabled="!notificationState.enabled" />
            <SettingsToggleItem v-model="notificationState.system" title="系统通知" description="接收系统更新、活动等通知" :disabled="!notificationState.enabled" />
            <SettingsToggleItem v-model="notificationState.community" title="社区通知" description="接收社区动态、热门话题等通知" :disabled="!notificationState.enabled" />
          </div>

          <!-- 通知方式 -->
          <div class="space-y-4">
            <h2 class="text-xl font-bold text-teal-900">通知方式</h2>
            
            <SettingsToggleItem v-model="notificationState.inApp" title="应用内通知" description="在应用内接收通知" :disabled="!notificationState.enabled" />
            <SettingsToggleItem v-model="notificationState.sms" title="短信通知" description="通过短信接收重要通知" :disabled="!notificationState.enabled" />
            <SettingsToggleItem v-model="notificationState.email" title="邮件通知" description="通过邮件接收通知" :disabled="!notificationState.enabled" />
          </div>

          <!-- 提交按钮 -->
          <button
            type="button"
            class="w-full bg-gradient-to-br from-primary to-primary-dim text-on-primary font-bold py-4 rounded-xl shadow-lg hover:scale-[1.02] active:scale-95 transition-all duration-300"
          >
            保存设置
          </button>
        </div>
      </div>
    </main>

    <AppBottomNav />
  </div>
</template>

<script setup lang="ts">
import { reactive } from 'vue'
import AppBottomNav from '../../components/AppBottomNav.vue'
import AppTopNav from '../../components/AppTopNav.vue'
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
