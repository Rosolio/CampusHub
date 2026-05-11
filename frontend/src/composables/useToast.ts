import { ref } from 'vue'

export type ToastType = 'success' | 'error' | 'info'

type ToastState = {
  id: number
  message: string
  type: ToastType
}

const toast = ref<ToastState | null>(null)
let toastTimer: number | null = null
let toastId = 0

export const showToast = (message: string, type: ToastType = 'info', duration = 2800) => {
  toastId += 1
  toast.value = { id: toastId, message, type }

  if (toastTimer !== null) {
    window.clearTimeout(toastTimer)
  }

  toastTimer = window.setTimeout(() => {
    if (toast.value?.id === toastId) {
      toast.value = null
    }
  }, duration)
}

export const closeToast = () => {
  toast.value = null
  if (toastTimer !== null) {
    window.clearTimeout(toastTimer)
    toastTimer = null
  }
}

export const useToast = () => ({ toast, showToast, closeToast })
