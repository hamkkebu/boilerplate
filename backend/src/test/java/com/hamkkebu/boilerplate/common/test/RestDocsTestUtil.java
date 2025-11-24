package com.hamkkebu.boilerplate.common.test;

import com.epages.restdocs.apispec.MockMvcRestDocumentationWrapper;
import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;
import org.springframework.restdocs.operation.preprocess.OperationResponsePreprocessor;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.request.ParameterDescriptor;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;

/**
 * REST Docs 테스트 유틸리티
 *
 * <p>REST Docs 및 OpenAPI 스펙 생성을 위한 헬퍼 메서드를 제공합니다.</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>공통 요청/응답 전처리기</li>
 *   <li>공통 필드 디스크립터 (ApiResponse 구조)</li>
 *   <li>OpenAPI 스펙 생성을 위한 document 래퍼</li>
 * </ul>
 */
public class RestDocsTestUtil {

    /**
     * 요청 전처리기
     * <p>요청을 보기 좋게 포맷팅하고 헤더를 제거합니다.</p>
     */
    public static OperationRequestPreprocessor getRequestPreprocessor() {
        return preprocessRequest(
            modifyUris()
                .scheme("https")
                .host("api.hamkkebu.com")
                .removePort(),
            prettyPrint()
        );
    }

    /**
     * 응답 전처리기
     * <p>응답을 보기 좋게 포맷팅합니다.</p>
     */
    public static OperationResponsePreprocessor getResponsePreprocessor() {
        return preprocessResponse(prettyPrint());
    }

    /**
     * OpenAPI 스펙을 생성하는 document 헬퍼
     *
     * @param identifier 문서 식별자
     * @param resourceSnippetParameters 리소스 스니펫 파라미터
     * @return RestDocumentationResultHandler
     */
    public static RestDocumentationResultHandler document(
        String identifier,
        ResourceSnippetParameters resourceSnippetParameters
    ) {
        return MockMvcRestDocumentationWrapper.document(
            identifier,
            getRequestPreprocessor(),
            getResponsePreprocessor(),
            resource(resourceSnippetParameters)
        );
    }

    // ==================== 공통 ApiResponse 필드 ====================

    /**
     * ApiResponse의 공통 성공 응답 필드
     */
    public static FieldDescriptor[] getSuccessResponseFields(FieldDescriptor... dataFields) {
        FieldDescriptor[] commonFields = {
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부"),
            fieldWithPath("message").type(JsonFieldType.STRING).description("응답 메시지").optional(),
            fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시각 (ISO 8601)")
        };

        if (dataFields.length == 0) {
            return commonFields;
        }

        FieldDescriptor[] result = new FieldDescriptor[commonFields.length + dataFields.length];
        System.arraycopy(commonFields, 0, result, 0, commonFields.length);
        System.arraycopy(dataFields, 0, result, commonFields.length, dataFields.length);
        return result;
    }

    /**
     * ApiResponse의 공통 에러 응답 필드
     */
    public static FieldDescriptor[] getErrorResponseFields() {
        return new FieldDescriptor[]{
            fieldWithPath("success").type(JsonFieldType.BOOLEAN).description("성공 여부 (false)"),
            fieldWithPath("error").type(JsonFieldType.OBJECT).description("에러 정보"),
            fieldWithPath("error.code").type(JsonFieldType.STRING).description("에러 코드"),
            fieldWithPath("error.message").type(JsonFieldType.STRING).description("에러 메시지"),
            fieldWithPath("error.details").type(JsonFieldType.OBJECT).description("에러 상세 정보").optional(),
            fieldWithPath("timestamp").type(JsonFieldType.STRING).description("응답 시각 (ISO 8601)")
        };
    }

    /**
     * 페이징 응답 필드 (PageResponseDto)
     */
    public static FieldDescriptor[] getPageResponseFields(String prefix, FieldDescriptor... contentFields) {
        FieldDescriptor[] pageFields = {
            fieldWithPath(prefix + "content").type(JsonFieldType.ARRAY).description("데이터 목록"),
            fieldWithPath(prefix + "totalPages").type(JsonFieldType.NUMBER).description("전체 페이지 수"),
            fieldWithPath(prefix + "totalElements").type(JsonFieldType.NUMBER).description("전체 데이터 개수"),
            fieldWithPath(prefix + "size").type(JsonFieldType.NUMBER).description("페이지 크기"),
            fieldWithPath(prefix + "number").type(JsonFieldType.NUMBER).description("현재 페이지 번호 (0부터 시작)"),
            fieldWithPath(prefix + "numberOfElements").type(JsonFieldType.NUMBER).description("현재 페이지의 데이터 개수"),
            fieldWithPath(prefix + "first").type(JsonFieldType.BOOLEAN).description("첫 페이지 여부"),
            fieldWithPath(prefix + "last").type(JsonFieldType.BOOLEAN).description("마지막 페이지 여부"),
            fieldWithPath(prefix + "empty").type(JsonFieldType.BOOLEAN).description("빈 페이지 여부")
        };

        if (contentFields.length == 0) {
            return pageFields;
        }

        FieldDescriptor[] result = new FieldDescriptor[pageFields.length + contentFields.length];
        System.arraycopy(pageFields, 0, result, 0, pageFields.length);
        System.arraycopy(contentFields, 0, result, pageFields.length, contentFields.length);
        return result;
    }

    // ==================== 공통 파라미터 ====================

    /**
     * 페이징 요청 파라미터
     */
    public static ParameterDescriptor[] getPageRequestParameters() {
        return new ParameterDescriptor[]{
            parameterWithName("page").description("페이지 번호 (0부터 시작)").optional(),
            parameterWithName("size").description("페이지 크기").optional(),
            parameterWithName("sortBy").description("정렬 기준 필드").optional(),
            parameterWithName("direction").description("정렬 방향 (asc, desc)").optional()
        };
    }

    /**
     * Schema 생성 헬퍼
     */
    public static Schema schema(String name) {
        return new Schema(name);
    }
}
