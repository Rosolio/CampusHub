<template>
  <div class="space-y-5">
    <section v-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 px-5 py-4 text-sm font-semibold text-rose-700">
      {{ error }}
    </section>

    <section class="grid gap-4 md:grid-cols-3">
      <article v-for="card in workspaceCards" :key="card.label" class="admin-panel p-5">
        <div class="flex items-start justify-between gap-4">
          <div>
            <p class="admin-kicker">{{ card.label }}</p>
            <p class="mt-3 text-3xl font-extrabold tracking-tight text-slate-900">{{ card.value }}</p>
          </div>
          <div class="rounded-2xl bg-slate-100 p-3 text-slate-700">
            <span class="material-symbols-outlined text-[22px]">{{ card.icon }}</span>
          </div>
        </div>
        <p class="mt-3 text-sm leading-6 text-slate-600">{{ card.hint }}</p>
      </article>
    </section>

    <section class="admin-panel p-4 sm:p-5">
      <div class="flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between">
        <div class="flex flex-wrap gap-2">
          <RouterLink
            v-for="tab in workspaceTabs"
            :key="tab.value"
            :to="{ path: '/admin/community', query: { tab: tab.value }, hash: tab.hash }"
            class="inline-flex items-center gap-2 rounded-xl px-4 py-2 text-sm font-bold transition"
            :class="activeTab === tab.value
              ? 'bg-slate-950 text-white'
              : 'bg-slate-100 text-slate-600 hover:bg-slate-200 hover:text-slate-900'"
          >
            <span class="material-symbols-outlined text-lg">{{ tab.icon }}</span>
            {{ tab.label }}
          </RouterLink>
        </div>

        <button
          type="button"
          class="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-700 transition hover:bg-slate-50"
          @click="loadWorkspace"
        >
          <span class="material-symbols-outlined text-lg">refresh</span>
          刷新工作台
        </button>
      </div>
    </section>

    <section v-if="activeTab === 'announcements'" id="announcement-desk" class="grid gap-5 xl:grid-cols-[0.95fr_1.05fr]">
      <article class="admin-panel p-5 md:p-6">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p class="admin-kicker">社区公告</p>
            <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">发布社区公告</h2>
          </div>
          <span class="rounded-lg bg-amber-50 px-3 py-1.5 text-xs font-extrabold uppercase tracking-[0.18em] text-amber-700">
            首页可见
          </span>
        </div>

        <form class="mt-6 space-y-4" @submit.prevent="handleCreateAnnouncement">
          <input
            v-model.trim="announcementForm.title"
            type="text"
            class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-slate-900 outline-none transition focus:border-slate-400"
            placeholder="公告标题，例如：系统维护通知 / 社区规则更新"
          />
          <textarea
            v-model.trim="announcementForm.content"
            rows="6"
            class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-slate-900 outline-none transition focus:border-slate-400"
            placeholder="公告内容会展示在普通用户首页顶部。"
          ></textarea>
          <div class="flex flex-wrap items-center justify-between gap-3">
            <label class="inline-flex items-center gap-3 rounded-xl bg-slate-100 px-4 py-2 text-sm font-semibold text-slate-700">
              <input v-model="announcementForm.pinned" type="checkbox" class="h-4 w-4 accent-slate-900" />
              置顶公告
            </label>
            <button
              type="submit"
              class="rounded-xl bg-slate-950 px-5 py-3 text-sm font-extrabold text-white transition hover:bg-slate-800 disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="creatingAnnouncement"
            >
              {{ creatingAnnouncement ? '发布中...' : '发布公告' }}
            </button>
          </div>
        </form>
      </article>

      <article class="admin-panel p-5 md:p-6">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <p class="admin-kicker">最近公告</p>
            <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">公告记录</h2>
          </div>
          <span class="rounded-lg bg-slate-100 px-3 py-1.5 text-xs font-extrabold uppercase tracking-[0.18em] text-slate-600">
            最近 {{ announcementList.length }} 条
          </span>
        </div>

        <div class="mt-6 space-y-4">
          <article v-for="announcement in announcementList.slice(0, 6)" :key="announcement.id" class="admin-panel-soft p-4">
            <div class="flex flex-wrap items-center justify-between gap-3">
              <div class="flex items-center gap-2">
                <span v-if="announcement.pinned" class="rounded-full bg-amber-100 px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em] text-amber-800">置顶</span>
                <span class="text-xs font-semibold text-slate-500">{{ formatTime(announcement.createdAt) }}</span>
              </div>
              <span class="text-xs font-semibold text-slate-500">{{ announcement.authorName || '管理员' }}</span>
            </div>
            <h3 class="mt-3 text-lg font-extrabold text-slate-900">{{ announcement.title }}</h3>
            <p class="mt-2 text-sm leading-7 text-slate-600">{{ announcement.content }}</p>
          </article>
          <div v-if="announcementList.length === 0" class="admin-panel-soft px-4 py-5 text-sm text-slate-500">
            当前还没有公告记录。
          </div>
        </div>
      </article>
    </section>

    <section v-else-if="activeTab === 'feedback'" id="feedback-queue" class="admin-panel p-5 md:p-6">
      <div class="flex flex-col gap-4">
        <div class="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
          <div class="space-y-4">
            <div>
              <p class="admin-kicker">反馈队列</p>
              <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">用户反馈处理</h2>
            </div>

            <div class="flex flex-wrap gap-2">
              <button
                v-for="filter in feedbackFilters"
                :key="filter.value"
                type="button"
                class="rounded-xl px-4 py-2 text-sm font-bold transition"
                :class="feedbackStatusFilter === filter.value ? 'bg-slate-950 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200 hover:text-slate-900'"
                @click="feedbackStatusFilter = filter.value"
              >
                {{ filter.label }} {{ filter.count }}
              </button>
            </div>
          </div>

          <div class="rounded-2xl border border-slate-200 bg-white px-4 py-3 xl:w-80">
            <label class="block text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">搜索</label>
            <input
              v-model.trim="feedbackKeyword"
              type="text"
              class="mt-2 w-full border-0 bg-transparent text-sm font-semibold text-slate-800 outline-none placeholder:text-slate-400"
              placeholder="搜索标题、内容、用户或学号"
            />
          </div>
        </div>

        <div class="grid gap-3 md:grid-cols-2 xl:grid-cols-4">
          <div class="rounded-2xl border border-slate-200 bg-white px-4 py-3">
            <label class="block text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">类型</label>
            <select v-model="feedbackTypeFilter" class="mt-2 w-full bg-transparent text-sm font-semibold text-slate-800 outline-none">
              <option value="all">全部类型</option>
              <option value="BUG">功能异常</option>
              <option value="TASK_DISPUTE">任务纠纷</option>
              <option value="ACCOUNT_REPORT">账号举报</option>
              <option value="CONTENT_REPORT">内容举报</option>
              <option value="SUGGESTION">功能建议</option>
              <option value="OTHER">其他反馈</option>
            </select>
          </div>

          <div class="rounded-2xl border border-slate-200 bg-white px-4 py-3">
            <label class="block text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">优先级</label>
            <select v-model="feedbackPriorityFilter" class="mt-2 w-full bg-transparent text-sm font-semibold text-slate-800 outline-none">
              <option value="all">全部优先级</option>
              <option value="LOW">低优先级</option>
              <option value="NORMAL">普通</option>
              <option value="HIGH">高优先级</option>
              <option value="URGENT">紧急</option>
            </select>
          </div>
        </div>
      </div>

      <div class="mt-6 space-y-4">
        <article v-for="item in filteredFeedbackList" :key="item.id" class="admin-panel-soft p-5">
          <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
            <div>
              <div class="flex flex-wrap items-center gap-2">
                <span class="rounded-full px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em]" :class="feedbackTypeClass(item.type)">
                  {{ feedbackTypeLabel(item.type) }}
                </span>
                <span class="rounded-full px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em]" :class="feedbackPriorityClass(item.priority)">
                  {{ feedbackPriorityLabel(item.priority) }}
                </span>
                <span class="rounded-full px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em]" :class="feedbackStatusClass(item.status)">
                  {{ feedbackStatusLabel(item.status) }}
                </span>
              </div>
              <h3 class="mt-3 text-lg font-extrabold text-slate-900">{{ item.title }}</h3>
              <p class="mt-2 text-sm text-slate-500">{{ item.userName || '匿名用户' }} / {{ item.userStudentId || '未知学号' }}</p>
            </div>
            <div class="text-right text-xs font-semibold text-slate-500">
              <p>{{ formatTime(item.createdAt) }}</p>
              <p v-if="item.handledAt" class="mt-1">处理于 {{ formatTime(item.handledAt) }}</p>
            </div>
          </div>

          <p class="mt-4 text-sm leading-7 text-slate-700">{{ item.content }}</p>

          <div class="mt-4 grid gap-3 md:grid-cols-[14rem_1fr]">
            <div class="rounded-2xl border border-slate-200 bg-white px-4 py-3">
              <label class="block text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">调整优先级</label>
              <select v-model="feedbackPriorities[item.id]" class="mt-2 w-full bg-transparent text-sm font-semibold text-slate-800 outline-none">
                <option value="LOW">低优先级</option>
                <option value="NORMAL">普通</option>
                <option value="HIGH">高优先级</option>
                <option value="URGENT">紧急</option>
              </select>
            </div>

            <textarea
              v-model.trim="feedbackReplies[item.id]"
              rows="3"
              class="w-full rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-slate-400"
              placeholder="填写给用户的回复。现在结案前必须写清楚处理说明。"
            ></textarea>
          </div>

          <div class="mt-4 flex flex-wrap gap-2">
            <button
              type="button"
              class="rounded-xl bg-slate-100 px-4 py-2 text-sm font-bold text-slate-700 transition hover:bg-slate-200"
              :disabled="updatingFeedbackId === item.id"
              @click="handleUpdateFeedback(item, 'open')"
            >
              退回待处理
            </button>
            <button
              type="button"
              class="rounded-xl bg-amber-100 px-4 py-2 text-sm font-bold text-amber-800 transition hover:bg-amber-200"
              :disabled="updatingFeedbackId === item.id"
              @click="handleUpdateFeedback(item, 'in_progress')"
            >
              标记处理中
            </button>
            <button
              type="button"
              class="rounded-xl bg-emerald-100 px-4 py-2 text-sm font-bold text-emerald-800 transition hover:bg-emerald-200"
              :disabled="updatingFeedbackId === item.id"
              @click="handleUpdateFeedback(item, 'resolved')"
            >
              回复并结案
            </button>
          </div>

          <div v-if="item.adminReply" class="mt-4 rounded-2xl bg-white px-4 py-3 text-sm text-slate-700">
            <div class="flex flex-wrap items-center justify-between gap-2">
              <p class="font-bold text-slate-900">当前回复</p>
              <span class="text-xs font-semibold text-slate-500">{{ item.adminName || '管理员' }}</span>
            </div>
            <p class="mt-2 leading-7 text-slate-600">{{ item.adminReply }}</p>
          </div>
        </article>

        <div v-if="filteredFeedbackList.length === 0" class="admin-panel-soft px-4 py-5 text-sm text-slate-500">
          当前没有匹配的反馈记录。
        </div>
      </div>
    </section>

    <section v-else class="grid gap-5 xl:grid-cols-[1.05fr_0.95fr]">
      <article class="admin-panel p-5 md:p-6">
        <div class="flex flex-col gap-4 xl:flex-row xl:items-end xl:justify-between">
          <div class="space-y-4">
            <div>
              <p class="admin-kicker">社区内容</p>
              <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">社区内容工作台</h2>
            </div>

            <div class="flex flex-wrap gap-2">
              <button
                v-for="filter in contentFilters"
                :key="filter.value"
                type="button"
                class="rounded-xl px-4 py-2 text-sm font-bold transition"
                :class="contentStatusFilter === filter.value ? 'bg-slate-950 text-white' : 'bg-slate-100 text-slate-600 hover:bg-slate-200 hover:text-slate-900'"
                @click="contentStatusFilter = filter.value"
              >
                {{ filter.label }} {{ filter.count }}
              </button>
            </div>
          </div>

          <div class="grid gap-3 sm:grid-cols-2 xl:w-[30rem]">
            <div class="rounded-2xl border border-slate-200 bg-white px-4 py-3">
              <label class="block text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">类型</label>
              <select v-model="contentModeFilter" class="mt-2 w-full bg-transparent text-sm font-semibold text-slate-800 outline-none">
                <option value="all">全部类型</option>
                <option value="task">任务帖</option>
                <option value="topic">话题帖</option>
              </select>
            </div>
            <div class="rounded-2xl border border-slate-200 bg-white px-4 py-3">
              <label class="block text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">搜索</label>
              <input
                v-model.trim="contentKeyword"
                type="text"
                class="mt-2 w-full border-0 bg-transparent text-sm font-semibold text-slate-800 outline-none placeholder:text-slate-400"
                placeholder="搜索标题、分类或发布者"
              />
            </div>
          </div>
        </div>

        <div class="mt-6 grid gap-4 xl:grid-cols-2">
          <article v-for="item in filteredContentList" :key="item.id" class="admin-panel-soft p-5">
            <div class="flex flex-wrap items-center gap-2">
              <span class="rounded-full bg-white px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em] text-slate-800">
                {{ item.taskMode === 'topic' ? '话题帖' : '任务帖' }}
              </span>
              <span class="rounded-full bg-white px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em] text-slate-500">
                {{ item.category || '未分类' }}
              </span>
              <span class="rounded-full px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em]" :class="reviewBadgeClass(item.reviewStatus)">
                {{ reviewLabel(item.reviewStatus) }}
              </span>
            </div>

            <h3 class="mt-4 text-xl font-extrabold text-slate-900">{{ item.title }}</h3>
            <p class="mt-3 line-clamp-4 text-sm leading-7 text-slate-600">{{ item.description }}</p>

            <div class="mt-5 grid gap-3 sm:grid-cols-2">
              <div class="rounded-2xl bg-white px-4 py-3">
                <p class="text-[11px] font-extrabold uppercase tracking-[0.18em] text-slate-500">发布者</p>
                <p class="mt-2 text-sm font-bold text-slate-800">{{ item.requesterName || `用户 #${item.requesterId}` }}</p>
              </div>
              <div class="rounded-2xl bg-white px-4 py-3">
                <p class="text-[11px] font-extrabold uppercase tracking-[0.18em] text-slate-500">发布时间</p>
                <p class="mt-2 text-sm font-bold text-slate-800">{{ formatTime(item.createdAt) }}</p>
              </div>
            </div>

            <p v-if="item.reviewNote" class="mt-4 rounded-2xl bg-amber-50 px-4 py-3 text-sm font-semibold text-amber-700">
              审核备注：{{ item.reviewNote }}
            </p>
          </article>

          <div v-if="filteredContentList.length === 0" class="admin-panel-soft px-4 py-5 text-sm text-slate-500 xl:col-span-2">
            当前没有匹配的社区内容。
          </div>
        </div>
      </article>

      <div class="space-y-5">
        <article class="admin-panel p-5 md:p-6">
          <div class="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p class="admin-kicker">公告概览</p>
              <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">公告摘要</h2>
            </div>
            <RouterLink
              :to="{ path: '/admin/community', query: { tab: 'announcements' }, hash: '#announcement-desk' }"
              class="text-sm font-bold text-slate-700 transition hover:text-slate-950"
            >
              管理公告
            </RouterLink>
          </div>

          <div class="mt-6 space-y-3">
            <article v-for="announcement in announcementList.slice(0, 3)" :key="announcement.id" class="admin-panel-soft p-4">
              <div class="flex flex-wrap items-center justify-between gap-2">
                <span v-if="announcement.pinned" class="rounded-full bg-amber-100 px-3 py-1 text-[11px] font-extrabold uppercase tracking-[0.18em] text-amber-800">置顶</span>
                <span class="text-xs font-semibold text-slate-500">{{ formatTime(announcement.createdAt) }}</span>
              </div>
              <h3 class="mt-3 text-base font-extrabold text-slate-900">{{ announcement.title }}</h3>
              <p class="mt-2 text-sm leading-6 text-slate-600">{{ announcement.content }}</p>
            </article>
            <div v-if="announcementList.length === 0" class="admin-panel-soft px-4 py-5 text-sm text-slate-500">
              当前没有公告。
            </div>
          </div>
        </article>

        <article class="admin-panel p-5 md:p-6">
          <div class="flex flex-wrap items-center justify-between gap-3">
            <div>
              <p class="admin-kicker">队列概览</p>
              <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">工作量概览</h2>
            </div>
            <RouterLink
              :to="{ path: '/admin/community', query: { tab: 'feedback' }, hash: '#feedback-queue' }"
              class="text-sm font-bold text-slate-700 transition hover:text-slate-950"
            >
              进入处理
            </RouterLink>
          </div>

          <div class="mt-6 space-y-3">
            <div class="admin-panel-soft flex items-center justify-between px-4 py-4">
              <div>
                <p class="text-sm font-extrabold text-slate-900">待处理反馈</p>
                <p class="mt-1 text-sm text-slate-500">优先处理高优先级和紧急事项。</p>
              </div>
              <span class="text-2xl font-extrabold tracking-tight text-rose-700">{{ pendingFeedbackCount }}</span>
            </div>
            <div class="admin-panel-soft flex items-center justify-between px-4 py-4">
              <div>
                <p class="text-sm font-extrabold text-slate-900">待审核内容</p>
                <p class="mt-1 text-sm text-slate-500">统计仍在等待人工审核的帖子。</p>
              </div>
              <span class="text-2xl font-extrabold tracking-tight text-slate-900">{{ pendingReviewCount }}</span>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { showToast } from '../../composables/useToast'
