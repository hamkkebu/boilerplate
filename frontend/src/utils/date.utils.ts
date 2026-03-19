/**
 * 날짜 관련 유틸리티 함수
 */

/**
 * ISO 날짜 문자열을 지정된 포맷으로 변환
 * @param isoString ISO 형식의 날짜 문자열
 * @param format 포맷 ('YYYY-MM-DD', 'YYYY-MM-DD HH:mm:ss', 'YYYY.MM.DD' 등)
 */
export function formatDate(isoString: string, format = 'YYYY-MM-DD HH:mm:ss'): string {
  if (!isoString) return '';

  const date = new Date(isoString);

  if (isNaN(date.getTime())) {
    return '';
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds);
}

/**
 * 상대적 시간 표시 (예: '5분 전', '2시간 전')
 */
export function formatRelativeTime(isoString: string): string {
  if (!isoString) return '';

  const date = new Date(isoString);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffSecs = Math.floor(diffMs / 1000);
  const diffMins = Math.floor(diffSecs / 60);
  const diffHours = Math.floor(diffMins / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffSecs < 60) {
    return '방금 전';
  } else if (diffMins < 60) {
    return `${diffMins}분 전`;
  } else if (diffHours < 24) {
    return `${diffHours}시간 전`;
  } else if (diffDays < 7) {
    return `${diffDays}일 전`;
  } else {
    return formatDate(isoString, 'YYYY-MM-DD');
  }
}

/**
 * 두 날짜 사이의 일수 계산
 */
export function daysBetween(date1: Date | string, date2: Date | string): number {
  const d1 = typeof date1 === 'string' ? new Date(date1) : date1;
  const d2 = typeof date2 === 'string' ? new Date(date2) : date2;

  const diffTime = Math.abs(d2.getTime() - d1.getTime());
  return Math.ceil(diffTime / (1000 * 60 * 60 * 24));
}

/**
 * 날짜에 일수 더하기
 */
export function addDays(date: Date | string, days: number): Date {
  const d = typeof date === 'string' ? new Date(date) : new Date(date);
  d.setDate(d.getDate() + days);
  return d;
}

/**
 * 현재 날짜를 ISO 형식으로 반환
 */
export function getCurrentDateISO(): string {
  return new Date().toISOString();
}

/**
 * 날짜가 유효한지 확인
 */
export function isValidDate(dateString: string): boolean {
  const date = new Date(dateString);
  return !isNaN(date.getTime());
}
