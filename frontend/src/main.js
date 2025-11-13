import { createApp } from 'vue'
import App from './App.vue'
import router from '@/components/router'
import axios from 'axios'

axios.defaults.headers.common['Access-Control-Allow-Origin'] = '*'
axios.defaults.headers.post['Content-Type'] = 'application/json'
axios.defaults.headers.get['Content-Type'] = 'application/json'
axios.defaults.headers.put['Content-Type'] = 'application/json'
axios.defaults.headers.delete['Content-Type'] = 'application/json'

// 응답 인터셉터: 에러 처리
axios.interceptors.response.use(
  // 성공 응답 처리
  response => {
    return response
  },
  // 에러 응답 처리
  error => {
    if (error.response) {
      const responseData = error.response.data

      // 백엔드 ApiResponse 구조: { success: false, error: { code, message, details } }
      let errorMessage = ''

      // 1. ApiResponse 형식인 경우 (error 객체가 있는 경우)
      if (responseData.error) {
        const errorInfo = responseData.error

        // Validation 에러 처리 (details에 필드별 에러가 있는 경우)
        if (errorInfo.details && typeof errorInfo.details === 'object') {
          const detailMessages = Object.entries(errorInfo.details)
            .map(([, message]) => `• ${message}`)
            .join('\n')

          errorMessage = `${errorInfo.message || '입력값 검증 실패'}\n\n${detailMessages}`
        }
        // 일반 에러 메시지
        else {
          errorMessage = errorInfo.message || '서버 오류가 발생했습니다.'
        }
      }
      // 2. 예상치 못한 응답 형식
      else {
        errorMessage = responseData.message || '서버 오류가 발생했습니다.'
      }

      // 에러 메시지 팝업 표시
      alert(errorMessage)
    } else if (error.request) {
      // 요청은 보냈지만 응답을 받지 못한 경우
      alert('서버로부터 응답이 없습니다. 네트워크 연결을 확인해주세요.')
    } else {
      // 요청 설정 중 에러가 발생한 경우
      alert('요청 처리 중 오류가 발생했습니다.')
    }

    return Promise.reject(error)
  }
)

const app = createApp(App)
app.use(router)
app.config.globalProperties.axios = axios
app.mount('#app')