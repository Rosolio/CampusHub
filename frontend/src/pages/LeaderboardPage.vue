<template>
  <div class="min-h-screen bg-surface font-body text-on-surface">
    <main class="mx-auto max-w-3xl px-6 pb-24 pt-24">
      <!-- Header -->
      <div class="mb-8 text-center">
        <h1 class="text-4xl font-extrabold tracking-tight text-teal-950">积分排行榜</h1>
        <p class="mt-3 text-on-surface-variant">积极参与互助、发布话题帖、获得点赞都能累积积分，看看谁最活跃</p>
      </div>

      <!-- Loading -->
      <div v-if="loading" class="rounded-[2rem] bg-surface-container-lowest p-12 text-center text-on-surface-variant">
        <span class="material-symbols-outlined text-4xl animate-spin">progress_activity</span>
        <p class="mt-4 font-semibold">加载中...</p>
      </div>

      <!-- Error -->
      <div v-else-if="error" class="rounded-[2rem] bg-rose-50 p-12 text-center">
        <span class="material-symbols-outlined text-4xl text-rose-400">error</span>
        <p class="mt-4 font-semibold text-rose-700">{{ error }}</p>
        <button
          class="mt-4 rounded-full bg-rose-100 px-6 py-2 text-sm font-bold text-rose-700 hover:bg-rose-200"
          @click="fetchLeaderboard"
        >重试</button>
      </div>

      <!-- Empty -->
      <div v-else-if="users.length === 0" class="rounded-[2rem] bg-surface-container-lowest p-12 text-center">
        <span class="material-symbols-outlined text-6xl text-on-surface-variant/30">leaderboard</span>
        <p class="mt-4 text-lg font-semibold text-on-surface-variant">暂无排行数据</p>
      </div>

      <!-- Leaderboard -->
      <div v-else>
        <!-- Top 3 Podium -->
        <div class="mb-8 grid grid-cols-3 gap-4 items-end">
          <!-- 2nd Place -->
          <div v-if="users[1]" class="text-center">
            <div class="relative mx-auto mb-2 w-16 h-16">
              <img
                :src="users[1].avatarUrl || defaultAvatarUrl"
                :alt="users[1].name"
                class="h-16 w-16 rounded-full border-3 border-gray-300 object-cover"
              />
              <div class="absolute -bottom-2 left-1/2 -translate-x-1/2 text-xl z-10">🥈</div>
            </div>
            <p class="text-sm font-bold text-on-surface truncate">{{ users[1].name }}</p>
            <p class="text-xs text-on-surface-variant">{{ users[1].points }} 积分</p>
            <div class="mt-2 h-20 rounded-t-2xl bg-gradient-to-t from-gray-200 to-gray-100 flex items-center justify-center">
              <span class="inline-flex items-center justify-center w-10 h-10 rounded-full bg-white text-gray-400 text-xl font-extrabold shadow-sm">2</span>
            </div>
          </div>

          <!-- 1st Place -->
          <div v-if="users[0]" class="text-center">
            <div class="relative mx-auto mb-2 w-20 h-20">
              <div class="absolute -top-4 left-1/2 -translate-x-1/2 text-2xl z-10">👑</div>
              <img
                :src="users[0].avatarUrl || defaultAvatarUrl"
                :alt="users[0].name"
                class="h-20 w-20 rounded-full border-3 border-amber-400 object-cover"
              />
            </div>
            <p class="text-sm font-bold text-on-surface truncate">{{ users[0].name }}</p>
            <p class="text-xs text-on-surface-variant">{{ users[0].points }} 积分</p>
            <div class="mt-2 h-28 rounded-t-2xl bg-gradient-to-t from-amber-200 to-amber-100 flex items-center justify-center">
              <span class="inline-flex items-center justify-center w-12 h-12 rounded-full bg-white text-amber-400 text-2xl font-extrabold shadow-sm">1</span>
            </div>
          </div>

          <!-- 3rd Place -->
          <div v-if="users[2]" class="text-center">
            <div class="relative mx-auto mb-2 w-16 h-16">
              <img
                :src="users[2].avatarUrl || defaultAvatarUrl"
                :alt="users[2].name"
                class="h-16 w-16 rounded-full border-3 border-orange-300 object-cover"
              />
              <div class="absolute -bottom-2 left-1/2 -translate-x-1/2 text-xl z-10">🥉</div>
            </div>
            <p class="text-sm font-bold text-on-surface truncate">{{ users[2].name }}</p>
            <p class="text-xs text-on-surface-variant">{{ users[2].points }} 积分</p>
            <div class="mt-2 h-16 rounded-t-2xl bg-gradient-to-t from-orange-200 to-orange-100 flex items-center justify-center">
              <span class="inline-flex items-center justify-center w-10 h-10 rounded-full bg-white text-orange-300 text-xl font-extrabold shadow-sm">3</span>
            </div>
          </div>
        </div>

        <!-- Rank 4+ List -->
        <div v-if="users.length > 3" class="rounded-[2rem] bg-surface-container-lowest shadow-sm overflow-hidden">
          <div
            v-for="(user, idx) in users.slice(3)"
            :key="user.id"
            class="flex items-center gap-4 px-6 py-4 border-b border-outline-variant/10 last:border-b-0 transition-colors hover:bg-surface-container-low"
            :class="{ 'bg-teal-50/50': isCurrentUser(user.id) }"
          >
            <span class="w-8 text-center text-lg font-extrabold text-on-surface-variant/50">{{ idx + 4 }}</span>
            <img
              :src="user.avatarUrl || defaultAvatarUrl"
              :alt="user.name"
              class="h-10 w-10 rounded-full object-cover"
            />
            <div class="flex-1 min-w-0">
              <p class="font-bold text-on-surface truncate">
                {{ user.name }}
                <span v-if="isCurrentUser(user.id)" class="ml-1 text-xs font-semibold text-teal-700">(你)</span>
              </p>
              <div class="flex items-center gap-2 mt-0.5">
                <span class="text-xs text-on-surface-variant">信用 {{ formatScore(user.score) }}</span>
              </div>
            </div>
            <div class="text-right">
              <p class="text-lg font-extrabold text-teal-900">{{ user.points }}</p>
              <p class="text-[10px] font-semibold text-on-surface-variant/60 uppercase">积分</p>
            </div>
          </div>
        </div>

        <!-- Current user not in top N hint -->
        <div v-if="currentUserNotInList" class="mt-6 rounded-2xl bg-teal-50 px-5 py-4 text-center">
          <p class="text-sm text-teal-800">
            你的排名暂未进入前 {{ limit }} 名，当前积分 <span class="font-extrabold">{{ currentUserPoints }}</span>，继续参与互助提升排名吧
          </p>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { DEFAULT_AVATAR_URL } from '../constants/assets'
