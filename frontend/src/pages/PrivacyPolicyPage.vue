<template>
  <div class="min-h-screen bg-surface font-body text-on-surface">
    <div class="sticky top-0 z-30 bg-surface/95 backdrop-blur-sm border-b border-outline-variant/10">
      <div class="mx-auto max-w-3xl flex items-center gap-4 px-6 py-4">
        <button
          type="button"
          class="flex items-center gap-2 rounded-full bg-surface-container-low px-4 py-2 text-sm font-bold text-on-surface-variant hover:text-on-surface transition-colors"
          @click="goBack"
        >
          <span class="material-symbols-outlined text-lg">arrow_back</span>
          返回
        </button>
        <h1 class="text-lg font-extrabold text-on-surface">隐私政策</h1>
      </div>
    </div>

    <main class="mx-auto max-w-3xl px-6 py-8">
      <div class="page-prose space-y-6">
        <div class="text-sm text-on-surface-variant">
          最后更新时间：2026年4月2日
        </div>

        <div>
          <p>
            CampusHub 非常重视用户隐私保护。本隐私政策旨在向您说明我们如何收集、使用、存储和保护您的个人信息，以及您享有的权利。请您仔细阅读本政策，了解我们的隐私保护实践。
          </p>
        </div>

        <div>
          <h2>我们收集的信息</h2>
          <p class="mb-4">
            我们可能收集以下类型的信息：
          </p>
          <ul class="space-y-3 pl-6 list-disc">
            <li>个人身份信息：如姓名、学号、院系、专业、年级等</li>
            <li>联系方式：如手机号码、电子邮箱等</li>
            <li>账户信息：如用户名、密码（加密存储）</li>
            <li>使用信息：如浏览历史、搜索记录、发布的任务和回复等</li>
            <li>设备信息：如设备类型、操作系统、IP地址等</li>
          </ul>
        </div>

        <div>
          <h2>我们如何使用您的信息</h2>
          <p class="mb-4">
            我们收集的信息将用于以下目的：
          </p>
          <ul class="space-y-3 pl-6 list-disc">
            <li>提供和改进我们的服务</li>
            <li>管理您的账户和任务</li>
            <li>发送通知和消息</li>
            <li>个性化您的使用体验</li>
            <li>分析和改进我们的产品</li>
            <li>保护用户安全和防止欺诈</li>
          </ul>
        </div>

        <div>
          <h2>信息共享</h2>
          <p class="mb-4">
            我们不会向第三方出售您的个人信息。但在以下情况下，我们可能会共享您的信息：
          </p>
          <ul class="space-y-3 pl-6 list-disc">
            <li>获得您的明确同意</li>
            <li>遵守法律法规要求</li>
            <li>保护我们的权利、财产或安全</li>
            <li>与我们的服务提供商共享（他们必须保护您的信息）</li>
          </ul>
        </div>

        <div>
          <h2>信息存储和安全</h2>
          <p class="mb-4">
            我们采取以下措施保护您的信息：
          </p>
          <ul class="space-y-3 pl-6 list-disc">
            <li>加密存储敏感信息</li>
            <li>使用安全的服务器和网络</li>
            <li>限制对信息的访问</li>
            <li>定期安全审计和更新</li>
          </ul>
          <p class="mt-4">
            我们会在法律要求的期限内存储您的信息，或为实现本政策所述目的所必需的时间内存储。
          </p>
        </div>

        <div>
          <h2>您的权利</h2>
          <p class="mb-4">
            您有权：
          </p>
          <ul class="space-y-3 pl-6 list-disc">
            <li>访问和查看您的个人信息</li>
            <li>修改或更新您的个人信息</li>
            <li>删除您的个人信息</li>
            <li>限制或反对信息处理</li>
            <li>数据可携带权</li>
            <li>撤回同意</li>
          </ul>
        </div>

        <div>
          <h2>联系我们</h2>
          <p>
            如果您对本隐私政策有任何疑问，请通过以下方式联系我们：
          </p>
          <div class="space-y-3 mt-4">
            <div class="flex items-center gap-3">
              <span class="material-symbols-outlined text-primary">email</span>
              <span class="text-on-surface-variant">privacy@campushub.com</span>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/auth')
  }
}
</script>
