<template>
  <div class="signup-container">
    <div class="signup-card">
      <div class="signup-header">
        <h1 class="signup-title">Create Account</h1>
        <p class="signup-subtitle">새로운 계정을 만들어보세요</p>
      </div>

      <form class="signup-form" @submit.prevent="signUpSubmit">
        <!-- 기본 정보 섹션 -->
        <div class="form-section">
          <h3 class="section-title">기본 정보</h3>

          <div class="input-group">
            <label :for="'username'" class="input-label">아이디 *</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"></path>
                <circle cx="12" cy="7" r="4"></circle>
              </svg>
              <input
                id="username"
                type="text"
                :class="['input-field', { 'error': idChecked && idDuplicate, 'success': idChecked && !idDuplicate }]"
                placeholder="사용할 아이디를 입력하세요"
                v-model="dict_columns.username"
                @blur="checkIdDuplicate"
                @input="resetIdCheck"
                required
              />
            </div>
            <p v-if="idChecked && idDuplicate" class="error-message">이미 사용 중인 아이디입니다</p>
            <p v-if="idChecked && !idDuplicate" class="success-message">사용 가능한 아이디입니다</p>
          </div>

          <div class="input-row">
            <div class="input-group">
              <label :for="'firstName'" class="input-label">이름 *</label>
              <input
                id="firstName"
                type="text"
                class="input-field"
                placeholder="이름"
                v-model="dict_columns.firstName"
                required
              />
            </div>

            <div class="input-group">
              <label :for="'lastName'" class="input-label">성 *</label>
              <input
                id="lastName"
                type="text"
                class="input-field"
                placeholder="성"
                v-model="dict_columns.lastName"
                required
              />
            </div>
          </div>

          <div class="input-group">
            <label :for="'nickname'" class="input-label">닉네임 *</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M5.52 19c.64-2.2 1.84-3 3.22-3h6.52c1.38 0 2.58.8 3.22 3"></path>
                <circle cx="12" cy="10" r="3"></circle>
                <circle cx="12" cy="12" r="10"></circle>
              </svg>
              <input
                id="nickname"
                type="text"
                :class="['input-field', { 'error': nicknameChecked && nicknameDuplicate, 'success': nicknameChecked && !nicknameDuplicate }]"
                placeholder="닉네임을 입력하세요"
                v-model="dict_columns.nickname"
                @blur="checkNicknameDuplicate"
                @input="resetNicknameCheck"
                required
              />
            </div>
            <p v-if="nicknameChecked && nicknameDuplicate" class="error-message">이미 사용 중인 닉네임입니다</p>
            <p v-if="nicknameChecked && !nicknameDuplicate" class="success-message">사용 가능한 닉네임입니다</p>
          </div>

          <div class="input-group">
            <label :for="'password'" class="input-label">비밀번호 *</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <input
                id="password"
                type="password"
                :class="['input-field', {
                  'error': dict_columns.password && passwordStrength.level === 'weak',
                  'success': dict_columns.password && passwordStrength.level === 'strong'
                }]"
                placeholder="비밀번호 (8자 이상, 영문+숫자+특수문자)"
                v-model="dict_columns.password"
                required
                minlength="8"
              />
            </div>

            <!-- 비밀번호 강도 표시 -->
            <div v-if="dict_columns.password" class="password-strength-container">
              <!-- 강도 게이지 -->
              <div class="strength-meter">
                <div
                  class="strength-meter-fill"
                  :class="[`strength-${passwordStrength.level}`]"
                  :style="{ width: `${passwordStrength.score * 25}%` }"
                ></div>
              </div>
              <div class="strength-label">
                <span :class="[`strength-text-${passwordStrength.level}`]">
                  {{ passwordStrength.text }}
                </span>
              </div>

              <!-- 조건 체크리스트 -->
              <div class="password-requirements">
                <div class="requirement-item" :class="{ 'met': passwordValidation.hasMinLength }">
                  <svg v-if="passwordValidation.hasMinLength" class="check-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                  <svg v-else class="x-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                  <span>8자 이상</span>
                </div>
                <div class="requirement-item" :class="{ 'met': passwordValidation.hasLetter }">
                  <svg v-if="passwordValidation.hasLetter" class="check-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                  <svg v-else class="x-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                  <span>영문자 포함</span>
                </div>
                <div class="requirement-item" :class="{ 'met': passwordValidation.hasNumber }">
                  <svg v-if="passwordValidation.hasNumber" class="check-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                  <svg v-else class="x-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                  <span>숫자 포함</span>
                </div>
                <div class="requirement-item" :class="{ 'met': passwordValidation.hasSpecial }">
                  <svg v-if="passwordValidation.hasSpecial" class="check-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <polyline points="20 6 9 17 4 12"></polyline>
                  </svg>
                  <svg v-else class="x-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                    <line x1="18" y1="6" x2="6" y2="18"></line>
                    <line x1="6" y1="6" x2="18" y2="18"></line>
                  </svg>
                  <span>특수문자 포함</span>
                </div>
              </div>
            </div>
          </div>

          <div class="input-group">
            <label :for="'passwordConfirm'" class="input-label">비밀번호 확인 *</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2"></rect>
                <path d="M7 11V7a5 5 0 0 1 10 0v4"></path>
              </svg>
              <input
                id="passwordConfirm"
                type="password"
                class="input-field"
                :class="{ 'error': passwordConfirm && passwordConfirm !== dict_columns.password }"
                placeholder="비밀번호를 다시 입력하세요"
                v-model="passwordConfirm"
                required
              />
            </div>
            <p v-if="passwordConfirm && passwordConfirm !== dict_columns.password" class="error-message">
              비밀번호가 일치하지 않습니다
            </p>
          </div>
        </div>

        <!-- 연락처 정보 섹션 -->
        <div class="form-section">
          <h3 class="section-title">연락처 정보</h3>

          <div class="input-group">
            <label :for="'email'" class="input-label">이메일 *</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"></path>
                <polyline points="22,6 12,13 2,6"></polyline>
              </svg>
              <input
                id="email"
                type="email"
                class="input-field"
                placeholder="example@email.com"
                v-model="dict_columns.email"
                required
              />
            </div>
          </div>

          <div class="input-group">
            <label :for="'phone'" class="input-label">전화번호 *</label>
            <div class="input-wrapper">
              <svg class="input-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z"></path>
              </svg>
              <input
                id="phone"
                type="tel"
                class="input-field"
                placeholder="010-1234-5678"
                v-model="dict_columns.phone"
                required
              />
            </div>
          </div>
        </div>

        <!-- 주소 정보 섹션 -->
        <div class="form-section">
          <h3 class="section-title">주소 정보</h3>

          <div class="input-group">
            <label :for="'country'" class="input-label">국가</label>
            <input
              id="country"
              type="text"
              class="input-field"
              placeholder="국가"
              v-model="dict_columns.country"
            />
          </div>

          <div class="input-row">
            <div class="input-group">
              <label :for="'city'" class="input-label">도시</label>
              <input
                id="city"
                type="text"
                class="input-field"
                placeholder="도시"
                v-model="dict_columns.city"
              />
            </div>

            <div class="input-group">
              <label :for="'state'" class="input-label">주/도</label>
              <input
                id="state"
                type="text"
                class="input-field"
                placeholder="주/도"
                v-model="dict_columns.state"
              />
            </div>
          </div>

          <div class="input-group">
            <label :for="'street1'" class="input-label">주소 1</label>
            <input
              id="street1"
              type="text"
              class="input-field"
              placeholder="상세 주소"
              v-model="dict_columns.street1"
            />
          </div>

          <div class="input-group">
            <label :for="'street2'" class="input-label">주소 2</label>
            <input
              id="street2"
              type="text"
              class="input-field"
              placeholder="상세 주소 (선택사항)"
              v-model="dict_columns.street2"
            />
          </div>

          <div class="input-group">
            <label :for="'zip'" class="input-label">우편번호</label>
            <input
              id="zip"
              type="text"
              class="input-field"
              placeholder="우편번호"
              v-model="dict_columns.zip"
            />
          </div>
        </div>

        <button type="submit" class="btn-signup">
          <span>가입하기</span>
        </button>

        <div class="login-link">
          이미 계정이 있으신가요?
          <a href="#" @click.prevent="goToLogin">로그인</a>
        </div>
      </form>
    </div>
  </div>
