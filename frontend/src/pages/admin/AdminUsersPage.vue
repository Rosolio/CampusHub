<template>
  <div class="space-y-6">
    <section v-if="error" class="rounded-[1.5rem] border border-rose-200 bg-rose-50 px-5 py-4 text-sm font-semibold text-rose-700">
      {{ error }}
    </section>

    <section class="rounded-[1.9rem] border border-[#ddd6c9] bg-[#fffdf8] p-5 shadow-[0_16px_48px_rgba(15,23,42,0.05)] md:p-6">
      <div class="flex flex-col gap-3 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-[11px] font-extrabold uppercase tracking-[0.24em] text-slate-500">User Actions</p>
          <h2 class="mt-2 text-2xl font-extrabold tracking-tight text-slate-900">用户管理面板</h2>
        </div>
        <div class="flex flex-wrap gap-3">
          <div class="rounded-full bg-[#f3ede0] px-4 py-2 text-sm font-bold text-slate-700">
            共 {{ users.length }} 个账号
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

      <div class="mt-6 grid gap-4">
        <article
          v-for="user in users"
          :key="user.id"
          class="rounded-[1.4rem] border border-[#ece5d8] bg-[#faf7f0] p-4 transition hover:-translate-y-0.5 hover:shadow-[0_14px_36px_rgba(15,23,42,0.06)]"
        >
          <div class="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
            <div class="min-w-0">
              <div class="flex flex-wrap items-center gap-2">
                <h3 class="text-lg font-extrabold text-slate-900">{{ user.name }}</h3>
                <span
                  class="rounded-full px-3 py-1 text-xs font-extrabold uppercase tracking-[0.14em]"
                  :class="user.status === 'ACTIVE' ? 'bg-emerald-100 text-emerald-800' : 'bg-rose-100 text-rose-700'"
                >
                  {{ userStatusLabel(user.status) }}
                </span>
                <span v-if="user.role === 'ADMIN'" class="rounded-full bg-[#13212b] px-3 py-1 text-xs font-extrabold uppercase tracking-[0.14em] text-white">
                  Admin
                </span>
              </div>

              <div class="mt-3 flex flex-wrap gap-x-5 gap-y-2 text-sm text-slate-600">
                <span>学号 {{ user.studentId }}</span>
                <span>{{ user.email }}</span>
                <span>{{ user.major || '未填写专业' }}</span>
              </div>

              <p v-if="user.disabledReason" class="mt-3 rounded-2xl bg-rose-50 px-3 py-2 text-sm font-semibold text-rose-700">
                禁用原因：{{ user.disabledReason }}
              </p>
            </div>

            <div v-if="user.role !== 'ADMIN'" class="flex flex-wrap gap-2">
              <button
                type="button"
                class="rounded-full bg-[#0f766e] px-4 py-2 text-sm font-extrabold text-white transition hover:bg-[#0b5f59] disabled:opacity-45"
                :disabled="pendingUserIds.has(user.id) || user.status === 'ACTIVE'"
                @click="changeUserStatus(user, 'ACTIVE')"
              >
                启用账号
              </button>
              <button
                type="button"
                class="rounded-full bg-[#c2410c] px-4 py-2 text-sm font-extrabold text-white transition hover:bg-[#9a3412] disabled:opacity-45"
                :disabled="pendingUserIds.has(user.id) || user.status === 'DISABLED'"
                @click="changeUserStatus(user, 'DISABLED')"
              >
                禁用账号
              </button>
            </div>
          </div>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { adminApi } from '../../services/api'
import type { AdminUser } from './adminTypes'

const error = ref('')
const users = ref<AdminUser[]>([])
const pendingUserIds = ref(new Set<number>())

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

onMounted(() => {
  loadUsers()
})
</script>
