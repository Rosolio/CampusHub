<template>
  <div class="page-shell bg-background text-on-surface md:pt-20">
    <AppTopNav :avatar-url="currentUser.avatarUrl || defaultAvatarUrl" />

    <main class="page-shell-main max-w-7xl">
      <section class="relative mb-10">
        <div class="relative flex flex-col gap-6 overflow-hidden rounded-[2rem] bg-surface-container-low p-6 md:flex-row md:items-end md:p-10">
          <div class="absolute -top-24 -right-24 w-96 h-96 bg-primary/5 rounded-full blur-3xl pointer-events-none"></div>

          <div class="relative">
            <div class="w-32 h-32 md:w-44 md:h-44 rounded-[2.5rem] bg-surface-container-lowest p-2 shadow-xl rotate-3">
              <img
                :alt="currentUser.name || '当前用户'"
                class="w-full h-full object-cover rounded-[2rem]"
                data-alt="Portrait of a college student avatar"
                :src="currentUser.avatarUrl || defaultAvatarUrl"
              />
            </div>
            <div class="absolute -bottom-2 -right-2 bg-secondary-container text-on-secondary-container px-4 py-1.5 rounded-full text-xs font-bold shadow-lg flex items-center gap-1">
              <span class="material-symbols-outlined text-sm" style="font-variation-settings:'FILL' 1;">stars</span>
              当前账号
            </div>
          </div>

          <div class="flex-1 space-y-2 text-center md:text-left">
            <h1 class="text-4xl md:text-5xl font-extrabold text-headline tracking-tighter text-on-surface">{{ currentUser.name || '未命名用户' }}</h1>
            <p class="text-base font-medium text-on-surface-variant md:text-lg">{{ currentUser.major || '暂未填写专业信息' }}</p>
            <div class="flex flex-wrap justify-center md:justify-start gap-3 pt-4">
              <span class="px-4 py-1.5 rounded-full text-sm font-semibold flex items-center gap-2" :class="creditTier.badgeClass">
                <span class="material-symbols-outlined text-lg">workspace_premium</span>
                {{ creditTier.label }}
              </span>
              <span
                v-if="currentUser.verifiedStatus === 'VERIFIED'"
                class="bg-emerald-100 px-4 py-1.5 rounded-full text-sm font-semibold flex items-center gap-2"
              >
                <span class="material-symbols-outlined text-emerald-600 text-lg" style="font-variation-settings:'FILL' 1;">verified</span>
                身份已验证
              </span>
              <span
                v-else-if="currentUser.verifiedStatus === 'PENDING'"
                class="bg-amber-50 px-4 py-1.5 rounded-full text-sm font-semibold flex items-center gap-2"
              >
                <span class="material-symbols-outlined text-amber-500 text-lg">hourglass_top</span>
                认证审核中
              </span>
              <RouterLink
                v-else
                to="/verification"
                class="bg-surface-container-highest/50 px-4 py-1.5 rounded-full text-sm font-semibold flex items-center gap-2 hover:bg-primary/10 hover:text-primary transition-colors"
              >
                <span class="material-symbols-outlined text-primary text-lg">verified</span>
                申请认证
              </RouterLink>
              <span class="bg-surface-container-highest/50 px-4 py-1.5 rounded-full text-sm font-semibold flex items-center gap-2">
                <span class="material-symbols-outlined text-primary text-lg">location_on</span>
                {{ currentUser.email || '未设置邮箱' }}
              </span>
            </div>
          </div>

          <div class="flex w-full flex-col gap-3 md:w-auto">
            <button
              class="bg-gradient-to-br from-primary to-primary-dim text-on-primary px-8 py-4 rounded-xl font-bold flex items-center justify-center gap-3 shadow-lg transition-all hover:brightness-110 active:scale-95"
              type="button"
              @click="goToPublish"
            >
              <span class="material-symbols-outlined">add_circle</span>
              发布新需求
            </button>
            <RouterLink
              to="/settings/profile"
              class="bg-surface-container-highest text-on-surface-variant px-8 py-4 rounded-xl font-bold text-sm transition-all hover:bg-surface-container-high active:scale-95 text-center"
            >
              编辑个人资料
            </RouterLink>
          </div>
        </div>
      </section>

      <section class="mb-10 grid grid-cols-1 gap-5 md:grid-cols-2 xl:grid-cols-5">
        <button type="button" class="group relative flex flex-col justify-between overflow-hidden rounded-[2rem] bg-surface-container-lowest p-6 text-left transition-colors hover:bg-surface-container-low md:col-span-2 xl:col-span-2" @click="openPointRecordsDialog">
          <div class="absolute top-0 right-0 w-64 h-64 bg-secondary/10 rounded-full blur-3xl -mr-20 -mt-20 group-hover:bg-secondary/20 transition-all duration-700"></div>
          <div>
            <div class="flex justify-between items-start mb-8">
              <div class="space-y-1">
                <h3 class="text-on-surface-variant font-bold text-sm uppercase tracking-widest">社区信任等级</h3>
                <p class="text-3xl font-extrabold text-headline">{{ currentUser.points ?? 0 }} 积分</p>
                <p class="text-sm font-semibold" :class="creditTier.textClass">{{ creditTier.label }}</p>
              </div>
              <div class="w-12 h-12 rounded-full bg-secondary-container flex items-center justify-center text-on-secondary-container">
                <span class="material-symbols-outlined">auto_awesome</span>
              </div>
            </div>
            <div class="h-4 w-full bg-surface-container-low rounded-full overflow-hidden">
              <div class="h-full bg-secondary rounded-full transition-all duration-500" :style="{ width: `${creditProgress}%` }"></div>
            </div>
          </div>
          <p class="text-sm text-on-surface-variant mt-6 leading-relaxed">
            当前信用星级 <span class="text-secondary font-bold">{{ formatScore(currentUser.score) }}</span> / 5
          </p>
        </button>

        <div class="flex flex-col items-center justify-center gap-4 rounded-[2rem] bg-surface-container-lowest p-6 text-center">
          <div class="w-16 h-16 rounded-full bg-tertiary-container/30 flex items-center justify-center text-tertiary">
            <span class="material-symbols-outlined text-3xl" style="font-variation-settings:'FILL' 1;">volunteer_activism</span>
          </div>
          <div>
            <span class="block text-4xl font-extrabold text-headline">{{ completedTaskCount }}</span>
            <span class="text-on-surface-variant font-semibold">累计完成任务</span>
          </div>
        </div>

        <div class="flex flex-col items-center justify-center gap-4 rounded-[2rem] bg-surface-container-lowest p-6 text-center">
          <div class="w-16 h-16 rounded-full bg-secondary-container/30 flex items-center justify-center text-secondary">
            <span class="material-symbols-outlined text-3xl" style="font-variation-settings:'FILL' 1;">favorite</span>
          </div>
          <div>
            <span class="block text-4xl font-extrabold text-headline">{{ receivedLikeCount }}</span>
            <span class="text-on-surface-variant font-semibold">累计收获点赞</span>
          </div>
        </div>

        <div class="flex flex-col items-center justify-center gap-4 rounded-[2rem] bg-surface-container-lowest p-6 text-center">
          <div class="w-16 h-16 rounded-full bg-primary-container/30 flex items-center justify-center text-primary">
            <span class="material-symbols-outlined text-3xl" style="font-variation-settings:'FILL' 1;">pending_actions</span>
          </div>
          <div>
            <span class="block text-4xl font-extrabold text-headline">{{ activeTaskCount }}</span>
            <span class="text-on-surface-variant font-semibold">当前需求</span>
          </div>
        </div>
      </section>

      <section class="mb-10 grid grid-cols-1 gap-5 lg:grid-cols-3">
        <article class="rounded-[2rem] bg-surface-container-lowest p-6 shadow-sm">
          <p class="text-sm font-bold uppercase tracking-[0.2em] text-on-surface-variant">评价摘要</p>
          <div class="mt-4 flex items-center gap-3">
            <p class="text-4xl font-extrabold text-teal-900">{{ averageReviewStars }}</p>
            <span class="material-symbols-outlined text-3xl text-amber-500" style="font-variation-settings:'FILL' 1;">star</span>
          </div>
          <p class="mt-2 text-sm text-on-surface-variant">这里显示你收到的任务互评平均星级，按真实互评记录实时计算。</p>
          <div class="mt-4 flex flex-wrap gap-2">
            <span v-for="item in reviewSummaryHighlights" :key="item.label" class="rounded-full bg-surface-container-low px-4 py-2 text-sm font-semibold text-on-surface">
              {{ item.label }} {{ item.value }}
            </span>
          </div>
        </article>

        <article class="rounded-[2rem] bg-surface-container-lowest p-6 shadow-sm">
          <p class="text-sm font-bold uppercase tracking-[0.2em] text-on-surface-variant">历史概览</p>
          <div class="mt-4 grid grid-cols-2 gap-3">
            <div class="rounded-2xl bg-emerald-50 px-4 py-4">
              <p class="text-xs font-bold uppercase tracking-[0.18em] text-emerald-700/70">已完成订单</p>
              <p class="mt-2 text-3xl font-extrabold text-emerald-800">{{ completedTaskCount }}</p>
            </div>
            <div class="rounded-2xl bg-amber-50 px-4 py-4">
              <p class="text-xs font-bold uppercase tracking-[0.18em] text-amber-700/70">进行中订单</p>
              <p class="mt-2 text-3xl font-extrabold text-amber-800">{{ activeTaskCount }}</p>
            </div>
          </div>
          <p class="mt-4 text-sm text-on-surface-variant">这里只统计任务型订单，不包含话题帖互动数据。</p>
        </article>

        <article class="rounded-[2rem] bg-surface-container-lowest p-6 shadow-sm">
          <p class="text-sm font-bold uppercase tracking-[0.2em] text-on-surface-variant">内容发布概览</p>
          <p class="mt-4 text-4xl font-extrabold text-teal-900">{{ myTopicPosts.length }}</p>
          <p class="mt-2 text-sm text-on-surface-variant">这是你累计发布的话题帖数量，和订单统计分开展示。</p>
          <div class="mt-4 flex flex-wrap gap-2">
            <span class="rounded-full bg-rose-100 px-4 py-2 text-sm font-semibold text-rose-700">话题互动单独统计</span>
            <span class="rounded-full bg-surface-container-low px-4 py-2 text-sm font-semibold text-on-surface">已拆分展示</span>
          </div>
        </article>
      </section>

      <section class="space-y-6">
        <div class="flex gap-6 overflow-x-auto border-b border-outline-variant/15 px-1 pb-1">
          <button
            @click="activeTab = 'requests'"
            class="shrink-0 pb-4 text-lg font-bold tracking-tight transition-all md:text-xl"
            :class="activeTab === 'requests' ? 'border-b-4 border-primary text-primary' : 'border-b-4 border-transparent text-on-surface-variant/50 hover:text-on-surface-variant'"
            type="button"
          >
            我的需求
          </button>
          <button
            @click="activeTab = 'topics'"
            class="shrink-0 pb-4 text-lg font-bold tracking-tight transition-all md:text-xl"
            :class="activeTab === 'topics' ? 'border-b-4 border-primary text-primary' : 'border-b-4 border-transparent text-on-surface-variant/50 hover:text-on-surface-variant'"
            type="button"
          >
            我的话题帖
          </button>
          <button
            @click="activeTab = 'services'"
            class="shrink-0 pb-4 text-lg font-bold tracking-tight transition-all md:text-xl"
            :class="activeTab === 'services' ? 'border-b-4 border-primary text-primary' : 'border-b-4 border-transparent text-on-surface-variant/50 hover:text-on-surface-variant'"
            type="button"
          >
            我的服务
          </button>
        </div>

        <div v-if="activeTab !== 'topics'" class="rounded-[1.75rem] bg-surface-container-lowest p-4 shadow-sm">
          <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <div class="flex flex-wrap gap-2">
            <button
              v-for="filter in statusFilters"
              :key="filter.value"
              type="button"
              class="rounded-full px-4 py-2 text-sm font-bold transition-colors"
              :class="activeStatusFilter === filter.value ? 'bg-teal-900 text-white' : 'bg-surface-container-low text-teal-900 hover:bg-surface-container-high'"
              @click="activeStatusFilter = filter.value"
            >
              {{ filter.label }}
            </button>
          </div>
            <label class="flex items-center gap-3 rounded-2xl bg-surface-container-low px-4 py-3 text-sm text-on-surface lg:min-w-[19rem]">
            <span class="material-symbols-outlined text-lg text-on-surface-variant">search</span>
            <input
              v-model.trim="historyKeyword"
              type="text"
              class="min-w-0 flex-1 bg-transparent outline-none placeholder:text-on-surface-variant/60"
              placeholder="按标题、地点、奖励筛选历史订单"
            />
            </label>
          </div>
        </div>

        <!-- Content Area (My Requests) -->
        <div class="grid grid-cols-1 gap-6 lg:grid-cols-2" v-if="activeTab === 'requests'">
          <div
            v-for="task in filteredMyTasks"
            :key="task.id"
            class="group cursor-pointer rounded-[1.5rem] bg-surface-container-lowest p-5 transition-colors duration-300 hover:bg-surface-container-low md:p-6"
            @click="goToTaskDetail(task.id)"
          >
            <div class="flex flex-col gap-5 sm:flex-row">
              <div class="h-24 w-full overflow-hidden rounded-2xl sm:w-24 sm:flex-shrink-0">
                <img
                  :alt="task.title"
                  class="h-full w-full object-cover"
                  data-alt="Task image"
                  :src="task.mapImageUrl || defaultTaskImage"
                />
              </div>
              <div class="flex-1 space-y-4">
                <div class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                  <div class="flex flex-wrap gap-2">
                    <span class="rounded-full bg-tertiary-container px-3 py-1 text-[10px] font-bold uppercase text-on-tertiary-container">{{ formatStatus(task.status) }}</span>
                    <span class="rounded-full bg-surface-container-high px-3 py-1 text-[10px] font-bold uppercase text-on-surface-variant">{{ task.category || '互助需求' }}</span>
                    <span
                      v-if="task.status === 'completed'"
                      class="rounded-full px-3 py-1 text-[10px] font-bold uppercase"
                      :class="reviewStatusBadgeClass(task.id)"
                    >
                      {{ reviewStatusLabel(task.id) }}
                    </span>
                  </div>
                  <span class="text-xs font-medium text-on-surface-variant">{{ formatCreatedAt(task.createdAt) }}</span>
                </div>
                <div>
                  <h4 class="text-xl font-bold leading-tight text-headline">{{ task.title }}</h4>
                  <p class="mt-2 line-clamp-2 text-sm leading-6 text-on-surface-variant">
                    {{ task.description || '暂无补充说明。' }}
                  </p>
                </div>
                <div class="grid gap-3 text-sm text-on-surface-variant sm:grid-cols-2">
                  <span class="flex items-center gap-2 rounded-2xl bg-surface-container-low px-3 py-3">
                    <span class="material-symbols-outlined text-base">schedule</span>
                    {{ task.timeText || '时间待定' }}
                  </span>
                  <span class="flex items-center gap-2 rounded-2xl bg-surface-container-low px-3 py-3">
                    <span class="material-symbols-outlined text-base">payments</span>
                    {{ task.rewardText || task.rewardTitle || '奖励待定' }}
                  </span>
                </div>
                <div
                  v-if="showRequesterAcceptanceReminder(task)"
                  class="rounded-2xl border border-amber-200 bg-gradient-to-r from-amber-50 to-orange-50 px-4 py-3 text-amber-900"
                >
                  <div class="flex items-start justify-between gap-3">
                    <div>
                      <p class="text-xs font-bold uppercase tracking-[0.16em]">有人接单</p>
                      <p class="mt-1 text-sm leading-6">
                        {{ task.helperName || '接单同学' }} 已接单，建议尽快确认细节。
                      </p>
                    </div>
                    <span class="material-symbols-outlined text-xl">notifications_active</span>
                  </div>
                  <button
                    type="button"
                    class="mt-3 inline-flex items-center gap-2 rounded-full bg-amber-900 px-3 py-1.5 text-xs font-bold text-white transition-colors hover:bg-amber-950"
                    @click.stop="goToTaskMessages(task)"
                  >
                    <span class="material-symbols-outlined text-sm">chat</span>
                    去消息页沟通
                  </button>
                </div>
                <div class="flex flex-col gap-3 border-t border-outline-variant/12 pt-4 sm:flex-row sm:items-center sm:justify-between">
                  <div class="flex items-center gap-2 text-sm font-bold text-primary">
                    查看详情
                    <span class="material-symbols-outlined text-sm">arrow_forward</span>
                  </div>
                  <div class="flex flex-wrap gap-2">
                    <button
                      v-if="task.status === 'accepted' || task.status === 'completion_pending'"
                      type="button"
                      class="inline-flex items-center justify-center gap-1 rounded-full bg-emerald-100 px-3 py-2 text-xs font-bold text-emerald-800 transition-colors hover:bg-emerald-200"
                      :disabled="completingTaskId === task.id"
                      @click.stop="handleCompleteTask(task)"
                    >
                      <span class="material-symbols-outlined text-sm">task_alt</span>
                      {{ completingTaskId === task.id ? '处理中' : (task.status === 'completion_pending' ? '确认对方完成' : '提交完成确认') }}
                    </button>
                    <button
                      type="button"
                      class="inline-flex items-center justify-center gap-1 rounded-full px-3 py-2 text-xs font-bold transition-colors"
                      :class="isDeleteDisabled(task.status) || deletingTaskId === task.id
                        ? 'cursor-not-allowed bg-surface-container-high text-on-surface-variant'
                        : 'bg-error/10 text-error hover:bg-error/15'"
                      :disabled="isDeleteDisabled(task.status) || deletingTaskId === task.id"
                      @click.stop="handleDeleteTask(task)"
                    >
                      <span class="material-symbols-outlined text-sm">delete</span>
                      {{ deletingTaskId === task.id ? '删除中' : '删除' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-if="filteredMyTasks.length === 0" class="lg:col-span-2 bg-surface-container-lowest p-8 rounded-[2rem]">
            <h3 class="text-xl font-bold text-headline mb-2">还没有发布需求</h3>
            <p class="text-on-surface-variant">{{ historyEmptyText }}</p>
          </div>
        </div>

        <div v-if="activeTab === 'topics'" class="rounded-[1.75rem] bg-surface-container-lowest p-4 shadow-sm">
          <div class="flex justify-end">
            <label class="flex w-full items-center gap-3 rounded-2xl bg-surface-container-low px-4 py-3 text-sm text-on-surface lg:max-w-md">
            <span class="material-symbols-outlined text-lg text-on-surface-variant">search</span>
            <input
              v-model.trim="historyKeyword"
              type="text"
              class="min-w-0 flex-1 bg-transparent outline-none placeholder:text-on-surface-variant/60"
              placeholder="按标题、分类、地点搜索话题帖"
            />
            </label>
          </div>
        </div>

        <!-- Content Area (My Services) -->
        <div v-if="activeTab === 'topics'" class="grid grid-cols-1 gap-6 lg:grid-cols-2">
          <div
            v-for="task in filteredMyTopicPosts"
            :key="task.id"
            class="group cursor-pointer rounded-[1.5rem] bg-surface-container-lowest p-5 transition-colors duration-300 hover:bg-surface-container-low md:p-6"
            @click="goToTaskDetail(task.id)"
          >
            <div class="flex flex-col gap-5 sm:flex-row">
              <div class="h-24 w-full flex-shrink-0 rounded-2xl bg-gradient-to-br from-amber-400 via-rose-400 to-sky-500 p-[2px] sm:w-24">
                <div class="flex h-full w-full items-center justify-center rounded-[0.95rem] bg-white">
                  <span class="material-symbols-outlined text-4xl text-teal-900">forum</span>
                </div>
              </div>
              <div class="flex-1 space-y-4">
                <div class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                  <div class="flex flex-wrap gap-2">
                    <span class="rounded-full bg-amber-100 px-3 py-1 text-[10px] font-bold uppercase text-amber-800">话题帖</span>
                    <span class="rounded-full bg-surface-container-high px-3 py-1 text-[10px] font-bold uppercase text-on-surface-variant">{{ task.category || '校园互助' }}</span>
                  </div>
                  <span class="text-xs font-medium text-on-surface-variant">{{ formatCreatedAt(task.createdAt) }}</span>
                </div>
                <div>
                  <h4 class="text-xl font-bold leading-tight text-headline">{{ task.title }}</h4>
                  <p class="mt-2 line-clamp-2 text-sm leading-6 text-on-surface-variant">{{ task.description }}</p>
                </div>
                <div class="grid gap-3 text-sm text-on-surface-variant sm:grid-cols-2">
                  <span class="flex items-center gap-2 rounded-2xl bg-surface-container-low px-3 py-3">
                    <span class="material-symbols-outlined text-base">chat_bubble</span>
                    {{ task.commentCount || 0 }} 评论
                  </span>
                  <span class="flex items-center gap-2 rounded-2xl bg-surface-container-low px-3 py-3">
                    <span class="material-symbols-outlined text-base">favorite</span>
                    {{ task.likeCount || 0 }} 点赞
                  </span>
                </div>
                <div class="flex flex-col gap-3 border-t border-outline-variant/12 pt-4 sm:flex-row sm:items-center sm:justify-between">
                  <div class="flex items-center gap-2 text-sm font-bold text-primary">
                    进入帖子
                    <span class="material-symbols-outlined text-sm">arrow_forward</span>
                  </div>
                  <button
                    type="button"
                    class="inline-flex items-center justify-center gap-1 rounded-full px-3 py-2 text-xs font-bold transition-colors"
                    :class="deletingTaskId === task.id
                      ? 'cursor-not-allowed bg-surface-container-high text-on-surface-variant'
                      : 'bg-error/10 text-error hover:bg-error/15'"
                    :disabled="deletingTaskId === task.id"
                    @click.stop="handleDeleteTask(task)"
                  >
                    <span class="material-symbols-outlined text-sm">delete</span>
                    {{ deletingTaskId === task.id ? '删除中' : '删除' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
          <div v-if="filteredMyTopicPosts.length === 0" class="lg:col-span-2 bg-surface-container-lowest p-8 rounded-[2rem]">
            <h3 class="text-xl font-bold text-headline mb-2">还没有发布话题帖</h3>
            <p class="text-on-surface-variant">{{ historyEmptyText }}</p>
          </div>
        </div>

        <div v-else class="grid grid-cols-1 gap-6 lg:grid-cols-2">
          <div
            v-for="task in filteredMyServiceTasks"
            :key="task.id"
            class="group cursor-pointer rounded-[1.5rem] bg-surface-container-lowest p-5 transition-colors duration-300 hover:bg-surface-container-low md:p-6"
            @click="goToTaskDetail(task.id)"
          >
            <div class="flex flex-col gap-5 sm:flex-row">
              <div class="h-24 w-full overflow-hidden rounded-2xl sm:w-24 sm:flex-shrink-0">
                <img
                  :alt="task.title"
                  class="h-full w-full object-cover"
                  data-alt="Service image"
                  :src="task.mapImageUrl || defaultTaskImage"
                />
              </div>
              <div class="flex-1 space-y-4">
                <div class="flex flex-col gap-3 sm:flex-row sm:items-start sm:justify-between">
                  <div class="flex flex-wrap gap-2">
                    <span class="rounded-full bg-secondary-container px-3 py-1 text-[10px] font-bold uppercase text-on-secondary-container">{{ formatServiceStatus(task.status) }}</span>
                    <span class="rounded-full bg-surface-container-high px-3 py-1 text-[10px] font-bold uppercase text-on-surface-variant">{{ task.category || '我的服务' }}</span>
                    <span
                      v-if="task.status === 'completed'"
                      class="rounded-full px-3 py-1 text-[10px] font-bold uppercase"
                      :class="reviewStatusBadgeClass(task.id)"
                    >
                      {{ reviewStatusLabel(task.id) }}
                    </span>
                  </div>
                  <span class="text-xs font-medium text-on-surface-variant">{{ formatCreatedAt(task.createdAt) }}</span>
                </div>
                <div>
                  <h4 class="text-xl font-bold leading-tight text-headline">{{ task.title }}</h4>
                  <p class="mt-2 line-clamp-2 text-sm leading-6 text-on-surface-variant">
                    {{ task.description || '暂无补充说明。' }}
                  </p>
                </div>
                <div class="grid gap-3 text-sm text-on-surface-variant sm:grid-cols-2">
                  <span class="flex items-center gap-2 rounded-2xl bg-surface-container-low px-3 py-3">
                    <span class="material-symbols-outlined text-base">schedule</span>
                    {{ task.timeText || '时间待定' }}
                  </span>
                  <span class="flex items-center gap-2 rounded-2xl bg-surface-container-low px-3 py-3">
                    <span class="material-symbols-outlined text-base">payments</span>
                    {{ task.rewardText || task.rewardTitle || '奖励待定' }}
                  </span>
                </div>
                <div class="flex flex-col gap-3 border-t border-outline-variant/12 pt-4 sm:flex-row sm:items-center sm:justify-between">
                  <div class="flex items-center gap-2 text-sm font-bold text-primary">
                    查看详情
                    <span class="material-symbols-outlined text-sm">arrow_forward</span>
                  </div>
                  <div class="flex flex-wrap gap-2">
                    <button
                      v-if="task.status === 'accepted' || task.status === 'completion_pending'"
                      type="button"
                      class="inline-flex items-center justify-center gap-1 rounded-full bg-emerald-100 px-3 py-2 text-xs font-bold text-emerald-800 transition-colors hover:bg-emerald-200"
                      :disabled="completingTaskId === task.id"
                      @click.stop="handleCompleteTask(task)"
                    >
                      <span class="material-symbols-outlined text-sm">task_alt</span>
                      {{ completingTaskId === task.id ? '处理中' : (task.status === 'completion_pending' ? '确认对方完成' : '提交完成确认') }}
                    </button>
                    <button
                      v-if="task.status === 'accepted'"
                      type="button"
                      class="inline-flex items-center justify-center gap-1 rounded-full bg-amber-100 px-3 py-2 text-xs font-bold text-amber-800 transition-colors hover:bg-amber-200"
                      :disabled="cancelingServiceTaskId === task.id"
                      @click.stop="handleUnacceptTask(task)"
                    >
                      <span class="material-symbols-outlined text-sm">undo</span>
                      {{ cancelingServiceTaskId === task.id ? '处理中' : '取消接单' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div v-if="filteredMyServiceTasks.length === 0" class="lg:col-span-2 bg-surface-container-lowest p-8 rounded-[2rem]">
            <h3 class="text-xl font-bold text-headline mb-2">还没有接单记录</h3>
            <p class="text-on-surface-variant">{{ historyEmptyText }}</p>
          </div>
        </div>
      </section>
    </main>

    <div v-if="showPointRecordsDialog" class="fixed inset-0 z-[60] flex items-center justify-center bg-slate-950/45 px-6" @click.self="closePointRecordsDialog">
      <div class="w-full max-w-2xl rounded-[2rem] bg-surface-container-lowest p-8 shadow-2xl">
        <div class="mb-6 flex items-start justify-between gap-4">
          <div>
            <h3 class="text-2xl font-extrabold text-on-surface">积分明细</h3>
            <p class="mt-2 text-sm text-on-surface-variant">展示最近 50 条积分变动记录。评论或回复类奖励每日最多获得 20 积分。</p>
          </div>
          <button type="button" class="rounded-full p-2 text-on-surface-variant transition-colors hover:bg-surface-container-low hover:text-on-surface" @click="closePointRecordsDialog">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>

        <div v-if="pointRecordsLoading" class="rounded-2xl bg-surface-container-low px-4 py-8 text-center text-on-surface-variant">
          正在加载积分明细...
        </div>
        <div v-else-if="pointRecords.length === 0" class="rounded-2xl bg-surface-container-low px-4 py-8 text-center text-on-surface-variant">
          暂无积分记录。
        </div>
        <div v-else class="max-h-[26rem] space-y-3 overflow-y-auto pr-1">
          <article v-for="record in pointRecords" :key="record.id" class="rounded-2xl bg-surface-container-low px-4 py-4">
            <div class="flex items-start justify-between gap-4">
              <div>
                <p class="font-bold text-on-surface">{{ record.description }}</p>
                <p class="mt-1 text-xs text-on-surface-variant">{{ formatCreatedAt(record.createdAt) }}</p>
              </div>
              <span class="text-sm font-extrabold" :class="Number(record.points) >= 0 ? 'text-emerald-700' : 'text-rose-600'">
                {{ Number(record.points) >= 0 ? `+${record.points}` : record.points }} 积分
              </span>
            </div>
          </article>
        </div>

        <section class="mt-6 rounded-[1.75rem] bg-surface-container-low p-5">
          <p class="text-sm font-bold uppercase tracking-[0.18em] text-on-surface-variant">积分规则说明</p>
          <div class="mt-4 space-y-4 text-sm leading-7 text-on-surface-variant">
            <p>
              1. 发布一条评论或回复可获得 5 积分。当日通过评论或回复获得的积分上限为 20 分，超过后仍可发言，但不会重复加分。
            </p>
            <p>
              2. 你的话题帖每收到一次点赞可获得 1 积分；你的评论每收到一次点赞也可获得 1 积分。对方取消点赞后，对应积分会同步扣回。
            </p>
            <p>
              3. 互助任务在双方都确认完成后，需求方和服务方都可获得 10 积分。
            </p>
            <p>
              4. 成功提交一条任务评价可获得 3 积分；被评价方会按评分结算额外积分：5 星 +8、4 星 +5、3 星 +2、2 星 0、1 星 -3。
            </p>
            <p>
              5. 所有积分变动都会记录在积分明细中，方便随时查看来源。
            </p>
          </div>
        </section>
      </div>
    </div>

    <AppBottomNav />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import AppBottomNav from '../components/AppBottomNav.vue'
import AppTopNav from '../components/AppTopNav.vue'
import { useConfirm } from '../composables/useConfirm'
import { usePreferences } from '../composables/usePreferences'
import { showToast } from '../composables/useToast'
import { DEFAULT_AVATAR_URL, DEFAULT_TASK_IMAGE } from '../constants/assets'
import { taskApi, userApi } from '../services/api'
import { storedUser, setStoredUser } from '../utils/auth'

type TabKey = 'requests' | 'topics' | 'services'
type StatusFilter = 'all' | 'active' | 'completed' | 'canceled'

const props = defineProps<{
  initialTab?: TabKey
}>()

const router = useRouter()
const { formatLocaleDateTime } = usePreferences()
const { openConfirm } = useConfirm()
const activeTab = ref<TabKey>(props.initialTab ?? 'requests')
const currentUser = computed(() => storedUser.value || {})
const myTasks = ref<any[]>([])
const myServiceTasks = ref<any[]>([])
const receivedLikeCount = ref(0)
const activeStatusFilter = ref<StatusFilter>('all')
const historyKeyword = ref('')
const deletingTaskId = ref<number | null>(null)
const cancelingServiceTaskId = ref<number | null>(null)
const completingTaskId = ref<number | null>(null)
const taskReviewStatusMap = ref<Record<number, 'pending' | 'completed'>>({})
const receivedReviewAverage = ref(0)
const receivedReviewCount = ref(0)
const showPointRecordsDialog = ref(false)
const pointRecordsLoading = ref(false)
const pointRecords = ref<any[]>([])
const defaultAvatarUrl = DEFAULT_AVATAR_URL
const defaultTaskImage = DEFAULT_TASK_IMAGE
const statusFilters: Array<{ label: string; value: StatusFilter }> = [
  { label: '全部订单', value: 'all' },
  { label: '进行中', value: 'active' },
  { label: '已完成', value: 'completed' },
  { label: '已取消', value: 'canceled' }
]

const uniqueTaskMap = computed(() => {
  const taskMap = new Map<number, any>()
  ;[...myRequestTasks.value, ...myServiceTasks.value].forEach((task) => {
    if (typeof task?.id === 'number') {
      taskMap.set(task.id, task)
    }
  })
  return taskMap
})

const myRequestTasks = computed(() => myTasks.value.filter((task) => String(task?.taskMode || 'task') === 'task'))
const myTopicPosts = computed(() => myTasks.value.filter((task) => String(task?.taskMode || '') === 'topic'))

const completedTaskCount = computed(() => Array.from(uniqueTaskMap.value.values()).filter(task => task.status === 'completed').length)
const activeTaskCount = computed(() => Array.from(uniqueTaskMap.value.values()).filter(task => ['pending', 'accepted', 'completion_pending'].includes(String(task.status || ''))).length)
const creditScore = computed(() => Number(currentUser.value?.score || 0))
const creditTier = computed(() => {
  const score = creditScore.value
  if (score >= 4.5) {
    return {
      label: '信用等级 S',
      badgeClass: 'bg-emerald-100 text-emerald-800',
      textClass: 'text-emerald-700',
      tip: '你已经进入高信任区间，继续保持稳定履约和友好评价。'
    }
  }
  if (score >= 4.0) {
    return {
      label: '信用等级 A',
      badgeClass: 'bg-sky-100 text-sky-800',
      textClass: 'text-sky-700',
      tip: '整体表现很好，再提升一点稳定性就能冲到最高等级。'
    }
  }
  if (score >= 3.0) {
    return {
      label: '信用等级 B',
      badgeClass: 'bg-amber-100 text-amber-800',
      textClass: 'text-amber-700',
      tip: '基础信用稳定，建议在完成确认和响应速度上继续加强。'
    }
  }
  return {
    label: '信用等级 C',
    badgeClass: 'bg-rose-100 text-rose-800',
    textClass: 'text-rose-700',
    tip: '当前信用分还有较大提升空间，优先保证按时沟通和交付。'
  }
})
const creditProgress = computed(() => {
  const normalized = Math.max(0, Math.min(100, (creditScore.value / 5) * 100))
  return normalized.toFixed(0)
})
const averageReviewStars = computed(() => receivedReviewAverage.value.toFixed(1))
const reviewSummaryHighlights = computed(() => ([
  { label: '平均星级', value: `${averageReviewStars.value} 星` },
  { label: '收到评价', value: `${receivedReviewCount.value} 条` },
  { label: '完成订单', value: completedTaskCount.value },
  { label: '收到点赞', value: receivedLikeCount.value }
]))

const matchesStatusFilter = (task: any) => {
  const status = String(task?.status || '')
  if (activeStatusFilter.value === 'active') return ['pending', 'accepted', 'completion_pending'].includes(status)
  if (activeStatusFilter.value === 'completed') return status === 'completed'
  if (activeStatusFilter.value === 'canceled') return status === 'canceled'
  return true
}

const matchesHistoryKeyword = (task: any) => {
  const keyword = historyKeyword.value.trim().toLowerCase()
  if (!keyword) return true
  const haystack = [task?.title, task?.locationText, task?.rewardText, task?.timeText, task?.category, task?.description]
    .filter(Boolean)
    .join(' ')
    .toLowerCase()
  return haystack.includes(keyword)
}

const filteredMyTasks = computed(() => myRequestTasks.value.filter(task => matchesStatusFilter(task) && matchesHistoryKeyword(task)))
const filteredMyTopicPosts = computed(() => myTopicPosts.value.filter(task => matchesHistoryKeyword(task)))
const filteredMyServiceTasks = computed(() => myServiceTasks.value.filter(task => matchesStatusFilter(task) && matchesHistoryKeyword(task)))
const historyEmptyText = computed(() => {
  const hasKeyword = Boolean(historyKeyword.value.trim())
  if (hasKeyword || (activeTab.value !== 'topics' && activeStatusFilter.value !== 'all')) {
    return '当前筛选条件下没有匹配的订单记录，试试切换状态或清空关键词。'
  }
  return activeTab.value === 'requests'
    ? '当前账号在数据库中还没有需求记录，去发布页面创建第一条吧。'
    : activeTab.value === 'topics'
      ? '当前账号还没有发布话题帖，可以从发布页创建一条用于交流的帖子。'
    : '接下社区里的跑腿需求后，这里会显示你当前服务中的任务和已完成记录。'
})

const goToPublish = () => {
  router.push('/publish')
}

const goToTaskDetail = (taskId: number) => {
  router.push(`/detail/${taskId}`)
}

const goToTaskMessages = (task: any) => {
  router.push({
    path: '/messages',
    query: {
      taskId: String(task.id ?? ''),
      userId: String(task.helperId ?? ''),
      taskTitle: task.title || '',
      userName: task.helperName || '接单同学'
    }
  })
}

const showRequesterAcceptanceReminder = (task: any) => (
  Boolean(task?.helperId) && ['accepted', 'completion_pending'].includes(String(task?.status || ''))
)

const reviewStatusLabel = (taskId: number) => (
  taskReviewStatusMap.value[Number(taskId)] === 'completed' ? '已完成互评' : '等待互评'
)

const reviewStatusBadgeClass = (taskId: number) => (
  taskReviewStatusMap.value[Number(taskId)] === 'completed'
    ? 'bg-emerald-100 text-emerald-800'
    : 'bg-amber-100 text-amber-800'
)

const isDeleteDisabled = (status?: string) => status === 'accepted' || status === 'completion_pending' || status === 'completed'

const formatStatus = (status?: string) => {
  const statusMap: Record<string, string> = {
    pending: '待接单',
    accepted: '进行中',
    completion_pending: '待双方确认',
    completed: '已完成',
    canceled: '已取消'
  }
  return statusMap[status || ''] || '未知状态'
}

const formatServiceStatus = (status?: string) => {
  const statusMap: Record<string, string> = {
    accepted: '服务中',
    completion_pending: '待确认完成',
    completed: '已完成',
    pending: '待确认'
  }
  return statusMap[status || ''] || formatStatus(status)
}

const formatCreatedAt = (value?: string) => {
  return formatLocaleDateTime(value, {
    month: 'numeric',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  }, '刚刚发布')
}

const formatScore = (value?: number | string) => {
  const score = Number(value ?? 0)
  return Number.isFinite(score) ? score.toFixed(2) : '0.00'
}

const normalizeResponseList = (response: any) => {
  if (Array.isArray(response)) return response
  if (Array.isArray(response?.data)) return response.data
  return []
}

const normalizeNumberResponse = (response: any) => {
  if (typeof response === 'number') return response
  if (typeof response?.data === 'number') return response.data
  return 0
}

const fetchPointRecords = async () => {
  pointRecordsLoading.value = true
  try {
      pointRecords.value = await userApi.getPointRecords() as any[]
  } catch (error) {
    console.error('加载积分明细失败:', error)
    pointRecords.value = []
  } finally {
    pointRecordsLoading.value = false
  }
}

const openPointRecordsDialog = async () => {
  showPointRecordsDialog.value = true
  await fetchPointRecords()
}

const closePointRecordsDialog = () => {
  showPointRecordsDialog.value = false
}

const fetchReviewStatuses = async (requestTasks: any[], serviceTasks: any[]) => {
  const completedTaskIds = Array.from(new Set(
    [...requestTasks, ...serviceTasks]
      .filter((task) => String(task?.taskMode || 'task') === 'task' && String(task?.status || '') === 'completed')
      .map((task) => Number(task.id))
      .filter((taskId) => Number.isFinite(taskId))
  ))

  if (completedTaskIds.length === 0) {
    taskReviewStatusMap.value = {}
    receivedReviewAverage.value = 0
    receivedReviewCount.value = 0
    return
  }

  const currentUserId = Number(currentUser.value?.id)
  try {
    const [reviewCounts, receivedReviews] = await Promise.all([
      taskApi.getTaskReviewsBatch(completedTaskIds) as Promise<Record<string, number>>,
      Promise.all(completedTaskIds.map((taskId) => taskApi.getTaskReviews(taskId)))
    ])

    taskReviewStatusMap.value = Object.fromEntries(
      completedTaskIds.map((taskId) => [taskId, Number(reviewCounts?.[taskId] ?? 0) >= 2 ? 'completed' : 'pending'] as const)
    )

    const flattenedReviews = receivedReviews.flatMap((reviews) => normalizeResponseList(reviews))
    const ownReviews = flattenedReviews.filter((review) => Number(review?.revieweeId) === currentUserId)
    receivedReviewCount.value = ownReviews.length
    receivedReviewAverage.value = ownReviews.length === 0
      ? 0
      : ownReviews.reduce((sum, review) => sum + Number(review?.rating || 0), 0) / ownReviews.length
  } catch (error) {
    console.error('批量获取互评状态失败:', error)
    taskReviewStatusMap.value = {}
    receivedReviewAverage.value = 0
    receivedReviewCount.value = 0
  }
}

const fetchProfileData = async () => {
  try {
    const [userResponse, taskResponse, serviceResponse, likeCountResponse] = await Promise.all([
      userApi.getCurrentUser(),
      taskApi.getMyTasks(),
      taskApi.getMyAcceptedTasks(),
      taskApi.getMyReceivedLikeCount()
    ])

    if (userResponse) {
      setStoredUser(userResponse)
    }

    myTasks.value = normalizeResponseList(taskResponse)
    myServiceTasks.value = normalizeResponseList(serviceResponse)
    receivedLikeCount.value = normalizeNumberResponse(likeCountResponse)
    await fetchReviewStatuses(myTasks.value, myServiceTasks.value)
  } catch (error) {
    console.error('加载个人中心数据失败:', error)
    myTasks.value = []
    myServiceTasks.value = []
    receivedLikeCount.value = 0
    taskReviewStatusMap.value = {}
  }
}

const handleDeleteTask = async (task: any) => {
  if (isDeleteDisabled(task.status)) {
    showToast('进行中或已完成的需求暂不支持删除', 'error')
    return
  }

  const confirmed = await openConfirm({
    title: '确认删除需求',
    message: `确认删除“${task.title}”吗？删除后将无法恢复。`,
    confirmText: '删除',
    tone: 'danger'
  })
  if (!confirmed) {
    return
  }

  deletingTaskId.value = task.id

  try {
    await taskApi.deleteTask(task.id)
    myTasks.value = myTasks.value.filter(item => item.id !== task.id)
  } catch (error: any) {
    console.error('删除需求失败:', error)
    const responseData = error?.response?.data
    const message =
      responseData?.message ||
      responseData?.error ||
      (typeof responseData === 'string' ? responseData : '') ||
      '删除失败，请稍后重试'
    showToast(message, 'error')
  } finally {
    deletingTaskId.value = null
  }
}

const handleCompleteTask = async (task: any) => {
  const confirmed = await openConfirm({
    title: task.status === 'completion_pending' ? '确认任务完成' : '提交完成申请',
    message: task.status === 'completion_pending'
      ? `确认“${task.title}”已经完成吗？确认后将正式结束并进入互评。`
      : `确认提交“${task.title}”的完成申请吗？提交后会等待对方确认。`,
    confirmText: '确认'
  })
  if (!confirmed) {
    return
  }

  completingTaskId.value = task.id

  try {
    await taskApi.completeTask(task.id)
    myTasks.value = myTasks.value.map((item) => (
      item.id === task.id
        ? { ...item, status: item.status === 'completion_pending' ? 'completed' : 'completion_pending' }
        : item
    ))
    myServiceTasks.value = myServiceTasks.value.map((item) => (
      item.id === task.id
        ? { ...item, status: item.status === 'completion_pending' ? 'completed' : 'completion_pending' }
        : item
    ))
    await fetchProfileData()
  } catch (error: any) {
    console.error('完成任务失败:', error)
    const responseData = error?.response?.data
    const message =
      responseData?.message ||
      responseData?.error ||
      (typeof responseData === 'string' ? responseData : '') ||
      '完成任务失败，请稍后重试'
    showToast(message, 'error')
  } finally {
    completingTaskId.value = null
  }
}

const handleUnacceptTask = async (task: any) => {
  const confirmed = await openConfirm({
    title: '确认取消接单',
    message: `确认取消接单“${task.title}”吗？取消后任务会重新回到社区首页。`,
    confirmText: '确认取消',
    tone: 'danger'
  })
  if (!confirmed) {
    return
  }

  cancelingServiceTaskId.value = task.id
  try {
    await taskApi.unacceptTask(task.id)
    myServiceTasks.value = myServiceTasks.value.filter(item => item.id !== task.id)
  } catch (error: any) {
    console.error('取消接单失败:', error)
    const responseData = error?.response?.data
    const message =
      responseData?.message ||
      responseData?.error ||
      (typeof responseData === 'string' ? responseData : '') ||
      '取消接单失败，请稍后重试'
    showToast(message, 'error')
  } finally {
    cancelingServiceTaskId.value = null
  }
}

onMounted(() => {
  fetchProfileData()
})
</script>
