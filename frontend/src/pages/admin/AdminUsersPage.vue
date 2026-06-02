<template>
  <div class="space-y-5">
    <section v-if="error" class="rounded-2xl border border-rose-200 bg-rose-50 px-5 py-4 text-sm font-semibold text-rose-700">
      {{ error }}
    </section>

    <section class="admin-panel p-5 md:p-6">
      <div class="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="admin-kicker">Users</p>
          <h2 class="mt-2 text-xl font-extrabold tracking-tight text-slate-900">用户列表</h2>
        </div>
        <div class="flex flex-wrap gap-3">
          <div class="rounded-xl bg-slate-100 px-4 py-2 text-sm font-bold text-slate-700">
            共 {{ filteredUsers.length }} 个结果
          </div>
          <button
            type="button"
            class="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-bold text-slate-700 transition hover:bg-slate-50"
            @click="loadUsers"
          >
            <span class="material-symbols-outlined text-lg">refresh</span>
            刷新
          </button>
        </div>
      </div>

      <div class="admin-panel-soft mt-6 p-4">
        <label class="block text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">学号搜索</label>
        <div class="mt-3 flex items-center gap-3 rounded-2xl border border-slate-200 bg-white px-4 py-3">
          <span class="material-symbols-outlined text-slate-400">person_search</span>
          <input
            v-model.trim="studentIdQuery"
            type="text"
            class="w-full border-0 bg-transparent text-sm font-semibold text-slate-800 outline-none placeholder:text-slate-400"
            placeholder="输入学号，也支持姓名/邮箱模糊搜索"
          />
        </div>
      </div>

      <div class="mt-6 overflow-x-auto rounded-2xl border border-slate-200 bg-white">
        <table class="w-full min-w-[700px]">
          <thead>
            <tr class="border-b border-slate-200 bg-slate-50 text-left text-[11px] font-extrabold uppercase tracking-[0.2em] text-slate-500">
              <th class="px-5 py-3 w-[20%]">用户</th>
              <th class="px-5 py-3 w-[20%]">学号 / 邮箱</th>
              <th class="px-5 py-3 w-[12%]">专业</th>
              <th class="px-5 py-3 w-[10%]">状态</th>
              <th class="px-5 py-3 w-[18%]">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="paginatedUsers.length === 0">
              <td colspan="5" class="px-5 py-10 text-center text-sm font-medium text-slate-500">
                没有匹配的用户。
              </td>
            </tr>
            <tr
              v-for="user in paginatedUsers"
              :key="user.id"
              class="border-b border-slate-100 last:border-b-0 hover:bg-slate-50/50 transition-colors"
            >
              <td class="px-5 py-3.5">
                <div class="flex items-center gap-2">
                  <span class="text-sm font-extrabold text-slate-900">{{ user.name }}</span>
                  <span v-if="user.role === 'ADMIN'" class="rounded-full bg-slate-950 px-2 py-1 text-[9px] font-extrabold uppercase tracking-[0.14em] text-white">Admin</span>
                </div>
                <p v-if="user.disabledReason" class="mt-0.5 text-[11px] font-semibold text-rose-600">禁用原因：{{ user.disabledReason }}</p>
              </td>
              <td class="px-5 py-3.5 text-sm">
                <p class="font-bold text-slate-800">{{ user.studentId }}</p>
                <p class="truncate text-slate-500 text-xs">{{ user.email }}</p>
              </td>
              <td class="px-5 py-3.5 text-sm text-slate-600">{{ user.major || '-' }}</td>
              <td class="px-5 py-3.5">
                <span
                  class="rounded-full px-2.5 py-1 text-[10px] font-extrabold uppercase tracking-[0.12em]"
                  :class="user.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-700' : 'bg-rose-100 text-rose-700'"
                >{{ userStatusLabel(user.status) }}</span>
              </td>
              <td class="px-5 py-3.5">
                <div class="flex gap-2">
                  <button
                    v-if="user.role !== 'ADMIN'"
                    class="rounded-lg bg-slate-900 px-3 py-1.5 text-[11px] font-extrabold text-white hover:bg-slate-800 disabled:opacity-40"
                    :disabled="pendingUserIds.has(user.id) || user.status === 'ACTIVE'"
                    @click="changeUserStatus(user, 'ACTIVE')"
                  >启用</button>
                  <button
                    v-if="user.role !== 'ADMIN'"
                    class="rounded-lg border border-rose-200 bg-rose-50 px-3 py-1.5 text-[11px] font-extrabold text-rose-600 hover:bg-rose-100 disabled:opacity-40"
                    :disabled="pendingUserIds.has(user.id) || user.status === 'DISABLED'"
                    @click="changeUserStatus(user, 'DISABLED')"
                  >禁用</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div class="flex flex-col gap-3 pt-2 md:flex-row md:items-center md:justify-between">
        <p class="text-sm font-semibold text-slate-500">
          第 {{ currentPage }} / {{ totalPages }} 页，每页 20 条
        </p>
        <div class="flex flex-wrap gap-2">
          <button
            type="button"
            class="rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-extrabold text-slate-700 transition hover:bg-slate-50 disabled:opacity-45"
            :disabled="currentPage === 1"
            @click="currentPage = Math.max(1, currentPage - 1)"
          >
            上一页
          </button>
          <button
            type="button"
            class="rounded-xl border border-slate-200 bg-white px-4 py-2 text-sm font-extrabold text-slate-700 transition hover:bg-slate-50 disabled:opacity-45"
            :disabled="currentPage === totalPages"
            @click="currentPage = Math.min(totalPages, currentPage + 1)"
          >
            下一页
          </button>
        </div>
      </div>
    </section>

    <div v-if="showDisableDialog" class="fixed inset-0 z-[70] flex items-center justify-center bg-slate-950/45 px-6" @click.self="showDisableDialog = false">
      <div class="w-full max-w-lg rounded-[2rem] bg-white p-8 shadow-2xl">
        <div class="mb-6 flex items-start justify-between gap-4">
          <div>
            <h3 class="text-xl font-extrabold text-slate-900">禁用用户</h3>
            <p class="mt-1 text-sm text-slate-500">{{ disableTargetUser?.name }} ({{ disableTargetUser?.studentId }})</p>
          </div>
          <button type="button" class="rounded-full p-2 text-slate-400 hover:bg-slate-100 hover:text-slate-600" @click="showDisableDialog = false">
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>
        <label class="block text-sm font-bold text-slate-700 mb-2">禁用原因</label>
        <textarea
          v-model="disableReason"
          class="w-full rounded-2xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm text-slate-900 outline-none transition focus:border-rose-300 focus:ring-2 focus:ring-rose-100"
          rows="3"
          placeholder="请输入禁用原因..."
        ></textarea>
        <div class="mt-6 flex items-center justify-end gap-3">
          <button type="button" class="rounded-xl px-5 py-3 text-sm font-bold text-slate-600 hover:bg-slate-100" @click="showDisableDialog = false">取消</button>
          <button type="button" class="rounded-xl bg-rose-600 px-5 py-3 text-sm font-bold text-white hover:bg-rose-700 disabled:opacity-50" @click="confirmDisableUser">确认禁用</button>
        </div>
      </div>
    </div>
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

const showDisableDialog = ref(false)
const disableReason = ref('')
const disableTargetUser = ref<AdminUser | null>(null)

const openDisableDialog = (user: AdminUser) => {
  disableTargetUser.value = user
  disableReason.value = user.disabledReason || '违规内容发布'
  showDisableDialog.value = true
}

const confirmDisableUser = async () => {
  const user = disableTargetUser.value
  if (!user) return
  showDisableDialog.value = false
  pendingUserIds.value.add(user.id)
  try {
    await adminApi.updateUserStatus(user.id, { status: 'DISABLED', disabledReason: disableReason.value || '违规内容发布' })
    await loadUsers()
  } catch (err: any) {
    error.value = err?.response?.data?.message || '用户状态更新失败'
  } finally {
    pendingUserIds.value.delete(user.id)
    disableTargetUser.value = null
  }
}

const changeUserStatus = async (user: AdminUser, status: 'ACTIVE' | 'DISABLED') => {
  if (status === 'DISABLED') {
    openDisableDialog(user)
    return
  }

  pendingUserIds.value.add(user.id)
  try {
    await adminApi.updateUserStatus(user.id, { status, disabledReason: '' })
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
