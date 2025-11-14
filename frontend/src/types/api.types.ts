/**
 * API 공통 타입 정의
 */

/**
 * 통일된 API 응답 형식
 */
export interface ApiResponse<T> {
  success: boolean;
  data?: T;
  message?: string;
  error?: ErrorResponse;
  timestamp: string;
}

/**
 * 에러 응답 형식
 */
export interface ErrorResponse {
  code: string;
  message: string;
  details?: any;
}

/**
 * 페이지 요청 DTO
 */
export interface PageRequest {
  page: number;
  size: number;
  sortBy?: string;
  direction?: 'asc' | 'desc';
}

/**
 * 페이지 응답 DTO
 */
export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: {
      sorted: boolean;
      unsorted: boolean;
      empty: boolean;
    };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  totalPages: number;
  totalElements: number;
  last: boolean;
  size: number;
  number: number;
  sort: {
    sorted: boolean;
    unsorted: boolean;
    empty: boolean;
  };
  numberOfElements: number;
  first: boolean;
  empty: boolean;
}

/**
 * API 요청 옵션
 */
export interface ApiRequestConfig {
  headers?: Record<string, string>;
  params?: Record<string, any>;
  timeout?: number;
}
