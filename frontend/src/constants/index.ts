/**
 * 애플리케이션 상수 정의
 */

/**
 * API 엔드포인트
 */
export const API_ENDPOINTS = {
  // Sample (User) 관련
  SAMPLES: '/api/v1/samples',
  SAMPLE_BY_ID: (id: string) => `/api/v1/samples/${id}`,
  SAMPLE_CHECK_DUPLICATE: (id: string) => `/api/v1/samples/check/${id}`,
  SAMPLE_CHECK_NICKNAME_DUPLICATE: (nickname: string) => `/api/v1/samples/check/nickname/${nickname}`,
  SAMPLES_PAGE: '/api/v1/samples/page',

  // 인증 관련
  AUTH: {
    LOGIN: '/api/v1/auth/login',
    LOGOUT: '/api/v1/auth/logout',
    REFRESH: '/api/v1/auth/refresh',
    VALIDATE: '/api/v1/auth/validate',
    ME: '/api/v1/auth/me',
  },
} as const;

/**
 * 라우트 경로
 */
export const ROUTES = {
  HOME: '/',
  LOGIN: '/login',
  SIGNUP: '/signup',
  USER_INFO: '/userinfo',
  LEAVE_USER: '/leaveuser',
  NOT_FOUND: '/404',
} as const;

/**
 * 에러 메시지
 */
export const ERROR_MESSAGES = {
  NETWORK_ERROR: '네트워크 오류가 발생했습니다. 인터넷 연결을 확인해주세요.',
  SERVER_ERROR: '서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.',
  UNAUTHORIZED: '인증이 필요합니다. 다시 로그인해주세요.',
  FORBIDDEN: '접근 권한이 없습니다.',
  NOT_FOUND: '요청한 리소스를 찾을 수 없습니다.',
  BAD_REQUEST: '잘못된 요청입니다.',
  UNKNOWN_ERROR: '알 수 없는 오류가 발생했습니다.',
  VALIDATION_ERROR: '입력값을 확인해주세요.',
} as const;

/**
 * 성공 메시지
 */
export const SUCCESS_MESSAGES = {
  SIGNUP_SUCCESS: '회원가입이 완료되었습니다!',
  UPDATE_SUCCESS: '정보가 성공적으로 수정되었습니다.',
  DELETE_SUCCESS: '삭제가 완료되었습니다.',
  SAVE_SUCCESS: '저장되었습니다.',
} as const;

/**
 * 로컬 스토리지 키
 */
export const STORAGE_KEYS = {
  AUTH_TOKEN: 'authToken',
  REFRESH_TOKEN: 'refreshToken',
  CURRENT_USER: 'currentUser',
  THEME: 'theme',
  LANGUAGE: 'language',
} as const;

/**
 * 페이지네이션 설정
 */
export const PAGINATION = {
  DEFAULT_PAGE_SIZE: 20,
  PAGE_SIZE_OPTIONS: [10, 20, 50, 100],
} as const;

/**
 * 타임아웃 설정 (밀리초)
 */
export const TIMEOUTS = {
  API_TIMEOUT: 10000, // 10초
  DEBOUNCE: 300, // 0.3초
  TOAST_DURATION: 3000, // 3초
} as const;

/**
 * 정규식 패턴
 */
export const REGEX_PATTERNS = {
  EMAIL: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
  PHONE: /^01[016789]-?\d{3,4}-?\d{4}$/,
  PASSWORD: /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*#?&])[A-Za-z\d@$!%*#?&]{8,}$/,
  NUMBER_ONLY: /^\d+$/,
  ALPHANUMERIC: /^[a-zA-Z0-9]+$/,
} as const;

/**
 * 날짜 포맷
 */
export const DATE_FORMATS = {
  STANDARD: 'YYYY-MM-DD',
  WITH_TIME: 'YYYY-MM-DD HH:mm:ss',
  KOREAN: 'YYYY년 MM월 DD일',
  COMPACT: 'YYYYMMDD',
} as const;

/**
 * HTTP 상태 코드
 */
export const HTTP_STATUS = {
  OK: 200,
  CREATED: 201,
  NO_CONTENT: 204,
  BAD_REQUEST: 400,
  UNAUTHORIZED: 401,
  FORBIDDEN: 403,
  NOT_FOUND: 404,
  CONFLICT: 409,
  INTERNAL_SERVER_ERROR: 500,
  SERVICE_UNAVAILABLE: 503,
} as const;
