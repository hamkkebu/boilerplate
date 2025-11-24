package com.hamkkebu.boilerplate.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * 날짜/시간 유틸리티 클래스
 *
 * <p>날짜와 시간 관련 공통 기능을 제공합니다.</p>
 *
 * <p>주요 기능:</p>
 * <ul>
 *   <li>포맷팅: LocalDateTime ↔ String</li>
 *   <li>변환: LocalDateTime ↔ Date, Timestamp</li>
 *   <li>계산: 날짜 더하기/빼기, 차이 계산</li>
 *   <li>비교: 이전/이후 판단, 범위 체크</li>
 *   <li>특수 날짜: 월 첫날/마지막날, 분기 시작/종료</li>
 * </ul>
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class DateTimeUtil {

    // ==================== 포맷 상수 ====================

    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATETIME_FORMAT_ISO = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String DATETIME_FORMAT_WITH_MILLIS = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DATE_FORMAT_COMPACT = "yyyyMMdd";
    public static final String DATETIME_FORMAT_COMPACT = "yyyyMMddHHmmss";

    // ==================== 포맷터 ====================

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT);
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern(DATETIME_FORMAT);
    public static final DateTimeFormatter DATETIME_FORMATTER_ISO = DateTimeFormatter.ofPattern(DATETIME_FORMAT_ISO);

    // ==================== 현재 시각 ====================

    /**
     * 현재 LocalDateTime 반환
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 현재 LocalDate 반환
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * 현재 LocalTime 반환
     */
    public static LocalTime currentTime() {
        return LocalTime.now();
    }

    // ==================== 포맷팅 ====================

    /**
     * LocalDateTime을 문자열로 변환 (기본 포맷: yyyy-MM-dd HH:mm:ss)
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * LocalDateTime을 문자열로 변환 (커스텀 포맷)
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * LocalDate를 문자열로 변환 (yyyy-MM-dd)
     */
    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMATTER);
    }

    /**
     * LocalTime을 문자열로 변환 (HH:mm:ss)
     */
    public static String formatTime(LocalTime time) {
        if (time == null) {
            return null;
        }
        return time.format(TIME_FORMATTER);
    }

    /**
     * 문자열을 LocalDateTime으로 변환 (yyyy-MM-dd HH:mm:ss)
     */
    public static LocalDateTime parse(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    /**
     * 문자열을 LocalDateTime으로 변환 (커스텀 포맷)
     */
    public static LocalDateTime parse(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 문자열을 LocalDate로 변환 (yyyy-MM-dd)
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    // ==================== 변환 ====================

    /**
     * LocalDateTime → Date
     */
    public static Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date → LocalDateTime
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return LocalDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    /**
     * LocalDate → LocalDateTime (시간은 00:00:00)
     */
    public static LocalDateTime toLocalDateTime(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.atStartOfDay();
    }

    /**
     * LocalDateTime → Timestamp (밀리초)
     */
    public static long toTimestamp(LocalDateTime dateTime) {
        if (dateTime == null) {
            return 0L;
        }
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    /**
     * Timestamp → LocalDateTime
     */
    public static LocalDateTime fromTimestamp(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    // ==================== 날짜 계산 ====================

    /**
     * N일 더하기
     */
    public static LocalDateTime plusDays(LocalDateTime dateTime, long days) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusDays(days);
    }

    /**
     * N일 빼기
     */
    public static LocalDateTime minusDays(LocalDateTime dateTime, long days) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.minusDays(days);
    }

    /**
     * N개월 더하기
     */
    public static LocalDateTime plusMonths(LocalDateTime dateTime, long months) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusMonths(months);
    }

    /**
     * N개월 빼기
     */
    public static LocalDateTime minusMonths(LocalDateTime dateTime, long months) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.minusMonths(months);
    }

    /**
     * N년 더하기
     */
    public static LocalDateTime plusYears(LocalDateTime dateTime, long years) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusYears(years);
    }

    /**
     * N시간 더하기
     */
    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(hours);
    }

    /**
     * N분 더하기
     */
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusMinutes(minutes);
    }

    // ==================== 날짜 차이 계산 ====================

    /**
     * 두 날짜 사이의 일수 차이
     */
    public static long daysBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * 두 날짜 사이의 시간 차이 (시간)
     */
    public static long hoursBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0L;
        }
        return ChronoUnit.HOURS.between(start, end);
    }

    /**
     * 두 날짜 사이의 시간 차이 (분)
     */
    public static long minutesBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0L;
        }
        return ChronoUnit.MINUTES.between(start, end);
    }

    /**
     * 두 날짜 사이의 시간 차이 (초)
     */
    public static long secondsBetween(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            return 0L;
        }
        return ChronoUnit.SECONDS.between(start, end);
    }

    // ==================== 날짜 비교 ====================

    /**
     * dateTime1이 dateTime2보다 이전인지 확인
     */
    public static boolean isBefore(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        if (dateTime1 == null || dateTime2 == null) {
            return false;
        }
        return dateTime1.isBefore(dateTime2);
    }

    /**
     * dateTime1이 dateTime2보다 이후인지 확인
     */
    public static boolean isAfter(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        if (dateTime1 == null || dateTime2 == null) {
            return false;
        }
        return dateTime1.isAfter(dateTime2);
    }

    /**
     * 두 날짜가 같은지 확인
     */
    public static boolean isEqual(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        if (dateTime1 == null || dateTime2 == null) {
            return false;
        }
        return dateTime1.isEqual(dateTime2);
    }

    /**
     * 날짜가 특정 범위 내에 있는지 확인
     */
    public static boolean isBetween(LocalDateTime target, LocalDateTime start, LocalDateTime end) {
        if (target == null || start == null || end == null) {
            return false;
        }
        return (target.isEqual(start) || target.isAfter(start)) &&
               (target.isEqual(end) || target.isBefore(end));
    }

    // ==================== 특수 날짜 ====================

    /**
     * 월의 첫날 (1일 00:00:00)
     */
    public static LocalDateTime startOfMonth(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
    }

    /**
     * 월의 마지막날 (말일 23:59:59)
     */
    public static LocalDateTime endOfMonth(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.with(TemporalAdjusters.lastDayOfMonth()).toLocalDate().atTime(23, 59, 59);
    }

    /**
     * 년도의 첫날 (1월 1일 00:00:00)
     */
    public static LocalDateTime startOfYear(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.with(TemporalAdjusters.firstDayOfYear()).toLocalDate().atStartOfDay();
    }

    /**
     * 년도의 마지막날 (12월 31일 23:59:59)
     */
    public static LocalDateTime endOfYear(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.with(TemporalAdjusters.lastDayOfYear()).toLocalDate().atTime(23, 59, 59);
    }

    /**
     * 하루의 시작 (00:00:00)
     */
    public static LocalDateTime startOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().atStartOfDay();
    }

    /**
     * 하루의 끝 (23:59:59)
     */
    public static LocalDateTime endOfDay(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().atTime(23, 59, 59);
    }

    /**
     * 현재가 과거인지 확인
     */
    public static boolean isPast(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isBefore(LocalDateTime.now());
    }

    /**
     * 현재가 미래인지 확인
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isAfter(LocalDateTime.now());
    }

    /**
     * 오늘인지 확인
     */
    public static boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.toLocalDate().isEqual(LocalDate.now());
    }
}
