package com.hamkkebu.boilerplate.common.user.annotation;

import java.lang.annotation.*;

/**
 * 현재 인증된 사용자의 ID를 주입받기 위한 어노테이션
 *
 * <p>컨트롤러 메서드 파라미터에 사용하여 현재 인증된 사용자의 ID를 주입받습니다.</p>
 * <p>JWT 토큰에서 추출된 사용자 정보를 기반으로 동작합니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @GetMapping("/me")
 * public ResponseEntity<?> getMyInfo(@CurrentUser Long userId) {
 *     // userId는 JWT 토큰에서 추출된 사용자 ID
 *     return ResponseEntity.ok(userService.getUserInfo(userId));
 * }
 * }
 * </pre>
 *
 * <p>주의: 이 어노테이션을 사용하려면 서비스에서
 * {@code AbstractCurrentUserArgumentResolver}를 상속받은 Resolver를
 * WebMvcConfig에 등록해야 합니다.</p>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CurrentUser {
}
