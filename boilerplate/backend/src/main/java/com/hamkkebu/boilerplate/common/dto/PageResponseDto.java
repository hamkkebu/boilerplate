package com.hamkkebu.boilerplate.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이징 응답 DTO
 *
 * <p>페이징 처리된 데이터를 클라이언트에게 반환할 때 사용합니다.</p>
 *
 * <p>응답 예시:</p>
 * <pre>
 * {
 *   "content": [...],           // 데이터 목록
 *   "page": 0,                  // 현재 페이지 (0부터 시작)
 *   "size": 20,                 // 페이지 크기
 *   "totalElements": 100,       // 전체 데이터 개수
 *   "totalPages": 5,            // 전체 페이지 수
 *   "isFirst": true,            // 첫 페이지 여부
 *   "isLast": false,            // 마지막 페이지 여부
 *   "hasNext": true,            // 다음 페이지 존재 여부
 *   "hasPrevious": false        // 이전 페이지 존재 여부
 * }
 * </pre>
 *
 * <p>사용 예시:</p>
 * <pre>
 * // Service
 * Page{@literal <}Transaction{@literal >} page = transactionRepository.findAll(pageable);
 *
 * // Controller
 * return ApiResponse.success(PageResponseDto.of(page));
 * </pre>
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponseDto<T> {

    /**
     * 데이터 목록
     */
    private List<T> content;

    /**
     * 현재 페이지 번호 (0부터 시작)
     */
    private int page;

    /**
     * 페이지 크기
     */
    private int size;

    /**
     * 전체 데이터 개수
     */
    private long totalElements;

    /**
     * 전체 페이지 수
     */
    private int totalPages;

    /**
     * 첫 페이지 여부
     */
    private boolean isFirst;

    /**
     * 마지막 페이지 여부
     */
    private boolean isLast;

    /**
     * 다음 페이지 존재 여부
     */
    private boolean hasNext;

    /**
     * 이전 페이지 존재 여부
     */
    private boolean hasPrevious;

    /**
     * 현재 페이지의 데이터 개수
     */
    private int numberOfElements;

    /**
     * 빈 페이지 여부
     */
    private boolean isEmpty;

    /**
     * Spring Data Page 객체로부터 PageResponseDto 생성
     */
    public static <T> PageResponseDto<T> of(Page<T> page) {
        return PageResponseDto.<T>builder()
            .content(page.getContent())
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .isFirst(page.isFirst())
            .isLast(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .numberOfElements(page.getNumberOfElements())
            .isEmpty(page.isEmpty())
            .build();
    }

    /**
     * Spring Data Page 객체로부터 PageResponseDto 생성 (데이터 변환)
     *
     * <p>엔티티를 DTO로 변환하면서 페이징 정보를 함께 반환할 때 사용</p>
     *
     * <pre>
     * Page{@literal <}Transaction{@literal >} page = repository.findAll(pageable);
     * PageResponseDto{@literal <}TransactionDto{@literal >} response = PageResponseDto.of(page, TransactionDto::from);
     * </pre>
     */
    public static <T, U> PageResponseDto<U> of(Page<T> page, java.util.function.Function<T, U> converter) {
        List<U> convertedContent = page.getContent().stream()
            .map(converter)
            .toList();

        return PageResponseDto.<U>builder()
            .content(convertedContent)
            .page(page.getNumber())
            .size(page.getSize())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .isFirst(page.isFirst())
            .isLast(page.isLast())
            .hasNext(page.hasNext())
            .hasPrevious(page.hasPrevious())
            .numberOfElements(page.getNumberOfElements())
            .isEmpty(page.isEmpty())
            .build();
    }

    /**
     * 빈 페이지 생성
     */
    public static <T> PageResponseDto<T> empty() {
        return PageResponseDto.<T>builder()
            .content(List.of())
            .page(0)
            .size(0)
            .totalElements(0)
            .totalPages(0)
            .isFirst(true)
            .isLast(true)
            .hasNext(false)
            .hasPrevious(false)
            .numberOfElements(0)
            .isEmpty(true)
            .build();
    }

    /**
     * 간단한 페이징 정보만 포함하는 응답 생성
     */
    public static <T> PageResponseDto<T> simple(List<T> content, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return PageResponseDto.<T>builder()
            .content(content)
            .page(page)
            .size(size)
            .totalElements(totalElements)
            .totalPages(totalPages)
            .isFirst(page == 0)
            .isLast(page >= totalPages - 1)
            .hasNext(page < totalPages - 1)
            .hasPrevious(page > 0)
            .numberOfElements(content.size())
            .isEmpty(content.isEmpty())
            .build();
    }
}
