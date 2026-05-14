<template>
  <div
    class="bg-background font-body text-on-background selection:bg-primary-container selection:text-on-primary-container min-h-screen flex items-center justify-center p-6 md:p-12 overflow-x-hidden"
  >
    <!-- Hero Background Element (Visual Anchor) -->
    <div class="fixed inset-0 z-0 opacity-40 pointer-events-none">
      <img
        class="w-full h-full object-cover"
        data-alt="Soft watercolor illustration of a modern university campus with green spaces, students walking, and architectural arches in teal and amber tones"
        src="https://lh3.googleusercontent.com/aida-public/AB6AXuBnOkqlNyus7BDNlmH2W-SKkyulg3w6IeHnUiiTmdpQ-oRA8Gk7MgF-uK_IidPd6BhjYyraFqxmEuFOY89BJVIUrXqL2HH6QPwMUvfF4qO0uriY1a8rdRWg7b902or12VIoDK98-pdi2e8OF3YnUIz5-658-pLmy29-0TGbWgNirWPK5fwzF1d7EHPKhsGWeYMcsVdNLMJ6yk3oj0uf_4Ccr7DJCAfKb31fyjCe1iXBKF8_BzzMMhDXQXWZPHX8xxwhf2HnYRvBI5Ac"
      />
    </div>

    <!-- Main Auth Container -->
    <main
      class="relative z-10 grid w-full max-w-6xl grid-cols-1 overflow-hidden rounded-[2rem] bg-surface-container-lowest shadow-[0_24px_48px_rgba(0,52,57,0.08)] md:grid-cols-[0.9fr_1.1fr] xl:grid-cols-[0.95fr_1.05fr]"
    >
      <!-- Left Column: Branding & Illustration -->
      <div class="relative hidden overflow-hidden bg-surface-container-low p-10 md:flex md:flex-col md:justify-between xl:p-12">
        <div class="relative z-10">
          <div class="flex items-center gap-3 mb-12">
            <div class="w-10 h-10 primary-gradient rounded-xl flex items-center justify-center text-white">
              <span class="material-symbols-outlined" data-icon="volunteer_activism">volunteer_activism</span>
            </div>
            <span class="font-headline text-2xl font-extrabold tracking-tight text-teal-900">CampusHub</span>
          </div>
          <h1 class="mb-6 font-headline text-4xl font-extrabold leading-[1.2] tracking-tight text-on-surface xl:text-5xl">
            校园互助，<br /><span class="text-primary">让生活更美好</span>
          </h1>
          <p class="max-w-sm text-base leading-relaxed text-on-surface-variant xl:text-lg">
            加入由学生主导的互助网络，共享资源，互相支持。
          </p>
        </div>

        <!-- Background Decorative Orb -->
        <div class="absolute -bottom-24 -left-24 w-64 h-64 bg-secondary/10 rounded-full blur-3xl"></div>
      </div>

      <!-- Right Column: Form Section -->
      <div class="flex flex-col justify-center bg-surface-container-lowest p-6 sm:p-8 md:px-10 md:py-12 xl:px-16">
        <div class="mx-auto w-full max-w-md lg:max-w-lg">
          <!-- Header Mobile Logo -->
          <div class="mb-8 flex items-center gap-2 md:hidden">
            <div class="w-8 h-8 primary-gradient rounded-lg flex items-center justify-center text-white">
              <span class="material-symbols-outlined text-sm" data-icon="volunteer_activism">volunteer_activism</span>
            </div>
            <span class="font-headline text-xl font-bold tracking-tight text-teal-900">CampusHub</span>
          </div>

          <div class="mb-10">
            <h2 class="font-headline text-3xl font-bold text-on-surface mb-2">{{ activeTab === 'login' ? '欢迎回来' : '创建账号' }}</h2>
            <p class="text-on-surface-variant font-label">{{ activeTab === 'login' ? '输入您的校园凭据以继续' : '填写以下信息注册新账号' }}</p>
          </div>

          <!-- Auth Tabs (Simplified) -->
          <div class="flex gap-8 mb-8">
            <button 
              class="pb-3 text-sm font-bold font-headline transition-colors" 
              :class="activeTab === 'login' ? 'text-primary border-b-2 border-primary' : 'text-on-surface-variant/60 hover:text-on-surface'"
              type="button"
              @click="switchTab('login')"
            >
              学生登录
            </button>
            <button 
              class="pb-3 text-sm font-medium font-headline transition-colors" 
              :class="activeTab === 'register' ? 'text-primary border-b-2 border-primary' : 'text-on-surface-variant/60 hover:text-on-surface'"
              type="button"
              @click="switchTab('register')"
            >
              注册账号
            </button>
          </div>

          <!-- Error Message -->
          <div v-if="error" class="mb-6 p-4 bg-error/10 border border-error/20 rounded-xl">
            <p class="text-error text-sm font-medium flex items-center gap-2">
              <span class="material-symbols-outlined text-base">error</span>
              {{ error }}
            </p>
          </div>

          <!-- Login Form -->
          <form v-if="activeTab === 'login'" class="space-y-6" @submit.prevent="handleLogin">
            <div class="space-y-2">
              <label class="block text-sm font-semibold text-on-surface-variant ml-1" for="student-id">用户名 / 学号</label>
              <div class="relative">
                <span
                  class="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-on-surface-variant/50"
                  data-icon="badge"
                  >badge</span
                >
                <input
                  class="w-full pl-12 pr-4 py-3.5 bg-surface-container-low ghost-border rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all placeholder:text-on-surface-variant/40"
                  id="student-id"
                  placeholder="例如：20230045 或 wanppi"
                  type="text"
                  v-model="loginForm.studentId"
                />
              </div>
            </div>

            <div class="space-y-2">
              <div class="flex justify-between items-center px-1">
                <label class="block text-sm font-semibold text-on-surface-variant" for="password">密码</label>
                <button class="text-xs font-bold text-primary hover:underline" type="button" @click="handleForgotPassword">忘记密码？</button>
              </div>
              <div class="relative">
                <span
                  class="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-on-surface-variant/50"
                  data-icon="lock"
                  >lock</span
                >
                <input
                  class="w-full pl-12 pr-12 py-3.5 bg-surface-container-low ghost-border rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all placeholder:text-on-surface-variant/40"
                  id="password"
                  placeholder="••••••••"
                  :type="showLoginPassword ? 'text' : 'password'"
                  v-model="loginForm.password"
                />
                <button
                  class="absolute right-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-on-surface-variant/50"
                  :data-icon="showLoginPassword ? 'visibility_off' : 'visibility'"
                  type="button"
                  :aria-label="showLoginPassword ? '隐藏密码' : '显示密码'"
                  @click="showLoginPassword = !showLoginPassword"
                >
                  {{ showLoginPassword ? 'visibility_off' : 'visibility' }}
                </button>
              </div>
            </div>

            <div class="flex items-center gap-3 px-1">
              <input
                class="w-5 h-5 rounded border-outline-variant text-primary focus:ring-primary/20 cursor-pointer"
                id="remember"
                type="checkbox"
                v-model="loginForm.remember"
              />
              <label class="text-sm font-medium text-on-surface-variant cursor-pointer" for="remember">保持登录</label>
            </div>

            <button
              class="w-full py-4 px-6 primary-gradient text-white font-headline font-bold rounded-xl shadow-lg shadow-primary/20 hover:scale-[0.98] transition-transform flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
              type="submit"
              :disabled="loading"
            >
              <span v-if="loading" class="material-symbols-outlined text-lg animate-spin">refresh</span>
              <span v-else class="material-symbols-outlined text-lg" data-icon="arrow_forward">arrow_forward</span>
              {{ loading ? '登录中...' : '登录' }}
            </button>
          </form>

          <!-- Register Form -->
          <form v-else-if="activeTab === 'register'" class="space-y-6" @submit.prevent="handleRegister">
            <div class="space-y-2">
              <label class="block text-sm font-semibold text-on-surface-variant ml-1" for="reg-student-id">学号</label>
              <div class="relative">
                <span
                  class="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-on-surface-variant/50"
                  data-icon="badge"
                  >badge</span
                >
                <input
                  class="w-full pl-12 pr-4 py-3.5 bg-surface-container-low ghost-border rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all placeholder:text-on-surface-variant/40"
                  id="reg-student-id"
                  placeholder="例如：20230045"
                  type="text"
                  v-model="registerForm.studentId"
                />
              </div>
            </div>

            <div class="space-y-2">
              <label class="block text-sm font-semibold text-on-surface-variant ml-1" for="reg-name">昵称</label>
              <div class="relative">
                <span
                  class="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-on-surface-variant/50"
                  data-icon="person"
                  >person</span
                >
                <input
                  class="w-full pl-12 pr-4 py-3.5 bg-surface-container-low ghost-border rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all placeholder:text-on-surface-variant/40"
                  id="reg-name"
                  placeholder="请输入您的昵称"
                  type="text"
                  v-model="registerForm.name"
                />
              </div>
            </div>

            <div class="space-y-2">
              <label class="block text-sm font-semibold text-on-surface-variant ml-1" for="reg-email">邮箱</label>
              <div class="relative">
                <span
                  class="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-on-surface-variant/50"
                  data-icon="email"
                  >email</span
                >
                <input
                  class="w-full pl-12 pr-4 py-3.5 bg-surface-container-low ghost-border rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all placeholder:text-on-surface-variant/40"
                  id="reg-email"
                  placeholder="请输入您的邮箱"
                  type="email"
                  v-model="registerForm.email"
                />
              </div>
            </div>

            <div class="space-y-2">
              <label class="block text-sm font-semibold text-on-surface-variant ml-1" for="reg-password">密码</label>
              <div class="relative">
                <span
                  class="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-on-surface-variant/50"
                  data-icon="lock"
                  >lock</span
                >
                <input
                  class="w-full pl-12 pr-12 py-3.5 bg-surface-container-low ghost-border rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all placeholder:text-on-surface-variant/40"
                  id="reg-password"
                  placeholder="••••••••"
                  :type="showRegisterPassword ? 'text' : 'password'"
                  v-model="registerForm.password"
                />
                <button
                  class="absolute right-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-on-surface-variant/50"
                  :data-icon="showRegisterPassword ? 'visibility_off' : 'visibility'"
                  type="button"
                  :aria-label="showRegisterPassword ? '隐藏密码' : '显示密码'"
                  @click="showRegisterPassword = !showRegisterPassword"
                >
                  {{ showRegisterPassword ? 'visibility_off' : 'visibility' }}
                </button>
              </div>
            </div>

            <div class="space-y-2">
              <label class="block text-sm font-semibold text-on-surface-variant ml-1" for="reg-confirm-password">确认密码</label>
              <div class="relative">
                <span
                  class="absolute left-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-on-surface-variant/50"
                  data-icon="lock"
                  >lock</span
                >
                <input
                  class="w-full pl-12 pr-12 py-3.5 bg-surface-container-low ghost-border rounded-xl focus:ring-2 focus:ring-primary/20 focus:border-primary outline-none transition-all placeholder:text-on-surface-variant/40"
                  id="reg-confirm-password"
                  placeholder="••••••••"
                  :type="showRegisterConfirmPassword ? 'text' : 'password'"
                  v-model="registerForm.confirmPassword"
                />
                <button
                  class="absolute right-4 top-1/2 -translate-y-1/2 material-symbols-outlined text-on-surface-variant/50"
                  :data-icon="showRegisterConfirmPassword ? 'visibility_off' : 'visibility'"
                  type="button"
                  :aria-label="showRegisterConfirmPassword ? '隐藏确认密码' : '显示确认密码'"
                  @click="showRegisterConfirmPassword = !showRegisterConfirmPassword"
                >
                  {{ showRegisterConfirmPassword ? 'visibility_off' : 'visibility' }}
                </button>
              </div>
            </div>

            <div class="flex items-center gap-3 px-1">
              <input
                class="w-5 h-5 rounded border-outline-variant text-primary focus:ring-primary/20 cursor-pointer"
                id="reg-agree"
                type="checkbox"
                v-model="registerForm.agree"
              />
              <label class="text-sm font-medium text-on-surface-variant cursor-pointer" for="reg-agree">我已阅读并同意 <RouterLink class="text-primary hover:underline" to="/settings/agreement">用户协议</RouterLink> 和 <RouterLink class="text-primary hover:underline" to="/settings/privacy">隐私政策</RouterLink></label>
            </div>

            <button
              class="w-full py-4 px-6 primary-gradient text-white font-headline font-bold rounded-xl shadow-lg shadow-primary/20 hover:scale-[0.98] transition-transform flex items-center justify-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
              type="submit"
              :disabled="loading"
            >
              <span v-if="loading" class="material-symbols-outlined text-lg animate-spin">refresh</span>
              <span v-else class="material-symbols-outlined text-lg" data-icon="arrow_forward">arrow_forward</span>
              {{ loading ? '注册中...' : '注册' }}
            </button>
          </form>

          <!-- Divider -->
          <div class="relative my-10 text-center">
            <div class="absolute inset-0 flex items-center">
              <div class="w-full border-t ghost-border"></div>
            </div>
            <span class="relative px-4 bg-surface-container-lowest text-xs font-bold text-on-surface-variant/50 uppercase tracking-widest">
              或通过以下方式连接
            </span>
          </div>

          <!-- Third Party Logins -->
          <div class="grid grid-cols-1 gap-3 sm:grid-cols-2 sm:gap-4">
            <button
              class="flex min-h-14 items-center justify-center gap-3 rounded-xl bg-surface-container-low px-4 py-3.5 transition-colors hover:bg-surface-container-high ghost-border"
              type="button"
              @click="openThirdPartyDialog('QQ')"
            >
              <span class="material-symbols-outlined text-blue-500">chat</span>
              <span class="text-sm font-bold text-on-surface font-headline">QQ</span>
            </button>
            <button
              class="flex min-h-14 items-center justify-center gap-3 rounded-xl bg-surface-container-low px-4 py-3.5 text-center transition-colors hover:bg-surface-container-high ghost-border"
              type="button"
              @click="openThirdPartyDialog('SSO')"
            >
              <span class="material-symbols-outlined text-teal-800" data-icon="school">school</span>
              <span class="text-sm font-bold text-on-surface font-headline">统一身份认证 SSO</span>
            </button>
          </div>

          <p v-if="activeTab === 'login'" class="mt-12 text-center text-sm text-on-surface-variant">
            第一次使用校助？
            <button 
              class="text-secondary font-bold hover:underline cursor-pointer" 
              type="button"
              @click="switchTab('register')"
            >创建学生账号</button>
          </p>
        </div>
      </div>
    </main>

    <div
      v-if="showThirdPartyDialog"
      class="fixed inset-0 z-20 flex items-center justify-center bg-slate-950/45 px-6"
      @click.self="closeThirdPartyDialog"
    >
      <div class="w-full max-w-lg rounded-3xl bg-surface-container-lowest p-8 shadow-[0_24px_48px_rgba(0,52,57,0.2)]">
        <div class="mb-6 flex items-start justify-between gap-4">
          <div>
            <p class="text-sm font-bold uppercase tracking-[0.22em] text-on-surface-variant">第三方登录</p>
            <h3 class="mt-2 text-2xl font-extrabold text-on-surface">
              连接 {{ selectedProvider === 'QQ' ? 'QQ' : '统一身份认证 (SSO)' }}
            </h3>
            <p class="mt-2 text-sm leading-6 text-on-surface-variant">
              首次登录会自动创建账号，后续使用同一第三方账号标识可直接登录。
            </p>
          </div>
          <button
            class="rounded-full p-2 text-on-surface-variant transition-colors hover:bg-surface-container-low hover:text-on-surface"
            type="button"
            @click="closeThirdPartyDialog"
          >
            <span class="material-symbols-outlined">close</span>
          </button>
        </div>

        <form class="space-y-5" @submit.prevent="handleThirdPartyLogin">
          <div class="space-y-2">
            <label class="block text-sm font-semibold text-on-surface-variant ml-1" for="third-party-id">
              {{ selectedProvider === 'QQ' ? 'QQ 账号标识' : '统一身份认证账号' }}
            </label>
            <input
              id="third-party-id"
              v-model="thirdPartyForm.providerUserId"
              type="text"
              class="w-full rounded-xl bg-surface-container-low px-4 py-3.5 ghost-border outline-none transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
              :placeholder="selectedProvider === 'QQ' ? '例如：qq_20230045' : '例如：20230045@sso'"
            />
          </div>

          <div class="space-y-2">
            <label class="block text-sm font-semibold text-on-surface-variant ml-1" for="third-party-name">
              昵称（可选）
            </label>
            <input
              id="third-party-name"
              v-model="thirdPartyForm.displayName"
              type="text"
              class="w-full rounded-xl bg-surface-container-low px-4 py-3.5 ghost-border outline-none transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
              placeholder="留空则自动生成默认昵称"
            />
          </div>

          <div class="space-y-2">
            <label class="block text-sm font-semibold text-on-surface-variant ml-1" for="third-party-email">
              邮箱（可选）
            </label>
            <input
              id="third-party-email"
              v-model="thirdPartyForm.email"
              type="email"
              class="w-full rounded-xl bg-surface-container-low px-4 py-3.5 ghost-border outline-none transition-all focus:border-primary focus:ring-2 focus:ring-primary/20"
              placeholder="留空则由系统自动生成绑定邮箱"
            />
          </div>

          <div class="flex items-center justify-end gap-3 pt-2">
            <button
              class="rounded-xl px-5 py-3 font-semibold text-on-surface-variant transition-colors hover:bg-surface-container-low"
              type="button"
              :disabled="loading"
              @click="closeThirdPartyDialog"
            >
              取消
            </button>
            <button
              class="rounded-xl px-6 py-3 primary-gradient font-bold text-white shadow-lg shadow-primary/20 disabled:opacity-50 disabled:cursor-not-allowed"
              type="submit"
              :disabled="loading"
            >
              {{ loading ? '连接中...' : '确认连接并登录' }}
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- Footer Security Note -->
    <footer class="fixed bottom-6 left-1/2 hidden -translate-x-1/2 items-center gap-2 text-on-surface-variant/60 text-xs font-medium md:flex">
      <span class="material-symbols-outlined text-sm" data-icon="verified_user">verified_user</span>
      <span>由校务系统提供的安全身份验证</span>
    </footer>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { showToast } from '../composables/useToast'
