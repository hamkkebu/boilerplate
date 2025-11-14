import { ref, computed } from 'vue';
import type { AuthUser } from '@/types/domain.types';

/**
 * 인증 상태 관리 Composable
 */
const currentUser = ref<AuthUser | null>(null);
const isAuthenticated = computed(() => currentUser.value !== null);

export function useAuth() {
  /**
   * 로그인
   */
  const login = (user: AuthUser, token?: string) => {
    currentUser.value = user;

    if (token) {
      localStorage.setItem('authToken', token);
    }

    localStorage.setItem('currentUser', JSON.stringify(user));
  };

  /**
   * 로그아웃
   */
  const logout = () => {
    currentUser.value = null;
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
  };

  /**
   * 저장된 사용자 정보 복원
   */
  const restoreUser = () => {
    const userJson = localStorage.getItem('currentUser');
    if (userJson) {
      try {
        currentUser.value = JSON.parse(userJson);
      } catch (error) {
        console.error('Failed to restore user:', error);
        logout();
      }
    }
  };

  /**
   * 인증 토큰 가져오기
   */
  const getToken = (): string | null => {
    return localStorage.getItem('authToken');
  };

  return {
    currentUser: computed(() => currentUser.value),
    isAuthenticated,
    login,
    logout,
    restoreUser,
    getToken,
  };
}
