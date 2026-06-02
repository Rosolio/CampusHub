<template>
  <div class="page-shell bg-surface font-body text-on-surface">

    <main class="page-shell-main page-shell-main--narrow">
      <PageBackHeader />

      <div class="page-card">
        <div class="page-card-header">
          <p class="page-kicker">Agreement</p>
          <h1 class="page-title">用户协议</h1>
          <p class="page-description">说明 CampusHub 服务的使用边界、用户义务、平台权利以及协议更新方式。</p>
        </div>
        
        <div class="page-prose space-y-6">
          <div class="text-sm text-on-surface-variant">
            最后更新时间：2026年4月2日
          </div>

          <div>
            <p>
              欢迎使用 CampusHub！本用户协议（以下简称“协议”）是您与 CampusHub 之间关于使用我们服务的法律协议。通过注册、登录或使用我们的服务，您同意遵守本协议的所有条款和条件。
            </p>
          </div>

          <div>
            <h2>账户注册</h2>
            <p class="mb-4">
              您必须满足以下条件才能注册和使用 CampusHub 账户：
            </p>
            <ul class="space-y-3 pl-6 list-disc">
              <li>您必须是在校大学生</li>
              <li>您必须提供真实、准确、完整的个人信息</li>
              <li>您必须遵守本协议和相关法律法规</li>
            </ul>
            <p class="mt-4">
              您有责任维护账户密码的安全，并对账户下的所有活动负责。
            </p>
          </div>

          <div>
            <h2>用户行为规范</h2>
            <p class="mb-4">
              在使用 CampusHub 服务时，您同意：
            </p>
            <ul class="space-y-3 pl-6 list-disc">
              <li>遵守法律法规和公序良俗</li>
              <li>不发布违法、违规或有害的内容</li>
              <li>不侵犯他人的知识产权和合法权益</li>
              <li>不干扰或破坏服务的正常运行</li>
              <li>不利用服务进行欺诈或其他非法活动</li>
            </ul>
          </div>

          <div>
            <h2>服务使用</h2>
            <p class="mb-4">
              CampusHub 提供以下服务：
            </p>
            <ul class="space-y-3 pl-6 list-disc">
              <li>发布和接受校园任务</li>
              <li>浏览和搜索社区动态</li>
              <li>发送和接收消息</li>
              <li>管理个人资料和任务</li>
            </ul>
            <p class="mt-4">
              我们有权根据业务需要调整或变更服务内容，无需提前通知。
            </p>
          </div>

          <div>
            <h2>知识产权</h2>
            <p>
              CampusHub 及其相关服务中包含的所有内容（包括但不限于文本、图片、图标、标识、软件等）的知识产权归我们所有。您在使用服务过程中产生的内容，其知识产权归您所有，但您授予我们在服务范围内使用、复制、修改、发布和展示这些内容的权利。
            </p>
          </div>

          <div>
            <h2>免责声明</h2>
            <p>
              CampusHub 不对以下情况承担责任：
            </p>
            <ul class="space-y-3 pl-6 list-disc">
              <li>因网络故障、系统故障等原因导致的服务中断或数据丢失</li>
              <li>因用户自身原因导致的损失或损害</li>
              <li>用户之间因使用服务而产生的纠纷</li>
              <li>第三方网站或服务的内容和行为</li>
            </ul>
          </div>

          <div>
            <h2>协议变更</h2>
            <p>
              我们有权不时修改本协议。修改后的协议将在平台上公布，您继续使用服务即视为接受修改后的协议。
            </p>
          </div>

          <div>
            <h2>终止服务</h2>
            <p>
              您可以随时停止使用 CampusHub 服务。我们也有权在以下情况下终止或暂停您的服务：
            </p>
            <ul class="space-y-3 pl-6 list-disc">
              <li>您违反本协议的任何条款</li>
              <li>您提供的个人信息不真实、不准确或不完整</li>
              <li>法律法规要求我们这样做</li>
            </ul>
          </div>

          <div>
            <h2>法律适用</h2>
            <p>
              本协议的订立、执行、解释及争议的解决均适用中华人民共和国法律。如就本协议发生任何争议，双方应友好协商解决；协商不成的，任何一方均有权将争议提交至有管辖权的人民法院诉讼解决。
            </p>
          </div>
        </div>
      </div>
    </main>

  </div>
</template>

<script setup lang="ts">
import PageBackHeader from '../../components/PageBackHeader.vue'
</script>
