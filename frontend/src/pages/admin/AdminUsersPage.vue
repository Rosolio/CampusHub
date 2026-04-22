<template>
  <div class="space-y-6">
    <section v-if="error" class="rounded-[1.5rem] border border-rose-200 bg-rose-50 px-5 py-4 text-sm font-semibold text-rose-700">
      {{ error }}
    </section>

    <section class="rounded-[1.9rem] border border-[#ddd6c9] bg-[#fffdf8] p-5 shadow-[0_16px_48px_rgba(15,23,42,0.05)] md:p-6">
      <div class="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-[11px] font-extrabold uppercase tracking-[0.24em] text-slate-500">User Actions</p>
          <h2 class="mt-2 text-2xl font-extrabold tracking-tight text-slate-900">用户列表</h2>
        </div>
        <div class="flex flex-wrap gap-3">
          <div class="rounded-full bg-[#f3ede0] px-4 py-2 text-sm font-bold text-slate-700">
            共 {{ filteredUsers.length }} 个结果
          </div>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-full bg-[#102a33] px-4 py-2 text-sm font-extrabold text-white transition hover:bg-[#163a46]"
            @click="loadUsers"
          >
            <span class="material-symbols-outlined text-lg">refresh</span>
            刷新
          </button>
        </div>
      </div>

      <div class="mt-6 rounded-[1.35rem] border border-[#ece5d8] bg-[#faf7f0] p-4">
        <label class="block text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">学号搜索</label>
        <div class="mt-3 flex items-center gap-3 rounded-full bg-white px-4 py-3 shadow-[inset_0_1px_0_rgba(15,23,42,0.04)]">
          <span class="material-symbols-outlined text-slate-400">person_search</span>
          <input
            v-model.trim="studentIdQuery"
            type="text"
            class="w-full border-0 bg-transparent text-sm font-semibold text-slate-800 outline-none placeholder:text-slate-400"
            placeholder="输入学号，也支持姓名/邮箱模糊搜索"
          />
        </div>
      </div>

      <div class="mt-6 overflow-hidden rounded-[1.5rem] border border-[#e7dfd2] bg-[#faf7f0]">
        <div class="hidden grid-cols-[1.1fr_1fr_1fr_0.8fr_1.3fr] gap-4 border-b border-[#e7dfd2] bg-[#f2ebdf] px-5 py-4 text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500 lg:grid">
          <span>用户</span>
          <span>学号 / 邮箱</span>
          <span>专业</span>
          <span>状态</span>
          <span>操作</span>
        </div>

        <div v-if="paginatedUsers.length === 0" class="px-5 py-10 text-center text-sm font-medium text-slate-500">
          没有匹配的用户。
        </div>

        <div v-else>
          <div
            v-for="user in paginatedUsers"
            :key="user.id"
            class="border-b border-[#ece5d8] px-5 py-4 last:border-b-0"
          >
            <div class="grid gap-4 lg:grid-cols-[1.1fr_1fr_1fr_0.8fr_1.3fr] lg:items-center">
              <div class="min-w-0">
                <div class="flex flex-wrap items-center gap-2">
                  <p class="text-sm font-extrabold text-slate-900">{{ user.name }}</p>
                  <span v-if="user.role === 'ADMIN'" class="rounded-full bg-[#13212b] px-2.5 py-1 text-[10px] font-extrabold uppercase tracking-[0.14em] text-white">
                    Admin
                  </span>
                </div>
                <p v-if="user.disabledReason" class="mt-1 text-xs font-semibold text-rose-700">禁用原因：{{ user.disabledReason }}</p>
              </div>

              <div class="min-w-0 text-sm text-slate-600">
                <p class="font-bold text-slate-800">{{ user.studentId }}</p>
                <p class="truncate">{{ user.email }}</p>
              </div>

              <div class="text-sm font-medium text-slate-600">
                {{ user.major || '未填写专业' }}
              </div>

              <div>
                <span
                  class="rounded-full px-3 py-1 text-xs font-extrabold uppercase tracking-[0.14em]"
                  :class="user.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-700'"
                >
                  {{ userStatusLabel(user.status) }}
                </span>
              </div>

              <div class="flex flex-wrap gap-2">
                <button
                  v-if="user.role !== 'ADMIN'"
                  type="button"
                  class="rounded-full bg-[#0f766e] px-3 py-2 text-xs font-extrabold text-white transition hover:bg-[#0b5f59] disabled:opacity-45"
                  :disabled="pendingUserIds.has(user.id) || user.status === 'ACTIVE'"
                  @click="changeUserStatus(user, 'ACTIVE')"
                >
                  启用
                </button>
                <button
                  v-if="user.role !== 'ADMIN'"
                  type="button"
                  class="rounded-full bg-[#c2410c] px-3 py-2 text-xs font-extrabold text-white transition hover:bg-[#9a3412] disabled:opacity-45"
                  :disabled="pendingUserIds.has(user.id) || user.status === 'DISABLED'"
                  @click="changeUserStatus(user, 'DISABLED')"
                >
                  禁用
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <p class="text-sm font-semibold text-slate-500">
          第 {{ currentPage }} / {{ totalPages }} 页，每页 20 条
        </p>
        <div class="flex flex-wrap gap-2">
          <button
            type="button"
            class="rounded-full border border-[#d5cdbf] bg-white px-4 py-2 text-sm font-extrabold text-slate-700 transition hover:bg-[#f8f4eb] disabled:opacity-45"
            :disabled="currentPage === 1"
            @click="currentPage = Math.max(1, currentPage - 1)"
          >
            上一页
          </button>
          <button
            type="button"
            class="rounded-full border border-[#d5cdbf] bg-white px-4 py-2 text-sm font-extrabold text-slate-700 transition hover:bg-[#f8f4eb] disabled:opacity-45"
            :disabled="currentPage === totalPages"
            @click="currentPage = Math.min(totalPages, currentPage + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { adminApi } from '../../services/api'
import type { AdminUser } from './adminTypes'

const error = ref('')
const users = ref<AdminUser[]>([])
const pendingUserIds = ref(new Set<number>())
const studentIdQuery = ref('')
const currentPage = ref(1)
const pageSize = 20

const filteredUsers = computed(() => {
  const query = studentIdQuery.value.toLowerCase()
  if (!query) {
    return users.value
  }

  return users.value.filter((user) => [
    user.studentId,
    user.name,
    user.email
  ].some((value) => String(value || '').toLowerCase().includes(query)))
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredUsers.value.length / pageSize)))
const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  return filteredUsers.value.slice(start, start + pageSize)
})

const loadUsers = async () => {
  error.value = ''
  try {
    users.value = await adminApi.getUsers() as unknown as AdminUser[]
  } catch (err: any) {
    error.value = err?.response?.data?.message || '用户数据加载失败'
  }
}

const changeUserStatus = async (user: AdminUser, status: 'ACTIVE' | 'DISABLED') => {
  const reason = status === 'DISABLED'
    ? window.prompt('请输入禁用原因', user.disabledReason || '违规内容发布') || ''
    : ''

  pendingUserIds.value.add(user.id)
  try {
    await adminApi.updateUserStatus(user.id, { status, disabledReason: reason })
    await loadUsers()
  } catch (err: any) {
    error.value = err?.response?.data?.message || '用户状态更新失败'
  } finally {
    pendingUserIds.value.delete(user.id)
  }
}

const userStatusLabel = (value: string) => value === 'DISABLED' ? '已禁用' : '正常'

watch(studentIdQuery, () => {
  currentPage.value = 1
})

watch(totalPages, (value) => {
  if (currentPage.value > value) {
    currentPage.value = value
  }
})

onMounted(() => {
  loadUsers()
})
</script>
