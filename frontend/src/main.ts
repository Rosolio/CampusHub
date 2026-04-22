import { createApp } from 'vue'
import 'virtual:uno.css'
import './style.css'
import App from './App.vue'
import router from './router'
import { initializePreferences } from './composables/usePreferences'

void initializePreferences()

createApp(App).use(router).mount('#app')