</template>

<script lang="ts">
import { defineComponent, reactive, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import apiClient from '@/api/client';
import { useApi } from '@/composables/useApi';
import { API_ENDPOINTS, ROUTES, SUCCESS_MESSAGES } from '@/constants';
import type { CreateSampleRequest, Sample } from '@/types/domain.types';

export default defineComponent({
  name: 'SignUp',
  setup() {
    const router = useRouter();
    const { loading, execute } = useApi<Sample>();
    const passwordConfirm = ref('');
    const idChecked = ref(false);
    const idDuplicate = ref(false);
    const nicknameChecked = ref(false);
    const nicknameDuplicate = ref(false);

    const formData = reactive<CreateSampleRequest>({
      username: '',
      firstName: '',
      lastName: '',
      password: '',
      nickname: '',
      email: '',
      phone: '',
      country: '',
      city: '',
      state: '',
      street1: '',
      street2: '',
      zip: '',
    });

    // 비밀번호 유효성 검증 (실시간)
    const passwordValidation = computed(() => {
      const password = formData.password;
      return {
        hasMinLength: password.length >= 8,
        hasLetter: /[a-zA-Z]/.test(password),
        hasNumber: /\d/.test(password),
        hasSpecial: /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/.test(password),
      };
    });

    // 비밀번호 강도 계산
    const passwordStrength = computed(() => {
      const password = formData.password;
      if (!password) {
        return { level: 'none', text: '', score: 0 };
      }

      const validation = passwordValidation.value;
      let score = 0;

      // 각 조건당 1점
      if (validation.hasMinLength) score++;
      if (validation.hasLetter) score++;
      if (validation.hasNumber) score++;
      if (validation.hasSpecial) score++;

      // 추가 점수: 12자 이상이면 보너스
      if (password.length >= 12) score += 0.5;
      // 대소문자 혼합이면 보너스
      if (/[a-z]/.test(password) && /[A-Z]/.test(password)) score += 0.5;

      // 점수를 4점 만점으로 정규화
      score = Math.min(score, 4);

      // 강도 레벨 결정
      let level: 'weak' | 'medium' | 'strong';
      let text: string;

      if (score < 2) {
        level = 'weak';
        text = '취약';
      } else if (score < 4) {
        level = 'medium';
        text = '보통';
      } else {
        level = 'strong';
        text = '강력';
      }

      return { level, text, score };
    });

    // 아이디 중복 확인
    const checkIdDuplicate = async () => {
      if (!formData.username || formData.username.trim().length < 3) {
        idChecked.value = false;
        return;
      }

      try {
        const response = await apiClient.get(
          API_ENDPOINTS.SAMPLE_CHECK_DUPLICATE(formData.username)
        );
        // DuplicateCheckResponse: { exists, available, value }
        idDuplicate.value = response.data.data.exists;
        idChecked.value = true;
      } catch (error) {
        console.error('아이디 중복 확인 중 오류 발생:', error);
        idChecked.value = false;
      }
    };

    // 아이디 입력 시 중복 확인 상태 초기화
    const resetIdCheck = () => {
      idChecked.value = false;
      idDuplicate.value = false;
    };

    // 닉네임 중복 확인
    const checkNicknameDuplicate = async () => {
      if (!formData.nickname || formData.nickname.trim().length < 2) {
        nicknameChecked.value = false;
        return;
      }

      try {
        const response = await apiClient.get(
          API_ENDPOINTS.SAMPLE_CHECK_NICKNAME_DUPLICATE(formData.nickname)
        );
        // DuplicateCheckResponse: { exists, available, value }
        nicknameDuplicate.value = response.data.data.exists;
        nicknameChecked.value = true;
      } catch (error) {
        console.error('닉네임 중복 확인 중 오류 발생:', error);
        nicknameChecked.value = false;
      }
    };

    // 닉네임 입력 시 중복 확인 상태 초기화
    const resetNicknameCheck = () => {
      nicknameChecked.value = false;
      nicknameDuplicate.value = false;
    };

    const signUpSubmit = async () => {
      // 아이디 중복 확인 여부 체크
      if (!idChecked.value) {
        alert('아이디 중복 확인이 필요합니다.');
        return;
      }

      // 아이디 중복 확인
      if (idDuplicate.value) {
        alert('이미 사용 중인 아이디입니다. 다른 아이디를 입력해주세요.');
        return;
      }

      // 닉네임 중복 확인 여부 체크
      if (!nicknameChecked.value) {
        alert('닉네임 중복 확인이 필요합니다.');
        return;
      }

      // 닉네임 중복 확인
      if (nicknameDuplicate.value) {
        alert('이미 사용 중인 닉네임입니다. 다른 닉네임을 입력해주세요.');
        return;
      }

      // 비밀번호 확인 검증
      if (formData.password !== passwordConfirm.value) {
        alert('비밀번호가 일치하지 않습니다.');
        return;
      }

      // 비밀번호 강도 검증
      if (passwordStrength.value.score < 4) {
        alert('비밀번호가 안전하지 않습니다. 모든 조건을 만족하는 비밀번호를 입력해주세요.');
        return;
      }

      const result = await execute(
        () => apiClient.post(API_ENDPOINTS.SAMPLES, formData),
        {
          onSuccess: () => {
            alert(SUCCESS_MESSAGES.SIGNUP_SUCCESS);
            router.push(ROUTES.LOGIN);
          },
        }
      );
    };

    const goToLogin = () => {
      router.push(ROUTES.LOGIN);
    };

    return {
      dict_columns: formData,
      passwordConfirm,
      idChecked,
      idDuplicate,
      nicknameChecked,
      nicknameDuplicate,
      passwordValidation,
      passwordStrength,
      loading,
      checkIdDuplicate,
      resetIdCheck,
      checkNicknameDuplicate,
      resetNicknameCheck,
      signUpSubmit,
      goToLogin,
    };
  },
});
</script>

<style scoped>
.signup-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px;
}

