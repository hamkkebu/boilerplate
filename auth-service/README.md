# Auth Service

Hamkkebu 프로젝트의 인증 및 사용자 관리 서비스입니다.

## 기술 스택

- **Backend**: Spring Boot 3.1.2, Java 17
- **Database**: MySQL 8.0
- **Build**: Gradle
- **Infrastructure**: Terraform, AWS ECS Fargate
- **Common**: Boilerplate (Submodule)

## 프로젝트 구조

```
auth-service/
├── backend/
│   ├── src/main/java/com/hamkkebu/authservice/
│   │   ├── controller/          # REST API Controllers
│   │   ├── service/              # Business Logic
│   │   ├── repository/           # Data Access Layer
│   │   ├── entity/               # JPA Entities
│   │   ├── dto/                  # Data Transfer Objects
│   │   ├── mapper/               # MapStruct Mappers
│   │   ├── event/                # Kafka Events
│   │   └── AuthServiceApplication.java
│   ├── infrastructure/           # Terraform IaC
│   ├── build.gradle
│   └── Dockerfile
├── frontend/                     # Vue.js Frontend (Optional)
├── common/                       # Boilerplate Submodule
└── docker-compose.yml
```

## Boilerplate 통합

이 서비스는 Hamkkebu Boilerplate를 submodule로 사용하여 공통 코드를 공유합니다.

### 공통 코드 (from Boilerplate)
- `ApiResponse<T>` - 표준화된 API 응답
- `PageRequestDto` - 페이징 요청 DTO
- `BusinessException` - 비즈니스 예외 처리
- 기타 공통 유틸리티

### Submodule 업데이트
```bash
# Submodule 초기화
git submodule update --init --recursive

# Submodule 최신 버전으로 업데이트
git submodule update --remote
```

## 로컬 개발 환경

### 사전 요구사항
- Docker & Docker Compose
- Java 17+
- Gradle

### 실행 방법

#### 1. Docker Compose로 전체 실행
```bash
docker-compose up -d
```

#### 2. Backend만 로컬에서 실행
```bash
# MySQL 먼저 시작
docker-compose up -d database

# Backend 빌드 및 실행
cd backend
./gradlew bootRun
```

### 접속
- **Backend API**: http://localhost:8080/api
- **Health Check**: http://localhost:8080/api/users/health
- **MySQL**: localhost:3306 (root/root)

## API 엔드포인트

### 사용자 관리

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/users` | 사용자 생성 |
| GET | `/api/users/{userId}` | 사용자 조회 (ID) |
| GET | `/api/users/username/{username}` | 사용자 조회 (Username) |
| GET | `/api/users/email/{email}` | 사용자 조회 (Email) |
| GET | `/api/users` | 전체 사용자 조회 (페이징) |
| GET | `/api/users/active` | 활성 사용자 조회 (페이징) |
| PUT | `/api/users/{userId}` | 사용자 정보 수정 |
| DELETE | `/api/users/{userId}` | 사용자 비활성화 |
| PATCH | `/api/users/{userId}/verify` | 사용자 인증 |
| PATCH | `/api/users/{userId}/last-login` | 마지막 로그인 시간 업데이트 |
| GET | `/api/users/health` | Health Check |

### 요청/응답 예시

#### 사용자 생성
```bash
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

#### 응답 (성공)
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "username": "newuser",
    "email": "newuser@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "isActive": true,
    "isVerified": false,
    "createdAt": "2025-01-20T12:00:00"
  },
  "message": "User created successfully"
}
```

## 데이터베이스

### 스키마
- **users**: 사용자 기본 정보
- **user_roles**: 사용자 권한 (추후 확장)

### 초기 데이터
개발 환경에서는 다음 샘플 사용자가 자동으로 생성됩니다:
- k1m743hyun (ADMIN, USER)
- admin (ADMIN, USER)
- testuser (USER)
- demo (USER)

## AWS 배포

### 사전 요구사항
1. 공통 인프라가 먼저 배포되어 있어야 합니다
2. ECR 리포지토리 생성
3. Docker 이미지 빌드 및 푸시

### ECR 설정
```bash
# ECR 리포지토리 생성
aws ecr create-repository \
  --repository-name hamkkebu-auth-service \
  --region ap-northeast-2

# Docker 이미지 빌드 및 푸시
cd backend
docker build -t hamkkebu-auth-service:latest .

aws ecr get-login-password --region ap-northeast-2 | \
  docker login --username AWS --password-stdin <account-id>.dkr.ecr.ap-northeast-2.amazonaws.com

docker tag hamkkebu-auth-service:latest \
  <account-id>.dkr.ecr.ap-northeast-2.amazonaws.com/hamkkebu-auth-service:latest

docker push <account-id>.dkr.ecr.ap-northeast-2.amazonaws.com/hamkkebu-auth-service:latest
```

### Terraform 배포
```bash
cd backend/infrastructure

# terraform.tfvars 파일 생성 및 수정
cp terraform.tfvars.example terraform.tfvars

# 초기화 및 배포
terraform init -backend-config="bucket=your-terraform-state-bucket"
terraform plan
terraform apply
```

### Secrets Manager 설정
```bash
aws secretsmanager put-secret-value \
  --secret-id hamkkebu/dev/auth-service/db-credentials \
  --secret-string '{"username":"root","password":"your-password"}' \
  --region ap-northeast-2
```

## 개발 가이드

### 새로운 기능 추가
1. Entity 생성 (`entity/`)
2. DTO 생성 (`dto/`)
3. Mapper 작성 (`mapper/`)
4. Repository 작성 (`repository/`)
5. Service 작성 (`service/`)
6. Controller 작성 (`controller/`)

### 공통 코드 사용
```java
// Boilerplate의 공통 코드 사용
import com.hamkkebu.boilerplate.common.dto.ApiResponse;
import com.hamkkebu.boilerplate.common.dto.PageRequestDto;
import com.hamkkebu.boilerplate.common.exception.BusinessException;

// Controller에서 ApiResponse 사용
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<UserResponseDto>> getUser(@PathVariable Long id) {
    UserResponseDto user = userService.getUserById(id);
    return ResponseEntity.ok(ApiResponse.success(user));
}

// Service에서 BusinessException 사용
if (!userRepository.existsById(userId)) {
    throw new BusinessException("User not found: " + userId);
}
```

## 테스트

```bash
# 단위 테스트 실행
./gradlew test

# 통합 테스트 실행
./gradlew integrationTest
```

## 환경 변수

| Variable | Description | Default |
|----------|-------------|---------|
| SERVER_PORT | 서버 포트 | 8080 |
| SPRING_DATASOURCE_URL | DB 연결 URL | jdbc:mysql://localhost:3306/hamkkebu_auth |
| SPRING_DATASOURCE_USERNAME | DB 사용자명 | root |
| SPRING_DATASOURCE_PASSWORD | DB 비밀번호 | root |
| SPRING_KAFKA_BOOTSTRAP_SERVERS | Kafka 서버 | localhost:9092 |

## 보안 고려사항

⚠️ **주의**: 현재 비밀번호는 평문으로 저장됩니다. 프로덕션 환경에서는 반드시 BCrypt 등을 사용하여 해싱해야 합니다.

TODO:
- [ ] BCrypt 비밀번호 해싱 구현
- [ ] JWT 토큰 기반 인증 추가
- [ ] OAuth2 소셜 로그인 통합
- [ ] Rate Limiting 구현

## 문의

문제가 발생하거나 질문이 있으시면 이슈를 등록해주세요.
