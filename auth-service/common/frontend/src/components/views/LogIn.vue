<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1 class="login-title">Welcome Back</h1>
        <p class="login-subtitle">계정에 로그인하세요</p>
      </div>

      <form class="login-form" @submit.prevent="logInSubmit">
        <div class="input-group">
          <label for="userId" class="input-label">아이디</label>
          <div class="input-wrapper">
            <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
              <circle cx="12" cy="7" r="4"></circle>
            </svg>
            <input
              id="userId"
              type="text"
              class="input-field"
              placeholder="아이디를 입력하세요"
              v-model="user_id"
              required
            />
          </div>
        </div>

        <div class="input-group">
          <label for="userPassword" class="input-label">비밀번호</label>
          <div class="input-wrapper">
            <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
            </svg>
            <input
              id="userPassword"
              type="password"
              class="input-field"
              placeholder="비밀번호를 입력하세요"
              v-model="user_pw"
              required
            />
          </div>
        </div>

        <button type="submit" class="btn-login">
          <span>로그인</span>
        </button>

        <div class="signup-link">
          계정이 없으신가요?
          <a href="#" @click.prevent="goToSignup">회원가입</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuth } from '@/composables/useAuth';
import apiClient from '@/api/client';
import { useApi } from '@/composables/useApi';
import { API_ENDPOINTS, ROUTES } from '@/constants';
import type { LoginResponse } from '@/types/domain.types';

export default defineComponent({
  name: 'LogIn',
  setup() {
    const router = useRouter();
    const { login } = useAuth();
    const { loading, execute } = useApi<LoginResponse>();

    const user_id = ref('');
    const user_pw = ref('');

    const logInSubmit = async () => {
      if (!user_id.value) {
        alert('아이디를 입력해주세요.');
        return;
      }

      if (!user_pw.value) {
        alert('비밀번호를 입력해주세요.');
        return;
      }

      // 로그인 API 호출 (비밀번호 검증 포함)
      const data = await execute(() =>
        apiClient.post(API_ENDPOINTS.AUTH.LOGIN, {
          sampleId: user_id.value,
          password: user_pw.value,
        })
      );

      if (data) {
        // 인증 정보 저장 (액세스 토큰 + 리프레시 토큰 포함)
        login(
          {
            username: data.username,
            firstName: data.firstName,
            lastName: data.lastName,
            email: data.email ?? undefined,
          },
          data.token.accessToken,
          data.token.refreshToken
        );

        // 사용자 정보 페이지로 이동
        router.push(ROUTES.USER_INFO);
      }
    };

    const goToSignup = () => {
      router.push(ROUTES.SIGNUP);
    };

    return {
      user_id,
      user_pw,
      loading,
      logInSubmit,
      goToSignup,
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

.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.input-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.input-label {
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.input-icon {
  position: absolute;
  left: 16px;
  width: 20px;
  height: 20px;
  color: #a0aec0;
  pointer-events: none;
  transition: color 0.2s;
  z-index: 1;
}

.input-field {
  width: 100%;
  padding: 14px 16px 14px 48px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 15px;
  transition: all 0.2s;
  outline: none;
  background: #f7fafc;
  box-sizing: border-box;
}

.input-field:focus {
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.input-wrapper:focus-within .input-icon {
  color: #667eea;
}

.input-field::placeholder {
  color: #cbd5e0;
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
  margin-top: 8px;
}

.btn-login:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.btn-login:active {
  transform: translateY(0);
}

.signup-link {
  text-align: center;
  font-size: 14px;
  color: #4a5568;
  margin-top: 8px;
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
