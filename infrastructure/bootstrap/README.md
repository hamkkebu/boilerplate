# Terraform Bootstrap

S3 + DynamoDB를 생성하여 Terraform state backend를 설정합니다.

## 사용법

```bash
# 1. Bootstrap 실행 (최초 1회만)
cd infrastructure/bootstrap
terraform init
terraform apply

# 2. 실제 인프라 배포
cd ../environments/dev
terraform init
cp terraform.tfvars.example terraform.tfvars
# terraform.tfvars 수정
terraform apply
```

## 생성되는 리소스

| 리소스 | 이름 | 용도 |
|--------|------|------|
| S3 Bucket | hamkkebu-terraform-state | State 저장 |
| DynamoDB | terraform-state-lock | State Lock |

## 비용

- S3: 프리티어 5GB 무료
- DynamoDB: PAY_PER_REQUEST 프리티어 무료
