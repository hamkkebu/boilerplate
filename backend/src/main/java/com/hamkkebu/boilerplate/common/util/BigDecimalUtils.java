package com.hamkkebu.boilerplate.common.util;

import java.math.BigDecimal;

/**
 * BigDecimal 유틸리티 클래스
 *
 * <p>null-safe한 BigDecimal 연산을 제공합니다.</p>
 */
public final class BigDecimalUtils {

    private BigDecimalUtils() {
        // 유틸리티 클래스 인스턴스화 방지
    }

    /**
     * null을 ZERO로 변환
     *
     * @param value BigDecimal 값
     * @return null이면 ZERO, 아니면 원래 값
     */
    public static BigDecimal nullToZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    /**
     * null-safe 덧셈
     *
     * @param a 첫 번째 값 (null 가능)
     * @param b 두 번째 값 (null 가능)
     * @return 두 값의 합 (null은 ZERO로 처리)
     */
    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return nullToZero(a).add(nullToZero(b));
    }

    /**
     * null-safe 뺄셈
     *
     * @param a 첫 번째 값 (null 가능)
     * @param b 두 번째 값 (null 가능)
     * @return a - b (null은 ZERO로 처리)
     */
    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return nullToZero(a).subtract(nullToZero(b));
    }

    /**
     * null-safe 잔액 계산 (수입 - 지출)
     *
     * @param income  수입 (null 가능)
     * @param expense 지출 (null 가능)
     * @return 잔액
     */
    public static BigDecimal calculateBalance(BigDecimal income, BigDecimal expense) {
        return subtract(income, expense);
    }

    /**
     * 값이 0보다 큰지 확인
     *
     * @param value BigDecimal 값 (null 가능)
     * @return 0보다 크면 true
     */
    public static boolean isPositive(BigDecimal value) {
        return nullToZero(value).compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * 값이 0보다 작은지 확인
     *
     * @param value BigDecimal 값 (null 가능)
     * @return 0보다 작으면 true
     */
    public static boolean isNegative(BigDecimal value) {
        return nullToZero(value).compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * 값이 0인지 확인
     *
     * @param value BigDecimal 값 (null 가능)
     * @return 0이면 true
     */
    public static boolean isZero(BigDecimal value) {
        return nullToZero(value).compareTo(BigDecimal.ZERO) == 0;
    }
}