import { adminApi } from '../../services/api'
import type { AdminTask } from './adminTypes'

type WorkspaceTab = 'content' | 'announcements' | 'feedback'
type FeedbackStatusFilter = 'all' | 'open' | 'in_progress' | 'resolved'
type FeedbackTypeFilter = 'all' | 'BUG' | 'SUGGESTION' | 'TASK_DISPUTE' | 'ACCOUNT_REPORT' | 'CONTENT_REPORT' | 'OTHER'
type FeedbackPriority = 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'
type FeedbackPriorityFilter = 'all' | FeedbackPriority
type ContentStatusFilter = 'all' | 'pending_review' | 'approved' | 'rejected'
type ContentModeFilter = 'all' | 'task' | 'topic'

const route = useRoute()
const error = ref('')
const dashboard = ref<any>({ overview: {} })
const announcementList = ref<any[]>([])
const feedbackList = ref<any[]>([])
const contentList = ref<AdminTask[]>([])
const creatingAnnouncement = ref(false)
const updatingFeedbackId = ref<number | null>(null)
const feedbackReplies = ref<Record<number, string>>({})
const feedbackPriorities = ref<Record<number, FeedbackPriority>>({})
const feedbackKeyword = ref('')
const feedbackStatusFilter = ref<FeedbackStatusFilter>('all')
const feedbackTypeFilter = ref<FeedbackTypeFilter>('all')
const feedbackPriorityFilter = ref<FeedbackPriorityFilter>('all')
const contentKeyword = ref('')
const contentStatusFilter = ref<ContentStatusFilter>('all')
const contentModeFilter = ref<ContentModeFilter>('all')

