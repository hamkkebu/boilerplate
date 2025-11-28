<template>
  <div>
    <div v-if="isLoading" class="loading-overlay">
      <div class="loading-spinner"></div>
      <p>인증 정보를 확인하는 중...</p>
    </div>
    <router-view v-else />
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import { useAuth } from '@/composables/useAuth';

export default defineComponent({
  name: 'App',
  setup() {
    const { initAuth, isKeycloakMode } = useAuth();
    const isLoading = ref(false);

    onMounted(async () => {
      // Keycloak 모드일 때만 초기화 로딩 표시
      if (isKeycloakMode) {
        isLoading.value = true;
        try {
          await initAuth();
        } catch (error) {
          console.error('Auth initialization failed:', error);
        } finally {
          isLoading.value = false;
        }
      }
    });

    return {
      isLoading,
    };
  },
});
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
}

body {
  margin: 0;
  padding: 0;
}

.loading-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(255, 255, 255, 0.9);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 4px solid #f3f3f3;
  border-top: 4px solid #3498db;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.loading-overlay p {
  margin-top: 16px;
  color: #666;
  font-size: 14px;
}
</style>
