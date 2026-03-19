/**
 * 로컬 스토리지 관련 유틸리티 함수
 */

/**
 * 로컬 스토리지에 데이터 저장
 */
export function setItem<T>(key: string, value: T): void {
  try {
    const serialized = JSON.stringify(value);
    localStorage.setItem(key, serialized);
  } catch (error) {
    console.error(`Failed to save to localStorage (key: ${key}):`, error);
  }
}

/**
 * 로컬 스토리지에서 데이터 가져오기
 */
export function getItem<T>(key: string): T | null {
  try {
    const serialized = localStorage.getItem(key);
    if (serialized === null) {
      return null;
    }
    return JSON.parse(serialized) as T;
  } catch (error) {
    console.error(`Failed to load from localStorage (key: ${key}):`, error);
    return null;
  }
}

/**
 * 로컬 스토리지에서 데이터 삭제
 */
export function removeItem(key: string): void {
  try {
    localStorage.removeItem(key);
  } catch (error) {
    console.error(`Failed to remove from localStorage (key: ${key}):`, error);
  }
}

/**
 * 로컬 스토리지 전체 삭제
 */
export function clear(): void {
  try {
    localStorage.clear();
  } catch (error) {
    console.error('Failed to clear localStorage:', error);
  }
}

/**
 * 로컬 스토리지에 키가 존재하는지 확인
 */
export function hasItem(key: string): boolean {
  return localStorage.getItem(key) !== null;
}

/**
 * 만료 시간을 가진 데이터 저장
 */
export function setItemWithExpiry<T>(key: string, value: T, expiryMs: number): void {
  try {
    const now = new Date();
    const item = {
      value: value,
      expiry: now.getTime() + expiryMs,
    };
    const serialized = JSON.stringify(item);
    localStorage.setItem(key, serialized);
  } catch (error) {
    console.error(`Failed to save to localStorage with expiry (key: ${key}):`, error);
  }
}

/**
 * 만료 시간을 확인하며 데이터 가져오기
 */
export function getItemWithExpiry<T>(key: string): T | null {
  try {
    const serialized = localStorage.getItem(key);
    if (!serialized) {
      return null;
    }

    const item = JSON.parse(serialized);
    const now = new Date();

    // 만료 시간 확인
    if (now.getTime() > item.expiry) {
      localStorage.removeItem(key);
      return null;
    }

    return item.value as T;
  } catch (error) {
    console.error(`Failed to load from localStorage with expiry (key: ${key}):`, error);
    return null;
  }
}

/**
 * 세션 스토리지에 데이터 저장
 */
export function setSessionItem<T>(key: string, value: T): void {
  try {
    const serialized = JSON.stringify(value);
    sessionStorage.setItem(key, serialized);
  } catch (error) {
    console.error(`Failed to save to sessionStorage (key: ${key}):`, error);
  }
}

/**
 * 세션 스토리지에서 데이터 가져오기
 */
export function getSessionItem<T>(key: string): T | null {
  try {
    const serialized = sessionStorage.getItem(key);
    if (serialized === null) {
      return null;
    }
    return JSON.parse(serialized) as T;
  } catch (error) {
    console.error(`Failed to load from sessionStorage (key: ${key}):`, error);
    return null;
  }
}

/**
 * 세션 스토리지에서 데이터 삭제
 */
export function removeSessionItem(key: string): void {
  try {
    sessionStorage.removeItem(key);
  } catch (error) {
    console.error(`Failed to remove from sessionStorage (key: ${key}):`, error);
  }
}