const announcementForm = ref({
  title: '',
  content: '',
  pinned: true
})

const workspaceTabs: Array<{ value: WorkspaceTab; label: string; icon: string; hash: string }> = [
  { value: 'content', label: '社区内容', icon: 'newspaper', hash: '' },
  { value: 'announcements', label: '公告发布', icon: 'campaign', hash: '#announcement-desk' },
  { value: 'feedback', label: '反馈处理', icon: 'reviews', hash: '#feedback-queue' }
]

const activeTab = computed<WorkspaceTab>(() => {
  const value = String(route.query.tab || 'content')
  return value === 'announcements' || value === 'feedback' ? value : 'content'
})

const pendingFeedbackCount = computed(() => feedbackList.value.filter((item) => item.status !== 'resolved').length)
const pendingReviewCount = computed(() => contentList.value.filter((item) => item.reviewStatus === 'pending_review').length)

const workspaceCards = computed(() => [
  { label: '置顶公告', value: announcementList.value.filter((item) => item.pinned).length, hint: '置顶公告会展示在普通用户首页顶部。', icon: 'campaign' },
  { label: '待处理反馈', value: pendingFeedbackCount.value, hint: '在这里闭环处理 Bug、争议、举报和建议。', icon: 'reviews' },
  { label: '今日订单', value: dashboard.value?.overview?.todayOrderCount ?? 0, hint: '用于感知今天的整体运营节奏。', icon: 'inventory_2' }
])

