# Infrastructure as Code with Terraform

이 디렉토리는 Hamkkebu 프로젝트의 인프라를 코드로 관리하기 위한 Terraform 구성을 포함합니다.

## 디렉토리 구조

```
infrastructure/
├── common/              # 공통 인프라 (VPC, ALB, ECS 클러스터 등)
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars.example
└── modules/             # 재사용 가능한 Terraform 모듈
    ├── vpc/
    ├── security/
    ├── database/
    ├── ecs/
    ├── alb/
    ├── s3/
    └── cloudwatch/
```

## 공통 인프라 구성

### 포함된 리소스

- **VPC**: 격리된 네트워크 환경
  - Public/Private 서브넷 (Multi-AZ)
  - NAT Gateway
  - Internet Gateway
  - Route Tables

- **Security Groups**: 보안 그룹
  - ALB 보안 그룹
  - ECS Tasks 보안 그룹
  - RDS 보안 그룹
  - Redis 보안 그룹

- **RDS**: 관리형 MySQL 데이터베이스
  - 자동 백업
  - Multi-AZ 옵션
  - 암호화된 스토리지

- **ECS**: 컨테이너 오케스트레이션
  - Fargate 클러스터
  - Container Insights

- **ALB**: Application Load Balancer
  - HTTP/HTTPS 리스너
  - Health Check

- **S3**: 객체 스토리지
  - 애플리케이션 버킷
  - 로그 버킷

- **CloudWatch**: 모니터링 및 로깅
  - Log Groups
  - Dashboard
  - Alarms

## 사용 방법

### 1. 사전 요구사항

- Terraform >= 1.5.0
- AWS CLI 설정 완료
- 적절한 AWS 권한

### 2. 백엔드 설정

Terraform state를 저장할 S3 버킷과 DynamoDB 테이블을 먼저 생성해야 합니다:

```bash
# S3 버킷 생성
aws s3api create-bucket \
  --bucket your-terraform-state-bucket \
  --region ap-northeast-2 \
  --create-bucket-configuration LocationConstraint=ap-northeast-2

# 버킷 버전관리 활성화
aws s3api put-bucket-versioning \
  --bucket your-terraform-state-bucket \
  --versioning-configuration Status=Enabled

# DynamoDB 테이블 생성 (state locking용)
aws dynamodb create-table \
  --table-name terraform-state-lock \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --billing-mode PAY_PER_REQUEST \
  --region ap-northeast-2
```

### 3. 공통 인프라 배포

```bash
cd infrastructure/common

# terraform.tfvars 파일 생성
cp terraform.tfvars.example terraform.tfvars
# terraform.tfvars 파일을 편집하여 환경에 맞게 수정

# Terraform 초기화
terraform init -backend-config="bucket=your-terraform-state-bucket"

# 인프라 변경사항 확인
terraform plan

# 인프라 배포
terraform apply
```

### 4. 환경별 배포

다른 환경(staging, prod)을 배포하려면 워크스페이스를 사용하거나 별도의 tfvars 파일을 생성:

```bash
# 워크스페이스 사용
terraform workspace new staging
terraform workspace new prod

# 또는 환경별 tfvars 파일 사용
terraform apply -var-file="staging.tfvars"
terraform apply -var-file="prod.tfvars"
```

## 서비스별 인프라

각 서비스는 자신의 `infrastructure/` 폴더에서 서비스 특화 리소스를 관리합니다:

- ECS Task Definition
- IAM Roles
- Service-specific configurations
- Environment variables

예시:
```
backend/
└── infrastructure/
    ├── main.tf
    ├── variables.tf
    └── task-definition.json

frontend/
└── infrastructure/
    ├── main.tf
    └── cloudfront.tf
```

## 보안 고려사항

1. **민감한 정보 관리**
   - `terraform.tfvars` 파일은 `.gitignore`에 추가
   - AWS Secrets Manager 또는 Parameter Store 사용 권장
   - DB 비밀번호는 환경변수로 전달

2. **State 파일 보안**
   - S3 버킷 암호화 활성화
   - 버킷 버전관리 활성화
   - 적절한 IAM 정책 설정

3. **네트워크 보안**
   - Private 서브넷에 데이터베이스 배치
   - 최소 권한 원칙에 따른 보안 그룹 설정

## 비용 최적화

- **개발 환경**:
  - `single_nat_gateway = true` 설정
  - `db_instance_class = "db.t3.micro"`
  - `multi_az = false`

- **프로덕션 환경**:
  - Multi-AZ 활성화
  - 적절한 인스턴스 타입 선택
  - Auto Scaling 설정

## 모듈 설명

### VPC Module
VPC, 서브넷, NAT Gateway, Internet Gateway를 생성합니다.

### Security Module
모든 서비스에 필요한 보안 그룹을 생성합니다.

### Database Module
RDS MySQL 인스턴스를 생성하고 설정합니다.

### ECS Module
ECS 클러스터를 생성합니다.

### ALB Module
Application Load Balancer와 기본 타겟 그룹을 생성합니다.

### S3 Module
애플리케이션 및 로그용 S3 버킷을 생성합니다.

### CloudWatch Module
로그 그룹, 대시보드, 알람을 설정합니다.

## 트러블슈팅

### State Lock 오류
```bash
# Lock 강제 해제 (주의해서 사용)
terraform force-unlock <LOCK_ID>
```

### 리소스 임포트
기존 리소스를 Terraform으로 관리하려면:
```bash
terraform import <resource_type>.<resource_name> <resource_id>
```

## 참고 자료

- [Terraform AWS Provider 문서](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS ECS Best Practices](https://docs.aws.amazon.com/AmazonECS/latest/bestpracticesguide/)
- [Terraform Best Practices](https://www.terraform-best-practices.com/)
