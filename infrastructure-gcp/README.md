# 함께부 GCP 인프라 (Terraform)

AWS에서 GCP로 전환된 인프라 코드입니다.

## 구조

```
infrastructure-gcp/
├── bootstrap/                    # 초기 설정 (GCS state bucket, SSH key)
├── modules/
│   ├── gce-k3s-freetier/        # GCE e2-micro + K3s + Docker MySQL
│   └── artifact-registry/        # Artifact Registry + GitHub Actions SA
├── environments/
│   ├── dev/                      # 개발 환경 (Always Free)
│   └── production/               # 프로덕션 환경 (GKE Autopilot)
```

## AWS → GCP 리소스 매핑

| AWS | GCP | 비고 |
|-----|-----|------|
| S3 (state) | GCS | GCS 내장 잠금 (DynamoDB 불필요) |
| EC2 t2.micro | GCE e2-micro | Always Free (기간 무제한) |
| ECR | Artifact Registry | 500MB 무료 |
| RDS MySQL | Docker MySQL (VM 내부) | Cloud SQL 무료 없음 |
| EKS | GKE Autopilot | 프로덕션용 |
| IAM User | Service Account + WIF | GitHub Actions OIDC |
| VPC + Subnet | VPC Network + Subnet | 유사 |
| EIP | Static External IP | 선택 |
| Security Group | Firewall Rules | 유사 |

## 사용 방법

### 1. 사전 준비
```bash
# GCP CLI 설치 및 인증
gcloud auth login
gcloud config set project YOUR_PROJECT_ID

# Terraform 설치 (>= 1.5.0)
```

### 2. Bootstrap (최초 1회)
```bash
cd bootstrap
terraform init
terraform plan -var="gcp_project_id=YOUR_PROJECT_ID"
terraform apply -var="gcp_project_id=YOUR_PROJECT_ID"

# SSH 키 백업
cp hamkkebu-dev-key.pem ~/.ssh/
```

### 3. Dev 환경 배포
```bash
cd environments/dev
terraform init
cp terraform.tfvars.example terraform.tfvars
# terraform.tfvars 편집
terraform plan
terraform apply
```

## 비용 (Always Free)

개발 환경은 GCP Always Free 티어를 활용하여 거의 무료입니다:
- GCE e2-micro: **$0** (기간 제한 없음!)
- 30GB Standard PD: **$0**
- Artifact Registry: **$0** (500MB)
- Cloud NAT: ~$1/월
- **총: ~$1/월**

참고: AWS Free Tier는 12개월 후 ~$25/월
