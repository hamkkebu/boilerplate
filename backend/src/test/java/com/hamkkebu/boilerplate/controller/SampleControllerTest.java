package com.hamkkebu.boilerplate.controller;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hamkkebu.boilerplate.common.test.IntegrationTest;
import com.hamkkebu.boilerplate.data.dto.RequestSample;
import com.hamkkebu.boilerplate.data.entity.Sample;
import com.hamkkebu.boilerplate.repository.SampleJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
        Sample sample = Sample.builder()
            .sampleId("k1m743hyun")
            .sampleFname("Taehyun")
            .sampleLname("Kim")
            .sampleNickname("k1m")
            .sampleEmail("k1m743hyun@example.com")
            .samplePhone("010-1234-5678")
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
        RequestSample request = RequestSample.builder()
            .sampleId("testuser")
            .sampleFname("Test")
            .sampleLname("User")
            .sampleNickname("tester")
            .sampleEmail("test@example.com")
            .samplePhone("010-1234-5678")
            .sampleCountry("South Korea")
            .sampleCity("Seoul")
            .sampleState("Seoul")
            .sampleStreet1("Test Street")
            .sampleZip("12345")
            .build();

        // when & then
        mockMvc.perform(post("/api/v1/samples")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.sampleId").value("testuser"))
            .andDo(document("sample-create",
                ResourceSnippetParameters.builder()
                    .tag("Sample API")
                    .summary("Sample 생성")
                    .description("새로운 Sample(사용자)을 생성합니다.")
                    .requestSchema(schema("RequestSample"))
                    .responseSchema(schema("ApiResponse<ResponseSample>"))
                    .requestFields(
                        fieldWithPath("sampleId").type(STRING).description("사용자 ID"),
                        fieldWithPath("sampleFname").type(STRING).description("이름"),
                        fieldWithPath("sampleLname").type(STRING).description("성"),
                        fieldWithPath("sampleNickname").type(STRING).description("닉네임"),
                        fieldWithPath("sampleEmail").type(STRING).description("이메일"),
                        fieldWithPath("samplePhone").type(STRING).description("전화번호"),
                        fieldWithPath("sampleCountry").type(STRING).description("국가").optional(),
                        fieldWithPath("sampleCity").type(STRING).description("도시").optional(),
                        fieldWithPath("sampleState").type(STRING).description("주/도").optional(),
                        fieldWithPath("sampleStreet1").type(STRING).description("주소 1").optional(),
                        fieldWithPath("sampleStreet2").type(STRING).description("주소 2").optional(),
                        fieldWithPath("sampleZip").type(STRING).description("우편번호").optional()
                    )
                    .responseFields(
                        getSuccessResponseFields(
                            fieldWithPath("data.sampleNum").type(NUMBER).description("Sample 번호 (PK)"),
                            fieldWithPath("data.sampleId").type(STRING).description("사용자 ID"),
                            fieldWithPath("data.sampleFname").type(STRING).description("이름"),
                            fieldWithPath("data.sampleLname").type(STRING).description("성"),
                            fieldWithPath("data.sampleNickname").type(STRING).description("닉네임"),
                            fieldWithPath("data.sampleEmail").type(STRING).description("이메일"),
                            fieldWithPath("data.samplePhone").type(STRING).description("전화번호"),
                            fieldWithPath("data.sampleCountry").type(STRING).description("국가").optional(),
                            fieldWithPath("data.sampleCity").type(STRING).description("도시").optional(),
                            fieldWithPath("data.sampleState").type(STRING).description("주/도").optional(),
                            fieldWithPath("data.sampleStreet1").type(STRING).description("주소 1").optional(),
                            fieldWithPath("data.sampleStreet2").type(STRING).description("주소 2").optional(),
                            fieldWithPath("data.sampleZip").type(STRING).description("우편번호").optional()
                        )
                    )
                    .build()
            ));
    }

    @Test
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
            .andExpect(jsonPath("$.data.sampleId").value(sampleId))
            .andDo(document("sample-get",
                ResourceSnippetParameters.builder()
                    .tag("Sample API")
                    .summary("Sample 단건 조회")
                    .description("Sample ID로 Sample 정보를 조회합니다.")
                    .pathParameters(
                        parameterWithName("sampleId").description("조회할 Sample ID")
                    )
                    .responseSchema(schema("ApiResponse<ResponseSample>"))
                    .responseFields(
                        getSuccessResponseFields(
                            fieldWithPath("data.sampleNum").type(NUMBER).description("Sample 번호 (PK)"),
                            fieldWithPath("data.sampleId").type(STRING).description("사용자 ID"),
                            fieldWithPath("data.sampleFname").type(STRING).description("이름"),
                            fieldWithPath("data.sampleLname").type(STRING).description("성"),
                            fieldWithPath("data.sampleNickname").type(STRING).description("닉네임"),
                            fieldWithPath("data.sampleEmail").type(STRING).description("이메일"),
                            fieldWithPath("data.samplePhone").type(STRING).description("전화번호"),
                            fieldWithPath("data.sampleCountry").type(STRING).description("국가").optional(),
                            fieldWithPath("data.sampleCity").type(STRING).description("도시").optional(),
                            fieldWithPath("data.sampleState").type(STRING).description("주/도").optional(),
                            fieldWithPath("data.sampleStreet1").type(STRING).description("주소 1").optional(),
                            fieldWithPath("data.sampleStreet2").type(STRING).description("주소 2").optional(),
                            fieldWithPath("data.sampleZip").type(STRING).description("우편번호").optional()
                        )
                    )
                    .build()
            ));
    }

    @Test
    @DisplayName("Sample 전체 조회 API")
    void getAllSampleInfo() throws Exception {
        // when & then
        mockMvc.perform(get("/api/v1/samples")
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data").isArray())
            .andDo(document("sample-list",
                ResourceSnippetParameters.builder()
                    .tag("Sample API")
                    .summary("Sample 전체 조회")
                    .description("모든 Sample 정보를 조회합니다.")
                    .responseSchema(schema("ApiResponse<List<ResponseSample>>"))
                    .responseFields(
                        getSuccessResponseFields(
                            fieldWithPath("data[].sampleNum").type(NUMBER).description("Sample 번호 (PK)"),
                            fieldWithPath("data[].sampleId").type(STRING).description("사용자 ID"),
                            fieldWithPath("data[].sampleFname").type(STRING).description("이름"),
                            fieldWithPath("data[].sampleLname").type(STRING).description("성"),
                            fieldWithPath("data[].sampleNickname").type(STRING).description("닉네임"),
                            fieldWithPath("data[].sampleEmail").type(STRING).description("이메일"),
                            fieldWithPath("data[].samplePhone").type(STRING).description("전화번호"),
                            fieldWithPath("data[].sampleCountry").type(STRING).description("국가").optional(),
                            fieldWithPath("data[].sampleCity").type(STRING).description("도시").optional(),
                            fieldWithPath("data[].sampleState").type(STRING).description("주/도").optional(),
                            fieldWithPath("data[].sampleStreet1").type(STRING).description("주소 1").optional(),
                            fieldWithPath("data[].sampleStreet2").type(STRING).description("주소 2").optional(),
                            fieldWithPath("data[].sampleZip").type(STRING).description("우편번호").optional()
                        )
                    )
                    .build()
            ));
    }

    @Test
    @DisplayName("Sample 삭제 API")
    void deleteSample() throws Exception {
        // given
        String sampleId = "k1m743hyun";

        // when & then
        mockMvc.perform(delete("/api/v1/samples/{sampleId}", sampleId)
                .contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").exists())
            .andDo(document("sample-delete",
                ResourceSnippetParameters.builder()
                    .tag("Sample API")
                    .summary("Sample 삭제")
                    .description("Sample ID로 Sample을 삭제합니다 (Soft Delete).")
                    .pathParameters(
                        parameterWithName("sampleId").description("삭제할 Sample ID")
                    )
                    .responseSchema(schema("ApiResponse<Void>"))
                    .responseFields(getSuccessResponseFields())
                    .build()
            ));
    }
}
