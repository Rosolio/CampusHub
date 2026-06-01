export type StatPoint = {
  label: string
  value: number
}

export type AdminUser = {
  id: number
  studentId: string
  name: string
  email: string
  major?: string
  role: string
  status: string
  disabledReason?: string
}

export type AdminTask = {
  id: number
  title: string
  description: string
  category?: string
  taskMode?: string
  requesterId: number
  requesterName?: string
  reviewStatus: string
  reviewNote?: string
  status?: string
  createdAt?: string
  updatedAt?: string
  commentCount?: number
  helperName?: string
}