const feedbackFilters = computed(() => [
  { value: 'all' as FeedbackStatusFilter, label: '全部', count: feedbackList.value.length },
  { value: 'open' as FeedbackStatusFilter, label: '待处理', count: feedbackList.value.filter((item) => item.status === 'open').length },
  { value: 'in_progress' as FeedbackStatusFilter, label: '处理中', count: feedbackList.value.filter((item) => item.status === 'in_progress').length },
  { value: 'resolved' as FeedbackStatusFilter, label: '已解决', count: feedbackList.value.filter((item) => item.status === 'resolved').length }
])

const contentFilters = computed(() => [
  { value: 'all' as ContentStatusFilter, label: '全部', count: contentList.value.length },
  { value: 'pending_review' as ContentStatusFilter, label: '待审核', count: contentList.value.filter((item) => item.reviewStatus === 'pending_review').length },
  { value: 'approved' as ContentStatusFilter, label: '已通过', count: contentList.value.filter((item) => item.reviewStatus === 'approved').length },
  { value: 'rejected' as ContentStatusFilter, label: '已驳回', count: contentList.value.filter((item) => item.reviewStatus === 'rejected').length }
])

const filteredFeedbackList = computed(() => {
  const query = feedbackKeyword.value.toLowerCase()
  return feedbackList.value.filter((item) => {
    const matchesStatus = feedbackStatusFilter.value === 'all' || item.status === feedbackStatusFilter.value
    const matchesType = feedbackTypeFilter.value === 'all' || item.type === feedbackTypeFilter.value
    const matchesPriority = feedbackPriorityFilter.value === 'all' || item.priority === feedbackPriorityFilter.value
    if (!matchesStatus || !matchesType || !matchesPriority) {
      return false
    }

    if (!query) {
      return true
    }

    return [
      item.title,
      item.content,
      item.userName,
      item.userStudentId,
      item.adminReply
    ].some((value) => String(value || '').toLowerCase().includes(query))
  })
})

