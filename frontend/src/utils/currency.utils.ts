/**
 * 통화 관련 유틸리티 함수
 */

/**
 * 숫자를 통화 형식으로 포맷
 * @param amount 금액
 * @param currency 통화 단위 ('KRW', 'USD', 'EUR' 등)
 * @param showSymbol 통화 기호 표시 여부
 */
export function formatCurrency(
  amount: number,
  currency = 'KRW',
  showSymbol = true
): string {
  if (amount === null || amount === undefined) {
    return '0';
  }

  const formatter = new Intl.NumberFormat('ko-KR', {
    style: showSymbol ? 'currency' : 'decimal',
    currency: currency,
    minimumFractionDigits: currency === 'KRW' ? 0 : 2,
    maximumFractionDigits: currency === 'KRW' ? 0 : 2,
  });

  return formatter.format(amount);
}

/**
 * 천단위 구분자 추가
 */
export function formatNumber(num: number): string {
  if (num === null || num === undefined) {
    return '0';
  }

  return new Intl.NumberFormat('ko-KR').format(num);
}

/**
 * 통화 문자열을 숫자로 변환
 */
export function parseCurrency(currencyString: string): number {
  if (!currencyString) return 0;

  // 숫자가 아닌 모든 문자 제거
  const numericString = currencyString.replace(/[^\d.-]/g, '');
  const number = parseFloat(numericString);

  return isNaN(number) ? 0 : number;
}

/**
 * 금액을 약식 표기로 변환 (예: 1,000,000 -> 1M)
 */
export function formatCompactCurrency(amount: number, currency = 'KRW'): string {
  if (amount === null || amount === undefined) {
    return '0';
  }

  const absAmount = Math.abs(amount);
  const sign = amount < 0 ? '-' : '';

  let value: number;
  let suffix: string;

  if (currency === 'KRW') {
    // 한국 원화: 만원, 억원 단위
    if (absAmount >= 100000000) {
      value = absAmount / 100000000;
      suffix = '억';
    } else if (absAmount >= 10000) {
      value = absAmount / 10000;
      suffix = '만';
    } else {
      return formatCurrency(amount, currency);
    }
  } else {
    // 기타 통화: K, M, B 단위
    if (absAmount >= 1000000000) {
      value = absAmount / 1000000000;
      suffix = 'B';
    } else if (absAmount >= 1000000) {
      value = absAmount / 1000000;
      suffix = 'M';
    } else if (absAmount >= 1000) {
      value = absAmount / 1000;
      suffix = 'K';
    } else {
      return formatCurrency(amount, currency);
    }
  }

  const formattedValue = value % 1 === 0 ? value.toFixed(0) : value.toFixed(1);
  return `${sign}${formattedValue}${suffix}${currency === 'KRW' ? '원' : ''}`;
}

/**
 * 퍼센트 포맷
 */
export function formatPercent(value: number, decimals = 1): string {
  if (value === null || value === undefined) {
    return '0%';
  }

  return `${value.toFixed(decimals)}%`;
}

/**
 * 소수점 반올림
 */
export function roundToDecimal(num: number, decimals = 2): number {
  const factor = Math.pow(10, decimals);
  return Math.round(num * factor) / factor;
}