import { authApi } from '../services/api'
import { setAuthSession } from '../utils/auth'

const route = useRoute()
const router = useRouter()

const normalizeTab = (tab: unknown) => tab === 'register' ? 'register' : 'login'

const activeTab = ref(normalizeTab(route.query.tab))
const loading = ref(false)
const error = ref('')
const showThirdPartyDialog = ref(false)
const selectedProvider = ref<'QQ' | 'SSO'>('QQ')
const showLoginPassword = ref(false)
const showRegisterPassword = ref(false)
const showRegisterConfirmPassword = ref(false)

const loginForm = ref({
  studentId: '',
  password: '',
  remember: false
})

const registerForm = ref({
  studentId: '',
  name: '',
  email: '',
  password: '',
  confirmPassword: '',
  agree: false
})

const thirdPartyForm = ref({
  providerUserId: '',
  displayName: '',
  email: ''
})

const switchTab = (tab: string) => {
  activeTab.value = tab
  error.value = ''
  router.replace({ path: '/auth', query: { tab } })
}

const resetThirdPartyForm = () => {
  thirdPartyForm.value = {
    providerUserId: '',
    displayName: '',
    email: ''
  }
}

watch(
  () => route.query.tab,
  (tab) => {
    activeTab.value = normalizeTab(tab)
  }
)

