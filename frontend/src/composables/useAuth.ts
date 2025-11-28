import { ref, computed } from 'vue';
import apiClient, { setTokenProvider } from '@/api/client';
import { API_ENDPOINTS } from '@/constants';
import { useKeycloak } from './useKeycloak';
import type { AuthUser } from '@/types/domain.types';

/**
 * 인증 모드
 */
type AuthMode = 'keycloak' | 'jwt';

/**
 * 인증 상태 관리 Composable
 *
 * Keycloak SSO 또는 자체 JWT 인증을 지원합니다.
 * VUE_APP_AUTH_MODE 환경변수로 모드 전환 가능:
 * - 'keycloak': Keycloak SSO 사용
 * - 'jwt': 자체 JWT 인증 사용 (기본값)
 */
const currentUser = ref<AuthUser | null>(null);
const isAuthenticated = computed(() => currentUser.value !== null);

/**
 * 인증 모드 결정
 */
const authMode: AuthMode = (process.env.VUE_APP_AUTH_MODE as AuthMode) || 'jwt';
const isKeycloakMode = authMode === 'keycloak';

/**
 * 앱 시작 시 localStorage에서 사용자 정보 자동 복원
 * Keycloak 모드에서는 initAuth()에서 처리하므로 여기서는 JWT 모드만 복원
 */
const initializeAuth = () => {
  // Keycloak 모드에서는 세션 확인 후 복원하므로 여기서 복원하지 않음
  if (authMode === 'keycloak') {
    return;
  }

  const userJson = localStorage.getItem('currentUser');
  if (userJson) {
    try {
      currentUser.value = JSON.parse(userJson);
    } catch (error) {
      console.error('Failed to initialize auth:', error);
      localStorage.removeItem('authToken');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('currentUser');
    }
  }
};

// 모듈 로드 시 자동으로 사용자 정보 복원 (JWT 모드만)
initializeAuth();

export function useAuth() {
  const keycloak = useKeycloak();

  /**
   * 인증 초기화 (Keycloak 모드 전용)
   */
  const initAuth = async (): Promise<boolean> => {
    if (!isKeycloakMode) {
      return isAuthenticated.value;
    }

    // Keycloak 모드: API 클라이언트에 토큰 제공자 설정
    setTokenProvider(() => keycloak.getToken());

    const authenticated = await keycloak.init();

    if (authenticated && keycloak.currentUser.value) {
      // Keycloak 사용자 정보를 AuthUser 형식으로 변환
      currentUser.value = {
        id: parseInt(keycloak.currentUser.value.id) || 0,
        username: keycloak.currentUser.value.username,
        email: keycloak.currentUser.value.email,
        firstName: keycloak.currentUser.value.firstName,
        lastName: keycloak.currentUser.value.lastName,
        role: keycloak.currentUser.value.roles.includes('ADMIN') ? 'ADMIN' :
              keycloak.currentUser.value.roles.includes('DEVELOPER') ? 'DEVELOPER' : 'USER',
        isActive: true,
        isVerified: true,
      };
    } else {
      // Keycloak 세션이 유효하지 않으면 상태 초기화
      currentUser.value = null;
    }

    return authenticated;
  };

  /**
   * 로그인
   */
  const login = async (
    user?: AuthUser,
    accessToken?: string,
    refreshToken?: string,
    redirectUri?: string
  ): Promise<void> => {
    if (isKeycloakMode) {
      // Keycloak 로그인 페이지로 리다이렉트
      await keycloak.login(redirectUri);
    } else {
      // 자체 JWT 로그인
      if (user) {
        currentUser.value = user;

        if (accessToken) {
          localStorage.setItem('authToken', accessToken);
        }

        if (refreshToken) {
          localStorage.setItem('refreshToken', refreshToken);
        }

        localStorage.setItem('currentUser', JSON.stringify(user));
      }
    }
  };

  /**
   * 로그아웃
   */
  const logout = async (redirectUri?: string): Promise<void> => {
    if (isKeycloakMode) {
      // Keycloak SSO 로그아웃 (모든 서비스에서 로그아웃)
      await keycloak.logout(redirectUri);
    } else {
      // 자체 JWT 로그아웃
      try {
        const refreshTokenValue = localStorage.getItem('refreshToken');
        await apiClient.post(API_ENDPOINTS.AUTH.LOGOUT, null, {
          headers: {
            'Refresh-Token': refreshTokenValue || '',
          },
        });
      } catch (error) {
        console.error('Logout API error:', error);
      } finally {
        // 로컬 스토리지 정리
        currentUser.value = null;
        localStorage.removeItem('authToken');
        localStorage.removeItem('refreshToken');
        localStorage.removeItem('currentUser');
      }
    }
  };

  /**
   * 회원가입 (Keycloak 모드)
   */
  const register = async (redirectUri?: string): Promise<void> => {
    if (isKeycloakMode) {
      await keycloak.register(redirectUri);
    }
  };

  /**
   * 계정 관리 (Keycloak 모드)
   */
  const accountManagement = async (): Promise<void> => {
    if (isKeycloakMode) {
      await keycloak.accountManagement();
    }
  };

  /**
   * 저장된 사용자 정보 복원 (JWT 모드 전용)
   */
  const restoreUser = () => {
    if (isKeycloakMode) {
      // Keycloak 모드에서는 initAuth()를 통해 복원
      console.warn('restoreUser() is not used in Keycloak mode. Use initAuth() instead.');
      return;
    }

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
  const getToken = async (): Promise<string | null> => {
    if (isKeycloakMode) {
      return await keycloak.getToken();
    }
    return localStorage.getItem('authToken');
  };

  /**
   * 동기적으로 토큰 가져오기 (JWT 모드 전용, 기존 호환성)
   * Keycloak 모드에서는 getToken()을 사용하세요
   */
  const getTokenSync = (): string | null => {
    if (isKeycloakMode) {
      // Keycloak 모드에서는 토큰이 메모리에만 있으므로 동기적으로 가져올 수 없음
      console.warn('getTokenSync() is not recommended in Keycloak mode. Use getToken() instead.');
      return keycloak.token.value;
    }
    return localStorage.getItem('authToken');
  };

  /**
   * 특정 역할 보유 여부 확인
   */
  const hasRole = (role: string): boolean => {
    if (isKeycloakMode) {
      return keycloak.hasRole(role);
    }
    return currentUser.value?.role === role;
  };

  /**
   * 관리자 여부 확인
   */
  const isAdmin = computed(() => {
    return hasRole('ADMIN') || hasRole('DEVELOPER');
  });

  return {
    // 상태
    currentUser: computed(() => currentUser.value),
    isAuthenticated,
    isAdmin,
    authMode,
    isKeycloakMode,

    // 메서드
    initAuth,
    login,
    logout,
    register,
    accountManagement,
    restoreUser,
    getToken,
    getTokenSync,
    hasRole,
  };
}
