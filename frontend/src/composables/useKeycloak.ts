import { ref, computed } from 'vue';
import Keycloak from 'keycloak-js';

/**
 * Keycloak 설정 인터페이스
 */
interface KeycloakConfig {
  url: string;
  realm: string;
  clientId: string;
}

/**
 * 사용자 정보 인터페이스
 */
interface KeycloakUser {
  id: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
}

/**
 * Keycloak 인스턴스 (싱글톤)
 */
let keycloakInstance: Keycloak | null = null;

/**
 * 상태 관리
 */
const isInitialized = ref(false);
const isAuthenticated = ref(false);
const currentUser = ref<KeycloakUser | null>(null);
const token = ref<string | null>(null);
const refreshToken = ref<string | null>(null);

/**
 * 기본 Keycloak 설정
 */
const defaultConfig: KeycloakConfig = {
  url: process.env.VUE_APP_KEYCLOAK_URL || 'http://localhost:8180',
  realm: process.env.VUE_APP_KEYCLOAK_REALM || 'hamkkebu',
  clientId: process.env.VUE_APP_KEYCLOAK_CLIENT_ID || 'hamkkebu-frontend',
};

/**
 * Keycloak Composable
 */
export function useKeycloak() {
  /**
   * Keycloak 초기화
   */
  const init = async (config: Partial<KeycloakConfig> = {}): Promise<boolean> => {
    if (isInitialized.value && keycloakInstance) {
      return isAuthenticated.value;
    }

    const finalConfig = { ...defaultConfig, ...config };

    keycloakInstance = new Keycloak({
      url: finalConfig.url,
      realm: finalConfig.realm,
      clientId: finalConfig.clientId,
    });

    try {
      const authenticated = await keycloakInstance.init({
        onLoad: 'check-sso',
        silentCheckSsoRedirectUri: window.location.origin + '/silent-check-sso.html',
        pkceMethod: 'S256',
        checkLoginIframe: true,
        checkLoginIframeInterval: 5, // 5초마다 세션 상태 확인
      });

      isInitialized.value = true;
      isAuthenticated.value = authenticated;

      if (authenticated) {
        updateUserInfo();
        setupTokenRefresh();
      }

      // 토큰 만료 이벤트
      keycloakInstance.onTokenExpired = () => {
        console.log('Token expired, refreshing...');
        refreshTokens();
      };

      // 인증 에러 이벤트
      keycloakInstance.onAuthError = (error) => {
        console.error('Auth error:', error);
        clearAuthState();
      };

      // 로그아웃 이벤트
      keycloakInstance.onAuthLogout = () => {
        console.log('User logged out');
        clearAuthState();
        // SSO 로그아웃 감지 시 로그인 페이지로 리다이렉트
        window.location.href = window.location.origin;
      };

      // 세션 상태 변경 이벤트 (다른 앱에서 로그아웃 시)
      keycloakInstance.onAuthRefreshError = () => {
        console.log('Auth refresh error - session may have ended');
        clearAuthState();
        window.location.href = window.location.origin;
      };

      return authenticated;
    } catch (error) {
      console.error('Failed to initialize Keycloak:', error);
      isInitialized.value = false;
      isAuthenticated.value = false;
      return false;
    }
  };

  /**
   * 로그인
   */
  const login = async (redirectUri?: string): Promise<void> => {
    if (!keycloakInstance) {
      await init();
    }

    await keycloakInstance?.login({
      redirectUri: redirectUri || window.location.href,
    });
  };

  /**
   * 로그아웃
   */
  const logout = async (redirectUri?: string): Promise<void> => {
    if (!keycloakInstance) {
      return;
    }

    clearAuthState();

    await keycloakInstance.logout({
      redirectUri: redirectUri || window.location.origin,
    });
  };

  /**
   * 회원가입 페이지로 이동
   */
  const register = async (redirectUri?: string): Promise<void> => {
    if (!keycloakInstance) {
      await init();
    }

    await keycloakInstance?.register({
      redirectUri: redirectUri || window.location.href,
    });
  };

  /**
   * 계정 관리 페이지로 이동
   */
  const accountManagement = async (): Promise<void> => {
    if (!keycloakInstance) {
      return;
    }

    await keycloakInstance.accountManagement();
  };

  /**
   * 토큰 갱신
   */
  const refreshTokens = async (): Promise<boolean> => {
    if (!keycloakInstance) {
      return false;
    }

    try {
      const refreshed = await keycloakInstance.updateToken(30);
      if (refreshed) {
        updateUserInfo();
        console.log('Token refreshed');
      }
      return true;
    } catch (error) {
      console.error('Failed to refresh token:', error);
      clearAuthState();
      return false;
    }
  };

  /**
   * 사용자 정보 업데이트
   */
  const updateUserInfo = (): void => {
    if (!keycloakInstance || !keycloakInstance.tokenParsed) {
      return;
    }

    const tokenParsed = keycloakInstance.tokenParsed as Record<string, unknown>;

    token.value = keycloakInstance.token || null;
    refreshToken.value = keycloakInstance.refreshToken || null;

    // Realm roles 추출
    const realmAccess = tokenParsed.realm_access as { roles?: string[] } | undefined;
    const roles = realmAccess?.roles || [];

    currentUser.value = {
      id: keycloakInstance.subject || '',
      username: (tokenParsed.preferred_username as string) || '',
      email: (tokenParsed.email as string) || '',
      firstName: (tokenParsed.given_name as string) || '',
      lastName: (tokenParsed.family_name as string) || '',
      roles,
    };

    // localStorage에도 저장 (다른 컴포넌트에서 사용)
    if (token.value) {
      localStorage.setItem('authToken', token.value);
    }
    if (refreshToken.value) {
      localStorage.setItem('refreshToken', refreshToken.value);
    }
    if (currentUser.value) {
      localStorage.setItem('currentUser', JSON.stringify(currentUser.value));
    }
  };

  /**
   * 인증 상태 초기화
   */
  const clearAuthState = (): void => {
    isAuthenticated.value = false;
    currentUser.value = null;
    token.value = null;
    refreshToken.value = null;

    localStorage.removeItem('authToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('currentUser');
  };

  /**
   * 토큰 자동 갱신 설정
   */
  const setupTokenRefresh = (): void => {
    // 토큰 만료 1분 전에 자동 갱신
    setInterval(() => {
      if (keycloakInstance?.isTokenExpired(60)) {
        refreshTokens();
      }
    }, 10000); // 10초마다 체크
  };

  /**
   * 특정 역할 보유 여부 확인
   */
  const hasRole = (role: string): boolean => {
    return keycloakInstance?.hasRealmRole(role) || false;
  };

  /**
   * 특정 리소스 역할 보유 여부 확인
   */
  const hasResourceRole = (role: string, resource: string): boolean => {
    return keycloakInstance?.hasResourceRole(role, resource) || false;
  };

  /**
   * Access Token 가져오기
   */
  const getToken = async (): Promise<string | null> => {
    if (!keycloakInstance) {
      return null;
    }

    // 토큰이 만료 예정이면 갱신
    if (keycloakInstance.isTokenExpired(30)) {
      await refreshTokens();
    }

    return keycloakInstance.token || null;
  };

  /**
   * Keycloak 인스턴스 가져오기 (고급 사용)
   */
  const getKeycloakInstance = (): Keycloak | null => {
    return keycloakInstance;
  };

  return {
    // 상태
    isInitialized: computed(() => isInitialized.value),
    isAuthenticated: computed(() => isAuthenticated.value),
    currentUser: computed(() => currentUser.value),
    token: computed(() => token.value),

    // 메서드
    init,
    login,
    logout,
    register,
    accountManagement,
    refreshTokens,
    hasRole,
    hasResourceRole,
    getToken,
    getKeycloakInstance,
  };
}