const filteredContentList = computed(() => {
  const query = contentKeyword.value.toLowerCase()
  return contentList.value.filter((item) => {
    const matchesStatus = contentStatusFilter.value === 'all' || item.reviewStatus === contentStatusFilter.value
    const matchesMode = contentModeFilter.value === 'all' || item.taskMode === contentModeFilter.value
    if (!matchesStatus || !matchesMode) {
      return false
    }

    if (!query) {
      return true
    }

    return [
      item.title,
      item.description,
      item.category,
      item.requesterName,
      item.reviewNote
    ].some((value) => String(value || '').toLowerCase().includes(query))
  })
})

const normalizeList = (response: any) => Array.isArray(response) ? response : Array.isArray(response?.data) ? response.data : []

const normalizePriority = (value?: string): FeedbackPriority => {
  if (value === 'LOW' || value === 'HIGH' || value === 'URGENT') {
    return value
  }
  return 'NORMAL'
}

const loadWorkspace = async () => {
  error.value = ''
  try {
    const [dashboardResponse, announcementResponse, feedbackResponse, tasksResponse] = await Promise.all([
      adminApi.getDashboard(),
      adminApi.getAnnouncements(),
      adminApi.getFeedback(),
      adminApi.getTasks()
    ]) as [any, any, any, any]

    dashboard.value = dashboardResponse || { overview: {} }
    announcementList.value = normalizeList(announcementResponse)
    feedbackList.value = normalizeList(feedbackResponse)
    contentList.value = normalizeList(tasksResponse)
    feedbackReplies.value = feedbackList.value.reduce((acc: Record<number, string>, item: any) => {
      acc[item.id] = feedbackReplies.value[item.id] ?? item.adminReply ?? ''
      return acc
    }, {})
    feedbackPriorities.value = feedbackList.value.reduce((acc: Record<number, FeedbackPriority>, item: any) => {
      acc[item.id] = feedbackPriorities.value[item.id] ?? normalizePriority(item.priority)
      return acc
    }, {})
  } catch (err: any) {
    error.value = err?.response?.data?.message || '管理后台工作台加载失败'
  }
}

