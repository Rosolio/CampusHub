<template>
  <SettingsItem :title="title" :description="description">
    <label class="relative inline-flex items-center" :class="disabled ? 'cursor-not-allowed opacity-60' : 'cursor-pointer'">
      <input
        :checked="modelValue"
        :disabled="disabled"
        type="checkbox"
        class="peer sr-only"
        @change="handleChange"
      >
      <div class="h-6 w-11 rounded-full bg-gray-200 transition-colors peer-checked:bg-primary peer-focus:outline-none peer-disabled:bg-gray-200/70 after:absolute after:left-[2px] after:top-[2px] after:h-5 after:w-5 after:rounded-full after:border after:border-gray-300 after:bg-white after:transition-all after:content-[''] peer-checked:after:translate-x-full peer-checked:after:border-white"></div>
    </label>
  </SettingsItem>
</template>

<script setup lang="ts">
import SettingsItem from './SettingsItem.vue'

withDefaults(defineProps<{
  title: string
  description?: string
  modelValue: boolean
  disabled?: boolean
}>(), {
  description: '',
  disabled: false
})

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
}>()

const handleChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  emit('update:modelValue', target.checked)
}
</script>
