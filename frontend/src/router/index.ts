import { createRouter, createWebHistory } from 'vue-router'
import { hasValidAuthToken, isAdminUser } from '../utils/auth'

const HomeGateway = () => import('../pages/HomeGateway.vue')
const RequestDetail = () => import('../pages/RequestDetail.vue')
const TaskReviewPage = () => import('../pages/TaskReviewPage.vue')
const Profile = () => import('../pages/Profile.vue')
const Auth = () => import('../pages/Auth.vue')
const Messages = () => import('../pages/Messages.vue')
const Settings = () => import('../pages/Settings.vue')
const Publish = () => import('../pages/Publish.vue')
const FeedbackPage = () => import('../pages/FeedbackPage.vue')
const ProfileSettings = () => import('../pages/settings/ProfileSettings.vue')
const NotificationSettings = () => import('../pages/settings/NotificationSettings.vue')
const ThemeSettings = () => import('../pages/settings/ThemeSettings.vue')
const LanguageSettings = () => import('../pages/settings/LanguageSettings.vue')
const AboutCampusHub = () => import('../pages/settings/AboutCampusHub.vue')
const PrivacyPolicy = () => import('../pages/settings/PrivacyPolicy.vue')
const UserAgreement = () => import('../pages/settings/UserAgreement.vue')
const PrivacyPolicyPage = () => import('../pages/PrivacyPolicyPage.vue')
const UserAgreementPage = () => import('../pages/UserAgreementPage.vue')
const AdminLayout = () => import('../pages/admin/AdminLayout.vue')
const AdminOverviewPage = () => import('../pages/admin/AdminOverviewPage.vue')
const AdminUsersPage = () => import('../pages/admin/AdminUsersPage.vue')
const AdminModerationPage = () => import('../pages/admin/AdminModerationPage.vue')
const AdminProfilePage = () => import('../pages/admin/AdminProfilePage.vue')
const AdminCommunityFeedPage = () => import('../pages/admin/AdminCommunityFeedPage.vue')
const AdminVerificationPage = () => import('../pages/admin/AdminVerificationPage.vue')
const VerificationPage = () => import('../pages/VerificationPage.vue')
const LeaderboardPage = () => import('../pages/LeaderboardPage.vue')
const NotFound = () => import('../pages/NotFound.vue')

const router = createRouter({
  history: createWebHistory(),
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    // Skip scroll-to-top when switching tabs on /home
    if (_to.path === '/home' && sessionStorage.getItem('tab_scroll')) {
      sessionStorage.removeItem('tab_scroll')
      return false
    }
    return { top: 0 }
  },
  routes: [
    {
      path: '/',
      redirect: () => hasValidAuthToken() ? '/home' : '/auth?tab=login'
    },
    { path: '/home', name: 'home', component: HomeGateway, meta: { requiresAuth: true } },
    { path: '/topics', redirect: '/home?tab=topic' },
    { path: '/detail/:id', name: 'detail', component: RequestDetail, props: true, meta: { requiresAuth: true } },
    { path: '/detail/:id/review', name: 'taskReview', component: TaskReviewPage, props: true, meta: { requiresAuth: true } },
    { path: '/profile', name: 'profile', component: Profile, meta: { requiresAuth: true } },
    { path: '/tasks', name: 'tasks', component: Profile, props: { initialTab: 'requests' }, meta: { requiresAuth: true } },
    { path: '/messages', name: 'messages', component: Messages, meta: { requiresAuth: true } },
    { path: '/publish', name: 'publish', component: Publish, meta: { requiresAuth: true } },
    { path: '/verification', name: 'verification', component: VerificationPage, meta: { requiresAuth: true } },
    { path: '/feedback', name: 'feedback', component: FeedbackPage, meta: { requiresAuth: true } },
    {
      path: '/admin',
      component: AdminLayout,
      meta: { requiresAuth: true, requiresAdmin: true },
      children: [
        { path: '', redirect: '/admin/overview' },
        { path: 'community', name: 'adminCommunity', component: AdminCommunityFeedPage, meta: { requiresAuth: true, requiresAdmin: true } },
        { path: 'overview', name: 'adminOverview', component: AdminOverviewPage, meta: { requiresAuth: true, requiresAdmin: true } },
        { path: 'users', name: 'adminUsers', component: AdminUsersPage, meta: { requiresAuth: true, requiresAdmin: true } },
        { path: 'moderation', name: 'adminModeration', component: AdminModerationPage, meta: { requiresAuth: true, requiresAdmin: true } },
        { path: 'verifications', name: 'adminVerifications', component: AdminVerificationPage, meta: { requiresAuth: true, requiresAdmin: true } },
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
    { path: '/search', name: 'search', component: () => import('../pages/SearchResults.vue'), meta: { requiresAuth: true } },
    { path: '/leaderboard', name: 'leaderboard', component: LeaderboardPage, meta: { requiresAuth: true } },
    { path: '/notifications', name: 'notifications', component: () => import('../pages/Notifications.vue'), meta: { requiresAuth: true } },
    { path: '/settings/about', name: 'aboutCampusHub', component: AboutCampusHub, meta: { requiresAuth: true } },
    { path: '/privacy', name: 'privacyPage', component: PrivacyPolicyPage },
    { path: '/agreement', name: 'agreementPage', component: UserAgreementPage },
    { path: '/settings/privacy', name: 'privacyPolicy', component: PrivacyPolicy, meta: { requiresAuth: true } },
    { path: '/settings/agreement', name: 'userAgreement', component: UserAgreement, meta: { requiresAuth: true } },
    { path: '/:pathMatch(.*)*', name: 'notFound', component: NotFound },
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

  if (authenticated && isAdminUser()) {
    if (isAuthRoute) {
      return '/admin/community'
    }
    if (to.path === '/home') {
      return '/admin/community'
    }
    if (to.path === '/publish' || to.path === '/messages') {
      return '/admin/community'
    }
    if (to.path === '/profile' || to.path === '/tasks') {
      return '/admin/profile'
    }
    if (to.path === '/verification') {
      return '/admin/verifications'
    }
  }

  if (isAuthRoute && authenticated) {
    return '/home'
  }

  return true
})

export default router
