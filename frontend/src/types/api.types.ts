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
 *
 * Backend PageResponseDto와 일치하도록 수정됨
 * - pageable, sort 객체 제거 (Backend에서 제공하지 않음)
 * - number → page로 변경
 * - hasNext, hasPrevious 추가 (편리한 페이지네이션 처리)
 */
export interface PageResponse<T> {
  content: T[];
  page: number;              // 현재 페이지 번호 (0부터 시작)
  size: number;              // 페이지 크기
  totalElements: number;     // 전체 데이터 개수
  totalPages: number;        // 전체 페이지 수
  first: boolean;            // 첫 페이지 여부
  last: boolean;             // 마지막 페이지 여부
  hasNext: boolean;          // 다음 페이지 존재 여부
  hasPrevious: boolean;      // 이전 페이지 존재 여부
  numberOfElements: number;  // 현재 페이지의 데이터 개수
  empty: boolean;            // 빈 페이지 여부
}

/**
 * API 요청 옵션
 */
export interface ApiRequestConfig {
  headers?: Record<string, string>;
  params?: Record<string, any>;
  timeout?: number;
}
