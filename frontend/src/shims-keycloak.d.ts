declare module 'keycloak-js' {
  interface KeycloakConfig {
    url?: string;
    realm: string;
    clientId: string;
  }

  interface KeycloakInitOptions {
    onLoad?: 'login-required' | 'check-sso';
    silentCheckSsoRedirectUri?: string;
    pkceMethod?: 'S256';
    checkLoginIframe?: boolean;
    checkLoginIframeInterval?: number;
    flow?: 'standard' | 'implicit' | 'hybrid';
  }

  interface KeycloakLoginOptions {
    redirectUri?: string;
    prompt?: string;
    maxAge?: number;
    loginHint?: string;
    idpHint?: string;
    scope?: string;
    locale?: string;
  }

  interface KeycloakLogoutOptions {
    redirectUri?: string;
  }

  interface KeycloakRegisterOptions {
    redirectUri?: string;
  }

  interface KeycloakTokenParsed {
    sub?: string;
    preferred_username?: string;
    email?: string;
    given_name?: string;
    family_name?: string;
    realm_access?: {
      roles?: string[];
    };
    [key: string]: unknown;
  }

  export default class Keycloak {
    constructor(config?: KeycloakConfig);

    token?: string;
    refreshToken?: string;
    tokenParsed?: KeycloakTokenParsed;
    subject?: string;

    init(options?: KeycloakInitOptions): Promise<boolean>;
    login(options?: KeycloakLoginOptions): Promise<void>;
    logout(options?: KeycloakLogoutOptions): Promise<void>;
    register(options?: KeycloakRegisterOptions): Promise<void>;
    accountManagement(): Promise<void>;
    updateToken(minValidity: number): Promise<boolean>;
    isTokenExpired(minValidity?: number): boolean;
    hasRealmRole(role: string): boolean;
    hasResourceRole(role: string, resource?: string): boolean;

    onTokenExpired?: () => void;
    onAuthError?: (error: unknown) => void;
    onAuthLogout?: () => void;
    onAuthRefreshError?: () => void;
  }
}
