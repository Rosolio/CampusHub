<template>
  <div class="bg-surface font-body text-on-surface min-h-screen pb-24 md:pb-0">
    <AppTopNav :show-avatar="false" />

    <main class="pt-24 px-6 max-w-5xl mx-auto">
      <PageBackHeader />

      <div class="bg-surface-container-lowest rounded-[2rem] p-8 shadow-sm">
        <h1 class="text-3xl font-extrabold font-headline text-teal-900 mb-6">用户协议</h1>
        
        <div class="space-y-6">
          <!-- 协议更新时间 -->
          <div class="text-sm text-on-surface-variant">
            最后更新时间：2026年4月2日
          </div>

          <!-- 引言 -->
          <div>
            <p class="text-on-surface-variant leading-relaxed">
              欢迎使用 CampusHub！本用户协议（以下简称“协议”）是您与 CampusHub 之间关于使用我们服务的法律协议。通过注册、登录或使用我们的服务，您同意遵守本协议的所有条款和条件。
            </p>
          </div>

          <!-- 账户注册 -->
          <div>
            <h2 class="text-xl font-bold text-teal-900 mb-4">账户注册</h2>
            <p class="text-on-surface-variant leading-relaxed mb-4">
              您必须满足以下条件才能注册和使用 CampusHub 账户：
            </p>
            <ul class="space-y-3 pl-6 list-disc text-on-surface-variant">
              <li>您必须是在校大学生</li>
              <li>您必须提供真实、准确、完整的个人信息</li>
              <li>您必须遵守本协议和相关法律法规</li>
            </ul>
            <p class="text-on-surface-variant leading-relaxed mt-4">
              您有责任维护账户密码的安全，并对账户下的所有活动负责。
            </p>
          </div>

          <!-- 用户行为规范 -->
          <div>
            <h2 class="text-xl font-bold text-teal-900 mb-4">用户行为规范</h2>
            <p class="text-on-surface-variant leading-relaxed mb-4">
              在使用 CampusHub 服务时，您同意：
            </p>
            <ul class="space-y-3 pl-6 list-disc text-on-surface-variant">
              <li>遵守法律法规和公序良俗</li>
              <li>不发布违法、违规或有害的内容</li>
              <li>不侵犯他人的知识产权和合法权益</li>
              <li>不干扰或破坏服务的正常运行</li>
              <li>不利用服务进行欺诈或其他非法活动</li>
            </ul>
          </div>

          <!-- 服务使用 -->
          <div>
            <h2 class="text-xl font-bold text-teal-900 mb-4">服务使用</h2>
            <p class="text-on-surface-variant leading-relaxed mb-4">
              CampusHub 提供以下服务：
            </p>
            <ul class="space-y-3 pl-6 list-disc text-on-surface-variant">
              <li>发布和接受校园任务</li>
              <li>浏览和搜索社区动态</li>
              <li>发送和接收消息</li>
              <li>管理个人资料和任务</li>
            </ul>
            <p class="text-on-surface-variant leading-relaxed mt-4">
              我们有权根据业务需要调整或变更服务内容，无需提前通知。
            </p>
          </div>

          <!-- 知识产权 -->
          <div>
            <h2 class="text-xl font-bold text-teal-900 mb-4">知识产权</h2>
            <p class="text-on-surface-variant leading-relaxed">
              CampusHub 及其相关服务中包含的所有内容（包括但不限于文本、图片、图标、标识、软件等）的知识产权归我们所有。您在使用服务过程中产生的内容，其知识产权归您所有，但您授予我们在服务范围内使用、复制、修改、发布和展示这些内容的权利。
            </p>
          </div>

          <!-- 免责声明 -->
          <div>
            <h2 class="text-xl font-bold text-teal-900 mb-4">免责声明</h2>
            <p class="text-on-surface-variant leading-relaxed">
              CampusHub 不对以下情况承担责任：
            </p>
            <ul class="space-y-3 pl-6 list-disc text-on-surface-variant">
              <li>因网络故障、系统故障等原因导致的服务中断或数据丢失</li>
              <li>因用户自身原因导致的损失或损害</li>
              <li>用户之间因使用服务而产生的纠纷</li>
              <li>第三方网站或服务的内容和行为</li>
            </ul>
          </div>

          <!-- 协议变更 -->
          <div>
            <h2 class="text-xl font-bold text-teal-900 mb-4">协议变更</h2>
            <p class="text-on-surface-variant leading-relaxed">
              我们有权不时修改本协议。修改后的协议将在平台上公布，您继续使用服务即视为接受修改后的协议。
            </p>
          </div>

          <!-- 终止服务 -->
          <div>
            <h2 class="text-xl font-bold text-teal-900 mb-4">终止服务</h2>
            <p class="text-on-surface-variant leading-relaxed">
              您可以随时停止使用 CampusHub 服务。我们也有权在以下情况下终止或暂停您的服务：
            </p>
            <ul class="space-y-3 pl-6 list-disc text-on-surface-variant">
              <li>您违反本协议的任何条款</li>
              <li>您提供的个人信息不真实、不准确或不完整</li>
              <li>法律法规要求我们这样做</li>
            </ul>
          </div>

          <!-- 法律适用 -->
          <div>
            <h2 class="text-xl font-bold text-teal-900 mb-4">法律适用</h2>
            <p class="text-on-surface-variant leading-relaxed">
              本协议的订立、执行、解释及争议的解决均适用中华人民共和国法律。如就本协议发生任何争议，双方应友好协商解决；协商不成的，任何一方均有权将争议提交至有管辖权的人民法院诉讼解决。
            </p>
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