.signup-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  padding: 48px 40px;
  width: 100%;
  max-width: 600px;
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

.signup-header {
  text-align: center;
  margin-bottom: 32px;
}

.signup-title {
  font-size: 32px;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.signup-subtitle {
  color: #718096;
  font-size: 14px;
  margin: 0;
}

.signup-form {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #2d3748;
  margin: 0 0 8px 0;
  padding-bottom: 8px;
  border-bottom: 2px solid #e2e8f0;
}

.input-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
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
  padding: 12px 16px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  font-size: 15px;
  transition: all 0.2s;
  outline: none;
  background: #f7fafc;
  box-sizing: border-box;
}

.input-wrapper .input-field {
  padding-left: 48px;
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

.input-field.error {
  border-color: #f56565;
  background-color: #fff5f5;
}

.input-field.error:focus {
  border-color: #f56565;
  box-shadow: 0 0 0 3px rgba(245, 101, 101, 0.1);
}

.input-field.success {
  border-color: #48bb78;
  background-color: #f0fff4;
}

.input-field.success:focus {
  border-color: #48bb78;
  box-shadow: 0 0 0 3px rgba(72, 187, 120, 0.1);
}

.input-hint {
  font-size: 12px;
  color: #718096;
  margin: 4px 0 0 0;
}

.error-message {
  font-size: 12px;
  color: #f56565;
  margin: 4px 0 0 0;
  font-weight: 500;
}

.success-message {
  font-size: 12px;
  color: #48bb78;
  margin: 4px 0 0 0;
  font-weight: 500;
}

/* 비밀번호 강도 표시 스타일 */
.password-strength-container {
  margin-top: 12px;
  padding: 16px;
  background: #f7fafc;
  border-radius: 12px;
  border: 1px solid #e2e8f0;
}

.strength-meter {
  width: 100%;
  height: 6px;
  background: #e2e8f0;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 8px;
}

.strength-meter-fill {
  height: 100%;
  transition: all 0.3s ease;
  border-radius: 3px;
}

.strength-meter-fill.strength-weak {
  background: linear-gradient(90deg, #f56565, #fc8181);
}

.strength-meter-fill.strength-medium {
  background: linear-gradient(90deg, #ed8936, #f6ad55);
}

.strength-meter-fill.strength-strong {
  background: linear-gradient(90deg, #48bb78, #68d391);
}

.strength-label {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.strength-label span {
  font-size: 13px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 12px;
}

.strength-text-weak {
  color: #c53030;
  background: #fed7d7;
}

.strength-text-medium {
  color: #c05621;
  background: #feebc8;
}

.strength-text-strong {
  color: #276749;
  background: #c6f6d5;
}

.password-requirements {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 8px;
}

.requirement-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #718096;
  transition: all 0.2s;
}

.requirement-item.met {
  color: #48bb78;
}

.check-icon {
  width: 16px;
  height: 16px;
  color: #48bb78;
  flex-shrink: 0;
}

.x-icon {
  width: 16px;
  height: 16px;
  color: #cbd5e0;
  flex-shrink: 0;
}

.btn-signup {
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

.btn-signup:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
}

.btn-signup:active {
  transform: translateY(0);
}

.login-link {
  text-align: center;
  font-size: 14px;
  color: #4a5568;
  margin-top: 8px;
}

.login-link a {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  margin-left: 4px;
  transition: color 0.2s;
}

.login-link a:hover {
  color: #764ba2;
}

@media (max-width: 640px) {
  .signup-card {
    padding: 32px 24px;
  }

  .signup-title {
    font-size: 28px;
  }

  .input-row {
    grid-template-columns: 1fr;
  }

  .password-requirements {
    grid-template-columns: 1fr;
  }
}
</style>
