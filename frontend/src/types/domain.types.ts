/**
 * 도메인 타입 정의
 */

/**
 * Sample/User 엔티티
 */
export interface Sample {
  sampleNum: number;
  sampleId: string;
  sampleFname: string;
  sampleLname: string;
  sampleNickname?: string;
  sampleEmail?: string;
  samplePhone?: string;
  sampleCountry?: string;
  sampleCity?: string;
  sampleState?: string;
  sampleStreet1?: string;
  sampleStreet2?: string;
  sampleZip?: string;
}

/**
 * Sample 생성 요청 DTO
 */
export interface CreateSampleRequest {
  sampleId: string;
  sampleFname: string;
  sampleLname: string;
  samplePassword: string;
  sampleNickname?: string;
  sampleEmail?: string;
  samplePhone?: string;
  sampleCountry?: string;
  sampleCity?: string;
  sampleState?: string;
  sampleStreet1?: string;
  sampleStreet2?: string;
  sampleZip?: string;
}

/**
 * 인증 관련 타입
 */
export interface AuthUser {
  sampleId: string;
  sampleFname: string;
  sampleLname: string;
  sampleEmail?: string;
}

export interface LoginRequest {
  sampleId: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
}

export interface LoginResponse {
  sampleId: string;
  sampleFname: string;
  sampleLname: string;
  sampleNickname?: string;
  sampleEmail?: string;
  token: TokenResponse;
}
