const TASK_CATEGORIES = new Set(['跑腿代办', '学习辅导'])

export const inferTaskMode = (task: Record<string, any> | null | undefined): 'task' | 'topic' => {
  const taskMode = String(task?.taskMode || '').toLowerCase()
  if (taskMode === 'task' || taskMode === 'topic') {
    return taskMode
  }

  const category = String(task?.category || '')
  return TASK_CATEGORIES.has(category) ? 'task' : 'topic'
}

export const normalizeTask = <T extends Record<string, any>>(task: T): T & { taskMode: 'task' | 'topic' } => ({
  ...task,
  taskMode: inferTaskMode(task)
})
