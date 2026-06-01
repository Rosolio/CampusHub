import axios from 'axios'
import {
  clearAuthStorage,
  getStoredRefreshToken,
  getStoredToken,
  hasValidAuthToken,
  isTokenExpired,
  setStoredToken
} from '../utils/auth'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api'

// 创建axios实例
const api = axios.create({
  baseURL: apiBaseUrl,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 文件上传专用实例（不预设 Content-Type，让浏览器自动设置 multipart boundary）
const uploadApi = axios.create({
  baseURL: apiBaseUrl,
  timeout: 30000
})
uploadApi.interceptors.request.use((config) => {
  const token = getStoredToken()
  if (token && hasValidAuthToken()) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

const authApiClient = axios.create({
  baseURL: apiBaseUrl,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

const requestData = async <T>(request: Promise<{ data: T }>) => {
  const response = await request
  return response.data
}

let refreshPromise: Promise<string | null> | null = null
let isRedirectingToLogin = false

const refreshAccessToken = async () => {
  if (refreshPromise) {
    return refreshPromise
  }

  refreshPromise = (async () => {
    const refreshToken = getStoredRefreshToken()
    if (!refreshToken || isTokenExpired(refreshToken)) {
      clearAuthStorage()
      return null
    }

    try {
      const response = await authApiClient.post<{ token: string }>('/auth/refresh', { refreshToken })
      const nextToken = response.data?.token
      if (!nextToken) {
        clearAuthStorage()
        return null
      }
      setStoredToken(nextToken)
      return nextToken
    } catch {
      clearAuthStorage()
      return null
    } finally {
      refreshPromise = null
    }
  })()

  return refreshPromise
}

// 请求拦截器 - 添加token
api.interceptors.request.use(
  (config) => {
    if (config.url?.startsWith('/auth/')) {
      delete config.headers.Authorization
      return config
    }

    if (!hasValidAuthToken()) {
      return config
    }

    const token = getStoredToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器 - 处理错误
api.interceptors.response.use(
  (response) => {
    return response
  },
  async (error) => {
    const originalRequest = error.config

    if (
      error.response?.status === 401 &&
      originalRequest &&
      !originalRequest.url?.startsWith('/auth/') &&
      !originalRequest._retry
    ) {
      originalRequest._retry = true
      const nextToken = await refreshAccessToken()
      if (nextToken) {
        originalRequest.headers.Authorization = `Bearer ${nextToken}`
        return api(originalRequest)
      }
    }

    if (error.response?.status === 401 && !isRedirectingToLogin) {
      isRedirectingToLogin = true
      clearAuthStorage()
      window.location.href = '/auth?tab=login'
    }

    return Promise.reject(error)
  }
)

// 认证相关API
export const authApi = {
  login: (studentId: string, password: string) => requestData(api.post('/auth/login', { studentId, password })),

  register: (data: {
    studentId: string
    name: string
    email: string
    password: string
  }) => requestData(api.post('/auth/register', data)),

  refreshToken: (refreshToken: string) => requestData(authApiClient.post('/auth/refresh', { refreshToken }))
}

export const taskApi = {
  getTasks: (params?: {
    mode?: 'recommended' | 'latest'
    category?: string
    location?: string
    availableAt?: string
    limit?: number
    page?: number
    size?: number
    taskMode?: 'task' | 'topic'
  }) => requestData(api.get('/tasks', { params })),
  getTaskById: (id: number) => requestData(api.get(`/tasks/${id}`)),
  likeTask: (taskId: number) => requestData(api.post(`/tasks/${taskId}/like`)),
  unlikeTask: (taskId: number) => requestData(api.delete(`/tasks/${taskId}/like`)),
  createTask: (data: {
    title: string
    description: string
    category: string
    taskMode: 'task' | 'topic'
    badgePrimary: string
    badgeSecondary: string
    locationText: string
    timeText: string
    rewardTitle: string
    rewardText: string
    impactTitle: string
    impactText: string
    mapImageUrl?: string
    contactInfo?: string
    expiresAt?: string
  }) => requestData(api.post('/tasks', data)),
  acceptTask: (taskId: number) => requestData(api.post(`/tasks/${taskId}/accept`)),
  unacceptTask: (taskId: number) => requestData(api.post(`/tasks/${taskId}/unaccept`)),
  getTaskComments: (taskId: number) => requestData(api.get(`/tasks/${taskId}/comments`)),
  createTaskComment: (taskId: number, data: {
    content: string
    parentId?: number | null
  }) => requestData(api.post(`/tasks/${taskId}/comments`, data)),
  likeTaskComment: (taskId: number, commentId: number) => requestData(api.post(`/tasks/${taskId}/comments/${commentId}/like`)),
  unlikeTaskComment: (taskId: number, commentId: number) => requestData(api.delete(`/tasks/${taskId}/comments/${commentId}/like`)),
  deleteTaskComment: (taskId: number, commentId: number) => requestData(api.delete(`/tasks/${taskId}/comments/${commentId}`)),
  completeTask: (taskId: number) => requestData(api.post(`/tasks/${taskId}/complete`)),
  getTaskReviews: (taskId: number) => requestData(api.get(`/tasks/${taskId}/reviews`)),
  getTaskReviewsBatch: (taskIds: number[]) => requestData(api.get('/task-reviews', { params: { taskIds: taskIds.join(',') } })),
  createTaskReview: (taskId: number, data: {
    rating: number
    content?: string
  }) => requestData(api.post(`/tasks/${taskId}/reviews`, data)),
  cancelTask: (taskId: number) => requestData(api.post(`/tasks/${taskId}/cancel`)),
  deleteTask: async (taskId: number) => {
    try {
      return await requestData(api.delete(`/tasks/${taskId}`))
    } catch (error: any) {
      const status = error?.response?.status
      if (status === 404 || status === 405) {
        return requestData(api.post(`/tasks/${taskId}/delete`))
      }
      throw error
    }
  },
  getMyTasks: () => requestData(api.get('/tasks/my')),
  getMyAcceptedTasks: () => requestData(api.get('/tasks/my/accepted')),
  getMyReceivedLikeCount: () => requestData(api.get('/tasks/my/received-likes/count'))
}

export const userApi = {
  getCurrentUser: () => requestData(api.get('/users/me')),
  getUserById: (id: number) => requestData(api.get(`/users/${id}`)),
  getPointRecords: () => requestData(api.get('/users/me/points/records')),
  updateUser: (data: {
    name?: string
    email?: string
    avatarUrl?: string
    major?: string
  }) => requestData(api.put('/users/me', data)),
  getUserSettings: () => requestData(api.get('/users/settings')),
  updateUserSettings: (data: {
    notificationEnabled?: boolean
    theme?: string
    language?: string
  }) => requestData(api.put('/users/settings', data))
}

export const adminApi = {
  getDashboard: () => requestData(api.get('/admin/dashboard')),
  getUsers: () => requestData(api.get('/admin/users')),
  updateUserStatus: (userId: number, data: { status: 'ACTIVE' | 'DISABLED'; disabledReason?: string }) => requestData(api.put(`/admin/users/${userId}/status`, data)),
  getTasks: () => requestData(api.get('/admin/tasks')),
  reviewTask: (taskId: number, data: { reviewStatus: 'approved' | 'rejected' | 'pending_review'; reviewNote?: string }) => requestData(api.put(`/admin/tasks/${taskId}/review`, data)),
  getAnnouncements: () => requestData(api.get('/admin/announcements')),
  createAnnouncement: (data: { title: string; content: string; pinned?: boolean }) => requestData(api.post('/admin/announcements', data)),
  getFeedback: () => requestData(api.get('/admin/feedback')),
  updateFeedback: (feedbackId: number, data: { status: 'open' | 'in_progress' | 'resolved'; priority?: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'; adminReply?: string }) => requestData(api.put(`/admin/feedback/${feedbackId}`, data))
}

export const announcementApi = {
  getAnnouncements: () => requestData(api.get('/announcements'))
}

export const feedbackApi = {
  createFeedback: (data: { type: 'BUG' | 'SUGGESTION' | 'TASK_DISPUTE' | 'ACCOUNT_REPORT' | 'CONTENT_REPORT' | 'OTHER'; priority?: 'LOW' | 'NORMAL' | 'HIGH' | 'URGENT'; title: string; content: string }) => requestData(api.post('/feedback', data)),
  getMyFeedback: () => requestData(api.get('/feedback/my')),
  withdrawFeedback: (feedbackId: number) => requestData(api.delete(`/feedback/${feedbackId}`))
}

export const verificationApi = {
  getMyVerification: () => requestData(api.get('/users/me/verification')),
  submitVerification: (formData: FormData) =>
    requestData(uploadApi.post('/users/me/verification', formData)),
  getVerifications: () => requestData(api.get('/admin/verifications')),
  reviewVerification: (id: number, data: { status: string; rejectReason?: string }) =>
    requestData(api.put(`/admin/verifications/${id}/review`, data)),
  revokeVerification: (id: number) =>
    requestData(api.put(`/admin/verifications/${id}/revoke`)),
  getMyVerificationImageUrl: (filename: string) =>
    `${apiBaseUrl}/users/me/verification/images/${filename}`,
  getAdminVerificationImageUrl: (id: number, filename: string) =>
    `${apiBaseUrl}/admin/verifications/${id}/images/${filename}`
}

export const messageApi = {
  getMessages: () => requestData(api.get('/messages')),
  getUnreadCount: () => requestData(api.get('/messages/unread/count')),
  sendMessage: (data: {
    receiverId: number
    content: string
    taskId?: number
  }) => requestData(api.post('/messages', data)),
  markAsRead: (messageId: number) => requestData(api.put(`/messages/${messageId}/read`)),
  markAsReadBatch: (ids: number[]) => requestData(api.put('/messages/read', { ids }))
}

export default api
