package com.hamkkebu.boilerplate.common.dto;

import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 페이징 요청 DTO
 *
 * <p>클라이언트로부터 페이징 요청을 받을 때 사용합니다.</p>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // Controller
 * {@literal @}GetMapping("/transactions")
 * public ApiResponse{@literal <}PageResponseDto{@literal <}Transaction{@literal >>} getTransactions(PageRequestDto pageRequest) {
 *     Pageable pageable = pageRequest.toPageable();
 *     Page{@literal <}Transaction{@literal >} page = transactionService.findAll(pageable);
 *     return ApiResponse.success(PageResponseDto.of(page));
 * }
 *
 * // 요청 예시
 * GET /api/v1/transactions?page=0&size=20&sort=createdAt,desc
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PageRequestDto {

    /**
     * 페이지 번호 (0부터 시작)
     */
    @Builder.Default
    private int page = 0;

    /**
     * 페이지 크기
     */
    @Builder.Default
    private int size = 20;

    /**
     * 정렬 필드
     * 예: "createdAt", "id", "name"
     */
    private String sortBy;

    /**
     * 정렬 방향
     * 예: "asc", "desc"
     */
    @Builder.Default
    private String direction = "desc";

    /**
     * 페이지 크기 최대값
     */
    private static final int MAX_SIZE = 100;

    /**
     * Spring Data Pageable로 변환
     */
    public Pageable toPageable() {
        // 페이지 크기 검증
        int validatedSize = Math.min(size, MAX_SIZE);

        // 정렬이 없는 경우
        if (sortBy == null || sortBy.isEmpty()) {
            return PageRequest.of(page, validatedSize);
        }

        // 정렬이 있는 경우
        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

        return PageRequest.of(page, validatedSize, Sort.by(sortDirection, sortBy));
    }

    /**
     * Spring Data Pageable로 변환 (기본 정렬 지정)
     */
    public Pageable toPageable(String defaultSortBy) {
        if (sortBy == null || sortBy.isEmpty()) {
            this.sortBy = defaultSortBy;
        }
        return toPageable();
    }

    /**
     * 여러 필드로 정렬
     */
    public Pageable toPageable(String... sortFields) {
        int validatedSize = Math.min(size, MAX_SIZE);

        if (sortFields == null || sortFields.length == 0) {
            return PageRequest.of(page, validatedSize);
        }

        Sort.Direction sortDirection = direction.equalsIgnoreCase("asc")
            ? Sort.Direction.ASC
            : Sort.Direction.DESC;

        return PageRequest.of(page, validatedSize, Sort.by(sortDirection, sortFields));
    }

    /**
     * 페이지 번호 검증 및 수정
     */
    public void validateAndFix() {
        if (page < 0) {
            page = 0;
        }
        if (size < 1) {
            size = 20;
        }
        if (size > MAX_SIZE) {
            size = MAX_SIZE;
        }
        if (direction == null || direction.isEmpty()) {
            direction = "desc";
        }
    }
}