import { userApi } from '../services/api'
import { storedUser } from '../utils/auth'

interface LeaderboardUser {
  id: number
  name: string
  avatarUrl?: string
  points: number
  score: number
}

const defaultAvatarUrl = DEFAULT_AVATAR_URL
const limit = 20

const users = ref<LeaderboardUser[]>([])
const loading = ref(true)
const error = ref('')

const currentUser = computed(() => storedUser.value || {})

const currentUserNotInList = computed(() =>
  users.value.length > 0 && !users.value.some(u => Number(u.id) === Number(currentUser.value?.id))
)

const currentUserPoints = computed(() => currentUser.value?.points ?? 0)

const isCurrentUser = (userId: number) => Number(userId) === Number(currentUser.value?.id)

const formatScore = (score: number) => {
  if (score == null) return '0.00'
  return Number(score).toFixed(2)
}

const fetchLeaderboard = async () => {
  loading.value = true
  error.value = ''
  try {
    users.value = (await userApi.getLeaderboard(limit)) as LeaderboardUser[]
  } catch (e: any) {
    console.error('获取排行榜失败:', e)
    error.value = '排行榜加载失败，请稍后重试'
    users.value = []
  } finally {
    loading.value = false
  }
}

onMounted(fetchLeaderboard)
</script>
