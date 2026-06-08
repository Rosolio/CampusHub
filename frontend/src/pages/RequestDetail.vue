<template>
  <div class="page-shell min-h-screen bg-background text-on-background">

    <main class="page-shell-main max-w-6xl">
      <PageBackHeader :to="isTopicPost ? '/home?tab=topic' : '/home'" label="返回列表" />

      <div v-if="feedbackMessage" class="mb-6 rounded-2xl border px-4 py-3 text-sm font-medium" :class="feedbackType === 'success' ? 'border-emerald-200 bg-emerald-50 text-emerald-700' : 'border-rose-200 bg-rose-50 text-rose-700'">
        {{ feedbackMessage }}
      </div>

      <div v-if="detailError" class="mb-6 rounded-2xl border border-rose-200 bg-rose-50 p-6 text-center text-rose-700">
        <p class="mb-4 text-sm font-medium">{{ detailError }}</p>
        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-full bg-rose-100 px-4 py-2 text-sm font-bold text-rose-800 transition-colors hover:bg-rose-200"
          @click="fetchTaskDetail"
        >
          <span class="material-symbols-outlined text-base">refresh</span>
          重新加载
        </button>
      </div>

      <div v-if="detailLoading" class="rounded-[2rem] bg-surface-container-low p-10 text-center text-on-surface-variant">
        正在加载内容详情...
      </div>

      <div v-else-if="!detailError" class="grid gap-6 lg:grid-cols-[1.45fr_0.95fr]">
        <section class="min-w-0 space-y-8">
          <article class="overflow-hidden rounded-[2rem] shadow-sm" :class="isTopicPost ? topicHeroClass : 'bg-surface-container-lowest'">
            <div v-if="isTopicPost" class="p-6 md:p-8">
              <div class="mb-6 flex flex-wrap gap-3">
                <span class="rounded-full border border-white/20 bg-white/10 px-4 py-1.5 text-xs font-bold uppercase tracking-[0.24em] text-white">
                  话题帖
                </span>
                <span class="rounded-full border border-white/20 bg-white/10 px-4 py-1.5 text-xs font-bold uppercase tracking-[0.24em] text-white">
                  {{ request.category || '校园互助' }}
                </span>
                <span class="rounded-full bg-white px-4 py-1.5 text-xs font-bold uppercase tracking-[0.24em] text-teal-900">
                  评论 / 回复
                </span>
              </div>

              <h1 class="text-4xl font-extrabold leading-tight tracking-tight text-white">
                {{ request.title || '内容详情' }}
              </h1>
              <p class="mt-5 text-lg leading-8 text-white/85">
                {{ request.description || '暂无详细描述。' }}
              </p>

              <div v-if="detailImages.length > 0" class="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-3">
                <div
                  v-for="(img, idx) in detailImages"
                  :key="idx"
                  class="aspect-square cursor-pointer overflow-hidden rounded-2xl"
                  @click="showImageLightbox = true; lightboxIndex = idx"
                >
                  <img :src="img" class="h-full w-full object-cover" :alt="'图片 ' + (idx + 1)" />
                </div>
              </div>

              <div class="mt-6 flex flex-wrap items-center gap-3">
                <button
                  type="button"
                  class="inline-flex items-center gap-2 rounded-full px-4 py-2 text-sm font-bold transition-all"
                  :class="[isTopicLiked ? 'bg-white text-rose-600' : 'border border-white/20 bg-white/10 text-white hover:bg-white/20', likeAnimating ? 'like-animate' : '']"
                  :disabled="topicLikeLoading || isOwnTask || isExpiredTopic"
                  @click="toggleTopicLike"
                >
                  <span class="material-symbols-outlined text-base">{{ isTopicLiked ? 'favorite' : 'favorite_border' }}</span>
                  {{ topicLikeLoading ? '处理中...' : `${topicLikeCount} 点赞` }}
                </button>
                <button
                  type="button"
                  class="inline-flex items-center gap-2 rounded-full px-4 py-2 text-sm font-bold transition-all"
                  :class="[isFavorited ? 'bg-white text-amber-600' : 'border border-white/20 bg-white/10 text-white hover:bg-white/20']"
                  :disabled="favoriteLoading || isOwnTask"
                  @click="toggleFavorite"
                >
                  <span class="material-symbols-outlined text-base">{{ isFavorited ? 'bookmark' : 'bookmark_border' }}</span>
                  {{ favoriteLoading ? '处理中...' : (isFavorited ? '已收藏' : '收藏') }}
                </button>
                <p class="text-sm" :class="isOwnTask ? 'text-white/75' : 'text-white/65'">
                  {{ isExpiredTopic ? '帖子已截止，当前仅支持查看。' : isOwnTask ? '自己的帖子暂不支持自赞。' : '每次被点赞可为作者增加 1 积分。' }}
                </p>
              </div>

              <div class="mt-6 grid gap-4 md:grid-cols-3">
                <div class="rounded-3xl bg-white/10 px-5 py-4 text-white">
                  <p class="text-xs font-bold uppercase tracking-[0.22em] text-white/60">补充信息</p>
                  <p class="mt-3 font-semibold">{{ request.locationText || '待补充' }}</p>
                </div>
                <div class="rounded-3xl bg-white/10 px-5 py-4 text-white">
                  <p class="text-xs font-bold uppercase tracking-[0.22em] text-white/60">时间说明</p>
                  <p class="mt-3 font-semibold">{{ request.timeText || '待补充' }}</p>
                </div>
                <div class="rounded-3xl bg-white/10 px-5 py-4 text-white">
                  <p class="text-xs font-bold uppercase tracking-[0.22em] text-white/60">{{ request.rewardTitle || '说明' }}</p>
                  <p class="mt-3 font-semibold">{{ request.rewardText || '待补充' }}</p>
                </div>
              </div>

              <div v-if="request.contactInfo" class="mt-4 rounded-3xl bg-white/10 px-5 py-4 text-white">
                <p class="text-xs font-bold uppercase tracking-[0.22em] text-white/60">联系方式</p>
                <p class="mt-3 whitespace-pre-wrap break-words font-semibold">{{ request.contactInfo }}</p>
              </div>

              <div class="mt-6 rounded-[1.75rem] bg-white/10 p-5 text-white">
                <div class="flex items-start gap-3">
                  <div class="rounded-2xl bg-white p-3 text-teal-900">
                    <span class="material-symbols-outlined">workspace_premium</span>
                  </div>
                  <div>
                    <h2 class="text-lg font-bold">互动规则</h2>
                    <p class="mt-2 text-sm leading-7 text-white/85">
                      这是话题帖模式，不支持接单。任何用户都可以在下方评论或回复别人的评论，成功发布一条评论即可获得 5 积分；帖子截止后将进入只读状态。
                    </p>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="p-6 md:p-8">
              <div class="grid gap-6 lg:grid-cols-[1.2fr_0.8fr]">
                <div>
                  <div class="mb-6 flex flex-wrap gap-3">
                    <span class="rounded-full px-4 py-1.5 text-xs font-bold uppercase tracking-[0.24em]" :class="request.badgePrimary === '紧急' ? 'bg-rose-100 text-rose-700' : 'bg-sky-100 text-sky-700'">
                      {{ request.badgePrimary || '普通需求' }}
                    </span>
                    <span class="rounded-full bg-surface-container-high px-4 py-1.5 text-xs font-bold uppercase tracking-[0.24em] text-on-surface-variant">
                      {{ request.category || '跑腿代办' }}
                    </span>
                    <span class="rounded-full bg-amber-50 px-4 py-1.5 text-xs font-bold uppercase tracking-[0.24em] text-amber-700">
                      {{ statusLabelForHero }}
                    </span>
                  </div>

                  <h1 class="text-4xl font-extrabold leading-tight tracking-tight text-on-surface">
                    {{ request.title || '内容详情' }}
                  </h1>
                  <p class="mt-5 text-lg leading-8 text-on-surface-variant">
                    {{ request.description || '暂无详细描述。' }}
                  </p>

                  <div v-if="detailImages.length > 0" class="mt-5 grid grid-cols-2 gap-3 sm:grid-cols-3">
                    <div
                      v-for="(img, idx) in detailImages"
                      :key="idx"
                      class="aspect-square cursor-pointer overflow-hidden rounded-2xl"
                      @click="showImageLightbox = true; lightboxIndex = idx"
                    >
                      <img :src="img" class="h-full w-full object-cover" :alt="'图片 ' + (idx + 1)" />
                    </div>
                  </div>

                  <div class="mt-6 grid gap-4 md:grid-cols-3">
                    <div class="rounded-[1.75rem] bg-teal-900 px-5 py-5 text-white shadow-sm">
                      <p class="text-xs font-bold uppercase tracking-[0.22em] text-white/65">{{ request.rewardTitle || '任务奖励' }}</p>
                      <p class="mt-3 text-2xl font-extrabold leading-tight">{{ request.rewardText || '待补充' }}</p>
                    </div>
                    <div class="rounded-[1.75rem] bg-surface-container-low px-5 py-5 text-on-surface">
                      <p class="text-xs font-bold uppercase tracking-[0.22em] text-on-surface-variant">地点</p>
                      <p class="mt-3 font-semibold leading-7">{{ request.locationText || '待补充' }}</p>
                    </div>
                    <div class="rounded-[1.75rem] bg-surface-container-low px-5 py-5 text-on-surface">
                      <p class="text-xs font-bold uppercase tracking-[0.22em] text-on-surface-variant">时间</p>
                      <p class="mt-3 font-semibold leading-7">{{ request.timeText || '待补充' }}</p>
                    </div>
                  </div>

                  <div class="mt-6 grid gap-4 md:grid-cols-2">
                    <div class="rounded-[1.75rem] border border-outline-variant/15 bg-white px-5 py-5">
                      <p class="text-xs font-bold uppercase tracking-[0.22em] text-on-surface-variant">协作说明</p>
                      <p class="mt-3 text-sm leading-7 text-on-surface-variant">
                        接单后会进入一对一沟通，双方确认完成后再进入互评。系统提醒会同步推送到消息页。
                      </p>
                    </div>
                    <div class="rounded-[1.75rem] border border-outline-variant/15 bg-white px-5 py-5">
                      <p class="text-xs font-bold uppercase tracking-[0.22em] text-on-surface-variant">当前进度</p>
                      <div class="mt-4 flex items-center gap-3">
                        <div class="h-3 flex-1 overflow-hidden rounded-full bg-surface-container-low">
                          <div class="h-full rounded-full bg-gradient-to-r from-teal-700 to-emerald-500" :style="{ width: `${taskProgressPercent}%` }"></div>
                        </div>
                        <span class="text-sm font-bold text-teal-900">{{ taskProgressPercent }}%</span>
                      </div>
                      <p class="mt-3 text-sm leading-7 text-on-surface-variant">{{ progressSummaryText }}</p>
                    </div>
                  </div>
                </div>

                <div class="space-y-4">
                  <div class="rounded-[1.9rem] border border-outline-variant/15 bg-gradient-to-br from-slate-50 to-teal-50 p-6">
                    <p class="text-xs font-bold uppercase tracking-[0.22em] text-on-surface-variant">履约步骤</p>
                    <div class="mt-5 space-y-4">
                      <div
                        v-for="step in taskTimelineSteps"
                        :key="step.title"
                        class="flex items-start gap-3"
                      >
                        <div class="mt-1 flex h-8 w-8 items-center justify-center rounded-full text-sm font-bold" :class="step.done ? 'bg-emerald-500 text-white' : 'bg-white text-on-surface-variant border border-outline-variant/20'">
                          <span v-if="step.done" class="material-symbols-outlined text-base">check</span>
                          <span v-else>{{ step.order }}</span>
                        </div>
                        <div>
                          <p class="font-bold text-on-surface">{{ step.title }}</p>
                          <p class="mt-1 text-sm leading-6 text-on-surface-variant">{{ step.description }}</p>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div v-if="request.contactInfo" class="rounded-[1.75rem] border border-outline-variant/15 bg-white px-5 py-5">
                    <p class="text-xs font-bold uppercase tracking-[0.22em] text-on-surface-variant">补充联系方式</p>
                    <p class="mt-3 whitespace-pre-wrap break-words text-sm font-semibold leading-7 text-on-surface">{{ request.contactInfo }}</p>
                  </div>
                </div>
              </div>
            </div>
          </article>

          <section v-if="isTopicPost" class="rounded-[2rem] bg-surface-container-lowest p-6 shadow-sm md:p-8">
            <div class="mb-6 flex flex-wrap items-center justify-between gap-3">
              <div>
                <h2 class="text-2xl font-extrabold text-on-surface">评论区</h2>
                <p class="mt-2 text-sm text-on-surface-variant">支持直接评论，也支持回复他人的评论。</p>
              </div>
              <div class="flex flex-wrap items-center gap-3">
                <label class="flex items-center gap-2 rounded-full bg-surface-container-low px-4 py-2 text-sm font-semibold text-on-surface">
                  <span>排序</span>
                  <select v-model="commentSortMode" class="bg-transparent text-sm font-semibold text-on-surface outline-none">
                    <option value="likes">点赞数高</option>
                    <option value="latest">最新发布</option>
                  </select>
                </label>
                <div class="rounded-full bg-amber-50 px-4 py-2 text-sm font-bold text-amber-700">
                  评论一次 +5 积分
                </div>
              </div>
            </div>

            <div class="rounded-[1.75rem] border border-outline-variant/15 bg-surface-container-low p-5">
              <div v-if="replyTarget" class="mb-4 flex items-center justify-between gap-3 rounded-2xl bg-cyan-50 px-4 py-3 text-sm text-teal-900">
                <span>正在回复 {{ replyTarget.authorName || `用户 #${replyTarget.authorId}` }}</span>
                <button type="button" class="font-bold text-primary" @click="cancelReply">取消回复</button>
              </div>
              <div v-if="isExpiredTopic" class="mb-4 rounded-2xl border border-amber-200 bg-amber-50 px-4 py-3 text-sm font-medium text-amber-800">
                这条话题帖已截止，当前仅支持查看，不能再点赞、评论或回复。
              </div>
              <div class="relative">
                <textarea
                  ref="commentTextarea"
                  v-model="commentForm.content"
                  class="min-h-36 w-[96%] mx-auto block rounded-3xl border border-outline-variant/20 bg-surface px-3 py-4 text-on-surface outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
                  :placeholder="replyTarget ? '写下你的回复...' : '写下你的评论，发布后可获得 5 积分...'"
                  :disabled="isExpiredTopic"
                  @keydown.enter.exact.prevent="submitComment"
                ></textarea>
                <div class="mt-3 flex justify-end">
                  <EmojiPicker :disabled="isExpiredTopic" align="right" @select="insertCommentEmoji" />
                </div>
              </div>
              <div class="mt-4 flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                <p class="text-sm text-on-surface-variant">公开回复会展示在帖子下方，方便其他同学继续参与讨论。</p>
                <button
                  type="button"
                  class="rounded-2xl bg-gradient-to-br from-primary to-primary-dim px-6 py-3 font-bold text-on-primary shadow-lg shadow-primary/20 transition-all hover:scale-[1.01] active:scale-95 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:scale-100"
                  :disabled="commentLoading || isExpiredTopic"
                  @click="submitComment"
                >
                  {{ commentLoading ? '发布中...' : (replyTarget ? '发布回复' : '发布评论') }}
                </button>
              </div>
            </div>

            <div class="mt-8 space-y-4">
              <div v-if="commentsLoading" class="rounded-3xl bg-surface-container-low px-5 py-8 text-center text-on-surface-variant">
                正在加载评论...
              </div>
              <div v-else-if="commentTree.length === 0" class="rounded-3xl bg-surface-container-low px-5 py-8 text-center text-on-surface-variant">
                还没有评论，来做第一个互动的人吧。
              </div>
              <div v-for="(comment, index) in commentTree" :key="comment.id" class="rounded-[1.75rem] border border-outline-variant/12 bg-white p-5 shadow-sm">
                <div class="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                  <div class="min-w-0">
                    <div class="flex flex-wrap items-center gap-2">
                      <p class="font-bold text-on-surface">{{ comment.authorName || `用户 #${comment.authorId}` }}</p>
                      <span
                        v-if="isRequesterComment(comment)"
                        class="rounded-full bg-amber-100 px-2.5 py-1 text-[11px] font-bold uppercase tracking-[0.16em] text-amber-800"
                      >
                        帖主
                      </span>
                      <span class="rounded-full bg-sky-50 px-2.5 py-1 text-[11px] font-bold uppercase tracking-[0.16em] text-sky-700">
                        {{ formatCommentFloor(index) }}
                      </span>
                    </div>
                    <p class="mt-1 text-xs text-on-surface-variant">{{ comment.authorMajor || '校园用户' }} · {{ formatDateTime(comment.createdAt) }}</p>
                  </div>
                  <div class="flex flex-wrap items-center gap-2 md:justify-end">
                    <button
                      type="button"
                      class="inline-flex items-center gap-1 rounded-full px-3 py-2 text-sm font-semibold transition-colors"
                      :class="comment.likedByCurrentUser ? 'bg-rose-50 text-rose-600' : 'bg-surface-container-low text-on-surface-variant hover:text-rose-600'"
                      :disabled="isExpiredTopic || isCommentLikeLoading(comment.id) || Number(comment.authorId) === Number(currentUser.id)"
                      @click="toggleCommentLike(comment)"
                    >
                      <span class="material-symbols-outlined text-base">{{ comment.likedByCurrentUser ? 'favorite' : 'favorite_border' }}</span>
                      {{ isCommentLikeLoading(comment.id) ? '处理中...' : (comment.likeCount || 0) }}
                    </button>
                    <button type="button" class="rounded-full bg-surface-container-low px-4 py-2 text-sm font-semibold text-primary disabled:cursor-not-allowed disabled:opacity-50" :disabled="isExpiredTopic" @click="startReply(comment)">
                      回复
                    </button>
                    <button
                      v-if="canDeleteComment(comment)"
                      type="button"
                      class="rounded-full bg-rose-50 px-4 py-2 text-sm font-semibold text-rose-600 transition-colors hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-50"
                      :disabled="isCommentDeleteLoading(comment.id)"
                      @click="deleteComment(comment)"
                    >
                      {{ isCommentDeleteLoading(comment.id) ? '删除中...' : '删除' }}
                    </button>
                  </div>
                </div>
                <p class="mt-4 whitespace-pre-wrap text-sm leading-7 text-on-surface-variant">
                  {{ comment.content }}
                </p>

                <div v-if="comment.children.length" class="mt-5 space-y-3 border-l border-surface-container-high pl-4">
                  <div v-for="reply in comment.children" :key="reply.id" class="rounded-2xl bg-surface-container-low px-5 py-4">
                    <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                      <div class="min-w-0">
                        <div class="flex flex-wrap items-center gap-2">
                          <p class="font-bold text-on-surface">
                          {{ reply.authorName || `用户 #${reply.authorId}` }}
                          <span v-if="reply.replyToAuthorName" class="ml-2 font-medium text-on-surface-variant">回复 {{ reply.replyToAuthorName }}</span>
                          </p>
                          <span
                            v-if="isRequesterComment(reply)"
                            class="rounded-full bg-amber-100 px-2.5 py-1 text-[11px] font-bold uppercase tracking-[0.16em] text-amber-800"
                          >
                            帖主
                          </span>
                        </div>
                        <p class="mt-1 text-xs text-on-surface-variant">{{ formatDateTime(reply.createdAt) }}</p>
                      </div>
                      <div class="flex flex-wrap items-center gap-3 md:justify-end">
                        <button
                          type="button"
                          class="inline-flex items-center gap-1 rounded-full bg-white px-3 py-2 text-sm font-semibold transition-colors"
                          :class="reply.likedByCurrentUser ? 'text-rose-600' : 'text-on-surface-variant hover:text-rose-600'"
                          :disabled="isExpiredTopic || isCommentLikeLoading(reply.id) || Number(reply.authorId) === Number(currentUser.id)"
                          @click="toggleCommentLike(reply)"
                        >
                          <span class="material-symbols-outlined text-base">{{ reply.likedByCurrentUser ? 'favorite' : 'favorite_border' }}</span>
                          {{ isCommentLikeLoading(reply.id) ? '处理中...' : (reply.likeCount || 0) }}
                        </button>
                        <button type="button" class="rounded-full bg-white px-4 py-2 text-sm font-semibold text-primary disabled:cursor-not-allowed disabled:opacity-50" :disabled="isExpiredTopic" @click="startReply(reply)">
                          回复
                        </button>
                        <button
                          v-if="canDeleteComment(reply)"
                          type="button"
                          class="rounded-full bg-white px-4 py-2 text-sm font-semibold text-rose-600 transition-colors hover:text-rose-700 disabled:cursor-not-allowed disabled:opacity-50"
                          :disabled="isCommentDeleteLoading(reply.id)"
                          @click="deleteComment(reply)"
                        >
                          {{ isCommentDeleteLoading(reply.id) ? '删除中...' : '删除' }}
                        </button>
                      </div>
                    </div>
                    <p class="mt-3 whitespace-pre-wrap text-sm leading-7 text-on-surface-variant">{{ reply.content }}</p>
                  </div>
                </div>
              </div>
            </div>
          </section>
        </section>

        <aside class="min-w-0 space-y-6">
          <section class="rounded-[2rem] bg-surface-container-lowest p-6 shadow-sm md:p-8">
            <div class="mb-6 text-center">
              <div class="relative mb-4 inline-block">
                <img
                  :alt="profilePanelTitle"
                  class="h-24 w-24 rounded-full border-4 border-surface-container-low object-cover shadow-md"
                  :src="profilePanelAvatarUrl"
                />
                <div
                  class="absolute bottom-1 right-1 h-6 w-6 rounded-full border-4 border-surface-container-lowest"
                  :class="isRequesterOnline ? 'bg-emerald-400' : 'bg-gray-300'"
                  :title="isRequesterOnline ? '在线' : '离线'"
                ></div>
              </div>
              <p class="text-xs font-bold uppercase tracking-[0.2em] text-on-surface-variant">{{ profilePanelTitle }}</p>
              <h2 class="mt-2 text-xl font-extrabold text-on-surface">{{ profilePanelName }}</h2>
              <p class="text-sm font-medium text-on-surface-variant">{{ profilePanelMajor }}</p>
            </div>

            <div class="mb-8 grid grid-cols-1 gap-3 sm:grid-cols-2">
              <div class="rounded-2xl bg-surface-container-low p-4 text-center">
                <p class="mb-1 text-xs font-bold uppercase tracking-tight text-on-surface-variant">评分</p>
                <div class="flex items-center justify-center gap-1 text-primary">
                  <span class="font-bold">{{ profilePanelScore }}</span>
                  <span class="material-symbols-outlined text-sm" style="font-variation-settings:'FILL' 1;">star</span>
                </div>
              </div>
              <div class="rounded-2xl bg-surface-container-low p-4 text-center">
                <p class="mb-1 text-xs font-bold uppercase tracking-tight text-on-surface-variant">积分</p>
                <p class="font-bold text-on-surface">{{ profilePanelPoints }}</p>
              </div>
            </div>

            <button
              type="button"
              class="flex w-full items-center justify-center gap-3 rounded-2xl bg-surface-container-high py-4 font-bold text-on-primary-container transition-all duration-200 active:scale-95 group hover:bg-surface-container-highest"
              :disabled="contactLoading || !canContactRequester"
              :class="{ 'cursor-not-allowed opacity-60 hover:bg-surface-container-high active:scale-100': contactLoading || !canContactRequester }"
              @click="openContactDialog"
            >
              <span class="material-symbols-outlined text-xl">chat_bubble</span>
              {{ contactLoading ? '发送中...' : contactButtonLabel }}
            </button>
          </section>

          <section class="rounded-[2rem] border border-outline-variant/15 bg-surface-container-high/40 p-6 md:p-8">
            <p class="mb-5 text-sm font-bold uppercase tracking-[0.18em] text-on-surface-variant">当前模式</p>
            <div v-if="isTopicPost" class="space-y-4">
              <div class="rounded-3xl bg-white px-5 py-4">
                <p class="text-xs font-bold uppercase tracking-[0.2em] text-on-surface-variant">互动统计</p>
                <div class="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div class="rounded-2xl bg-surface-container-low px-5 py-4 text-center">
                    <p class="text-xs font-bold uppercase tracking-[0.18em] text-on-surface-variant">点赞数</p>
                    <p class="mt-2 text-2xl font-extrabold text-teal-900">{{ topicLikeCount }}</p>
                  </div>
                  <div class="rounded-2xl bg-surface-container-low px-5 py-4 text-center">
                    <p class="text-xs font-bold uppercase tracking-[0.18em] text-on-surface-variant">评论数</p>
                    <p class="mt-2 text-2xl font-extrabold text-teal-900">{{ topicCommentCount }}</p>
                  </div>
                </div>
                <p class="mt-3 text-sm text-on-surface-variant">当前帖子累计点赞与评论互动数据。</p>
              </div>
              <div class="rounded-3xl bg-amber-50 px-5 py-4 text-amber-800">
                <p class="text-sm font-bold">帖子说明</p>
                <p class="mt-2 text-sm leading-7">
                  这是公开话题帖，不支持接单。更适合围绕内容评论、回复和点赞互动。
                </p>
              </div>
            </div>
            <div v-else class="space-y-4">
              <div
                v-if="showRequesterAcceptanceAlert"
                class="rounded-3xl border border-amber-200 bg-gradient-to-br from-amber-50 to-orange-50 px-5 py-5 text-amber-900"
              >
                <div class="flex items-start justify-between gap-4">
                  <div>
                    <p class="text-sm font-bold">已有同学接单</p>
                    <p class="mt-2 text-sm leading-7">
                      {{ helperDisplayName }} 已接下这项需求。建议尽快进入消息页确认时间、地点和交付方式。
                    </p>
                  </div>
                  <span class="material-symbols-outlined text-2xl">notifications_active</span>
                </div>
                <button
                  type="button"
                  class="mt-4 inline-flex items-center gap-2 rounded-full bg-amber-900 px-4 py-2 text-sm font-bold text-white transition-colors hover:bg-amber-950"
                  @click="goToHelperConversation"
                >
                  <span class="material-symbols-outlined text-base">chat</span>
                  去消息页沟通
                </button>
              </div>

              <div class="rounded-3xl bg-white px-5 py-4">
                <p class="text-xs font-bold uppercase tracking-[0.2em] text-on-surface-variant">订单进度</p>
                <div class="mt-4 grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div class="rounded-2xl px-5 py-4" :class="isCompletionConfirmedByRequester ? 'bg-emerald-50 text-emerald-800' : 'bg-surface-container-low text-on-surface'">
                    <p class="text-xs font-bold uppercase tracking-[0.18em] text-on-surface-variant">需求方确认</p>
                    <p class="mt-2 font-bold">{{ isCompletionConfirmedByRequester ? '已确认' : '待确认' }}</p>
                  </div>
                  <div class="rounded-2xl px-5 py-4" :class="isCompletionConfirmedByHelper ? 'bg-emerald-50 text-emerald-800' : 'bg-surface-container-low text-on-surface'">
                    <p class="text-xs font-bold uppercase tracking-[0.18em] text-on-surface-variant">服务方确认</p>
                    <p class="mt-2 font-bold">{{ isCompletionConfirmedByHelper ? '已确认' : '待确认' }}</p>
                  </div>
                </div>
                <p class="mt-3 text-sm text-on-surface-variant">
                  {{ request.status === 'completed' ? '双方都已确认完成，当前可进入互评。' : '任务完成需要双方各确认一次。' }}
                </p>
              </div>
              <div
                v-if="showReviewEntry"
                class="rounded-3xl border border-sky-200 bg-sky-50 px-5 py-5 text-sky-900"
              >
                <p class="text-sm font-bold">互评入口已独立</p>
                <p class="mt-2 text-sm leading-7">
                  任务完成后的评价现在在单独页面处理，避免和详情页流程混在一起。你可以在那里提交评价并查看双方互评结果。
                </p>
                <button
                  type="button"
                  class="mt-4 inline-flex items-center gap-2 rounded-full bg-sky-900 px-4 py-2 text-sm font-bold text-white transition-colors hover:bg-sky-950"
                  @click="goToReviewPage"
                >
                  <span class="material-symbols-outlined text-base">rate_review</span>
                  {{ hasCurrentUserReview ? '查看互评结果' : '进入互评页面' }}
                </button>
              </div>
              <button
                v-if="showTaskActionButton"
                type="button"
                class="w-full rounded-2xl bg-gradient-to-br from-primary to-primary-dim py-5 text-lg font-extrabold text-on-primary shadow-lg shadow-primary/20 transition-all duration-300 hover:scale-[1.02] active:scale-95"
                :disabled="acceptLoading || !canMutateTask"
                :class="{ 'cursor-not-allowed opacity-60 shadow-none hover:scale-100 active:scale-100': acceptLoading || !canMutateTask }"
                @click="handleTaskAction"
              >
                {{ acceptLoading ? '处理中...' : actionButtonText }}
              </button>
              <p class="text-center text-[11px] italic text-on-surface-variant/70">
                {{ actionNote }}
              </p>
            </div>
            <button
              v-if="!isOwnTask"
              type="button"
              class="mt-4 w-full rounded-2xl border border-amber-200 bg-amber-50 py-4 text-sm font-bold transition-colors"
              :class="isFavorited ? 'text-amber-700 hover:bg-amber-100' : 'text-amber-600 hover:bg-amber-100'"
              :disabled="favoriteLoading"
              @click="toggleFavorite"
            >
              <span class="material-symbols-outlined text-base align-middle mr-2">{{ isFavorited ? 'bookmark' : 'bookmark_border' }}</span>
              {{ favoriteLoading ? '处理中...' : (isFavorited ? '已收藏' : '收藏任务') }}
            </button>
            <button
              v-if="canDeleteTask"
              type="button"
              class="mt-4 w-full rounded-2xl border border-rose-200 bg-rose-50 py-4 text-sm font-bold text-rose-600 transition-colors hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="deleteLoading"
              @click="handleDeleteTask"
            >
              {{ deleteLoading ? '删除中...' : '删除帖子' }}
            </button>
          </section>

        </aside>
      </div>
    </main>

    <div v-if="showContactDialog" class="fixed inset-0 z-[60] flex items-center justify-center bg-slate-950/45 px-6" @click.self="closeContactDialog">
      <div class="w-full max-w-xl rounded-[2rem] bg-surface-container-lowest p-8 shadow-2xl">
        <div class="mb-6 flex items-start justify-between gap-4">
          <div>
            <h3 class="text-2xl font-extrabold text-on-surface">{{ contactButtonLabel }}</h3>
            <p class="mt-2 text-sm text-on-surface-variant">
              发送后会进入消息列表，你可以继续围绕“{{ request.title || '这条内容' }}”沟通。
            </p>
          </div>
          <button type="button" class="rounded-full p-2 text-on-surface-variant transition-colors hover:bg-surface-container-low hover:text-on-surface" @click="closeContactDialog">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>

        <div class="mb-4 rounded-2xl bg-surface-container-low px-4 py-3 text-sm text-on-surface-variant">
          收件人：{{ contactTargetName || '对方' }}
        </div>

        <label class="mb-2 block text-sm font-bold text-on-surface" for="contact-message">发送内容</label>
        <div class="relative">
          <textarea
            id="contact-message"
            ref="contactTextarea"
            v-model="contactMessage"
            class="min-h-40 w-[96%] mx-auto block rounded-3xl border border-outline-variant/20 bg-surface px-3 py-4 text-on-surface outline-none transition focus:border-primary focus:ring-2 focus:ring-primary/15"
            :placeholder="isTopicPost ? '例如：你好，我想进一步了解这条帖子。' : '例如：你好，我对这个需求感兴趣，想确认一下时间和具体地点。'"
          ></textarea>
          <div class="mt-3 flex justify-end">
            <EmojiPicker :disabled="contactLoading" align="right" @select="insertContactEmoji" />
          </div>
        </div>
        <p v-if="contactError" class="mt-3 text-sm font-medium text-rose-600">{{ contactError }}</p>

        <div class="mt-6 flex items-center justify-end gap-3">
          <button type="button" class="rounded-2xl px-5 py-3 font-bold text-on-surface-variant transition-colors hover:bg-surface-container-low" :disabled="contactLoading" @click="closeContactDialog">
            取消
          </button>
          <button type="button" class="rounded-2xl bg-gradient-to-br from-primary to-primary-dim px-6 py-3 font-bold text-on-primary shadow-lg shadow-primary/20 transition-all hover:scale-[1.01] active:scale-95 disabled:cursor-not-allowed disabled:opacity-60 disabled:hover:scale-100" :disabled="contactLoading" @click="handleContactRequester">
            {{ contactLoading ? '发送中...' : '发送消息' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="showImageLightbox" class="fixed inset-0 z-[70] flex items-center justify-center bg-black/90" @click.self="showImageLightbox = false">
      <button type="button" class="absolute right-6 top-6 rounded-full bg-white/20 p-3 text-white hover:bg-white/30" @click="showImageLightbox = false">
        <span class="material-symbols-outlined text-2xl">close</span>
      </button>
      <button
        v-if="detailImages.length > 1"
        type="button"
        class="absolute left-6 top-1/2 -translate-y-1/2 rounded-full bg-white/20 p-3 text-white hover:bg-white/30"
        @click.stop="lightboxIndex = (lightboxIndex - 1 + detailImages.length) % detailImages.length"
      >
        <span class="material-symbols-outlined text-2xl">chevron_left</span>
      </button>
      <img
        :src="detailImages[lightboxIndex]"
        class="max-h-[85vh] max-w-[90vw] rounded-2xl object-contain"
        :alt="'图片 ' + (lightboxIndex + 1)"
      />
      <button
        v-if="detailImages.length > 1"
        type="button"
        class="absolute right-6 top-1/2 -translate-y-1/2 rounded-full bg-white/20 p-3 text-white hover:bg-white/30"
        @click.stop="lightboxIndex = (lightboxIndex + 1) % detailImages.length"
      >
        <span class="material-symbols-outlined text-2xl">chevron_right</span>
      </button>
      <p class="absolute bottom-8 text-sm text-white/70">{{ lightboxIndex + 1 }} / {{ detailImages.length }}</p>
    </div>

  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import EmojiPicker from '../components/EmojiPicker.vue'
import PageBackHeader from '../components/PageBackHeader.vue'
import { useConfirm } from '../composables/useConfirm'
import { usePreferences } from '../composables/usePreferences'
import { DEFAULT_AVATAR_URL } from '../constants/assets'
import { messageApi, taskApi, userApi } from '../services/api'
import { setStoredUser, storedUser } from '../utils/auth'
import { inferTaskMode, normalizeTask as normalizeTaskRecord } from '../utils/tasks'

type FeedbackType = 'success' | 'error'
type CommentNode = any & { children: any[] }

const props = defineProps<{ id: string }>()

const router = useRouter()
const { formatLocaleDateTime } = usePreferences()
const { openConfirm } = useConfirm()

const defaultAvatarUrl = DEFAULT_AVATAR_URL
const request = ref<any>({})
const currentUser = computed(() => storedUser.value || {})
const acceptedTaskIds = ref<number[]>([])
const comments = ref<any[]>([])
const commentSortMode = ref<'likes' | 'latest'>('likes')
const commentLikeLoadingIds = ref<number[]>([])
const commentDeleteLoadingIds = ref<number[]>([])
const commentsLoading = ref(false)
const commentLoading = ref(false)
const reviews = ref<any[]>([])
const reviewsLoading = ref(false)
const replyTarget = ref<any | null>(null)
const commentForm = ref({ content: '' })
const commentTextarea = ref<HTMLTextAreaElement | null>(null)
const contactLoading = ref(false)
const acceptLoading = ref(false)
const deleteLoading = ref(false)
const topicLikeLoading = ref(false)
const favoriteLoading = ref(false)
const showContactDialog = ref(false)
const contactMessage = ref('')
const contactTextarea = ref<HTMLTextAreaElement | null>(null)
const contactError = ref('')
const feedbackMessage = ref('')
const feedbackType = ref<FeedbackType>('success')
const detailError = ref('')
const detailLoading = ref(true)

const isTopicPost = computed(() => inferTaskMode(request.value) === 'topic')

const isOwnTask = computed(() => {
  const currentUserId = Number(currentUser.value?.id)
  const requesterId = Number(request.value?.requesterId)
  return Boolean(currentUserId && requesterId && currentUserId === requesterId)
})

const isCurrentUserHelper = computed(() => acceptedTaskIds.value.includes(Number(request.value?.id)))
const isCompletionConfirmedByRequester = computed(() => Boolean(request.value?.requesterCompletedAt))
const isCompletionConfirmedByHelper = computed(() => Boolean(request.value?.helperCompletedAt))
const hasCurrentUserConfirmedCompletion = computed(() => (
  isOwnTask.value ? isCompletionConfirmedByRequester.value : isCompletionConfirmedByHelper.value
))
const isExpiredTopic = computed(() => {
  if (!isTopicPost.value || !request.value?.expiresAt) return false
  const expiresAt = new Date(request.value.expiresAt)
  return !Number.isNaN(expiresAt.getTime()) && expiresAt.getTime() < Date.now()
})

const profilePanelName = computed(() => {
  if (!isTopicPost.value && isOwnTask.value && request.value?.helperId) {
    return request.value?.helperName || `用户 #${request.value?.helperId || ''}`
  }
  return request.value?.requesterName || `用户 #${request.value?.requesterId || ''}`
})
const profilePanelMajor = computed(() => {
  if (!isTopicPost.value && isOwnTask.value && request.value?.helperId) {
    return request.value?.helperMajor || '未填写专业信息'
  }
  return request.value?.requesterMajor || '未填写专业信息'
})
const profilePanelAvatarUrl = computed(() => {
  if (!isTopicPost.value && isOwnTask.value && request.value?.helperId) {
    return request.value?.helperAvatarUrl || defaultAvatarUrl
  }
  return request.value?.requesterAvatarUrl || defaultAvatarUrl
})
const profilePanelScore = computed(() => {
  if (!isTopicPost.value && isOwnTask.value && request.value?.helperId) {
    return request.value?.helperScore || '0.00'
  }
  return request.value?.requesterScore || '0.00'
})
const profilePanelPoints = computed(() => {
  if (!isTopicPost.value && isOwnTask.value && request.value?.helperId) {
    return request.value?.helperPoints || 0
  }
  return request.value?.requesterPoints || 0
})
const isRequesterOnline = computed(() => Boolean(request.value?.requesterOnline))

const profilePanelTitle = computed(() => {
  if (!isTopicPost.value && isOwnTask.value && request.value?.helperId) {
    return '接单同学'
  }
  return isTopicPost.value ? '发帖人' : '需求方'
})
const contactTargetId = computed(() => {
  if (!isTopicPost.value && isOwnTask.value && request.value?.helperId) {
    return Number(request.value?.helperId || 0)
  }
  return Number(request.value?.requesterId || 0)
})
const contactTargetName = computed(() => {
  if (!isTopicPost.value && isOwnTask.value && request.value?.helperId) {
    return request.value?.helperName || `用户 #${request.value?.helperId || ''}`
  }
  return request.value?.requesterName || `用户 #${request.value?.requesterId || ''}`
})
const canContactRequester = computed(() => {
  if (isTopicPost.value) {
    return Boolean(contactTargetId.value) && !isOwnTask.value
  }
  if (isOwnTask.value) {
    return Boolean(contactTargetId.value)
  }
  return Boolean(contactTargetId.value) && !isOwnTask.value
})
const canAcceptTask = computed(() => !isTopicPost.value && !isOwnTask.value && request.value.status === 'pending')
const canUnacceptTask = computed(() => !isTopicPost.value && isCurrentUserHelper.value && request.value.status === 'accepted')
const canConfirmTaskCompletion = computed(() => (
  !isTopicPost.value &&
  (isOwnTask.value || isCurrentUserHelper.value) &&
  ['accepted', 'completion_pending'].includes(String(request.value.status || '')) &&
  !hasCurrentUserConfirmedCompletion.value
))
const canMutateTask = computed(() => canAcceptTask.value || canUnacceptTask.value || canConfirmTaskCompletion.value)
const showTaskActionButton = computed(() => canAcceptTask.value || canUnacceptTask.value || canConfirmTaskCompletion.value)
const canDeleteTask = computed(() => {
  if (!isOwnTask.value || !request.value?.id) return false
  return !['accepted', 'completion_pending', 'completed'].includes(String(request.value.status || ''))
})
const isTopicLiked = computed(() => Boolean(request.value?.likedByCurrentUser))
const isFavorited = computed(() => Boolean(request.value?.isFavorited))
const topicLikeCount = computed(() => Number(request.value?.likeCount || 0))
const topicCommentCount = computed(() => Number(request.value?.commentCount || comments.value.length || 0))
const helperDisplayName = computed(() => request.value?.helperName || '接单同学')
const contactButtonLabel = computed(() => {
  if (isTopicPost.value) return '私信发帖人'
  return isOwnTask.value && request.value?.helperId ? '联系接单人' : '联系需求方'
})
const currentUserReview = computed(() => reviews.value.find((review) => Number(review.reviewerId) === Number(currentUser.value?.id)) || null)
const hasCurrentUserReview = computed(() => Boolean(currentUserReview.value))
const showRequesterAcceptanceAlert = computed(() => (
  !isTopicPost.value &&
  isOwnTask.value &&
  Boolean(request.value?.helperId) &&
  ['accepted', 'completion_pending'].includes(String(request.value?.status || ''))
))
const showReviewEntry = computed(() => (
  !isTopicPost.value &&
  (String(request.value?.status || '') === 'completed' || reviews.value.length > 0)
))
const statusLabelForHero = computed(() => {
  const statusMap: Record<string, string> = {
    pending: '待接单',
    accepted: '已接单',
    completion_pending: '待双方确认',
    completed: '已完成',
    canceled: '已取消'
  }
  return statusMap[String(request.value?.status || '')] || '进行中'
})
const taskProgressPercent = computed(() => {
  const status = String(request.value?.status || '')
  if (status === 'pending') return 20
  if (status === 'accepted') return 55
  if (status === 'completion_pending') return 82
  if (status === 'completed') return 100
  if (status === 'canceled') return 0
  return 20
})
const progressSummaryText = computed(() => {
  if (request.value?.status === 'pending') return '当前还在等待接单，接单后会自动建立沟通会话。'
  if (request.value?.status === 'accepted') return '已经进入履约阶段，建议双方尽快在消息页确认细节。'
  if (request.value?.status === 'completion_pending') return '其中一方已提交完成确认，等待另一方确认后进入互评。'
  if (request.value?.status === 'completed') return '任务已完成，后续只需查看互评结果或补充评价。'
  if (request.value?.status === 'canceled') return '这项需求已经取消，当前仅保留历史记录。'
  return '当前状态已更新。'
})
const taskTimelineSteps = computed(() => {
  const status = String(request.value?.status || '')
  const accepted = ['accepted', 'completion_pending', 'completed'].includes(status)
  const completionPending = ['completion_pending', 'completed'].includes(status)
  const completed = status === 'completed'
  return [
    {
      order: 1,
      title: '发布需求',
      description: '需求已发布并等待合适的同学接单。',
      done: true
    },
    {
      order: 2,
      title: '接单沟通',
      description: accepted ? `${helperDisplayName.value} 已接单，可在消息页继续沟通。` : '接单成功后会自动进入一对一沟通。',
      done: accepted
    },
    {
      order: 3,
      title: '完成确认',
      description: completionPending ? '至少有一方已提交完成确认。' : '履约结束后，双方各自确认一次完成状态。',
      done: completionPending
    },
    {
      order: 4,
      title: '互评结算',
      description: completed ? '可以进入互评页提交评价并结算积分。' : '双方确认完成后开放互评页面。',
      done: completed
    }
  ]
})

const actionButtonText = computed(() => {
  if (canConfirmTaskCompletion.value) return request.value.status === 'completion_pending' ? '确认对方完成' : '提交完成确认'
  if (canUnacceptTask.value) return '取消接单'
  const statusMap: Record<string, string> = {
    pending: '接受任务',
    accepted: '任务进行中',
    completion_pending: '等待双方确认',
    completed: '任务已完成',
    canceled: '任务已取消'
  }
  return statusMap[request.value.status || 'pending'] || '接受任务'
})

const actionNote = computed(() => {
  if (canConfirmTaskCompletion.value) {
    if (request.value.status === 'completion_pending') {
      return '对方已经提交完成确认，你确认后任务会正式结束并进入互评。'
    }
    return '提交完成确认后，任务会进入待确认状态，等待对方确认结束。'
  }
  if (!isTopicPost.value && hasCurrentUserConfirmedCompletion.value && request.value.status === 'completion_pending') {
    return '你已经确认完成，当前正等待对方确认。'
  }
  if (isOwnTask.value && request.value.status === 'completed') return '这项任务已经完成，双方现在可以互相评分并沉淀信用分。'
  if (isOwnTask.value) return '这是你自己发布的任务，无需自行接单。'
  if (canUnacceptTask.value) return '你已经接下这项任务，如需退出可在这里取消接单，任务会重新回到社区首页。'
  if (request.value.status === 'completion_pending') return '任务已进入待确认阶段，双方都确认后才能完成。'
  if (request.value.status === 'accepted') return '该任务已有同学接单，正在处理中。'
  if (request.value.status === 'completed') return '该任务已经完成，感谢每一次校园互助。'
  if (request.value.status === 'canceled') return '该任务已取消，暂时无法继续处理。'
  return '通过接受任务，您承诺按约定时间完成这项需求。'
})

const topicHeroClass = computed(() => {
  const categoryMap: Record<string, string> = {
    学习辅导: 'bg-gradient-to-br from-indigo-600 to-blue-500',
    二手闲置: 'bg-gradient-to-br from-amber-500 to-orange-500',
    恋爱交友: 'bg-gradient-to-br from-rose-500 to-pink-500',
    打听求助: 'bg-gradient-to-br from-sky-600 to-cyan-500',
    兼职招聘: 'bg-gradient-to-br from-emerald-600 to-teal-500'
  }
  return categoryMap[request.value.category] || 'bg-gradient-to-br from-slate-700 to-slate-600'
})

const sortedComments = computed(() => {
  const parseTime = (value?: string) => {
    const timestamp = value ? new Date(value).getTime() : 0
    return Number.isFinite(timestamp) ? timestamp : 0
  }

  return [...comments.value].sort((a, b) => {
    const likeDiff = Number(b.likeCount || 0) - Number(a.likeCount || 0)
    const timeDiff = parseTime(b.createdAt) - parseTime(a.createdAt)

    if (commentSortMode.value === 'latest') {
      return timeDiff || likeDiff || Number(b.id || 0) - Number(a.id || 0)
    }

    return likeDiff || timeDiff || Number(b.id || 0) - Number(a.id || 0)
  })
})

const commentTree = computed<CommentNode[]>(() => {
  const map = new Map<number, CommentNode>()
  const roots: CommentNode[] = []
  const MAX_DEPTH = 20

  sortedComments.value.forEach((comment) => {
    map.set(comment.id, { ...comment, children: [] })
  })

  map.forEach((comment) => {
    if (comment.parentId && map.has(comment.parentId)) {
      let parent = map.get(comment.parentId) || null
      let depth = 0
      while (parent?.parentId && map.has(parent.parentId) && depth < MAX_DEPTH) {
        parent = map.get(parent.parentId) || parent
        depth++
      }
      parent?.children.push(comment)
    } else {
      roots.push(comment)
    }
  })

  return roots
})

const setFeedback = (message: string, type: FeedbackType) => {
  feedbackMessage.value = message
  feedbackType.value = type
}

const isCommentLikeLoading = (commentId: number) => commentLikeLoadingIds.value.includes(Number(commentId))
const isCommentDeleteLoading = (commentId: number) => commentDeleteLoadingIds.value.includes(Number(commentId))
const formatCommentFloor = (index: number) => `第 ${index + 1} 楼`
const isRequesterComment = (comment: any) => Number(comment?.authorId) === Number(request.value?.requesterId)
const canDeleteComment = (comment: any) => {
  const currentUserId = Number(currentUser.value?.id)
  return !isExpiredTopic.value && Boolean(currentUserId) && (
    currentUserId === Number(request.value?.requesterId) ||
    currentUserId === Number(comment.authorId)
  )
}

const normalizeRequestTask = (task: any) => {
  const category = task?.category || inferCategory(task)
  return normalizeTaskRecord({
    ...task,
    category
  })
}

const inferCategory = (task: any) => {
  const typeMap: Record<string, string> = {
    errand: '跑腿代办',
    study: '学习辅导',
    secondhand: '二手闲置',
    help: '打听求助',
    social: '恋爱交友',
    job: '兼职招聘',
    校园配送: '跑腿代办',
    学业辅导: '学习辅导',
    闲置交换: '二手闲置',
    信息求助: '打听求助',
    社交互助: '恋爱交友',
    兼职机会: '兼职招聘'
  }
  return typeMap[task?.impactText || task?.badgeSecondary || task?.taskType] || '跑腿代办'
}

const formatDateTime = (value?: string) => {
  return formatLocaleDateTime(value, {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }, '刚刚')
}

const fetchTaskDetail = async () => {
  try {
    detailError.value = ''
    detailLoading.value = true
    request.value = normalizeRequestTask(await taskApi.getTaskById(Number(props.id)))
    if (isTopicPost.value) {
      await fetchComments()
      reviews.value = []
    } else if (['accepted', 'completion_pending', 'completed'].includes(String(request.value.status || ''))) {
      await fetchReviews()
    } else {
      reviews.value = []
    }
  } catch (err) {
    console.error('获取详情失败:', err)
    request.value = {}
    comments.value = []
    detailError.value = '获取任务详情失败，请稍后重试。'
  } finally {
    detailLoading.value = false
  }
}

const fetchReviews = async () => {
  if (isTopicPost.value) return
  reviewsLoading.value = true
  try {
    reviews.value = await taskApi.getTaskReviews(Number(props.id)) as any[]
  } catch (error: any) {
    console.error('获取评价失败:', error)
    reviews.value = []
    if (request.value.status === 'completed') {
      setFeedback(error?.response?.data?.message || '评价信息加载失败，请稍后刷新重试。', 'error')
    }
  } finally {
    reviewsLoading.value = false
  }
}

const fetchAcceptedTasks = async () => {
  try {
    const tasks = await taskApi.getMyAcceptedTasks() as any[]
    acceptedTaskIds.value = tasks.map((task: any) => Number(task.id)).filter((id: number) => Number.isFinite(id))
  } catch (error) {
    console.error('获取我的服务失败:', error)
    acceptedTaskIds.value = []
  }
}

const fetchComments = async () => {
  if (!isTopicPost.value) return
  commentsLoading.value = true
  try {
    comments.value = await taskApi.getTaskComments(Number(props.id)) as any[]
    request.value = {
      ...request.value,
      commentCount: comments.value.length
    }
  } catch (error) {
    console.error('获取评论失败:', error)
    comments.value = []
    setFeedback('评论加载失败，请稍后刷新重试。', 'error')
  } finally {
    commentsLoading.value = false
  }
}

const startReply = (comment: any) => {
  if (isExpiredTopic.value) {
    setFeedback('话题帖已截止，暂不支持回复。', 'error')
    return
  }
  replyTarget.value = comment
  commentForm.value.content = ''
}

const cancelReply = () => {
  replyTarget.value = null
}

const patchCommentLikeState = (commentId: number, likedByCurrentUser: boolean, likeCount: number) => {
  comments.value = comments.value.map((comment) => (
    Number(comment.id) === Number(commentId)
      ? { ...comment, likedByCurrentUser, likeCount }
      : comment
  ))
}

const deleteComment = async (comment: any) => {
  if (isExpiredTopic.value) {
    setFeedback('话题帖已截止，暂不支持删除评论或回复。', 'error')
    return
  }
  if (!canDeleteComment(comment)) {
    setFeedback('只有帖主或评论发布者可以删除这条内容。', 'error')
    return
  }

  const confirmed = await openConfirm({
    title: '确认删除',
    message: comment.parentId ? '确定要删除这条回复吗？删除后无法恢复。' : '确定要删除这条评论吗？删除后无法恢复。',
    confirmText: '删除',
    cancelText: '取消'
  })

  if (!confirmed) return

  const commentId = Number(comment.id)
  commentDeleteLoadingIds.value = [...commentDeleteLoadingIds.value, commentId]

  try {
    await taskApi.deleteTaskComment(Number(props.id), commentId)
    if (replyTarget.value && Number(replyTarget.value.id) === commentId) {
      cancelReply()
    }
    setFeedback(comment.parentId ? '回复已删除。' : '评论已删除。', 'success')
    await fetchComments()
  } catch (error: any) {
    console.error('删除评论失败:', error)
    setFeedback(error?.response?.data?.message || '删除失败，请稍后重试。', 'error')
  } finally {
    commentDeleteLoadingIds.value = commentDeleteLoadingIds.value.filter((id) => id !== commentId)
  }
}

const submitComment = async () => {
  if (isExpiredTopic.value) {
    setFeedback('话题帖已截止，暂不支持评论或回复。', 'error')
    return
  }
  if (!commentForm.value.content.trim()) {
    setFeedback('请输入评论内容。', 'error')
    return
  }

  commentLoading.value = true
  setFeedback('', 'success')

  try {
    await taskApi.createTaskComment(Number(props.id), {
      content: commentForm.value.content.trim(),
      parentId: replyTarget.value?.id ?? null
    })
    commentForm.value.content = ''
    const latestUser = await userApi.getCurrentUser() as Record<string, any>
    if (latestUser) {
      setStoredUser(latestUser)
    }
    setFeedback(replyTarget.value ? '回复已发布。' : '评论已发布。', 'success')
    cancelReply()
    await fetchComments()
  } catch (error: any) {
    console.error('发布评论失败:', error)
    setFeedback(error?.response?.data?.message || '发布评论失败，请稍后重试。', 'error')
  } finally {
    commentLoading.value = false
  }
}

const insertAtCursor = (target: HTMLTextAreaElement | null, currentValue: string, value: string) => {
  const start = target?.selectionStart ?? currentValue.length
  const end = target?.selectionEnd ?? currentValue.length
  const nextValue = currentValue.slice(0, start) + value + currentValue.slice(end)
  const nextCursor = start + value.length
  return { nextValue, nextCursor }
}

const insertCommentEmoji = (emoji: string) => {
  const { nextValue, nextCursor } = insertAtCursor(commentTextarea.value, commentForm.value.content, emoji)
  commentForm.value.content = nextValue
  nextTick(() => {
    const el = commentTextarea.value
    if (!el) return
    el.focus()
    el.setSelectionRange(nextCursor, nextCursor)
  })
}

const insertContactEmoji = (emoji: string) => {
  const { nextValue, nextCursor } = insertAtCursor(contactTextarea.value, contactMessage.value, emoji)
  contactMessage.value = nextValue
  nextTick(() => {
    const el = contactTextarea.value
    if (!el) return
    el.focus()
    el.setSelectionRange(nextCursor, nextCursor)
  })
}

const openContactDialog = () => {
  if (!canContactRequester.value) {
    setFeedback(isOwnTask.value ? '不能联系自己发布的内容。' : '当前内容暂时无法联系发布者。', 'error')
    return
  }
  contactError.value = ''
  if (!contactMessage.value) {
    contactMessage.value = isTopicPost.value
      ? `你好，我看了你发布的”${request.value.title || '这条帖子'}”，想进一步了解一下。`
      : isOwnTask.value
        ? `你好，关于”${request.value.title || '这项任务'}”，我想和你确认一下履约细节。`
        : `你好，我对”${request.value.title || '这项任务'}”感兴趣，想确认一下时间和具体地点。`
  }
  showContactDialog.value = true
}

const closeContactDialog = () => {
  if (contactLoading.value) return
  showContactDialog.value = false
  contactError.value = ''
}

const goToReviewPage = () => {
  router.push(`/detail/${props.id}/review`)
}

const goToHelperConversation = () => {
  if (!request.value?.helperId) {
    setFeedback('当前还没有可进入的接单对话。', 'error')
    return
  }

  router.push({
    path: '/messages',
    query: {
      taskId: String(request.value.id ?? ''),
      userId: String(request.value.helperId ?? ''),
      taskTitle: request.value.title || '',
      userName: request.value.helperName || '接单同学'
    }
  })
}

const handleContactRequester = async () => {
  if (!contactMessage.value.trim()) {
    contactError.value = '请输入想发送的内容。'
    return
  }

  contactLoading.value = true
  try {
    await messageApi.sendMessage({
      receiverId: Number(contactTargetId.value),
      taskId: Number(request.value.id),
      content: contactMessage.value.trim()
    })
    showContactDialog.value = false
    setFeedback('消息已发送，正在跳转到消息页。', 'success')
    router.push({
      path: '/messages',
      query: {
        taskId: String(request.value.id ?? ''),
        userId: String(contactTargetId.value ?? ''),
        taskTitle: request.value.title || '',
        userName: contactTargetName.value || ''
      }
    })
  } catch (error: any) {
    console.error('联系发布者失败:', error)
    const message = error?.response?.data?.message || '发送失败，请稍后重试。'
    contactError.value = message
    setFeedback(message, 'error')
  } finally {
    contactLoading.value = false
  }
}

const handleDeleteTask = async () => {
  if (!canDeleteTask.value) {
    setFeedback('当前状态暂不支持删除帖子。', 'error')
    return
  }

  const confirmed = await openConfirm({
    title: '确认删除内容',
    message: `确认删除“${request.value.title || '这条帖子'}”吗？删除后将无法恢复。`,
    confirmText: '删除',
    tone: 'danger'
  })
  if (!confirmed) {
    return
  }

  deleteLoading.value = true
  try {
    await taskApi.deleteTask(Number(props.id))
    router.push('/profile')
  } catch (error: any) {
    console.error('删除帖子失败:', error)
    const responseData = error?.response?.data
    const message =
      responseData?.message ||
      responseData?.error ||
      (typeof responseData === 'string' ? responseData : '') ||
      '删除失败，请稍后重试。'
    setFeedback(message, 'error')
  } finally {
    deleteLoading.value = false
  }
}

const likeAnimating = ref(false)
const showImageLightbox = ref(false)
const lightboxIndex = ref(0)

const detailImages = computed(() => {
  try {
    const raw = request.value?.imageUrls
    if (!raw) return []
    const parsed = typeof raw === 'string' ? JSON.parse(raw) : raw
    if (!Array.isArray(parsed)) return []
    return parsed.map((url: string) => {
      if (url.startsWith('http')) return url
      return url.startsWith('/api') ? url : `/api${url}`
    })
  } catch {
    return []
  }
})

const toggleTopicLike = async () => {
  if (!isTopicPost.value) return
  if (isExpiredTopic.value) {
    setFeedback('话题帖已截止，暂不支持点赞操作。', 'error')
    return
  }
  if (isOwnTask.value) {
    setFeedback('自己的帖子暂不支持点赞。', 'error')
    return
  }

  topicLikeLoading.value = true
  try {
    const response = isTopicLiked.value
      ? await taskApi.unlikeTask(Number(props.id))
      : await taskApi.likeTask(Number(props.id))
    const nextTaskState = (response && typeof response === 'object') ? response as Record<string, any> : {}
    request.value = normalizeRequestTask({
      ...request.value,
      ...nextTaskState
    })
    if (!isTopicLiked.value) {
      likeAnimating.value = true
      setTimeout(() => { likeAnimating.value = false }, 400)
    }
  } catch (error: any) {
    console.error('帖子点赞操作失败:', error)
    setFeedback(error?.response?.data?.message || '帖子点赞操作失败，请稍后重试。', 'error')
  } finally {
    topicLikeLoading.value = false
  }
}

const toggleFavorite = async () => {
  if (isOwnTask.value) {
    setFeedback('不能收藏自己发布的内容。', 'error')
    return
  }

  favoriteLoading.value = true
  try {
    if (isFavorited.value) {
      await taskApi.unfavoriteTask(Number(props.id))
      request.value = { ...request.value, isFavorited: false }
      setFeedback('已取消收藏。', 'success')
    } else {
      await taskApi.favoriteTask(Number(props.id))
      request.value = { ...request.value, isFavorited: true }
      setFeedback('已添加收藏，可在个人主页查看。', 'success')
    }
  } catch (error: any) {
    console.error('收藏操作失败:', error)
    setFeedback(error?.response?.data?.message || '收藏操作失败，请稍后重试。', 'error')
  } finally {
    favoriteLoading.value = false
  }
}

const toggleCommentLike = async (comment: any) => {
  if (isExpiredTopic.value) {
    setFeedback('话题帖已截止，暂不支持点赞操作。', 'error')
    return
  }
  if (Number(comment.authorId) === Number(currentUser.value?.id)) {
    setFeedback('自己的评论暂不支持点赞。', 'error')
    return
  }

  const commentId = Number(comment.id)
  commentLikeLoadingIds.value = [...commentLikeLoadingIds.value, commentId]

  try {
    const response = comment.likedByCurrentUser
      ? await taskApi.unlikeTaskComment(Number(props.id), commentId)
      : await taskApi.likeTaskComment(Number(props.id), commentId)
    const nextComment = response as Record<string, any>
    patchCommentLikeState(commentId, Boolean(nextComment.likedByCurrentUser), Number(nextComment.likeCount || 0))
  } catch (error: any) {
    console.error('评论点赞操作失败:', error)
    setFeedback(error?.response?.data?.message || '评论点赞操作失败，请稍后重试。', 'error')
  } finally {
    commentLikeLoadingIds.value = commentLikeLoadingIds.value.filter((id) => id !== commentId)
  }
}

const handleAcceptTask = async () => {
  if (!canAcceptTask.value) {
    setFeedback(actionNote.value, 'error')
    return
  }

  acceptLoading.value = true
  try {
    const response = await taskApi.acceptTask(Number(props.id)) as Record<string, any>
    request.value = normalizeRequestTask({
      ...request.value,
      ...response,
      status: 'accepted'
    })
    if (!acceptedTaskIds.value.includes(Number(props.id))) {
      acceptedTaskIds.value = [...acceptedTaskIds.value, Number(props.id)]
    }
    setFeedback('任务已成功接受，你现在可以联系需求方继续沟通。', 'success')
  } catch (error: any) {
    console.error('接受任务失败:', error)
    setFeedback(error?.response?.data?.message || '接受任务失败，请稍后重试。', 'error')
    await fetchTaskDetail()
  } finally {
    acceptLoading.value = false
  }
}

const handleUnacceptTask = async () => {
  if (!canUnacceptTask.value) {
    setFeedback(actionNote.value, 'error')
    return
  }

  const confirmed = await openConfirm({
    title: '确认取消接单',
    message: `确认取消接单“${request.value.title || '这项任务'}”吗？取消后任务会重新变为可接单状态。`,
    confirmText: '确认取消',
    tone: 'danger'
  })
  if (!confirmed) {
    return
  }

  acceptLoading.value = true
  try {
    const response = await taskApi.unacceptTask(Number(props.id)) as Record<string, any>
    request.value = normalizeRequestTask({
      ...request.value,
      ...response,
      status: 'pending'
    })
    acceptedTaskIds.value = acceptedTaskIds.value.filter((id) => id !== Number(props.id))
    setFeedback('已取消接单，任务会重新出现在社区首页。', 'success')
  } catch (error: any) {
    console.error('取消接单失败:', error)
    setFeedback(error?.response?.data?.message || '取消接单失败，请稍后重试。', 'error')
    await fetchTaskDetail()
    await fetchAcceptedTasks()
  } finally {
    acceptLoading.value = false
  }
}

const handleCompleteTask = async () => {
  if (!canConfirmTaskCompletion.value) {
    setFeedback(actionNote.value, 'error')
    return
  }

  const confirmed = await openConfirm({
    title: request.value.status === 'completion_pending' ? '确认任务完成' : '提交完成申请',
    message: request.value.status === 'completion_pending'
      ? `确认“${request.value.title || '这项任务'}”已经完成吗？确认后将正式结束并进入互评。`
      : `确认提交“${request.value.title || '这项任务'}”的完成申请吗？提交后会等待对方确认。`,
    confirmText: '确认'
  })
  if (!confirmed) {
    return
  }

  acceptLoading.value = true
  try {
    const response = await taskApi.completeTask(Number(props.id)) as Record<string, any>
    request.value = normalizeRequestTask({
      ...request.value,
      ...response
    })
    if (request.value.status === 'completed') {
      acceptedTaskIds.value = acceptedTaskIds.value.filter((id) => id !== Number(props.id))
      request.value = normalizeRequestTask(await taskApi.getTaskById(Number(props.id)))
      setFeedback('双方已确认完成，任务已结束，现在可以互相评价。', 'success')
      await fetchAcceptedTasks()
      await fetchReviews()
    } else {
      setFeedback('你已确认完成，正在等待对方确认。', 'success')
    }
  } catch (error: any) {
    console.error('完成任务失败:', error)
    setFeedback(error?.response?.data?.message || '完成任务失败，请稍后重试。', 'error')
    await fetchTaskDetail()
    await fetchAcceptedTasks()
  } finally {
    acceptLoading.value = false
  }
}

const handleTaskAction = async () => {
  if (canConfirmTaskCompletion.value) {
    await handleCompleteTask()
    return
  }
  if (canUnacceptTask.value) {
    await handleUnacceptTask()
    return
  }
  await handleAcceptTask()
}

onMounted(async () => {
  await Promise.all([fetchAcceptedTasks(), fetchTaskDetail()])
})
</script>
