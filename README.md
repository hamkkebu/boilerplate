# Hamkkebu Boilerplate

함께부(Hamkkebu) 프로젝트를 위한 마이크로서비스 보일러플레이트입니다.

## 프로젝트 구조

```
boilerplate/
├── backend/              # Spring Boot 백엔드 서비스
│   ├── src/
│   └── infrastructure/   # ECS 배포 설정
├── frontend/             # Vue.js 프론트엔드
│   ├── src/
│   └── infrastructure/   # CloudFront + S3 배포 설정
├── infrastructure/       # 공통 인프라 (VPC, RDS, ECS 클러스터 등)
│   ├── common/
│   └── modules/
├── db/                   # 로컬 개발용 MySQL 데이터
├── docs/                 # 문서
└── scripts/              # 유틸리티 스크립트
```

## 시작하기

### 1. 로컬 개발 환경

#### 사전 요구사항
- Docker & Docker Compose
- Java 17+
- Node.js 16+

#### 실행
```bash
# 전체 서비스 실행 (MySQL, Backend, Frontend)
docker-compose up -d

# 개별 실행
docker-compose up -d mysql        # MySQL만
docker-compose up -d backend      # Backend만
docker-compose up -d frontend     # Frontend만
```

#### 접속
- Frontend: http://localhost:8081
- Backend API: http://localhost:8080
- MySQL: localhost:3306

### 2. AWS 인프라 배포

인프라는 Terraform으로 관리됩니다. 자세한 내용은 [infrastructure/README.md](infrastructure/README.md)를 참고하세요.

#### 배포 순서
1. 공통 인프라 배포 (VPC, RDS, ECS 클러스터)
2. Backend 서비스 배포
3. Frontend 서비스 배포

```bash
# 1. 공통 인프라
cd infrastructure/common
terraform apply

# 2. Backend 서비스
cd ../../backend/infrastructure
terraform apply

# 3. Frontend 서비스
cd ../../frontend/infrastructure
terraform apply
```

## 기술 스택

### Backend
- Spring Boot 3.x
- Java 17
- MySQL 8.0
- Gradle
- Spring Data JPA
- Kafka (이벤트 기반 통신)

### Frontend
- Vue.js 3
- Vue Router
- Vuex
- Axios
- Bootstrap

### Infrastructure
- AWS ECS Fargate (컨테이너 오케스트레이션)
- AWS RDS MySQL (데이터베이스)
- AWS ALB (로드 밸런싱)
- AWS CloudFront + S3 (정적 호스팅)
- Terraform (IaC)

## 주요 기능

### Backend
- RESTful API
- Pagination 지원
- 공통 예외 처리
- API 응답 표준화
- Kafka 이벤트 발행/구독

### Frontend
- SPA (Single Page Application)
- Vue Router 기반 라우팅
- 컴포넌트 기반 아키텍처

## 개발 가이드

### Backend 개발
```bash
cd backend
./gradlew bootRun
```

### Frontend 개발
```bash
cd frontend
npm install
npm run serve
```

### 데이터베이스 백업
```bash
./scripts/backup-database.sh
```

자세한 내용은 [docs/DATABASE_BACKUP_GUIDE.md](docs/DATABASE_BACKUP_GUIDE.md) 참고

## 문서

- [인프라 가이드](infrastructure/README.md) - Terraform 인프라 구성 및 배포
- [Backend 인프라](backend/infrastructure/README.md) - ECS 서비스 배포
- [Frontend 인프라](frontend/infrastructure/README.md) - CloudFront + S3 배포
- [데이터베이스 백업](docs/DATABASE_BACKUP_GUIDE.md) - MySQL 백업 및 복원
- [Git Submodule 가이드](docs/GIT_SUBMODULE_GUIDE.md) - 공통 코드 관리
- [SSL/TLS 설정](docs/SSL_TLS_SETUP.md) - HTTPS 인증서 설정
- [아키텍처 결정](docs/ARCHITECTURE_DECISIONS.md) - 설계 의사결정 기록

## 환경 변수

`.env.example` 파일을 `.env`로 복사하고 필요한 값을 설정하세요.

```bash
cp .env.example .env
```

## Git Submodule로 사용하기

이 boilerplate를 다른 서비스에서 공통 라이브러리로 사용할 수 있습니다.

```bash
# 새 서비스에 submodule 추가
git submodule add https://github.com/your-org/hamkkebu-boilerplate.git common

# 공통 코드 업데이트
git submodule update --remote
```

자세한 내용은 [docs/GIT_SUBMODULE_GUIDE.md](docs/GIT_SUBMODULE_GUIDE.md) 참고

## 보안

- 모든 패스워드는 환경 변수로 관리
- AWS Secrets Manager 사용 권장
- `.env` 파일은 Git에 커밋하지 않음
- HTTPS 사용 (프로덕션)

## 라이선스

MIT License
