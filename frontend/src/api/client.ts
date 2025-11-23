import axios, { AxiosInstance, AxiosError, AxiosResponse, InternalAxiosRequestConfig } from 'axios';
import type { ApiResponse } from '@/types/api.types';
import type { TokenResponse } from '@/types/domain.types';

/**
 * Axios 인스턴스 생성
 */
const apiClient: AxiosInstance = axios.create({
  baseURL: process.env.VUE_APP_baseApiURL || '',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * Token Refresh 관련 상태
 */
let isRefreshing = false;
let failedQueue: Array<{
  resolve: (value?: any) => void;
  reject: (reason?: any) => void;
}> = [];

/**
 * 요청 인터셉터
 */
apiClient.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 토큰이 있으면 헤더에 추가
    const token = localStorage.getItem('authToken');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    // 요청 로깅 (개발 환경에서만)
    if (process.env.NODE_ENV === 'development') {
      console.log(`[API Request] ${config.method?.toUpperCase()} ${config.url}`, config.data);
    }

    return config;
  },
  (error: AxiosError) => {
    console.error('[API Request Error]', error);
    return Promise.reject(error);
  }
);

/**
 * 대기 중인 요청 처리
 */
const processQueue = (error: AxiosError | null, token: string | null = null) => {
  failedQueue.forEach((promise) => {
    if (error) {
      promise.reject(error);
    } else {
      promise.resolve(token);
    }
  });

  failedQueue = [];
};

/**
 * Token Refresh 함수
 */
const refreshAccessToken = async (): Promise<string> => {
  const refreshToken = localStorage.getItem('refreshToken');

  if (!refreshToken) {
    throw new Error('No refresh token available');
  }

  try {
    // Refresh API 호출 (인터셉터를 거치지 않도록 새로운 axios 인스턴스 사용)
    const response = await axios.post<ApiResponse<TokenResponse>>(
      `${process.env.VUE_APP_baseApiURL || ''}/api/v1/auth/refresh`,
      null,
      {
        headers: {
          'Refresh-Token': refreshToken,
        },
      }
    );

    const tokenData = response.data.data;

    if (!tokenData) {
      throw new Error('Invalid token response');
    }

    // 새로운 토큰 저장
    localStorage.setItem('authToken', tokenData.accessToken);
    localStorage.setItem('refreshToken', tokenData.refreshToken);

    return tokenData.accessToken;
  } catch (error) {
    // Refresh token도 만료된 경우
    console.error('[Token Refresh Failed]', error);
    localStorage.removeItem('authToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('currentUser');
    throw error;
  }
};

/**
 * 응답 인터셉터
 */
apiClient.interceptors.response.use(
  (response: AxiosResponse<ApiResponse<any>>) => {
    // 응답 로깅 (개발 환경에서만)
    if (process.env.NODE_ENV === 'development') {
      console.log(`[API Response] ${response.config.url}`, response.data);
    }

    return response;
  },
  async (error: AxiosError<ApiResponse<any>>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // 401 에러이고, refresh token이 있으며, 재시도하지 않은 요청인 경우
    if (
      error.response?.status === 401 &&
      !originalRequest._retry &&
      localStorage.getItem('refreshToken')
    ) {
      // Refresh API 자체가 실패한 경우는 재시도하지 않음
      if (originalRequest.url?.includes('/auth/refresh')) {
        console.error('[Refresh API Failed]');
        handleTokenExpired();
        return Promise.reject(error);
      }

      // 이미 토큰 갱신 중인 경우, 대기 큐에 추가
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            if (originalRequest.headers) {
              originalRequest.headers.Authorization = `Bearer ${token}`;
            }
            return apiClient(originalRequest);
          })
          .catch((err) => {
            return Promise.reject(err);
          });
      }

      // 토큰 갱신 시작
      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const newToken = await refreshAccessToken();

        // 대기 중인 요청들에게 새로운 토큰 전달
        processQueue(null, newToken);

        // 현재 요청 재시도
        if (originalRequest.headers) {
          originalRequest.headers.Authorization = `Bearer ${newToken}`;
        }
        return apiClient(originalRequest);
      } catch (refreshError) {
        // Refresh 실패 시 대기 중인 요청들 모두 reject
        processQueue(error, null);
        handleTokenExpired();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    // 401 이외의 에러 처리
    if (error.response) {
      const { status, data } = error.response;

      // API 응답 에러
      if (data?.error) {
        let errorMessage = data.error.message || '오류가 발생했습니다.';

        // 검증 에러(COMMON-009)인 경우 상세 에러 메시지 추출
        if (data.error.code === 'COMMON-009' && data.error.details) {
          const details = data.error.details;
          // details는 { fieldName: errorMessage } 형태의 객체
          if (typeof details === 'object' && details !== null) {
            const errorMessages = Object.entries(details)
              .map(([field, message]) => `${message}`)
              .join('\n');
            if (errorMessages) {
              errorMessage = errorMessages;
            }
          }
        }

        console.error(`[API Error ${status}]`, errorMessage);
        alert(errorMessage);
      } else if (status !== 401) {
        // 일반 HTTP 에러 (401은 이미 처리했으므로 제외)
        handleHttpError(status);
      }
    } else if (error.request) {
      // 요청은 보냈지만 응답을 받지 못함
      console.error('[Network Error]', error.message);
      alert('네트워크 오류가 발생했습니다. 인터넷 연결을 확인해주세요.');
    } else {
      // 요청 설정 중 오류 발생
      console.error('[Request Setup Error]', error.message);
      alert('요청 처리 중 오류가 발생했습니다.');
    }

    return Promise.reject(error);
  }
);

/**
 * 토큰 만료 처리
 */
function handleTokenExpired(): void {
  alert('세션이 만료되었습니다. 다시 로그인해주세요.');
  localStorage.removeItem('authToken');
  localStorage.removeItem('refreshToken');
  localStorage.removeItem('currentUser');
  window.location.href = '/login';
}

/**
 * HTTP 상태 코드별 에러 처리
 */
function handleHttpError(status: number): void {
  switch (status) {
    case 400:
      alert('잘못된 요청입니다.');
      break;
    case 401:
      // 401 에러는 interceptor에서 처리하므로 여기서는 무시
      break;
    case 403:
      alert('접근 권한이 없습니다.');
      break;
    case 404:
      alert('요청한 리소스를 찾을 수 없습니다.');
      break;
    case 500:
      alert('서버 오류가 발생했습니다.');
      break;
    case 503:
      alert('서비스를 일시적으로 사용할 수 없습니다.');
      break;
    default:
      alert(`오류가 발생했습니다. (${status})`);
  }
}

export default apiClient;
