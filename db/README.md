# Database Migrations

이 디렉토리는 데이터베이스 마이그레이션 스크립트를 포함합니다.

## 디렉토리 구조

```
db/
├── README.md                          # 이 파일
└── migrations/                        # 마이그레이션 스크립트
    └── V001__add_conditional_unique_constraints.sql
```

## 마이그레이션 적용 방법

### 개발 환경

개발 환경에서는 `schema.sql`이 자동으로 실행됩니다:

```yaml
# application-dev.yml
spring:
  sql:
    init:
      mode: always  # 시작 시 SQL 스크립트 실행
```

### 프로덕션 환경

프로덕션 환경에서는 **반드시 수동으로** 마이그레이션을 적용해야 합니다:

1. **백업 수행**
   ```bash
   mysqldump -u root -p hamkkebu_boilerplate_db > backup_$(date +%Y%m%d_%H%M%S).sql
   ```

2. **마이그레이션 스크립트 실행**
   ```bash
   mysql -u root -p hamkkebu_boilerplate_db < db/migrations/V001__add_conditional_unique_constraints.sql
   ```

3. **결과 확인**
   ```sql
   USE hamkkebu_boilerplate_db;

   -- 인덱스 생성 확인
   SHOW INDEX FROM tbl_boilerplate_sample
   WHERE Key_name IN ('idx_sample_id_active', 'idx_sample_nickname_active');

   -- 중복 테스트
   INSERT INTO tbl_boilerplate_sample (sample_id, sample_password, deleted)
   VALUES ('test_dup', 'test', FALSE);

   -- 아래는 Duplicate key error가 발생해야 함
   INSERT INTO tbl_boilerplate_sample (sample_id, sample_password, deleted)
   VALUES ('test_dup', 'test', FALSE);

   -- 테스트 데이터 삭제
   DELETE FROM tbl_boilerplate_sample WHERE sample_id = 'test_dup';
   ```

4. **롤백 (필요시)**
   ```sql
   DROP INDEX idx_sample_id_active ON tbl_boilerplate_sample;
   DROP INDEX idx_sample_nickname_active ON tbl_boilerplate_sample;
   ```

## 마이그레이션 목록

### V001: Conditional Unique Constraints

**목적**: Race Condition 방어를 위한 조건부 유니크 제약조건 추가

**변경 사항**:
- 기존의 잘못된 `UNIQUE (sample_id, deleted)` 제약조건 제거
- Functional Index 기반 조건부 UNIQUE 제약조건 추가
  - `idx_sample_id_active`: deleted=false인 레코드에 대해서만 sample_id 유일
  - `idx_sample_nickname_active`: deleted=false인 레코드에 대해서만 sample_nickname 유일

**영향**:
- 동시에 같은 ID로 가입 시도 시 DB 레벨에서 차단
- SampleService의 DataIntegrityViolationException 처리 로직 활성화
- Soft delete된 사용자의 ID 재사용 가능

**요구사항**:
- MySQL 8.0.13 이상

**호환성**:
- MySQL 8.0.13+ : Functional Index 지원 ✅
- MySQL 5.7 이하 : Functional Index 미지원 ⚠️ (generated column 사용 필요)

## MySQL 5.7 이하 호환성

MySQL 5.7 이하 버전에서는 Functional Index를 지원하지 않습니다.
대신 Generated Column을 사용하는 방법이 있습니다:

```sql
-- Generated Column 추가
ALTER TABLE tbl_boilerplate_sample
ADD COLUMN sample_id_active VARCHAR(20)
  GENERATED ALWAYS AS (CASE WHEN deleted = FALSE THEN sample_id END) STORED;

ALTER TABLE tbl_boilerplate_sample
ADD COLUMN sample_nickname_active VARCHAR(50)
  GENERATED ALWAYS AS (CASE WHEN deleted = FALSE THEN sample_nickname END) STORED;

-- UNIQUE 제약조건 추가
CREATE UNIQUE INDEX idx_sample_id_active
  ON tbl_boilerplate_sample(sample_id_active);

CREATE UNIQUE INDEX idx_sample_nickname_active
  ON tbl_boilerplate_sample(sample_nickname_active);
```

## 주의사항

1. **백업 필수**: 마이그레이션 전 반드시 백업을 수행하세요
2. **중복 데이터 확인**: 마이그레이션 전 deleted=false인 레코드 중 중복 데이터가 있는지 확인
3. **다운타임**: 인덱스 생성 중 테이블 락이 발생할 수 있으므로 트래픽이 적은 시간에 수행
4. **애플리케이션 코드**: SampleService.java의 예외 처리 로직이 제대로 동작하는지 확인

## Flyway/Liquibase 통합 (선택적)

프로덕션 환경에서 체계적인 마이그레이션 관리가 필요한 경우 Flyway나 Liquibase 사용을 권장합니다:

### Flyway 예시

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

```yaml
# application-prod.yml
spring:
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
```

스크립트 파일명을 Flyway 규칙에 맞게 변경:
- `V001__add_conditional_unique_constraints.sql` → `V1__add_conditional_unique_constraints.sql`
