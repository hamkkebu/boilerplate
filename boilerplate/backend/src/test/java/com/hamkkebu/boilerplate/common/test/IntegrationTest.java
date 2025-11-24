package com.hamkkebu.boilerplate.common.test;

import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestConstructor;

import java.lang.annotation.*;

/**
 * 통합 테스트를 위한 메타 어노테이션
 *
 * <p>이 어노테이션을 테스트 클래스에 붙이면 다음 설정이 자동으로 적용됩니다:</p>
 * <ul>
 *   <li>{@code @SpringBootTest}: Spring Boot 전체 컨텍스트 로드</li>
 *   <li>{@code @AutoConfigureMockMvc}: MockMvc 자동 설정</li>
 *   <li>{@code @AutoConfigureRestDocs}: REST Docs 자동 설정</li>
 *   <li>{@code @ActiveProfiles("test")}: test 프로파일 활성화</li>
 *   <li>{@code @TestConstructor}: 생성자 주입 지원</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @IntegrationTest
 * class SampleControllerTest {
 *
 *     @Autowired
 *     private MockMvc mockMvc;
 *
 *     @Test
 *     void testGetSample() throws Exception {
 *         mockMvc.perform(get("/api/v1/samples/{id}", "test123"))
 *             .andExpect(status().isOk())
 *             .andDo(document("sample-get"));
 *     }
 * }
 * }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@AutoConfigureRestDocs(uriScheme = "https", uriHost = "api.hamkkebu.com", uriPort = 443)
@ActiveProfiles("test")
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public @interface IntegrationTest {
}