const handleLogin = async () => {
  if (!loginForm.value.studentId.trim()) {
    error.value = '请输入用户名或学号'
    return
  }
  if (!loginForm.value.password) {
    error.value = '请输入密码'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const payload = await authApi.login(loginForm.value.studentId, loginForm.value.password) as Record<string, any>
    const token = payload?.accessToken ?? payload?.token
    const refreshToken = payload?.refreshToken
    const user = payload?.user

    if (token && user) {
      setAuthSession({ token, refreshToken, user })
      router.push('/')
    } else {
      error.value = payload?.message || '登录失败'
    }
  } catch (err: any) {
    console.error('登录失败:', err)
    error.value = err.response?.data?.message || '登录失败，请检查学号和密码'
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  if (!registerForm.value.studentId.trim()) {
    error.value = '请输入学号'
    return
  }
  if (!registerForm.value.name.trim()) {
    error.value = '请输入昵称'
    return
  }
  if (!registerForm.value.email.trim()) {
    error.value = '请输入邮箱'
    return
  }
  if (!registerForm.value.password) {
    error.value = '请输入密码'
    return
  }
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    error.value = '两次输入的密码不一致'
    return
  }
  if (!registerForm.value.agree) {
    error.value = '请阅读并同意用户协议和隐私政策'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const payload = await authApi.register({
      studentId: registerForm.value.studentId,
      name: registerForm.value.name,
      email: registerForm.value.email,
      password: registerForm.value.password
    }) as Record<string, any>
    const token = payload?.accessToken ?? payload?.token
    const refreshToken = payload?.refreshToken
    const user = payload?.user

    if (token && user) {
      setAuthSession({ token, refreshToken, user })
      router.push('/')
    } else {
      error.value = payload?.message || '注册失败'
    }
  } catch (err: any) {
    console.error('注册失败:', err)
    error.value = err.response?.data?.message || '注册失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

const openThirdPartyDialog = (provider: 'QQ' | 'SSO') => {
  selectedProvider.value = provider
  error.value = ''
  resetThirdPartyForm()
  if (provider === 'QQ') {
    thirdPartyForm.value.email = ''
  }
  showThirdPartyDialog.value = true
}

const closeThirdPartyDialog = () => {
  showThirdPartyDialog.value = false
  resetThirdPartyForm()
}

const handleForgotPassword = () => {
  showToast('请联系管理员或校园统一身份认证入口重置密码。', 'info')
}

const handleThirdPartyLogin = async () => {
  if (!thirdPartyForm.value.providerUserId.trim()) {
    error.value = selectedProvider.value === 'QQ' ? '请输入 QQ 账号标识' : '请输入统一身份认证账号'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const payload = await authApi.thirdPartyLogin({
      provider: selectedProvider.value,
      providerUserId: thirdPartyForm.value.providerUserId.trim(),
      displayName: thirdPartyForm.value.displayName.trim(),
      email: thirdPartyForm.value.email.trim()
    }) as Record<string, any>
    const token = payload?.accessToken ?? payload?.token
    const refreshToken = payload?.refreshToken
    const user = payload?.user

    if (token && user) {
      setAuthSession({ token, refreshToken, user })
      closeThirdPartyDialog()
      router.push('/')
    } else {
      error.value = payload?.message || '第三方登录失败'
    }
  } catch (err: any) {
    console.error('第三方登录失败:', err)
    error.value = err.response?.data?.message || '第三方登录失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>
