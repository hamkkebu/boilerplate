# Backend Service Infrastructure

Backend 서비스를 위한 Terraform 인프라 구성입니다.

## 리소스

이 구성은 다음 리소스를 생성합니다:

- **ECS Task Definition**: Spring Boot 애플리케이션을 실행하는 Fargate 태스크
- **ECS Service**: 태스크를 관리하고 실행하는 서비스
- **ALB Target Group**: 백엔드 서비스를 위한 타겟 그룹
- **ALB Listener Rule**: `/api/*` 경로를 백엔드로 라우팅
- **IAM Roles**: ECS 태스크 실행 및 태스크 역할
- **CloudWatch Log Group**: 애플리케이션 로그
- **Secrets Manager**: 데이터베이스 자격 증명
- **Auto Scaling**: CPU 기반 자동 스케일링

## 사전 요구사항

1. 공통 인프라가 먼저 배포되어 있어야 합니다
2. ECR 리포지토리가 생성되어 있어야 합니다
3. Docker 이미지가 ECR에 푸시되어 있어야 합니다

## ECR 리포지토리 생성

```bash
aws ecr create-repository \
  --repository-name hamkkebu-backend \
  --region ap-northeast-2
```

## Docker 이미지 빌드 및 푸시

```bash
# ECR 로그인
aws ecr get-login-password --region ap-northeast-2 | \
  docker login --username AWS --password-stdin <account-id>.dkr.ecr.ap-northeast-2.amazonaws.com

# 이미지 빌드
docker build -t hamkkebu-backend:latest .

# 이미지 태그
docker tag hamkkebu-backend:latest \
  <account-id>.dkr.ecr.ap-northeast-2.amazonaws.com/hamkkebu-backend:latest

# 이미지 푸시
docker push <account-id>.dkr.ecr.ap-northeast-2.amazonaws.com/hamkkebu-backend:latest
```

## 배포

```bash
cd backend/infrastructure

# terraform.tfvars 파일 생성 및 수정
cp terraform.tfvars.example terraform.tfvars

# Terraform 초기화
terraform init -backend-config="bucket=your-terraform-state-bucket"

# 변경사항 확인
terraform plan

# 배포
terraform apply
```

## Secrets Manager 설정

배포 후 데이터베이스 자격 증명을 Secrets Manager에 추가해야 합니다:

```bash
aws secretsmanager put-secret-value \
  --secret-id hamkkebu/dev/backend/db-credentials \
  --secret-string '{"username":"admin","password":"your-password"}' \
  --region ap-northeast-2
```

## 환경 변수

Task Definition에서 다음 환경 변수가 설정됩니다:

- `SPRING_PROFILES_ACTIVE`: 환경 (dev/staging/prod)
- `SERVER_PORT`: 애플리케이션 포트 (8080)
- `DB_HOST`: RDS 엔드포인트
- `DB_PORT`: RDS 포트
- `DB_NAME`: 데이터베이스 이름
- `DB_USERNAME`: Secrets Manager에서 가져옴
- `DB_PASSWORD`: Secrets Manager에서 가져옴

## Auto Scaling

ECS 서비스는 CPU 사용률을 기반으로 자동 스케일링됩니다:

- Target CPU Utilization: 70%
- Min Capacity: 1
- Max Capacity: 4
- Scale Out Cooldown: 60초
- Scale In Cooldown: 300초

## 모니터링

### CloudWatch Logs

로그는 다음 위치에 저장됩니다:
```
/ecs/hamkkebu-dev/backend
```

### 로그 확인

```bash
aws logs tail /ecs/hamkkebu-dev/backend --follow
```

## Health Check

ALB는 다음 엔드포인트로 헬스 체크를 수행합니다:
- Path: `/actuator/health`
- Expected Status: 200
- Interval: 30초
- Timeout: 5초
- Healthy Threshold: 2
- Unhealthy Threshold: 3

Spring Boot 애플리케이션에 Spring Actuator가 설정되어 있어야 합니다.

## 업데이트

새 버전 배포:

```bash
# 새 이미지 빌드 및 푸시
docker build -t hamkkebu-backend:v1.1 .
docker tag hamkkebu-backend:v1.1 \
  <account-id>.dkr.ecr.ap-northeast-2.amazonaws.com/hamkkebu-backend:v1.1
docker push <account-id>.dkr.ecr.ap-northeast-2.amazonaws.com/hamkkebu-backend:v1.1

# terraform.tfvars에서 image_tag 업데이트
# image_tag = "v1.1"

# 배포
terraform apply
```

또는 AWS CLI로 직접 업데이트:

```bash
aws ecs update-service \
  --cluster hamkkebu-dev-cluster \
  --service hamkkebu-dev-backend \
  --force-new-deployment
```

## 트러블슈팅

### 태스크가 시작하지 않는 경우

1. CloudWatch Logs 확인
2. Task Definition의 환경 변수 확인
3. IAM 역할 권한 확인
4. 보안 그룹 설정 확인

### 데이터베이스 연결 실패

1. RDS 엔드포인트 확인
2. 보안 그룹 규칙 확인
3. Secrets Manager의 자격 증명 확인

## 정리

리소스 삭제:

```bash
terraform destroy
```
