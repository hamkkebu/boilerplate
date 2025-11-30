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
 * Direct Login 결과 인터페이스
 */
interface DirectLoginResult {
  success: boolean;
  error?: string;
}

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
   * 로그인 (Keycloak 로그인 페이지로 리다이렉트)
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
   * Direct Login (Resource Owner Password Credentials)
   * 커스텀 로그인 폼에서 사용자명/비밀번호를 직접 전송
   */
  const directLogin = async (username: string, password: string): Promise<DirectLoginResult> => {
    const config = defaultConfig;
    const tokenUrl = `${config.url}/realms/${config.realm}/protocol/openid-connect/token`;

    try {
      const response = await fetch(tokenUrl, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({
          grant_type: 'password',
          client_id: config.clientId,
          username,
          password,
          scope: 'openid profile email',
        }),
      });

      if (!response.ok) {
        const errorData = await response.json();
        let errorMessage = '로그인에 실패했습니다.';

        if (errorData.error === 'invalid_grant') {
          errorMessage = '아이디 또는 비밀번호가 올바르지 않습니다.';
        } else if (errorData.error_description) {
          errorMessage = errorData.error_description;
        }

        return { success: false, error: errorMessage };
      }

      const tokenData = await response.json();

      // 토큰 저장
      token.value = tokenData.access_token;
      refreshToken.value = tokenData.refresh_token;
      isAuthenticated.value = true;

      // 토큰 파싱하여 사용자 정보 추출
      const tokenParts = tokenData.access_token.split('.');
      const payload = JSON.parse(atob(tokenParts[1]));

      const realmAccess = payload.realm_access as { roles?: string[] } | undefined;
      const roles = realmAccess?.roles || [];

      currentUser.value = {
        id: payload.sub || '',
        username: payload.preferred_username || '',
        email: payload.email || '',
        firstName: payload.given_name || '',
        lastName: payload.family_name || '',
        roles,
      };

      // Keycloak 인스턴스 초기화 (세션 관리용)
      if (!keycloakInstance) {
        keycloakInstance = new Keycloak({
          url: config.url,
          realm: config.realm,
          clientId: config.clientId,
        });
      }

      return { success: true };
    } catch (error) {
      console.error('Direct login failed:', error);
      return { success: false, error: '로그인 중 오류가 발생했습니다.' };
    }
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
   * Keycloak 모드에서는 localStorage를 사용하지 않음 (Keycloak이 세션 관리)
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

    // Keycloak 모드에서는 localStorage 사용 안함
    // 토큰은 keycloak-js가 메모리에서 관리하고, 세션은 Keycloak 서버가 관리
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
    // Direct Login으로 얻은 사용자 정보가 있는 경우
    if (currentUser.value?.roles) {
      return currentUser.value.roles.includes(role);
    }

    // Keycloak SSO로 인증된 경우
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
    // Direct Login으로 얻은 토큰이 있는 경우
    if (token.value) {
      // TODO: Direct Login 토큰 만료 체크 및 갱신 로직 추가 필요
      return token.value;
    }

    // Keycloak SSO로 얻은 토큰인 경우
    if (!keycloakInstance) {
      return null;
    }

    // 인증되지 않은 경우 null 반환
    if (!isAuthenticated.value) {
      return null;
    }

    try {
      // 토큰이 만료 예정이면 갱신
      if (keycloakInstance.isTokenExpired(30)) {
        await refreshTokens();
      }
    } catch (error) {
      console.debug('[Keycloak] Token refresh failed:', error);
      return null;
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
    directLogin,
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
