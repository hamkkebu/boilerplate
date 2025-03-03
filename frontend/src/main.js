import { createApp } from 'vue'
import App from './App.vue'
import router from '@/components/router'
import axios from 'axios'

axios.defaults.headers.common['Access-Control-Allow-Origin'] = '*'
axios.defaults.headers.post['Content-Type'] = 'application/json'
axios.defaults.headers.get['Content-Type'] = 'application/json'
axios.defaults.headers.put['Content-Type'] = 'application/json'
axios.defaults.headers.delete['Content-Type'] = 'application/json'

const app = createApp(App)
app.use(router)
app.config.globalProperties.axios = axios
app.mount('#app')