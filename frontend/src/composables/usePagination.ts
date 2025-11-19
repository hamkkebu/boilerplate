import { ref, computed } from 'vue';
import type { PageRequest, PageResponse } from '@/types/api.types';

/**
 * 페이지네이션 관리 Composable
 */
export function usePagination<T>(initialPageSize = 20) {
  const currentPage = ref(0);
  const pageSize = ref(initialPageSize);
  const totalPages = ref(0);
  const totalElements = ref(0);
  const sortBy = ref<string | undefined>(undefined);
  const direction = ref<'asc' | 'desc'>('asc');

  /**
   * 페이지 요청 객체
   */
  const pageRequest = computed<PageRequest>(() => ({
    page: currentPage.value,
    size: pageSize.value,
    sortBy: sortBy.value,
    direction: direction.value,
  }));

  /**
   * 첫 페이지 여부
   */
  const isFirstPage = computed(() => currentPage.value === 0);

  /**
   * 마지막 페이지 여부
   */
  const isLastPage = computed(() => currentPage.value >= totalPages.value - 1);

  /**
   * 페이지 범위 (페이지네이션 UI용)
   */
  const pageRange = computed(() => {
    const range: number[] = [];
    const maxPages = 5; // 표시할 최대 페이지 번호 개수
    let start = Math.max(0, currentPage.value - Math.floor(maxPages / 2));
    let end = Math.min(totalPages.value, start + maxPages);

    if (end - start < maxPages) {
      start = Math.max(0, end - maxPages);
    }

    for (let i = start; i < end; i++) {
      range.push(i);
    }

    return range;
  });

  /**
   * 페이지 응답 업데이트
   */
  const updateFromResponse = (response: PageResponse<T>) => {
    totalPages.value = response.totalPages;
    totalElements.value = response.totalElements;
    currentPage.value = response.page;  // ✅ number → page로 수정 (Backend와 일치)
  };

  /**
   * 페이지 변경
   */
  const goToPage = (page: number) => {
    if (page >= 0 && page < totalPages.value) {
      currentPage.value = page;
    }
  };

  /**
   * 다음 페이지
   */
  const nextPage = () => {
    if (!isLastPage.value) {
      currentPage.value++;
    }
  };

  /**
   * 이전 페이지
   */
  const previousPage = () => {
    if (!isFirstPage.value) {
      currentPage.value--;
    }
  };

  /**
   * 첫 페이지로
   */
  const firstPage = () => {
    currentPage.value = 0;
  };

  /**
   * 마지막 페이지로
   */
  const lastPage = () => {
    currentPage.value = totalPages.value - 1;
  };

  /**
   * 정렬 변경
   */
  const changeSort = (field: string, dir?: 'asc' | 'desc') => {
    sortBy.value = field;
    direction.value = dir || (direction.value === 'asc' ? 'desc' : 'asc');
    currentPage.value = 0; // 정렬 변경 시 첫 페이지로
  };

  /**
   * 페이지 크기 변경
   */
  const changePageSize = (size: number) => {
    pageSize.value = size;
    currentPage.value = 0; // 페이지 크기 변경 시 첫 페이지로
  };

  /**
   * 초기화
   */
  const reset = () => {
    currentPage.value = 0;
    totalPages.value = 0;
    totalElements.value = 0;
    sortBy.value = undefined;
    direction.value = 'asc';
  };

  return {
    currentPage,
    pageSize,
    totalPages,
    totalElements,
    sortBy,
    direction,
    pageRequest,
    isFirstPage,
    isLastPage,
    pageRange,
    updateFromResponse,
    goToPage,
    nextPage,
    previousPage,
    firstPage,
    lastPage,
    changeSort,
    changePageSize,
    reset,
  };
}
