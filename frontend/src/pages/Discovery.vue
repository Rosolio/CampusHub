<template>
  <div class="min-h-screen bg-surface font-body text-on-surface">
    <AppTopNav />

    <main class="mx-auto max-w-7xl px-6 pb-24 pt-24">
      <section class="mb-8 overflow-hidden rounded-[2rem] bg-[radial-gradient(circle_at_top_left,rgba(255,255,255,0.96),rgba(236,253,245,0.96)_36%,rgba(223,245,255,0.92)_68%,rgba(248,250,252,0.95)_100%)] p-6 shadow-sm md:p-8">
        <div class="grid gap-6 xl:grid-cols-[1.1fr_0.9fr] xl:items-start">
          <div>
            <div class="mb-4 flex flex-wrap items-center gap-3">
              <span class="rounded-full bg-teal-900 px-4 py-2 text-xs font-bold uppercase tracking-[0.22em] text-white">校园互助广场</span>
            </div>

            <h1 class="max-w-3xl text-4xl font-extrabold leading-[1.08] tracking-tight text-teal-950 md:text-5xl">
              需求与话题，一站浏览
            </h1>

            <p class="mt-4 max-w-2xl text-base leading-7 text-slate-600">
              需求广场和话题广场平等呈现。跑腿代办、学习辅导等任务类内容，与二手闲置、恋爱交友等话题类内容，按需切换浏览。
            </p>

            <div class="mt-6 flex flex-wrap gap-3">
              <button
                type="button"
                class="inline-flex items-center gap-2 rounded-full bg-teal-950 px-5 py-3 text-sm font-bold text-white transition-colors hover:bg-teal-900"
                @click="scrollToTabBar"
              >
                开始浏览
                <span class="material-symbols-outlined text-base">south</span>
              </button>
              <RouterLink
                to="/publish"
                class="inline-flex items-center gap-2 rounded-full border border-teal-200 bg-white/80 px-5 py-3 text-sm font-bold text-teal-900 transition-colors hover:bg-white dark:hover:bg-white/10"
              >
                发布
                <span class="material-symbols-outlined text-base">add</span>
              </RouterLink>
            </div>
          </div>

          <div class="grid gap-4 md:grid-cols-2">
            <button
              type="button"
              class="group rounded-[1.6rem] bg-teal-950 p-5 text-left text-white shadow-[0_18px_40px_rgba(15,23,42,0.14)] transition-all hover:-translate-y-0.5 hover:shadow-[0_22px_50px_rgba(15,23,42,0.18)]"
              @click="switchToTab('demand')"
            >
              <div class="flex items-start justify-between gap-4">
                <div>
                  <p class="text-xs font-bold uppercase tracking-[0.18em] text-cyan-100/70">需求广场</p>
                  <h2 class="mt-2 text-2xl font-extrabold text-white">任务接单</h2>
                </div>
                <div class="rounded-2xl bg-white/10 p-3 text-white ring-1 ring-white/10">
                  <span class="material-symbols-outlined text-3xl">assignment</span>
                </div>
              </div>
              <p class="mt-4 text-sm leading-7 text-cyan-50/80">
                跑腿代办、学习辅导等需要明确时间、地点和奖励的内容优先在这里处理。
              </p>
              <div class="mt-4 flex flex-wrap gap-2">
                <button
                  v-for="category in demandPreviewCategories"
                  :key="category"
                  type="button"
                  class="rounded-full px-3 py-1.5 text-xs font-semibold transition-all"
                  :class="selectedDemandEntryCategory === category
                    ? 'bg-white text-teal-900 shadow-sm'
                    : 'bg-white/10 text-cyan-50 hover:bg-white/20'"
                  @click.stop="selectDemandCategory(category)"
                >
                  {{ category }}
                </button>
                <span class="invisible rounded-full px-3 py-1.5 text-xs font-semibold">二手闲置</span>
                <span class="invisible rounded-full px-3 py-1.5 text-xs font-semibold">恋爱交友</span>
              </div>
              <div class="mt-5 inline-flex items-center gap-2 text-sm font-bold text-white transition-all group-hover:gap-3">
                进入需求广场
                <span class="material-symbols-outlined text-base">arrow_forward</span>
              </div>
            </button>

            <button
              type="button"
              class="group rounded-[1.6rem] bg-white/92 p-5 text-left shadow-[0_18px_40px_rgba(15,23,42,0.08)] ring-1 ring-teal-100/90 transition-all hover:-translate-y-0.5 hover:shadow-[0_22px_50px_rgba(15,23,42,0.12)]"
              @click="switchToTab('topic')"
            >
              <div class="flex items-start justify-between gap-4">
                <div>
                  <p class="text-xs font-bold uppercase tracking-[0.18em] text-teal-700/60">话题广场</p>
                  <h2 class="mt-2 text-2xl font-extrabold text-teal-950">公开互动</h2>
                </div>
                <div class="rounded-2xl bg-teal-50 p-3 text-teal-900 ring-1 ring-teal-100">
                  <span class="material-symbols-outlined text-3xl">forum</span>
                </div>
              </div>
              <p class="mt-4 text-sm leading-7 text-slate-600">
                二手、交友、求助、兼职等更适合公开讨论的内容，统一在这里浏览。
              </p>
              <div class="mt-4 flex flex-wrap gap-2">
                <button
                  v-for="category in topicPreviewCategories"
                  :key="category"
                  type="button"
                  class="rounded-full px-3 py-1.5 text-xs font-semibold transition-all"
                  :class="selectedTopicEntryCategory === category
                    ? 'bg-teal-900 text-white shadow-sm'
                    : 'bg-surface-container-low text-teal-900 hover:bg-teal-50'"
                  @click.stop="selectTopicCategory(category)"
                >
                  {{ category }}
                </button>
              </div>
              <div class="mt-5 inline-flex items-center gap-2 text-sm font-bold text-teal-900 transition-all group-hover:gap-3">
                进入话题广场
                <span class="material-symbols-outlined text-base">arrow_forward</span>
              </div>
            </button>
          </div>
        </div>
      </section>

      <div ref="tabBarRef" class="sticky top-16 z-30 mb-8 flex justify-center">
        <div class="inline-flex items-center rounded-2xl bg-surface-container-low p-1.5 shadow-sm">
          <button
            type="button"
            class="flex items-center gap-2 rounded-xl px-6 py-3 text-sm font-bold transition-all"
            :class="activeTab === 'demand'
              ? 'bg-teal-900 text-white shadow-sm'
              : 'text-on-surface-variant hover:text-on-surface'"
            @click="setTab('demand')"
          >
            <span class="material-symbols-outlined text-lg">assignment</span>
            需求广场
          </button>
          <button
            type="button"
            class="flex items-center gap-2 rounded-xl px-6 py-3 text-sm font-bold transition-all"
            :class="activeTab === 'topic'
              ? 'bg-teal-900 text-white shadow-sm'
              : 'text-on-surface-variant hover:text-on-surface'"
            @click="setTab('topic')"
          >
            <span class="material-symbols-outlined text-lg">forum</span>
            话题广场
          </button>
        </div>
      </div>

      <div v-if="activeTab === 'demand'" class="mb-6 grid gap-4 lg:grid-cols-[minmax(0,1fr)_18rem]">
        <section class="rounded-[1.75rem] bg-surface-container-lowest p-4 shadow-sm sm:p-5">
          <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <p class="text-sm font-bold uppercase tracking-[0.14em] text-teal-700/65">当前可接单任务</p>
              <h2 class="mt-2 text-2xl font-extrabold text-teal-950">先筛选，再查看详情</h2>
            </div>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="mode in recommendationModes"
                :key="mode.value"
                type="button"
                class="flex shrink-0 items-center gap-2 rounded-full px-4 py-2 text-sm font-semibold transition-all"
                :class="recommendationMode === mode.value ? 'bg-teal-950 text-white shadow-sm' : 'bg-surface-container-low text-on-surface-variant hover:bg-cyan-50/70'"
                @click="setRecommendationMode(mode.value)"
              >
                <span class="material-symbols-outlined text-lg">{{ mode.icon }}</span>
                {{ mode.label }}
              </button>
            </div>
            <div class="flex flex-wrap gap-2">
              <button
                v-for="category in categories"
                :key="category"
                type="button"
                class="flex shrink-0 items-center gap-2 rounded-full px-4 py-2 text-sm font-semibold transition-all"
                :class="activeCategory === category ? 'bg-primary text-white shadow-sm' : 'bg-surface-container-low text-on-surface-variant hover:bg-cyan-50/70'"
                @click="setCategory(category)"
              >
                <span class="material-symbols-outlined text-lg">{{ iconForCategory(category) }}</span>
                {{ category }}
              </button>
            </div>
          </div>

          <div class="mt-5 grid gap-3 lg:grid-cols-[minmax(0,1fr)_auto] lg:items-center">
            <label class="flex items-center gap-3 rounded-2xl bg-surface-container-low px-4 py-3 text-sm font-semibold text-on-surface-variant">
              <span class="material-symbols-outlined text-xl text-teal-900">location_on</span>
              <input
                v-model.trim="selectedLocation"
                type="text"
                class="min-w-0 flex-1 bg-transparent text-on-surface outline-none placeholder:text-on-surface-variant/70"
                placeholder="输入校内位置"
                @keyup.enter="fetchTasks"
              >
              <button
                type="button"
                class="rounded-full bg-white px-3 py-1.5 text-xs font-bold text-teal-900 shadow-sm"
                @click="fetchTasks"
              >
                应用
              </button>
            </label>

            <div class="flex flex-wrap gap-2">
              <button
                v-for="option in availableTimeOptions"
                :key="option.value"
                type="button"
                class="rounded-full px-4 py-2 text-sm font-semibold transition-all"
                :class="availableTime === option.value ? 'bg-teal-900 text-white shadow-sm' : 'bg-surface-container-low text-on-surface-variant hover:bg-cyan-50/70'"
                @click="setAvailableTime(option.value)"
              >
                {{ option.label }}
              </button>
            </div>
          </div>
        </section>

        <aside class="rounded-[1.75rem] bg-surface-container-lowest p-5 shadow-sm">
          <p class="text-xs font-bold uppercase tracking-[0.18em] text-teal-700/65">反馈入口</p>
          <h2 class="mt-2 text-lg font-extrabold text-teal-950">发现 bug 或有建议？</h2>
          <p class="mt-2 text-sm leading-6 text-on-surface-variant">
            反馈会进入管理员处理队列，回复后通过系统提醒通知你。
          </p>
          <RouterLink
            to="/feedback"
            class="mt-4 inline-flex items-center gap-2 rounded-full bg-teal-900 px-4 py-2.5 text-sm font-bold text-white transition hover:bg-teal-800"
          >
            提交反馈
            <span class="material-symbols-outlined text-base">arrow_forward</span>
          </RouterLink>
        </aside>
      </div>

      <div v-if="activeTab === 'topic'" class="mb-6">
        <section class="rounded-[1.75rem] bg-surface-container-lowest p-4 shadow-sm sm:p-5">
          <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
            <div>
              <p class="text-sm font-bold uppercase tracking-[0.14em] text-teal-700/65">筛选话题帖</p>
              <h2 class="mt-2 text-2xl font-extrabold text-teal-950">先分类，再搜索</h2>
            </div>
            <div class="flex flex-wrap gap-3">
              <button
                v-for="category in topicCategories"
                :key="category"
                type="button"
                class="rounded-full px-4 py-2 text-sm font-semibold transition-all"
                :class="topicActiveCategory === category ? 'bg-primary text-white shadow-sm' : 'bg-surface-container-low text-on-surface-variant hover:bg-cyan-50/60'"
                @click="setTopicCategory(category)"
              >
                {{ category }}
              </button>
            </div>
          </div>

          <label class="mt-4 flex w-full items-center gap-3 rounded-[1.25rem] bg-surface-container-low px-4 py-3 text-sm text-on-surface lg:max-w-xl">
            <span class="material-symbols-outlined text-lg text-on-surface-variant">search</span>
            <input
              v-model.trim="topicKeyword"
              type="text"
              class="min-w-0 flex-1 bg-transparent outline-none placeholder:text-on-surface-variant/60"
              placeholder="搜索标题、内容、分类、地点或发布者"
            />
            <button
              v-if="topicKeyword"
              type="button"
              class="inline-flex h-8 w-8 items-center justify-center rounded-full text-on-surface-variant transition-colors hover:bg-surface-container-high hover:text-on-surface"
              aria-label="清空搜索"
              @click="clearTopicKeyword"
            >
              <span class="material-symbols-outlined text-lg">close</span>
            </button>
          </label>
        </section>
      </div>

      <div class="grid gap-6 xl:grid-cols-[minmax(0,1fr)_18rem]">
        <section>
          <template v-if="activeTab === 'demand'">
            <section v-if="loading" class="rounded-[2rem] bg-surface-container-low p-10 text-center text-on-surface-variant">
              正在加载社区内容...
            </section>

            <section v-else-if="error" class="rounded-[2rem] border border-rose-200 bg-rose-50 p-10 text-center text-rose-700">
              <p class="mb-4">{{ error }}</p>
              <button
                type="button"
                class="inline-flex items-center gap-2 rounded-full bg-rose-100 px-4 py-2 text-sm font-bold text-rose-800 transition-colors hover:bg-rose-200"
                @click="fetchTasks"
              >
                <span class="material-symbols-outlined text-base">refresh</span>
                重新加载
              </button>
            </section>

            <section v-else-if="filteredCards.length === 0" class="rounded-[2rem] bg-surface-container-low p-10 text-center">
              <div class="mx-auto mb-5 flex h-20 w-20 items-center justify-center rounded-full bg-surface-container-high">
                <span class="material-symbols-outlined text-4xl text-on-surface-variant">inbox</span>
              </div>
              <h2 class="text-2xl font-bold text-teal-900">当前没有可接单任务</h2>
              <p class="mt-3 text-on-surface-variant">稍后再来看看，或者自己先发一条跑腿代办或学习辅导。</p>
              <button
                v-if="hasActiveMatchingFilters"
                type="button"
                class="mt-5 inline-flex items-center gap-2 rounded-full bg-teal-900 px-5 py-3 text-sm font-bold text-white transition hover:bg-teal-800"
                @click="clearMatchingFilters"
              >
                清空筛选
                <span class="material-symbols-outlined text-base">filter_alt_off</span>
              </button>
            </section>

            <section v-else class="grid gap-6 lg:grid-cols-2">
              <article
                v-for="card in filteredCards"
                :key="card.id"
                class="overflow-hidden rounded-[1.75rem] border border-outline-variant/10 bg-surface-container-lowest shadow-sm transition-all hover:-translate-y-1 hover:shadow-lg"
              >
                <div class="p-6">
                  <div class="mb-4 flex flex-wrap items-center gap-2">
                    <span class="rounded-full bg-surface-container-high px-3 py-1 text-[11px] font-bold uppercase tracking-[0.14em] text-on-surface-variant">
                      {{ card.category }}
                    </span>
                    <span
                      class="rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-[0.14em]"
                      :class="card.badgePrimary === '紧急' ? 'border border-rose-200 bg-rose-50 text-rose-700' : 'border border-sky-200 bg-sky-50 text-sky-700'"
                    >
                      {{ card.badgePrimary }}
                    </span>
                    <span class="rounded-full border border-amber-200 bg-amber-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.14em] text-amber-700">
                      {{ taskStatusBadge(card.status) }}
                    </span>
                    <span
                      v-if="card.matchScore !== null"
                      class="rounded-full border border-teal-200 bg-teal-50 px-3 py-1 text-[11px] font-bold uppercase tracking-[0.14em] text-teal-800"
                    >
                      匹配度 {{ card.matchScore }}
                    </span>
                  </div>

                  <div class="flex flex-col gap-3">
                    <div class="flex items-start justify-between gap-4">
                      <h2 class="min-w-0 text-xl font-extrabold leading-tight text-teal-900">
                        {{ card.title }}
                      </h2>
                      <span class="shrink-0 text-sm font-bold text-secondary">
                        {{ card.rewardText }}
                      </span>
                    </div>

                    <p class="line-clamp-3 text-sm leading-7 text-on-surface-variant">
                      {{ card.description }}
                    </p>
                  </div>

                  <div class="mt-5 grid gap-3 sm:grid-cols-2">
                    <div class="rounded-2xl bg-surface-container-low px-4 py-3 text-on-surface">
                      <p class="text-[11px] font-bold uppercase tracking-[0.14em] text-on-surface-variant">地点</p>
                      <p class="mt-2 text-sm font-medium">{{ card.locationText || '待补充' }}</p>
                    </div>
                    <div class="rounded-2xl bg-surface-container-low px-4 py-3 text-on-surface">
                      <p class="text-[11px] font-bold uppercase tracking-[0.14em] text-on-surface-variant">截止时间</p>
                      <p class="mt-2 text-sm font-medium">{{ card.timeText || '待补充' }}</p>
                    </div>
                  </div>

                  <div v-if="card.matchReasons.length > 0" class="mt-4 flex flex-wrap gap-2">
                    <span
                      v-for="reason in card.matchReasons.slice(0, 2)"
                      :key="reason"
                      class="inline-flex items-center gap-1 rounded-full bg-cyan-50 px-3 py-1.5 text-xs font-bold text-teal-900"
                    >
                      <span class="material-symbols-outlined text-sm">auto_awesome</span>
                      {{ reason }}
                    </span>
                  </div>

                  <div class="mt-6 flex items-center justify-between gap-3 border-t border-outline-variant/10 pt-4">
                    <div>
                      <p class="text-sm font-semibold text-teal-900">{{ card.publisher }}</p>
                      <p class="mt-1 text-xs text-on-surface-variant">{{ taskStatusText(card) }}</p>
                    </div>
                    <RouterLink
                      :to="`/detail/${card.id}`"
                      class="inline-flex items-center gap-2 rounded-full bg-primary px-5 py-3 text-sm font-bold text-white transition-all hover:gap-3"
                    >
                      查看任务
                      <span class="material-symbols-outlined text-base">arrow_forward</span>
                    </RouterLink>
                  </div>
                </div>
              </article>
            </section>
          </template>

          <template v-else>
            <section v-if="topicLoading" class="rounded-[2rem] bg-surface-container-low p-10 text-center text-on-surface-variant">
              正在加载话题帖...
            </section>

            <section v-else-if="topicError" class="rounded-[2rem] border border-rose-200 bg-rose-50 p-10 text-center text-rose-700">
              <p class="mb-4">{{ topicError }}</p>
              <button
                type="button"
                class="inline-flex items-center gap-2 rounded-full bg-rose-100 px-4 py-2 text-sm font-bold text-rose-800 transition-colors hover:bg-rose-200"
                @click="fetchTopicPosts"
              >
                <span class="material-symbols-outlined text-base">refresh</span>
                重新加载
              </button>
            </section>

            <section v-else-if="filteredTopicPosts.length === 0" class="rounded-[2rem] bg-surface-container-low p-10 text-center">
              <div class="mx-auto mb-5 flex h-20 w-20 items-center justify-center rounded-full bg-surface-container-high">
                <span class="material-symbols-outlined text-4xl text-on-surface-variant">forum</span>
              </div>
              <h2 class="text-2xl font-bold text-teal-900">{{ topicEmptyTitle }}</h2>
              <p class="mt-3 text-on-surface-variant">{{ topicEmptyDescription }}</p>
            </section>

            <section v-else class="space-y-4">
              <article
                v-for="card in pagedTopicPosts"
                :key="card.id"
                class="rounded-[1.5rem] border border-outline-variant/12 bg-surface-container-lowest p-5 shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md"
              >
                <div class="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
                  <div class="min-w-0 flex-1">
                    <div class="mb-3 flex flex-wrap items-center gap-2">
                      <span class="rounded-full px-3 py-1 text-[11px] font-bold uppercase tracking-[0.12em]" :class="topicCategoryBadgeClass(card.category)">
                        {{ card.category }}
                      </span>
                      <span class="rounded-full bg-surface-container-low px-3 py-1 text-[11px] font-bold uppercase tracking-[0.12em] text-on-surface-variant">话题帖</span>
                      <span class="text-xs font-semibold text-on-surface-variant">{{ card.publisher }}</span>
                    </div>

                    <h2 class="text-xl font-extrabold text-teal-950">
                      {{ card.title }}
                    </h2>
                    <p class="mt-2 line-clamp-3 text-sm leading-7 text-on-surface-variant">
                      {{ card.description }}
                    </p>

                    <div class="mt-4 flex flex-wrap items-center gap-x-4 gap-y-2 text-xs font-semibold text-on-surface-variant">
                      <span class="inline-flex items-center gap-1">
                        <span class="material-symbols-outlined text-sm">place</span>
                        {{ card.locationText || '待补充' }}
                      </span>
                      <span class="inline-flex items-center gap-1">
                        <span class="material-symbols-outlined text-sm">schedule</span>
                        {{ card.timeText || '待补充' }}
                      </span>
                      <span class="inline-flex items-center gap-1">
                        <span class="material-symbols-outlined text-sm">favorite</span>
                        {{ card.likeCount || 0 }}
                      </span>
                      <span class="inline-flex items-center gap-1">
                        <span class="material-symbols-outlined text-sm">chat_bubble</span>
                        {{ card.commentCount || 0 }}
                      </span>
                    </div>
                  </div>

                  <div class="shrink-0 border-t border-outline-variant/10 pt-4 md:border-t-0 md:pt-0">
                    <RouterLink
                      :to="`/detail/${card.id}`"
                      class="inline-flex w-full items-center justify-center gap-2 rounded-full bg-teal-900 px-5 py-3 text-sm font-bold text-white transition-all hover:bg-teal-800 md:w-auto"
                    >
                      进入帖子
                      <span class="material-symbols-outlined text-base">arrow_forward</span>
                    </RouterLink>
                  </div>
                </div>
              </article>

              <div v-if="totalTopicPages > 1" class="flex flex-wrap items-center justify-between gap-3 pt-5">
                <p class="text-sm font-semibold text-on-surface-variant">
                  第 {{ topicCurrentPage }} / {{ totalTopicPages }} 页，共 {{ filteredTopicPosts.length }} 条
                </p>
                <div class="flex items-center gap-2">
                  <button type="button" class="rounded-full bg-surface-container-low px-4 py-2 text-sm font-bold text-teal-900 disabled:opacity-50" :disabled="topicCurrentPage === 1" @click="topicCurrentPage -= 1">
                    上一页
                  </button>
                  <button type="button" class="rounded-full bg-teal-900 px-4 py-2 text-sm font-bold text-white disabled:opacity-50" :disabled="topicCurrentPage === totalTopicPages" @click="topicCurrentPage += 1">
                    下一页
                  </button>
                </div>
              </div>
            </section>
          </template>
        </section>

        <aside class="space-y-6 self-start xl:sticky xl:top-24">
          <section class="rounded-[1.8rem] bg-[linear-gradient(135deg,rgba(15,60,68,0.98),rgba(13,93,104,0.92),rgba(102,183,172,0.82))] p-5 text-white shadow-sm">
            <div class="flex flex-wrap items-center gap-2">
              <span class="rounded-full border border-white/16 bg-white/10 px-3 py-1.5 text-[11px] font-bold uppercase tracking-[0.18em] text-white/76">Pinned</span>
              <span class="rounded-full bg-white/12 px-3 py-1.5 text-[11px] font-bold text-white/78">管理员发布</span>
            </div>
            <h2 class="mt-3 text-xl font-extrabold">社区公告</h2>
            <div class="mt-4 space-y-3">
              <article
                v-for="announcement in pinnedAnnouncements"
                :key="announcement.id"
                class="rounded-[1.25rem] border border-white/14 bg-white/10 px-4 py-4 backdrop-blur-sm"
              >
                <div class="flex flex-wrap items-center justify-between gap-3">
                  <h3 class="text-sm font-extrabold text-white">{{ announcement.title }}</h3>
                  <span class="text-[11px] font-semibold text-white/66">{{ formatAnnouncementTime(announcement.createdAt) }}</span>
                </div>
                <p class="mt-2 text-sm leading-6 text-white/76">{{ announcement.content }}</p>
              </article>
              <div v-if="pinnedAnnouncements.length === 0" class="rounded-[1.25rem] border border-white/14 bg-white/10 px-4 py-4 text-sm text-white/72">
                当前没有公告。
              </div>
            </div>
          </section>

          <section class="rounded-[1.8rem] border border-outline-variant/12 bg-surface-container-lowest p-5 shadow-sm">
            <div class="flex items-center justify-between gap-3">
              <div>
                <p class="text-xs font-bold uppercase tracking-[0.24em] text-teal-700/65">社区热榜</p>
                <h2 class="mt-2 text-xl font-extrabold text-teal-950">热度最高的话题帖</h2>
              </div>
              <span class="rounded-full bg-amber-100 px-3 py-1.5 text-[11px] font-bold uppercase tracking-[0.2em] text-amber-800">Top 10</span>
            </div>

            <div v-if="hotTopicRanking.length === 0" class="mt-6 rounded-2xl bg-surface-container-low px-4 py-5 text-sm text-on-surface-variant">
              还没有话题帖进入热榜。
            </div>

            <div v-else class="mt-5 space-y-2.5">
              <RouterLink
                v-for="(topic, index) in hotTopicRanking"
                :key="topic.id"
                :to="`/detail/${topic.id}`"
                class="flex items-start gap-3 rounded-[1.2rem] bg-surface-container-low px-3.5 py-3 transition-all hover:-translate-y-0.5 hover:bg-cyan-50/80"
              >
                <span
                  class="mt-0.5 inline-flex h-6 min-w-6 items-center justify-center rounded-full text-[11px] font-extrabold"
                  :class="index < 3 ? 'bg-teal-900 text-white' : 'bg-white text-teal-900'"
                >
                  {{ index + 1 }}
                </span>
                <span class="line-clamp-2 text-sm font-semibold leading-6 text-teal-950">{{ topic.title }}</span>
              </RouterLink>
            </div>
          </section>
        </aside>
      </div>
    </main>

    <RouterLink to="/publish" class="group fixed bottom-24 right-6 z-40 hidden h-14 w-14 items-center justify-center rounded-full bg-primary text-on-primary shadow-[0_12px_40px_rgba(0,52,57,0.15)] transition-all hover:scale-105 active:scale-95 md:flex">
      <span class="material-symbols-outlined text-3xl">add</span>
      <span class="absolute right-full mr-4 whitespace-nowrap rounded-xl bg-teal-900 px-4 py-2 text-sm font-headline text-white opacity-0 transition-opacity group-hover:opacity-100">发布</span>
    </RouterLink>

    <AppBottomNav />
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import AppBottomNav from '../components/AppBottomNav.vue'
import AppTopNav from '../components/AppTopNav.vue'
import { announcementApi, taskApi } from '../services/api'

