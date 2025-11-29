<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1 class="login-title">Welcome Back</h1>
        <p class="login-subtitle">계정에 로그인하세요</p>
      </div>

      <div class="login-actions">
        <button type="button" class="btn-login" @click="handleLogin" :disabled="loading">
          <span v-if="!loading">Keycloak으로 로그인</span>
          <span v-else class="loading-spinner"></span>
        </button>

        <div class="signup-link">
          계정이 없으신가요?
          <a href="#" @click.prevent="handleRegister">회원가입</a>
        </div>
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { useAuth } from '@/composables/useAuth';

export default defineComponent({
  name: 'LogIn',
  setup() {
    const { login, register } = useAuth();
    const loading = ref(false);

    const handleLogin = async () => {
      loading.value = true;
      try {
        await login();
      } catch (error) {
        console.error('Login failed:', error);
        loading.value = false;
      }
    };

    const handleRegister = async () => {
      await register();
    };

    return {
      loading,
      handleLogin,
      handleRegister,
    };
  },
});
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.login-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 48px 40px;
  width: 100%;
  max-width: 440px;
  animation: slideUp 0.5s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 32px;
}

.login-title {
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.login-subtitle {
  color: #718096;
  font-size: 14px;
  margin: 0;
}

.login-actions {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.btn-login {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.btn-login:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.btn-login:active {
  transform: translateY(0);
}

.btn-login:disabled {
  opacity: 0.6;
  cursor: not-allowed;
  transform: none;
}

.loading-spinner {
  display: inline-block;
  width: 20px;
  height: 20px;
  border: 2px solid rgba(255,255,255,0.3);
  border-top-color: white;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.signup-link {
  text-align: center;
  font-size: 14px;
  color: #4a5568;
}

.signup-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
  transition: color 0.2s;
}

.signup-link a:hover {
  color: #764ba2;
}

@media (max-width: 640px) {
  .login-card {
    padding: 32px 24px;
  }

  .login-title {
    font-size: 28px;
  }
}
</style>
