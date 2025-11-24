-- Migration Script: V001 - Add Conditional Unique Constraints for Soft Delete
-- Created: 2025-11-18
-- Database: MySQL 8.0.13+
-- Purpose: Race Condition 방어 - DB 레벨에서 중복 가입 방지

-- ============================================================================
-- IMPORTANT: 이 스크립트를 실행하기 전에 반드시 백업을 수행하세요!
-- ============================================================================

USE hamkkebu_boilerplate_db;

-- Step 1: 기존의 잘못된 UNIQUE 제약조건 제거 (있는 경우)
-- 기존 schema.sql에 UNIQUE (sample_id, deleted) 형태로 정의된 경우 제거

-- MySQL에서는 UNIQUE 제약조건이 자동으로 INDEX를 생성하므로
-- SHOW INDEX를 통해 확인 후 제거해야 함

-- 기존 unique 제약조건 확인
-- SHOW INDEX FROM tbl_boilerplate_sample WHERE Key_name LIKE 'unique_sample_%';

-- 기존 제약조건이 존재하는 경우 삭제
DROP INDEX IF EXISTS unique_sample_id_when_not_deleted ON tbl_boilerplate_sample;
DROP INDEX IF EXISTS unique_sample_nickname_when_not_deleted ON tbl_boilerplate_sample;

-- 기존 복합 인덱스 제거 (불필요)
DROP INDEX IF EXISTS idx_sample_id_deleted ON tbl_boilerplate_sample;
DROP INDEX IF EXISTS idx_sample_nickname_deleted ON tbl_boilerplate_sample;

-- Step 2: 데이터 정합성 검증
-- deleted=false인 레코드 중 중복된 sample_id가 있는지 확인
SELECT
    sample_id,
    COUNT(*) as duplicate_count
FROM tbl_boilerplate_sample
WHERE deleted = FALSE
GROUP BY sample_id
HAVING COUNT(*) > 1;

-- 중복이 발견되면 먼저 중복 데이터를 정리해야 함
-- 예: 최신 레코드만 남기고 나머지는 soft delete 처리
-- UPDATE tbl_boilerplate_sample
-- SET deleted = TRUE, deleted_at = NOW()
-- WHERE sample_num IN (SELECT sample_num FROM (
--     SELECT sample_num, ROW_NUMBER() OVER (PARTITION BY sample_id ORDER BY created_at DESC) as rn
--     FROM tbl_boilerplate_sample
--     WHERE deleted = FALSE
-- ) t WHERE t.rn > 1);

-- Step 3: 조건부 UNIQUE 인덱스 생성 (Functional Index - MySQL 8.0.13+)
-- deleted=false인 레코드에 대해서만 sample_id가 유일해야 함
-- NULL 값은 유니크 제약에서 제외됨 (여러 deleted=true 레코드 허용)

CREATE UNIQUE INDEX idx_sample_id_active
    ON tbl_boilerplate_sample ((CASE WHEN deleted = FALSE THEN sample_id END));

CREATE UNIQUE INDEX idx_sample_nickname_active
    ON tbl_boilerplate_sample ((CASE WHEN deleted = FALSE THEN sample_nickname END));

-- Step 4: 조회 성능을 위한 일반 인덱스 생성
-- 기존에 없는 경우에만 생성
CREATE INDEX IF NOT EXISTS idx_sample_id ON tbl_boilerplate_sample(sample_id);
CREATE INDEX IF NOT EXISTS idx_sample_nickname ON tbl_boilerplate_sample(sample_nickname);

-- Step 5: 인덱스 생성 확인
SHOW INDEX FROM tbl_boilerplate_sample
WHERE Key_name IN ('idx_sample_id_active', 'idx_sample_nickname_active');

-- Step 6: 테스트 (선택적)
-- 동일한 sample_id로 두 번 삽입 시도 → Duplicate key error 발생해야 함
-- INSERT INTO tbl_boilerplate_sample (sample_id, sample_password, deleted)
-- VALUES ('test_duplicate', 'test', FALSE);
--
-- INSERT INTO tbl_boilerplate_sample (sample_id, sample_password, deleted)
-- VALUES ('test_duplicate', 'test', FALSE);  -- ERROR 1062: Duplicate entry
--
-- DELETE FROM tbl_boilerplate_sample WHERE sample_id = 'test_duplicate';

-- ============================================================================
-- Migration Complete
-- ============================================================================

-- 결과 확인:
-- 1. idx_sample_id_active, idx_sample_nickname_active 인덱스가 생성되었는지 확인
-- 2. 애플리케이션에서 중복 가입 시도 시 DataIntegrityViolationException 발생 확인
-- 3. SampleService.java의 createSample() 메서드에서 예외 처리 확인

-- 롤백 방법 (필요시):
-- DROP INDEX idx_sample_id_active ON tbl_boilerplate_sample;
-- DROP INDEX idx_sample_nickname_active ON tbl_boilerplate_sample;
