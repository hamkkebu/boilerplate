import { ref, computed } from 'vue';
import apiClient from '@/api/client';
import { API_ENDPOINTS } from '@/constants';
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
  const login = (user: AuthUser, accessToken?: string, refreshToken?: string) => {
    currentUser.value = user;

    if (accessToken) {
      localStorage.setItem('authToken', accessToken);
    }

    if (refreshToken) {
      localStorage.setItem('refreshToken', refreshToken);
    }

    localStorage.setItem('currentUser', JSON.stringify(user));
  };

  /**
   * 로그아웃
   */
  const logout = async () => {
    try {
      // 백엔드에 로그아웃 요청하여 토큰 무효화
      const refreshToken = localStorage.getItem('refreshToken');
      await apiClient.post(API_ENDPOINTS.AUTH.LOGOUT, null, {
        headers: {
          'Refresh-Token': refreshToken || '',
        },
      });
    } catch (error) {
      console.error('Logout API error:', error);
      // API 호출 실패해도 로컬 데이터는 삭제
    } finally {
      // 로컬 스토리지 정리
      currentUser.value = null;
      localStorage.removeItem('authToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('currentUser');
    }
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