const route = useRoute()
const router = useRouter()

const activeTab = ref<'demand' | 'topic'>('demand')
const tabBarRef = ref<HTMLElement | null>(null)

const syncTabFromRoute = () => {
  const tab = String(route.query.tab || '')
  activeTab.value = tab === 'topic' ? 'topic' : 'demand'
}

watch(() => route.query.tab, syncTabFromRoute)

const setTab = (tab: 'demand' | 'topic') => {
  if (activeTab.value === tab) return
  activeTab.value = tab
  const query = tab === 'topic' ? { tab: 'topic' } : {}
  router.replace({ path: '/home', query })
  topicCurrentPage.value = 1
}

const switchToTab = async (tab: 'demand' | 'topic') => {
  setTab(tab)
  await nextTick()
  tabBarRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const scrollToTabBar = async () => {
  await nextTick()
  tabBarRef.value?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const categories = ['全部任务', '跑腿代办', '学习辅导']
const demandPreviewCategories = ['跑腿代办', '学习辅导']
const topicPreviewCategories = ['二手闲置', '恋爱交友', '打听求助', '兼职招聘']
const topicCategories = ['全部话题', '二手闲置', '恋爱交友', '打听求助', '兼职招聘']
const recommendationModes = [
  { value: 'recommended' as const, label: '智能推荐', icon: 'auto_awesome' },
  { value: 'latest' as const, label: '最新发布', icon: 'schedule' }
]
const availableTimeOptions = [
  { value: 'now', label: '现在' },
  { value: 'today', label: '今天内' },
  { value: 'tomorrow', label: '明天' },
  { value: 'anytime', label: '不限时间' }
]
const activeCategory = ref('全部任务')
const selectedTopicEntryCategory = ref('二手闲置')
const selectedDemandEntryCategory = ref('跑腿代办')
const recommendationMode = ref<'recommended' | 'latest'>('recommended')
const selectedLocation = ref('')
const availableTime = ref('now')
const tasks = ref<any[]>([])
const announcements = ref<any[]>([])
const loading = ref(false)
const error = ref('')

const topicPosts = ref<any[]>([])
const topicLoading = ref(false)
const topicError = ref('')
const topicActiveCategory = ref('全部话题')
const topicKeyword = ref('')
const topicCurrentPage = ref(1)
const topicPageSize = 8

const pinnedAnnouncements = computed(() => announcements.value.filter((item) => item.pinned).slice(0, 3))
const hasActiveMatchingFilters = computed(() => (
  activeCategory.value !== '全部任务' || selectedLocation.value !== '' || availableTime.value !== 'now'
))

const iconForCategory = (category: string) => {
  const iconMap: Record<string, string> = {
    全部任务: 'dashboard',
    跑腿代办: 'directions_run',
    学习辅导: 'school'
  }
  return iconMap[category] || 'widgets'
}

const topicCategoryBadgeClass = (category: string) => {
  const map: Record<string, string> = {
    二手闲置: 'bg-amber-100 text-amber-800',
    恋爱交友: 'bg-rose-100 text-rose-700',
    打听求助: 'bg-sky-100 text-sky-700',
    兼职招聘: 'bg-emerald-100 text-emerald-700'
  }
  return map[category] || 'bg-slate-100 text-slate-700'
}

const mapTaskTypeToCategory = (task: any) => {
  if (task.category) return task.category
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
  return typeMap[task.impactText || task.badgeSecondary || task.taskType] || '跑腿代办'
}

const inferTaskMode = (task: any) => {
  const category = mapTaskTypeToCategory(task)
  const resolvedTaskMode = ['跑腿代办', '学习辅导'].includes(category) ? 'task' : 'topic'
  return task.taskMode === resolvedTaskMode ? task.taskMode : resolvedTaskMode
}

const mapTaskToCard = (task: any) => ({
  id: task.id,
  category: mapTaskTypeToCategory(task),
  taskMode: inferTaskMode(task),
  status: task.status,
  expiresAt: task.expiresAt,
  badgePrimary: task.badgePrimary || '普通',
  title: task.title,
  description: task.description,
  rewardText: task.rewardText || task.rewardTitle || '待补充',
  locationText: task.locationText,
  timeText: task.timeText,
  matchScore: Number.isFinite(Number(task.matchScore)) ? Number(task.matchScore) : null,
  matchReasons: Array.isArray(task.matchReasons) ? task.matchReasons.filter(Boolean) : [],
  recommendationMode: task.recommendationMode,
  likeCount: Number(task.likeCount || 0),
  commentCount: Number(task.commentCount || 0),
  publisher: task.requesterName || task.publisher || `用户 #${task.requesterId ?? ''}`
})

const mapTopicToCard = (task: any) => ({
  id: task.id,
  category: mapTaskTypeToCategory(task),
  taskMode: inferTaskMode(task),
  expiresAt: task.expiresAt,
  title: task.title,
  description: task.description,
  rewardText: task.rewardText || task.rewardTitle || '待补充',
  locationText: task.locationText,
  timeText: task.timeText,
  likeCount: Number(task.likeCount || 0),
  commentCount: Number(task.commentCount || 0),
  publisher: task.requesterName || task.publisher || `用户 #${task.requesterId ?? ''}`
})

const formatLocalDateTime = (date: Date) => {
  const pad = (value: number) => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

const resolveAvailableAt = () => {
  const now = new Date()
  if (availableTime.value === 'anytime') return undefined
  if (availableTime.value === 'today') {
    const todayEnd = new Date(now)
    todayEnd.setHours(23, 59, 0, 0)
    return formatLocalDateTime(todayEnd)
  }
  if (availableTime.value === 'tomorrow') {
    const tomorrowEnd = new Date(now)
    tomorrowEnd.setDate(tomorrowEnd.getDate() + 1)
    tomorrowEnd.setHours(23, 59, 0, 0)
    return formatLocalDateTime(tomorrowEnd)
  }
  return formatLocalDateTime(now)
}

const fetchTasks = async () => {
  loading.value = true
  error.value = ''
  try {
    const response = await taskApi.getTasks({
      mode: recommendationMode.value,
      category: activeCategory.value === '全部任务' ? undefined : activeCategory.value,
      location: selectedLocation.value || undefined,
      availableAt: resolveAvailableAt(),
      taskMode: 'task',
      size: 50
    }) as any
    const rawTasks = Array.isArray(response) ? response : Array.isArray(response?.data) ? response.data : []
    tasks.value = rawTasks.map(mapTaskToCard)
  } catch (err: any) {
    error.value = err?.response?.data?.message || '获取任务列表失败，请稍后重试'
    tasks.value = []
  } finally {
    loading.value = false
  }
}

const fetchAnnouncements = async () => {
  try {
    const response = await announcementApi.getAnnouncements() as any
    const rows = Array.isArray(response) ? response : Array.isArray(response?.data) ? response.data : []
    announcements.value = rows
  } catch (err) {
    console.error('获取公告失败:', err)
    announcements.value = []
  }
}

const fetchTopicPosts = async () => {
  topicLoading.value = true
  topicError.value = ''
  try {
    const response = await taskApi.getTasks({
      taskMode: 'topic',
      mode: 'latest',
      size: 200
    }) as any
    const rawTasks = Array.isArray(response) ? response : Array.isArray(response?.data) ? response.data : []
    topicPosts.value = rawTasks.map(mapTopicToCard)
  } catch (err: any) {
    topicError.value = err?.response?.data?.message || '获取话题帖失败，请稍后重试'
    topicPosts.value = []
  } finally {
    topicLoading.value = false
  }
}

const taskCards = computed(() => tasks.value)

const filteredCards = computed(() => {
  if (activeCategory.value === '全部任务') return taskCards.value
  return taskCards.value.filter((card) => card.category === activeCategory.value)
})

const filteredTopicPosts = computed(() => {
  const normalizedKeyword = topicKeyword.value.trim().toLowerCase()

  return topicPosts.value.filter((card: any) => {
    const categoryMatched = topicActiveCategory.value === '全部话题' || card.category === topicActiveCategory.value
    if (!categoryMatched) return false
    if (!normalizedKeyword) return true

    const searchText = [
      card.title,
      card.description,
      card.category,
      card.locationText,
      card.publisher
    ]
      .filter(Boolean)
      .join(' ')
      .toLowerCase()

    return searchText.includes(normalizedKeyword)
  })
})

const totalTopicPages = computed(() => Math.max(Math.ceil(filteredTopicPosts.value.length / topicPageSize), 1))

const pagedTopicPosts = computed(() => {
  const start = (topicCurrentPage.value - 1) * topicPageSize
  return filteredTopicPosts.value.slice(start, start + topicPageSize)
})

const topicEmptyTitle = computed(() => (
  topicKeyword.value ? '没有找到匹配的话题帖' : '这个分类还没有话题帖'
))

const topicEmptyDescription = computed(() => (
  topicKeyword.value
    ? '试试更换关键词、切换分类，或者直接发布一条新帖子。'
    : '可以切换别的分类，或者直接发布一条新帖子。'
))

const setRecommendationMode = (mode: 'recommended' | 'latest') => {
  recommendationMode.value = mode
  fetchTasks()
}

const setCategory = (category: string) => {
  activeCategory.value = category
  fetchTasks()
}

const setAvailableTime = (value: string) => {
  availableTime.value = value
  fetchTasks()
}

const clearMatchingFilters = () => {
  activeCategory.value = '全部任务'
  selectedLocation.value = ''
  availableTime.value = 'now'
  fetchTasks()
}

const setTopicCategory = (category: string) => {
  topicActiveCategory.value = category
  topicCurrentPage.value = 1
}

const clearTopicKeyword = () => {
  topicKeyword.value = ''
  topicCurrentPage.value = 1
}

const selectTopicCategory = async (category: string) => {
  selectedTopicEntryCategory.value = category
  topicActiveCategory.value = category
  await switchToTab('topic')
}

const selectDemandCategory = async (category: string) => {
  selectedDemandEntryCategory.value = category
  activeCategory.value = category
  await switchToTab('demand')
}

const hotTopicRanking = computed(() => (
  topicPosts.value
    .slice()
    .sort((a, b) => {
      const heatDiff = (Number(b.commentCount || 0) * 3 + Number(b.likeCount || 0) * 2)
        - (Number(a.commentCount || 0) * 3 + Number(a.likeCount || 0) * 2)
      if (heatDiff !== 0) return heatDiff
      return Number(b.id || 0) - Number(a.id || 0)
    })
    .slice(0, 10)
))

const taskStatusText = (card: any) => {
  if (card.status === 'accepted') return '任务进行中'
  return '等待接单中'
}

const taskStatusBadge = (status?: string) => {
  if (status === 'accepted') return '任务进行中'
  return '待接单'
}

const formatAnnouncementTime = (value?: string) => {
  if (!value) return '刚刚'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '刚刚'
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

watch(filteredTopicPosts, () => {
  if (topicCurrentPage.value > totalTopicPages.value) {
    topicCurrentPage.value = totalTopicPages.value
  }
})

onMounted(() => {
  syncTabFromRoute()
  Promise.all([
    fetchTasks(),
    fetchAnnouncements(),
    fetchTopicPosts()
  ])
})
</script>
