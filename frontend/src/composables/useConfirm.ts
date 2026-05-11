import { ref } from 'vue'

type ConfirmOptions = {
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  tone?: 'default' | 'danger'
}

type ConfirmState = ConfirmOptions & {
  resolve: (value: boolean) => void
}

const confirmState = ref<ConfirmState | null>(null)

export const openConfirm = (options: ConfirmOptions) => new Promise<boolean>((resolve) => {
  confirmState.value = {
    ...options,
    resolve
  }
})

export const resolveConfirm = (value: boolean) => {
  const current = confirmState.value
  if (!current) return
  confirmState.value = null
  current.resolve(value)
}

export const useConfirm = () => ({ confirmState, openConfirm, resolveConfirm })
