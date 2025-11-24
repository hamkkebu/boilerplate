import { createApp } from 'vue';
import App from './App.vue';
import router from '@/components/router';
import apiClient from '@/api/client';
import { useAuth } from '@/composables/useAuth';

const app = createApp(App);

// Vue Router 사용
app.use(router);

// Axios 인스턴스를 전역 속성으로 등록 (기존 컴포넌트 호환성 유지)
app.config.globalProperties.axios = apiClient;

// 저장된 사용자 정보 복원
const { restoreUser } = useAuth();
restoreUser();

// 앱 마운트
app.mount('#app');
