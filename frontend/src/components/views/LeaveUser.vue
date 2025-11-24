<template>
  <div class="leave-container">
    <div class="leave-card">
      <div class="warning-icon">
        <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10"></circle>
          <line x1="12" y1="8" x2="12" y2="12"></line>
          <line x1="12" y1="16" x2="12.01" y2="16"></line>
        </svg>
      </div>

      <div class="leave-header">
        <h1 class="leave-title">회원탈퇴</h1>
        <p class="leave-subtitle">정말로 탈퇴하시겠습니까?</p>
      </div>

      <div class="warning-message">
        <div class="warning-content">
          <h3>탈퇴 시 유의사항</h3>
          <ul>
            <li>계정의 모든 정보가 영구적으로 삭제됩니다</li>
            <li>삭제된 데이터는 복구할 수 없습니다</li>
            <li>탈퇴 후 동일한 아이디 및 닉네임으로 재가입할 수 없습니다</li>
          </ul>
        </div>
      </div>

      <form class="leave-form" @submit.prevent="confirmAndLeave">
        <div class="input-group">
          <label for="password" class="input-label">비밀번호 확인</label>
          <div class="input-wrapper">
            <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
              <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
            </svg>
            <input
              id="password"
              type="password"
              class="input-field"
              placeholder="비밀번호를 입력하세요"
              v-model="password"
              required
            />
          </div>
        </div>

        <div class="confirmation-checkbox">
          <label class="checkbox-label">
            <input type="checkbox" v-model="confirmed" required />
            <span>위 유의사항을 모두 확인했으며, 탈퇴에 동의합니다</span>
          </label>
        </div>

        <div class="button-group">
          <button type="button" @click="goBack" class="btn-cancel">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="15 18 9 12 15 6"></polyline>
            </svg>
            <span>취소</span>
          </button>
          <button type="submit" class="btn-leave" :disabled="!confirmed || !password">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="3 6 5 6 21 6"></polyline>
              <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2"></path>
            </svg>
            <span>탈퇴하기</span>
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/api/client';
import { useApi } from '@/composables/useApi';
import { useAuth } from '@/composables/useAuth';
import { API_ENDPOINTS, ROUTES, SUCCESS_MESSAGES } from '@/constants';

export default defineComponent({
  name: 'LeaveUser',
  setup() {
    const router = useRouter();
    const { loading, execute } = useApi();
    const { currentUser, restoreUser, logout } = useAuth();

    const password = ref('');
    const confirmed = ref(false);

    // 컴포넌트 마운트 시 사용자 정보 복원
    onMounted(() => {
      restoreUser();

      // 로그인하지 않은 경우 로그인 페이지로 리다이렉트
      if (!currentUser.value) {
        alert('로그인이 필요합니다.');
        router.push(ROUTES.LOGIN);
      }
    });

    const confirmAndLeave = () => {
      if (!confirmed.value) {
        alert('탈퇴 동의에 체크해주세요.');
        return;
      }

      if (!password.value) {
        alert('비밀번호를 입력해주세요.');
        return;
      }

      if (!currentUser.value) {
        alert('로그인 정보를 찾을 수 없습니다.');
        router.push(ROUTES.LOGIN);
        return;
      }

      if (
        confirm(
          `정말로 "${currentUser.value.username}" 계정을 탈퇴하시겠습니까?\n이 작업은 되돌릴 수 없습니다.`
        )
      ) {
        leaveSubmit();
      }
    };

    const leaveSubmit = async () => {
      if (!currentUser.value) {
        return;
      }

      await execute(
        () =>
          apiClient.delete(API_ENDPOINTS.SAMPLE_BY_ID(currentUser.value!.username), {
            data: {
              password: password.value,
            },
            headers: {
              'Refresh-Token': localStorage.getItem('refreshToken') || '',
            },
          }),
        {
          onSuccess: () => {
            alert('회원 탈퇴가 완료되었습니다.');
            logout();
            router.push(ROUTES.LOGIN);
          },
        }
      );
    };

    const goBack = () => {
      router.push(ROUTES.USER_INFO);
    };

    return {
      password,
      confirmed,
      loading,
      confirmAndLeave,
      goBack,
    };
  },
});
</script>

<style scoped>
.leave-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.leave-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 48px 40px;
  width: 100%;
  max-width: 560px;
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

.warning-icon {
  width: 80px;
  height: 80px;
  margin: 0 auto 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 0 0 rgba(245, 101, 101, 0.7);
  }
  50% {
    transform: scale(1.05);
    box-shadow: 0 0 0 10px rgba(245, 101, 101, 0);
  }
}

.warning-icon svg {
  width: 48px;
  height: 48px;
  color: white;
}

.leave-header {
  text-align: center;
  margin-bottom: 24px;
}

.leave-title {
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 8px 0;
}

.leave-subtitle {
  color: #718096;
  font-size: 14px;
  margin: 0;
}

.warning-message {
  background: #fff5f5;
  border: 2px solid #feb2b2;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 24px;
}

.warning-content h3 {
  color: #c53030;
  font-size: 16px;
  margin: 0 0 12px 0;
}

.warning-content ul {
  margin: 0;
  padding-left: 20px;
  color: #742a2a;
}

.warning-content li {
  margin: 8px 0;
  font-size: 14px;
  line-height: 1.5;
}

.leave-form {
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
}

.input-field:focus {
  border-color: #f56565;
  background: white;
  box-shadow: 0 0 0 3px rgba(245, 101, 101, 0.1);
}

.input-wrapper:focus-within .input-icon {
  color: #f56565;
}

.input-field::placeholder {
  color: #cbd5e0;
}

.confirmation-checkbox {
  background: #f7fafc;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  padding: 16px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  color: #2d3748;
  font-size: 14px;
}

.checkbox-label input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #f56565;
}

.button-group {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.btn-cancel,
.btn-leave {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 14px;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-cancel {
  background: white;
  color: #4a5568;
  border: 2px solid #e2e8f0;
}

.btn-cancel:hover {
  border-color: #cbd5e0;
  background: #f7fafc;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.btn-cancel svg {
  width: 18px;
  height: 18px;
}

.btn-leave {
  background: linear-gradient(135deg, #f56565 0%, #e53e3e 100%);
  color: white;
  box-shadow: 0 4px 15px rgba(245, 101, 101, 0.4);
}

.btn-leave:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(245, 101, 101, 0.6);
}

.btn-leave:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-leave svg {
  width: 18px;
  height: 18px;
}

@media (max-width: 640px) {
  .leave-card {
    padding: 32px 24px;
  }

  .leave-title {
    font-size: 28px;
  }

  .warning-icon {
    width: 70px;
    height: 70px;
  }

  .warning-icon svg {
    width: 40px;
    height: 40px;
  }

  .button-group {
    flex-direction: column;
  }
}
</style>