const scrollToHashIfNeeded = async () => {
  if (!route.hash) return
  await nextTick()
  document.querySelector(route.hash)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

const handleCreateAnnouncement = async () => {
  if (!announcementForm.value.title || !announcementForm.value.content) {
    error.value = '公告标题和内容不能为空'
    return
  }

  creatingAnnouncement.value = true
  error.value = ''
  try {
    await adminApi.createAnnouncement({
      title: announcementForm.value.title,
      content: announcementForm.value.content,
      pinned: announcementForm.value.pinned
    })
    announcementForm.value = { title: '', content: '', pinned: true }
    showToast('公告已发布', 'success')
    await loadWorkspace()
  } catch (err: any) {
    error.value = err?.response?.data?.message || '公告发布失败'
  } finally {
    creatingAnnouncement.value = false
  }
}

const handleUpdateFeedback = async (item: any, status: 'open' | 'in_progress' | 'resolved') => {
  updatingFeedbackId.value = item.id
  error.value = ''
  try {
    await adminApi.updateFeedback(item.id, {
      status,
      priority: normalizePriority(feedbackPriorities.value[item.id]),
      adminReply: feedbackReplies.value[item.id]
    })
    showToast(status === 'resolved' ? '反馈已结案' : '反馈状态已更新', 'success')
    await loadWorkspace()
  } catch (err: any) {
    error.value = err?.response?.data?.message || '反馈处理失败'
  } finally {
    updatingFeedbackId.value = null
  }
}

const formatTime = (value?: string) => {
  if (!value) return '刚刚'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return '刚刚'
  return `${date.getMonth() + 1}/${date.getDate()} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const feedbackTypeLabel = (value?: string) => {
  if (value === 'BUG') return '功能异常'
  if (value === 'TASK_DISPUTE') return '任务纠纷'
  if (value === 'ACCOUNT_REPORT') return '账号举报'
  if (value === 'CONTENT_REPORT') return '内容举报'
  if (value === 'SUGGESTION') return '功能建议'
  return '其他反馈'
}

const feedbackTypeClass = (value?: string) => {
  if (value === 'BUG') return 'bg-rose-100 text-rose-700'
  if (value === 'TASK_DISPUTE') return 'bg-amber-100 text-amber-800'
  if (value === 'ACCOUNT_REPORT') return 'bg-fuchsia-100 text-fuchsia-700'
  if (value === 'CONTENT_REPORT') return 'bg-orange-100 text-orange-700'
  if (value === 'SUGGESTION') return 'bg-cyan-100 text-cyan-800'
  return 'bg-slate-100 text-slate-700'
}

const feedbackPriorityLabel = (value?: string) => {
  if (value === 'LOW') return '低优先级'
  if (value === 'HIGH') return '高优先级'
  if (value === 'URGENT') return '紧急'
  return '普通'
}

const feedbackPriorityClass = (value?: string) => {
  if (value === 'LOW') return 'bg-slate-100 text-slate-600'
  if (value === 'HIGH') return 'bg-amber-100 text-amber-800'
  if (value === 'URGENT') return 'bg-rose-100 text-rose-700'
  return 'bg-sky-100 text-sky-700'
}

const feedbackStatusLabel = (value?: string) => {
  if (value === 'resolved') return '已解决'
  if (value === 'in_progress') return '处理中'
  return '待处理'
}

const feedbackStatusClass = (value?: string) => {
  if (value === 'resolved') return 'bg-emerald-100 text-emerald-700'
  if (value === 'in_progress') return 'bg-amber-100 text-amber-800'
  return 'bg-slate-100 text-slate-700'
}

const reviewLabel = (value?: string) => {
  if (value === 'approved') return '已通过'
  if (value === 'rejected') return '已驳回'
  return '待审核'
}

const reviewBadgeClass = (value?: string) => {
  if (value === 'approved') return 'bg-emerald-100 text-emerald-700'
  if (value === 'rejected') return 'bg-rose-100 text-rose-700'
  return 'bg-amber-100 text-amber-800'
}

watch(() => route.fullPath, () => {
  scrollToHashIfNeeded()
})

onMounted(async () => {
  await loadWorkspace()
  scrollToHashIfNeeded()
})
</script>
