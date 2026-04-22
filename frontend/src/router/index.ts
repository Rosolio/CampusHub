import { createRouter, createWebHistory } from 'vue-router'
import Discovery from '../pages/Discovery.vue'
import TopicSquare from '../pages/TopicSquare.vue'
import RequestDetail from '../pages/RequestDetail.vue'
import TaskReviewPage from '../pages/TaskReviewPage.vue'
import Profile from '../pages/Profile.vue'
import Auth from '../pages/Auth.vue'
import Messages from '../pages/Messages.vue'
import Settings from '../pages/Settings.vue'
import Publish from '../pages/Publish.vue'
import ProfileSettings from '../pages/settings/ProfileSettings.vue'
import NotificationSettings from '../pages/settings/NotificationSettings.vue'
import ThemeSettings from '../pages/settings/ThemeSettings.vue'
import LanguageSettings from '../pages/settings/LanguageSettings.vue'
import AboutCampusAid from '../pages/settings/AboutCampusAid.vue'
import PrivacyPolicy from '../pages/settings/PrivacyPolicy.vue'
import UserAgreement from '../pages/settings/UserAgreement.vue'
import AdminLayout from '../pages/admin/AdminLayout.vue'
import AdminOverviewPage from '../pages/admin/AdminOverviewPage.vue'
import AdminUsersPage from '../pages/admin/AdminUsersPage.vue'
import AdminModerationPage from '../pages/admin/AdminModerationPage.vue'
import AdminProfilePage from '../pages/admin/AdminProfilePage.vue'
import { hasValidAuthToken, isAdminUser } from '../utils/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: () => hasValidAuthToken() ? '/home' : '/auth?tab=login'
    },
    { path: '/home', name: 'home', component: Discovery, meta: { requiresAuth: true } },
    { path: '/topics', name: 'topics', component: TopicSquare, meta: { requiresAuth: true } },
    { path: '/detail/:id', name: 'detail', component: RequestDetail, props: true, meta: { requiresAuth: true } },
    { path: '/detail/:id/review', name: 'taskReview', component: TaskReviewPage, props: true, meta: { requiresAuth: true } },
    { path: '/profile', name: 'profile', component: Profile, meta: { requiresAuth: true } },
    { path: '/tasks', name: 'tasks', component: Profile, props: { initialTab: 'requests' }, meta: { requiresAuth: true } },
    { path: '/messages', name: 'messages', component: Messages, meta: { requiresAuth: true } },
    { path: '/publish', name: 'publish', component: Publish, meta: { requiresAuth: true } },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: '', redirect: '/admin/overview' },
        { path: 'overview', name: 'adminOverview', component: AdminOverviewPage, meta: { requiresAuth: true, requiresAdmin: true } },
        { path: 'users', name: 'adminUsers', component: AdminUsersPage, meta: { requiresAuth: true, requiresAdmin: true } },
        { path: 'moderation', name: 'adminModeration', component: AdminModerationPage, meta: { requiresAuth: true, requiresAdmin: true } },
        { path: 'profile', name: 'adminProfile', component: AdminProfilePage, meta: { requiresAuth: true, requiresAdmin: true } },
      ]
    },
    { path: '/auth', name: 'auth', component: Auth },
    { path: '/login', redirect: '/auth?tab=login' },
    { path: '/register', redirect: '/auth?tab=register' },
    { path: '/settings', name: 'settings', component: Settings, meta: { requiresAuth: true } },
    { path: '/settings/profile', name: 'profileSettings', component: ProfileSettings, meta: { requiresAuth: true } },
    { path: '/settings/notification', name: 'notificationSettings', component: NotificationSettings, meta: { requiresAuth: true } },
    { path: '/settings/theme', name: 'themeSettings', component: ThemeSettings, meta: { requiresAuth: true } },
    { path: '/settings/language', name: 'languageSettings', component: LanguageSettings, meta: { requiresAuth: true } },
    { path: '/settings/about', name: 'aboutCampusAid', component: AboutCampusAid, meta: { requiresAuth: true } },
    { path: '/settings/privacy', name: 'privacyPolicy', component: PrivacyPolicy, meta: { requiresAuth: true } },
    { path: '/settings/agreement', name: 'userAgreement', component: UserAgreement, meta: { requiresAuth: true } },
  ],
})

router.beforeEach((to) => {
  const authenticated = hasValidAuthToken()
  const isAuthRoute = to.path === '/auth' || to.path === '/login' || to.path === '/register'

  if (to.meta.requiresAuth && !authenticated) {
    return '/auth?tab=login'
  }

  if (to.meta.requiresAdmin && !isAdminUser()) {
    return '/home'
  }

  if (isAuthRoute && authenticated) {
    return '/home'
  }

  return true
})

export default router
