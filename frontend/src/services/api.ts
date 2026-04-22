import axios from 'axios'
import { clearAuthStorage, getStoredToken, hasValidAuthToken } from '../utils/auth'

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || '/api'

// 创建axios实例
const api = axios.create({
  baseURL: apiBaseUrl,
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加token
api.interceptors.request.use(
  (config) => {
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
    return response.data
  },
  (error) => {
    if (error.response?.status === 401) {
      // token过期，清除本地存储并跳转到登录页
      clearAuthStorage()
      window.location.href = '/auth'
    }
    return Promise.reject(error)
  }
)

// 认证相关API
export const authApi = {
  // 登录
  login: (studentId: string, password: string) => {
    return api.post('/auth/login', { studentId, password })
  },
  
  // 注册
  register: (data: {
    studentId: string
    name: string
    email: string
    password: string
  }) => {
    return api.post('/auth/register', data)
  },

  thirdPartyLogin: (data: {
    provider: string
    providerUserId: string
    displayName?: string
    email?: string
  }) => {
    return api.post('/auth/third-party', data)
  },
  
  // 刷新token
  refreshToken: (refreshToken: string) => {
    return api.post('/auth/refresh', { refreshToken })
  }
}

// 任务相关API
export const taskApi = {
  // 获取所有任务
  getTasks: () => {
    return api.get('/tasks')
  },
  
  // 获取任务详情
  getTaskById: (id: number) => {
    return api.get(`/tasks/${id}`)
  },

  // 点赞帖子
  likeTask: (taskId: number) => {
    return api.post(`/tasks/${taskId}/like`)
  },

  // 取消帖子点赞
  unlikeTask: (taskId: number) => {
    return api.delete(`/tasks/${taskId}/like`)
  },
  
  // 创建任务
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
  }) => {
    return api.post('/tasks', data)
  },
  
  // 接受任务
  acceptTask: (taskId: number) => {
    return api.post(`/tasks/${taskId}/accept`)
  },

  // 取消接单
  unacceptTask: (taskId: number) => {
    return api.post(`/tasks/${taskId}/unaccept`)
  },

  // 获取话题帖评论
  getTaskComments: (taskId: number) => {
    return api.get(`/tasks/${taskId}/comments`)
  },

  // 发表评论或回复
  createTaskComment: (taskId: number, data: {
    content: string
    parentId?: number | null
  }) => {
    return api.post(`/tasks/${taskId}/comments`, data)
  },

  // 点赞评论
  likeTaskComment: (taskId: number, commentId: number) => {
    return api.post(`/tasks/${taskId}/comments/${commentId}/like`)
  },

  // 取消评论点赞
  unlikeTaskComment: (taskId: number, commentId: number) => {
    return api.delete(`/tasks/${taskId}/comments/${commentId}/like`)
  },

  // 删除评论或回复
  deleteTaskComment: (taskId: number, commentId: number) => {
    return api.delete(`/tasks/${taskId}/comments/${commentId}`)
  },
  
  // 完成任务
  completeTask: (taskId: number) => {
    return api.post(`/tasks/${taskId}/complete`)
  },

  // 获取任务评价
  getTaskReviews: (taskId: number) => {
    return api.get(`/tasks/${taskId}/reviews`)
  },

  // 提交任务评价
  createTaskReview: (taskId: number, data: {
    rating: number
    content?: string
  }) => {
    return api.post(`/tasks/${taskId}/reviews`, data)
  },
  
  // 取消任务
  cancelTask: (taskId: number) => {
    return api.post(`/tasks/${taskId}/cancel`)
  },

  // 删除任务
  deleteTask: async (taskId: number) => {
    try {
      return await api.delete(`/tasks/${taskId}`)
    } catch (error: any) {
      const status = error?.response?.status
      if (status === 404 || status === 405) {
        return api.post(`/tasks/${taskId}/delete`)
      }
      throw error
    }
  },
  
  // 获取我的任务
  getMyTasks: () => {
    return api.get('/tasks/my')
  },

  // 获取我的服务
  getMyAcceptedTasks: () => {
    return api.get('/tasks/my/accepted')
  },

  // 获取我收到的点赞数
  getMyReceivedLikeCount: () => {
    return api.get('/tasks/my/received-likes/count')
  }
}

// 用户相关API
export const userApi = {
  // 获取当前用户信息
  getCurrentUser: () => {
    return api.get('/users/me')
  },

  // 获取指定用户信息
  getUserById: (id: number) => {
    return api.get(`/users/${id}`)
  },

  // 获取当前用户积分明细
  getPointRecords: () => {
    return api.get('/users/me/points/records')
  },
  
  // 更新用户信息
  updateUser: (data: {
    name?: string
    email?: string
    avatarUrl?: string
    major?: string
  }) => {
    return api.put('/users/me', data)
  },
  
  // 获取用户设置
  getUserSettings: () => {
    return api.get('/users/settings')
  },
  
  // 更新用户设置
  updateUserSettings: (data: {
    notificationEnabled?: boolean
    theme?: string
    language?: string
  }) => {
    return api.put('/users/settings', data)
  }
}

export const adminApi = {
  getDashboard: () => {
    return api.get('/admin/dashboard')
  },

  getUsers: () => {
    return api.get('/admin/users')
  },

  updateUserStatus: (userId: number, data: { status: 'ACTIVE' | 'DISABLED'; disabledReason?: string }) => {
    return api.put(`/admin/users/${userId}/status`, data)
  },

  getTasks: () => {
    return api.get('/admin/tasks')
  },

  reviewTask: (taskId: number, data: { reviewStatus: 'approved' | 'rejected' | 'pending_review'; reviewNote?: string }) => {
    return api.put(`/admin/tasks/${taskId}/review`, data)
  }
}

// 消息相关API
export const messageApi = {
  // 获取消息列表
  getMessages: () => {
    return api.get('/messages')
  },

  // 获取未读消息数量
  getUnreadCount: () => {
    return api.get('/messages/unread/count')
  },
  
  // 发送消息
  sendMessage: (data: {
    receiverId: number
    content: string
    taskId?: number
  }) => {
    return api.post('/messages', data)
  },
  
  // 标记消息为已读
  markAsRead: (messageId: number) => {
    return api.put(`/messages/${messageId}/read`)
  }
}

export default api
