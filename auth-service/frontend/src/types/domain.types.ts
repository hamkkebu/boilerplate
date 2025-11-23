/**
 * 도메인 타입 정의
 */

/**
 * Sample/User 엔티티
 */
export interface Sample {
  id: number;
  username: string;
  firstName: string;
  lastName: string;
  nickname?: string;
  email?: string;
  phone?: string;
  country?: string;
  city?: string;
  state?: string;
  street1?: string;
  street2?: string;
  zip?: string;
}

/**
 * Sample 생성 요청 DTO
 *
 * ⚠️ IMPORTANT: Backend에서 email, phone, nickname은 REQUIRED입니다.
 * Optional fields: country, city, state, street1, street2, zip
 */
export interface CreateSampleRequest {
  username: string;
  firstName: string;
  lastName: string;
  password: string;
  nickname: string;  // REQUIRED - Backend validation
  email: string;      // REQUIRED - Backend validation
  phone: string;      // REQUIRED - Backend validation
  country?: string;
  city?: string;
  state?: string;
  street1?: string;
  street2?: string;
  zip?: string;
}

/**
 * 사용자 권한 타입
 */
export type UserRole = 'USER' | 'ADMIN' | 'DEVELOPER';

/**
 * 인증 관련 타입
 */
export interface AuthUser {
  username: string;
  firstName: string;
  lastName: string;
  email?: string;
  role?: UserRole;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

/**
 * 로그인 응답 DTO
 *
 * Backend는 항상 모든 필드를 반환합니다.
 * nullable 가능하지만 필드 자체는 항상 존재합니다.
 */
export interface LoginResponse {
  username: string;
  firstName: string;
  lastName: string;
  nickname: string | null;  // Backend always returns (may be null)
  email: string | null;      // Backend always returns (may be null)
  role: UserRole;            // 사용자 권한
  token: TokenResponse;
}

/**
 * 관리자 API 관련 타입
 */
export interface UserResponse {
  userId: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  isActive: boolean;
  isVerified: boolean;
  role: UserRole;
  lastLoginAt?: string;
  createdAt: string;
  updatedAt: string;
}

export interface UserStatsResponse {
  totalUsers: number;
  activeUsers: number;
  deletedUsers: number;
  adminUsers: number;
  developerUsers: number;
}
