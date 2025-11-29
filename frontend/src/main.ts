import { createApp } from 'vue';
import App from './App.vue';
import router from '@/components/router';
import apiClient from '@/api/client';

const app = createApp(App);

// Vue Router 사용
app.use(router);

// Axios 인스턴스를 전역 속성으로 등록 (기존 컴포넌트 호환성 유지)
app.config.globalProperties.axios = apiClient;

// 앱 마운트
// Keycloak 인증 초기화는 App.vue에서 수행됨
app.mount('#app');
