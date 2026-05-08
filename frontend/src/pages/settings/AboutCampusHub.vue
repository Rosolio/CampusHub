<template>
  <div class="bg-surface font-body text-on-surface min-h-screen pb-24 md:pb-0">
    <AppTopNav :show-avatar="false" />

    <main class="pt-24 px-6 max-w-5xl mx-auto">
      <PageBackHeader />

      <div class="bg-surface-container-lowest rounded-[2rem] p-8 shadow-sm">
        <div class="flex flex-col items-center mb-8">
          <div class="h-24 w-24 rounded-full bg-primary flex items-center justify-center mb-4">
            <span class="text-white text-4xl font-bold">CA</span>
          </div>
          <h1 class="text-3xl font-extrabold font-headline text-teal-900 mb-2">CampusHub</h1>
          <p class="text-on-surface-variant">版本 1.0.0</p>
        </div>
        
        <div class="space-y-6">
          <!-- 关于应用 -->
          <div>
            <h2 class="text-xl font-bold text-teal-900 mb-4">关于 CampusHub</h2>
            <p class="text-on-surface-variant leading-relaxed">
              CampusHub 是一个专为大学生设计的校园互助平台，旨在帮助学生之间互相帮助、共享资源、解决问题。通过这个平台，学生可以发布各种任务需求，如跑腿代办、二手交易、打听求助等，也可以为其他同学提供帮助并获得积分奖励。
            </p>
            <p class="text-on-surface-variant leading-relaxed mt-4">
              我们的使命是构建一个更加互助、和谐的校园社区，让每一位学生都能在需要时得到帮助，在有能力时帮助他人。
            </p>
          </div>

          <!-- 主要功能 -->
          <div>
            <h2 class="text-xl font-bold text-teal-900 mb-4">主要功能</h2>
            <ul class="space-y-3">
              <li class="flex items-start gap-3">
                <span class="material-symbols-outlined text-primary mt-1">check_circle</span>
                <span class="text-on-surface-variant">发布和接受各种校园任务</span>
              </li>
              <li class="flex items-start gap-3">
                <span class="material-symbols-outlined text-primary mt-1">check_circle</span>
                <span class="text-on-surface-variant">浏览和搜索社区动态</span>
              </li>
              <li class="flex items-start gap-3">
                <span class="material-symbols-outlined text-primary mt-1">check_circle</span>
                <span class="text-on-surface-variant">实时消息通知和聊天功能</span>
              </li>
              <li class="flex items-start gap-3">
                <span class="material-symbols-outlined text-primary mt-1">check_circle</span>
                <span class="text-on-surface-variant">个人中心和积分系统</span>
              </li>
              <li class="flex items-start gap-3">
                <span class="material-symbols-outlined text-primary mt-1">check_circle</span>
                <span class="text-on-surface-variant">多种主题和语言设置</span>
              </li>
            </ul>
          </div>

          <!-- 联系我们 -->
          <div>
            <h2 class="text-xl font-bold text-teal-900 mb-4">联系我们</h2>
            <p class="text-on-surface-variant leading-relaxed">
              如果您有任何问题、建议或反馈，欢迎通过以下方式联系我们：
            </p>
            <div class="space-y-3 mt-4">
              <div class="flex items-center gap-3">
                <span class="material-symbols-outlined text-primary">email</span>
                <span class="text-on-surface-variant">contact@campushub.com</span>
              </div>
              <div class="flex items-center gap-3">
                <span class="material-symbols-outlined text-primary">phone</span>
                <span class="text-on-surface-variant">123-4567-8910</span>
              </div>
              <div class="flex items-center gap-3">
                <span class="material-symbols-outlined text-primary">location_on</span>
                <span class="text-on-surface-variant">大学校园</span>
              </div>
            </div>
          </div>

          <!-- 社交媒体 -->
          <div>
            <h2 class="text-xl font-bold text-teal-900 mb-4">关注我们</h2>
            <div class="flex gap-4">
              <a href="#" class="w-12 h-12 rounded-full bg-surface-container-high flex items-center justify-center hover:bg-primary hover:text-white transition-colors">
                <span class="material-symbols-outlined">face</span>
              </a>
              <a href="#" class="w-12 h-12 rounded-full bg-surface-container-high flex items-center justify-center hover:bg-primary hover:text-white transition-colors">
                <span class="material-symbols-outlined">share</span>
              </a>
              <a href="#" class="w-12 h-12 rounded-full bg-surface-container-high flex items-center justify-center hover:bg-primary hover:text-white transition-colors">
                <span class="material-symbols-outlined">image</span>
              </a>
              <a href="#" class="w-12 h-12 rounded-full bg-surface-container-high flex items-center justify-center hover:bg-primary hover:text-white transition-colors">
                <span class="material-symbols-outlined">play_circle</span>
              </a>
            </div>
          </div>
        </div>
      </div>
    </main>

    <AppBottomNav />
  </div>
</template>

<script setup lang="ts">
import AppBottomNav from '../../components/AppBottomNav.vue'
import AppTopNav from '../../components/AppTopNav.vue'
import PageBackHeader from '../../components/PageBackHeader.vue'
</script>
