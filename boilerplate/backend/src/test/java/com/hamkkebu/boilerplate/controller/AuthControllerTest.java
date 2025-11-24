package com.hamkkebu.boilerplate.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamkkebu.boilerplate.common.test.IntegrationTest;
import com.hamkkebu.boilerplate.data.dto.LoginRequest;
import com.hamkkebu.boilerplate.data.entity.Sample;
import com.hamkkebu.boilerplate.repository.SampleJpaRepository;
import com.hamkkebu.boilerplate.common.security.JwtTokenProvider;
import com.hamkkebu.boilerplate.common.security.RefreshTokenWhitelistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.hamkkebu.boilerplate.common.test.RestDocsTestUtil.*;
import static org.springframework.restdocs.headers.HeaderDocumentation.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthController 통합 테스트
 *
 * <p>REST Docs를 사용하여 API 문서를 자동 생성합니다.</p>
 */
@IntegrationTest
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SampleJpaRepository sampleRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private RefreshTokenWhitelistService refreshTokenWhitelistService;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        sampleRepository.deleteAll();

        // 로그인용 테스트 사용자 생성
        // BCrypt hash (12 rounds) for "Password123!"
        Sample sample = Sample.builder()
            .sampleId("testuser")
            .sampleFirstName("Test")
            .sampleLastName("User")
            .sampleNickname("tester")
            .sampleEmail("test@example.com")
            .samplePhone("010-1234-5678")
            .samplePassword("$2a$12$2AC1QIGno/3uNHjp.r2kAeHS7Fp3PROTODrG2lGpFk4AZms0ugQre")
            .sampleCountry("South Korea")
            .sampleCity("Seoul")
            .sampleState("Seoul")
            .sampleStreet1("123 Test Street")
            .sampleZip("12345")
            .build();
        sampleRepository.save(sample);
    }

    @Test
    @DisplayName("로그인 API - 성공")
    void login_Success() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
            .username("testuser")
            .password("Password123!")
            .build();

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.username").value("testuser"))
            .andExpect(jsonPath("$.data.token.accessToken").exists())
            .andExpect(jsonPath("$.data.token.refreshToken").exists())
            .andExpect(jsonPath("$.data.token.expiresIn").exists())
            .andDo(document("auth-login",
                ResourceSnippetParameters.builder()
                    .tag("Auth API")
                    .summary("로그인")
                    .description("사용자 ID와 비밀번호로 로그인하고 JWT 토큰을 발급받습니다.")
                    .requestSchema(schema("LoginRequest"))
                    .responseSchema(schema("ApiResponse<LoginResponse>"))
                    .requestFields(
                        fieldWithPath("username").type(STRING).description("사용자 ID"),
                        fieldWithPath("password").type(STRING).description("비밀번호")
                    )
                    .responseFields(
                        getSuccessResponseFields(
                            fieldWithPath("data.username").type(STRING).description("사용자 ID"),
                            fieldWithPath("data.firstName").type(STRING).description("이름"),
                            fieldWithPath("data.lastName").type(STRING).description("성"),
                            fieldWithPath("data.nickname").type(STRING).description("닉네임"),
                            fieldWithPath("data.email").type(STRING).description("이메일"),
                            fieldWithPath("data.token.accessToken").type(STRING).description("JWT 액세스 토큰"),
                            fieldWithPath("data.token.refreshToken").type(STRING).description("JWT 리프레시 토큰"),
                            fieldWithPath("data.token.tokenType").type(STRING).description("토큰 타입 (Bearer)"),
                            fieldWithPath("data.token.expiresIn").type(NUMBER).description("액세스 토큰 만료 시간 (초)")
                        )
                    )
                    .build()
            ));
    }

    @Test
    @DisplayName("로그인 API - 실패 (잘못된 비밀번호)")
    void login_FailWithWrongPassword() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
            .username("testuser")
            .password("WrongPassword123!")
            .build();

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("로그인 API - 실패 (존재하지 않는 사용자)")
    void login_FailWithNonExistentUser() throws Exception {
        // given
        LoginRequest request = LoginRequest.builder()
            .username("nonexistent")
            .password("Password123!")
            .build();

        // when & then
        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("토큰 갱신 API - 성공")
    void refresh_Success() throws Exception {
        // given: 먼저 로그인해서 refreshToken 발급
        String refreshToken = jwtTokenProvider.createRefreshToken("testuser", "ROLE_USER");
        refreshTokenWhitelistService.addToWhitelist("testuser", refreshToken, 604800000L);

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .header("Refresh-Token", refreshToken))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.accessToken").exists())
            .andExpect(jsonPath("$.data.refreshToken").exists())
            .andExpect(jsonPath("$.data.expiresIn").exists())
            .andDo(document("auth-refresh",
                ResourceSnippetParameters.builder()
                    .tag("Auth API")
                    .summary("토큰 갱신")
                    .description("refreshToken으로 새로운 accessToken을 발급받습니다. " +
                        "Refresh Token Rotation 방식으로 새로운 refreshToken도 함께 발급됩니다.")
                    .requestHeaders(
                        headerWithName("Refresh-Token").description("리프레시 토큰")
                    )
                    .responseSchema(schema("ApiResponse<TokenResponse>"))
                    .responseFields(
                        getSuccessResponseFields(
                            fieldWithPath("data.accessToken").type(STRING).description("새로운 JWT 액세스 토큰"),
                            fieldWithPath("data.refreshToken").type(STRING).description("새로운 JWT 리프레시 토큰"),
                            fieldWithPath("data.tokenType").type(STRING).description("토큰 타입 (Bearer)"),
                            fieldWithPath("data.expiresIn").type(NUMBER).description("액세스 토큰 만료 시간 (초)")
                        )
                    )
                    .build()
            ));
    }

    @Test
    @DisplayName("토큰 갱신 API - 실패 (유효하지 않은 토큰)")
    void refresh_FailWithInvalidToken() throws Exception {
        // given
        String invalidRefreshToken = "invalid.token.here";

        // when & then
        mockMvc.perform(post("/api/v1/auth/refresh")
                .header("Refresh-Token", invalidRefreshToken))
            .andDo(print())
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("토큰 검증 API - 유효한 토큰")
    void validateToken_ValidToken() throws Exception {
        // given
        String accessToken = jwtTokenProvider.createAccessToken("testuser", "ROLE_USER");

        // when & then
        mockMvc.perform(get("/api/v1/auth/validate")
                .header("Authorization", "Bearer " + accessToken))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value(true))
            .andDo(document("auth-validate",
                ResourceSnippetParameters.builder()
                    .tag("Auth API")
                    .summary("토큰 검증")
                    .description("JWT 토큰의 유효성을 검증합니다.")
                    .requestHeaders(
                        headerWithName("Authorization").description("Bearer {JWT 토큰}")
                    )
                    .responseSchema(schema("ApiResponse<Boolean>"))
                    .responseFields(
                        getSuccessResponseFields(
                            fieldWithPath("data").type(BOOLEAN).description("토큰 유효 여부")
                        )
                    )
                    .build()
            ));
    }

    @Test
    @DisplayName("토큰 검증 API - 유효하지 않은 토큰")
    void validateToken_InvalidToken() throws Exception {
        // given
        String invalidToken = "invalid.token.here";

        // when & then
        mockMvc.perform(get("/api/v1/auth/validate")
                .header("Authorization", "Bearer " + invalidToken))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value(false));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("현재 사용자 정보 조회 API")
    void getCurrentUser() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/auth/me"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").value("testuser"))
            .andDo(document("auth-me",
                ResourceSnippetParameters.builder()
                    .tag("Auth API")
                    .summary("현재 사용자 정보")
                    .description("JWT 토큰에서 현재 사용자 ID를 조회합니다.")
                    .responseSchema(schema("ApiResponse<String>"))
                    .responseFields(
                        getSuccessResponseFields(
                            fieldWithPath("data").type(STRING).description("사용자 ID")
                        )
                    )
                    .build()
            ));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("로그아웃 API - 성공")
    void logout_Success() throws Exception {
        // given
        String refreshToken = jwtTokenProvider.createRefreshToken("testuser", "ROLE_USER");
        refreshTokenWhitelistService.addToWhitelist("testuser", refreshToken, 604800000L);

        // when & then
        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Refresh-Token", refreshToken))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andDo(document("auth-logout",
                ResourceSnippetParameters.builder()
                    .tag("Auth API")
                    .summary("로그아웃")
                    .description("로그아웃하고 refreshToken을 Whitelist에서 제거합니다.")
                    .requestHeaders(
                        headerWithName("Refresh-Token").description("리프레시 토큰 (선택)")
                    )
                    .responseSchema(schema("ApiResponse<Void>"))
                    .responseFields(getSuccessResponseFields())
                    .build()
            ));
    }

    @Test
    @WithMockUser(username = "testuser")
    @DisplayName("로그아웃 API - refreshToken 없이")
    void logout_WithoutRefreshToken() throws Exception {
        // when & then
        mockMvc.perform(post("/api/v1/auth/logout"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }
}
