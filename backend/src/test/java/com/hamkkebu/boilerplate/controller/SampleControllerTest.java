package com.hamkkebu.boilerplate.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamkkebu.boilerplate.common.test.IntegrationTest;
import com.hamkkebu.boilerplate.data.dto.DeleteSampleRequest;
import com.hamkkebu.boilerplate.data.dto.SampleRequest;
import com.hamkkebu.boilerplate.data.entity.Sample;
import com.hamkkebu.boilerplate.repository.SampleJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.hamkkebu.boilerplate.common.test.RestDocsTestUtil.*;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
import static org.springframework.restdocs.payload.JsonFieldType.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * SampleController 통합 테스트
 *
 * <p>REST Docs를 사용하여 API 문서를 자동 생성합니다.</p>
 * <p>테스트 실행 후 build/api-spec 디렉토리에 OpenAPI 3.0 스펙이 생성됩니다.</p>
 */
@IntegrationTest
@Transactional
class SampleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private SampleJpaRepository sampleRepository;

    @BeforeEach
    void setUp() {
        // 테스트 데이터 초기화
        sampleRepository.deleteAll();

        // 조회/삭제 테스트를 위한 기본 데이터 생성
        // BCrypt hash (12 rounds) for "Password123!"
        Sample sample = Sample.builder()
            .sampleId("k1m743hyun")
            .sampleFirstName("Taehyun")
            .sampleLastName("Kim")
            .sampleNickname("k1m")
            .sampleEmail("k1m743hyun@example.com")
            .samplePhone("010-1234-5678")
            .samplePassword("$2a$12$2AC1QIGno/3uNHjp.r2kAeHS7Fp3PROTODrG2lGpFk4AZms0ugQre")
            .sampleCountry("South Korea")
            .sampleCity("Seoul")
            .sampleState("Seoul")
            .sampleStreet1("123 Gangnam-daero")
            .sampleZip("06000")
            .build();
        sampleRepository.save(sample);
    }

    @Test
    @DisplayName("Sample 생성 API")
    void createSample() throws Exception {
        // given
        SampleRequest request = SampleRequest.builder()
            .username("testuser")
            .firstName("Test")
            .lastName("User")
            .nickname("tester")
            .email("test@example.com")
            .phone("010-1234-5678")
            .password("TestPassword123!")
            .country("South Korea")
            .city("Seoul")
            .state("Seoul")
            .street1("Test Street")
            .zip("12345")
            .build();

        // when & then
        mockMvc.perform(post("/api/v1/samples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.username").value("testuser"))
            .andDo(document("sample-create",
                ResourceSnippetParameters.builder()
                    .tag("Sample API")
                    .summary("Sample 생성")
                    .description("새로운 Sample(사용자)을 생성합니다.")
                    .requestSchema(schema("SampleRequest"))
                    .responseSchema(schema("ApiResponse<SampleResponse>"))
                    .requestFields(
                        fieldWithPath("username").type(STRING).description("사용자 ID (3-20자)"),
                        fieldWithPath("firstName").type(STRING).description("이름"),
                        fieldWithPath("lastName").type(STRING).description("성"),
                        fieldWithPath("nickname").type(STRING).description("닉네임 (2-20자)"),
                        fieldWithPath("email").type(STRING).description("이메일"),
                        fieldWithPath("phone").type(STRING).description("전화번호"),
                        fieldWithPath("password").type(STRING).description("비밀번호 (12-100자, 영문+숫자+특수문자 포함)"),
                        fieldWithPath("country").type(STRING).description("국가").optional(),
                        fieldWithPath("city").type(STRING).description("도시").optional(),
                        fieldWithPath("state").type(STRING).description("주/도").optional(),
                        fieldWithPath("street1").type(STRING).description("주소 1").optional(),
                        fieldWithPath("street2").type(STRING).description("주소 2").optional(),
                        fieldWithPath("zip").type(STRING).description("우편번호").optional()
                    )
                    .responseFields(
                        getSuccessResponseFields(
                            fieldWithPath("data.id").type(NUMBER).description("Sample 번호 (PK)"),
                            fieldWithPath("data.username").type(STRING).description("사용자 ID"),
                            fieldWithPath("data.firstName").type(STRING).description("이름"),
                            fieldWithPath("data.lastName").type(STRING).description("성"),
                            fieldWithPath("data.nickname").type(STRING).description("닉네임"),
                            fieldWithPath("data.email").type(STRING).description("이메일"),
                            fieldWithPath("data.phone").type(STRING).description("전화번호"),
                            fieldWithPath("data.country").type(STRING).description("국가").optional(),
                            fieldWithPath("data.city").type(STRING).description("도시").optional(),
                            fieldWithPath("data.state").type(STRING).description("주/도").optional(),
                            fieldWithPath("data.street1").type(STRING).description("주소 1").optional(),
                            fieldWithPath("data.street2").type(STRING).description("주소 2").optional(),
                            fieldWithPath("data.zip").type(STRING).description("우편번호").optional()
                        )
                    )
                    .build()
            ));
    }

    @Test
    @WithMockUser(username = "k1m743hyun")  // FIXED: 인증 정보 추가 (IDOR 방어로 인증 필요)
    @DisplayName("Sample 단건 조회 API")
    void getSampleInfo() throws Exception {
        // given
        String sampleId = "k1m743hyun";  // data.sql에 있는 데이터

        // when & then
        mockMvc.perform(get("/api/v1/samples/{sampleId}", sampleId)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.username").value(sampleId))
            .andDo(document("sample-get",
                ResourceSnippetParameters.builder()
                    .tag("Sample API")
                    .summary("Sample 단건 조회")
                    .description("Sample ID로 Sample 정보를 조회합니다.")
                    .pathParameters(
                        parameterWithName("sampleId").description("조회할 Sample ID")
                    )
                    .responseSchema(schema("ApiResponse<SampleResponse>"))
                    .responseFields(
                        getSuccessResponseFields(
                            fieldWithPath("data.id").type(NUMBER).description("Sample 번호 (PK)"),
                            fieldWithPath("data.username").type(STRING).description("사용자 ID"),
                            fieldWithPath("data.firstName").type(STRING).description("이름"),
                            fieldWithPath("data.lastName").type(STRING).description("성"),
                            fieldWithPath("data.nickname").type(STRING).description("닉네임"),
                            fieldWithPath("data.email").type(STRING).description("이메일"),
                            fieldWithPath("data.phone").type(STRING).description("전화번호"),
                            fieldWithPath("data.country").type(STRING).description("국가").optional(),
                            fieldWithPath("data.city").type(STRING).description("도시").optional(),
                            fieldWithPath("data.state").type(STRING).description("주/도").optional(),
                            fieldWithPath("data.street1").type(STRING).description("주소 1").optional(),
                            fieldWithPath("data.street2").type(STRING).description("주소 2").optional(),
                            fieldWithPath("data.zip").type(STRING).description("우편번호").optional()
                        )
                    )
                    .build()
            ));
    }

    // REMOVED: 전체 사용자 조회 API는 보안상 제거됨
    // @Test
    // @DisplayName("Sample 전체 조회 API")
    // void getAllSampleInfo() throws Exception {
    //     // SECURITY: 개인정보 대량 유출 방지를 위해 API 제거
    //     // Controller에서도 주석 처리됨
    // }

    @Test
    @WithMockUser(username = "k1m743hyun")
    @DisplayName("Sample 삭제 API")
    void deleteSample() throws Exception {
        // given
        String sampleId = "k1m743hyun";
        DeleteSampleRequest request = DeleteSampleRequest.builder()
            .password("Password123!")
            .build();

        // when & then
        mockMvc.perform(delete("/api/v1/samples/{sampleId}", sampleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists())
            .andDo(document("sample-delete",
                ResourceSnippetParameters.builder()
                    .tag("Sample API")
                    .summary("Sample 삭제")
                    .description("Sample ID로 Sample을 삭제합니다 (Soft Delete). 비밀번호 검증 후 삭제합니다.")
                    .pathParameters(
                        parameterWithName("sampleId").description("삭제할 Sample ID")
                    )
                    .requestFields(
                        fieldWithPath("password").type(STRING).description("비밀번호 (검증용)")
                    )
                    .responseSchema(schema("ApiResponse<Void>"))
                    .responseFields(getSuccessResponseFields())
                    .build()
            ));
    }

    @Test
    @DisplayName("아이디 중복 확인 API - 사용 중인 아이디")
    void checkIdDuplicate_Exists() throws Exception {
        // given
        String sampleId = "k1m743hyun";  // setUp에서 생성한 사용자

        // when & then
        mockMvc.perform(get("/api/v1/samples/check/{sampleId}", sampleId)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.exists").value(true))
            .andExpect(jsonPath("$.data.available").value(false))
            .andExpect(jsonPath("$.data.value").value(sampleId))
            .andDo(document("sample-check-id-duplicate",
                ResourceSnippetParameters.builder()
                    .tag("Sample API")
                    .summary("아이디 중복 확인")
                    .description("사용자 ID의 중복 여부를 확인합니다.")
                    .pathParameters(
                        parameterWithName("sampleId").description("확인할 사용자 ID (3-20자)")
                    )
                    .responseSchema(schema("ApiResponse<DuplicateCheckResponse>"))
                    .responseFields(
                        getSuccessResponseFields(
                            fieldWithPath("data.exists").type(BOOLEAN).description("중복 여부 (true: 사용 중, false: 사용 가능)"),
                            fieldWithPath("data.available").type(BOOLEAN).description("사용 가능 여부 (exists의 반대)"),
                            fieldWithPath("data.value").type(STRING).description("확인한 값")
                        )
                    )
                    .build()
            ));
    }

    @Test
    @DisplayName("아이디 중복 확인 API - 사용 가능한 아이디")
    void checkIdDuplicate_Available() throws Exception {
        // given
        String sampleId = "available_user";

        // when & then
        mockMvc.perform(get("/api/v1/samples/check/{sampleId}", sampleId)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.exists").value(false))
            .andExpect(jsonPath("$.data.available").value(true))
            .andExpect(jsonPath("$.data.value").value(sampleId));
    }

    @Test
    @DisplayName("닉네임 중복 확인 API - 사용 중인 닉네임")
    void checkNicknameDuplicate_Exists() throws Exception {
        // given
        String nickname = "k1m";  // setUp에서 생성한 사용자의 닉네임

        // when & then
        mockMvc.perform(get("/api/v1/samples/check/nickname/{nickname}", nickname)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.exists").value(true))
            .andExpect(jsonPath("$.data.available").value(false))
            .andExpect(jsonPath("$.data.value").value(nickname))
            .andDo(document("sample-check-nickname-duplicate",
                ResourceSnippetParameters.builder()
                    .tag("Sample API")
                    .summary("닉네임 중복 확인")
                    .description("닉네임의 중복 여부를 확인합니다.")
                    .pathParameters(
                        parameterWithName("nickname").description("확인할 닉네임 (2-20자)")
                    )
                    .responseSchema(schema("ApiResponse<DuplicateCheckResponse>"))
                    .responseFields(
                        getSuccessResponseFields(
                            fieldWithPath("data.exists").type(BOOLEAN).description("중복 여부 (true: 사용 중, false: 사용 가능)"),
                            fieldWithPath("data.available").type(BOOLEAN).description("사용 가능 여부 (exists의 반대)"),
                            fieldWithPath("data.value").type(STRING).description("확인한 값")
                        )
                    )
                    .build()
            ));
    }

    @Test
    @DisplayName("닉네임 중복 확인 API - 사용 가능한 닉네임")
    void checkNicknameDuplicate_Available() throws Exception {
        // given
        String nickname = "available_nick";

        // when & then
        mockMvc.perform(get("/api/v1/samples/check/nickname/{nickname}", nickname)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.exists").value(false))
            .andExpect(jsonPath("$.data.available").value(true))
            .andExpect(jsonPath("$.data.value").value(nickname));
    }
}
