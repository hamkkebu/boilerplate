# GitHub Actions Secrets 관리 가이드

## 개요

이 문서는 hamkkebu 프로젝트의 GitHub Actions에서 사용하는 Secrets를 설명합니다.

## Secrets 구조

### Organization Level Secrets (권장)

모든 서비스에서 공통으로 사용하는 secrets는 Organization level에서 관리합니다.

```
GitHub Organization (hamkkebu)
└── Settings → Secrets and variables → Actions
    ├── AWS_ACCESS_KEY_ID
    ├── AWS_SECRET_ACCESS_KEY
    ├── AWS_ACCOUNT_ID
    ├── KUBECONFIG
    ├── DB_HOST
    ├── DB_PORT
    ├── DB_USERNAME
    ├── DB_PASSWORD
    └── KAFKA_BOOTSTRAP_SERVERS
```

### Repository Level Secrets

서비스별 특수한 설정만 Repository level에서 관리합니다.

```
auth-service repository
└── Settings → Secrets and variables → Actions
    ├── KEYCLOAK_AUTH_SERVER_URL
    └── KEYCLOAK_REALM
```

---

## Secrets 상세 설명

### AWS 인증 관련

| Secret Name | 설명 | 예시 값 | 설정 방법 |
|-------------|------|---------|----------|
| `AWS_ACCESS_KEY_ID` | IAM User Access Key | `AKIAIOSFODNN7EXAMPLE` | Terraform ECR 모듈 생성 후 |
| `AWS_SECRET_ACCESS_KEY` | IAM User Secret Key | `wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY` | Terraform ECR 모듈 생성 후 |
| `AWS_ACCOUNT_ID` | AWS 계정 ID | `123456789012` | AWS Console에서 확인 |

### Kubernetes 관련

| Secret Name | 설명 | 예시 값 | 설정 방법 |
|-------------|------|---------|----------|
| `KUBECONFIG` | K8s 클러스터 접속 정보 (Base64) | `YXBpVmVyc2lvb...` | 아래 스크립트 참조 |

**KUBECONFIG 생성 방법 (K3s):**

```bash
# EC2 인스턴스에서 kubeconfig 가져오기
ssh -i key.pem ec2-user@<EC2_PUBLIC_IP> "sudo cat /etc/rancher/k3s/k3s.yaml" > kubeconfig.yaml

# Public IP로 변경
sed -i "s/127.0.0.1/<EC2_PUBLIC_IP>/g" kubeconfig.yaml

# Base64 인코딩
KUBECONFIG_BASE64=$(cat kubeconfig.yaml | base64 -w 0)

# GitHub Secret으로 설정
gh secret set KUBECONFIG --body "$KUBECONFIG_BASE64" --org hamkkebu
```

### Database 관련

| Secret Name | 설명 | 예시 값 | 설정 방법 |
|-------------|------|---------|----------|
| `DB_HOST` | RDS 엔드포인트 | `hamkkebu-dev-xxx.rds.amazonaws.com` | `terraform output rds_address` |
| `DB_PORT` | RDS 포트 | `3306` | 기본값 사용 |
| `DB_USERNAME` | DB 마스터 사용자 | `admin` | terraform.tfvars에서 설정 |
| `DB_PASSWORD` | DB 마스터 비밀번호 | (비밀) | terraform.tfvars에서 설정 |

### Kafka 관련

| Secret Name | 설명 | 예시 값 | 설정 방법 |
|-------------|------|---------|----------|
| `KAFKA_BOOTSTRAP_SERVERS` | Kafka 브로커 주소 | `kafka:9092` | K8s 내부 서비스 이름 |

### Keycloak 관련 (auth-service만)

| Secret Name | 설명 | 예시 값 | 설정 방법 |
|-------------|------|---------|----------|
| `KEYCLOAK_AUTH_SERVER_URL` | Keycloak 서버 URL | `http://keycloak:8080` | K8s 내부 서비스 이름 |
| `KEYCLOAK_REALM` | Keycloak Realm 이름 | `hamkkebu` | Keycloak 설정에서 |

---

## Terraform 배포 후 자동 설정

Terraform 배포 후 다음 스크립트로 Secrets를 자동 설정할 수 있습니다:

```bash
#!/bin/bash
# scripts/setup-github-secrets.sh

set -e

# Terraform outputs 가져오기
cd infrastructure/environments/dev

RDS_HOST=$(terraform output -raw rds_address)
RDS_PORT=$(terraform output -raw rds_port)
K3S_IP=$(terraform output -raw k3s_public_ip)

# GitHub Secrets 설정
gh secret set DB_HOST --body "$RDS_HOST" --org hamkkebu
gh secret set DB_PORT --body "$RDS_PORT" --org hamkkebu

# K3s kubeconfig 설정 (SSH 키 필요)
echo "Fetching kubeconfig from K3s..."
ssh -o StrictHostKeyChecking=no -i ~/.ssh/hamkkebu-dev.pem ec2-user@$K3S_IP \
  "sudo cat /etc/rancher/k3s/k3s.yaml" > /tmp/kubeconfig.yaml

# Public IP로 변경
sed -i "s/127.0.0.1/$K3S_IP/g" /tmp/kubeconfig.yaml

# Base64 인코딩 후 설정
KUBECONFIG_BASE64=$(cat /tmp/kubeconfig.yaml | base64 -w 0)
gh secret set KUBECONFIG --body "$KUBECONFIG_BASE64" --org hamkkebu

echo "GitHub Secrets updated successfully!"
```

---

## 서비스별 ConfigMap 생성 형식

각 서비스의 CI/CD에서 생성하는 ConfigMap 형식:

### auth-service
```yaml
DB_URL: jdbc:mysql://${DB_HOST}:${DB_PORT}/hamkkebu_auth?...
KAFKA_BOOTSTRAP_SERVERS: ${KAFKA_BOOTSTRAP_SERVERS}
KEYCLOAK_AUTH_SERVER_URL: ${KEYCLOAK_AUTH_SERVER_URL}
KEYCLOAK_REALM: ${KEYCLOAK_REALM}
```

### ledger-service
```yaml
DB_URL: jdbc:mysql://${DB_HOST}:${DB_PORT}/hamkkebu_ledger?...
KAFKA_BOOTSTRAP_SERVERS: ${KAFKA_BOOTSTRAP_SERVERS}
AUTH_SERVICE_GRPC_ADDRESS: static://auth-service-backend:9091
```

### transaction-service
```yaml
DB_URL: jdbc:mysql://${DB_HOST}:${DB_PORT}/hamkkebu_transaction?...
KAFKA_BOOTSTRAP_SERVERS: ${KAFKA_BOOTSTRAP_SERVERS}
AUTH_SERVICE_GRPC_ADDRESS: static://auth-service-backend:9091
```

---

## 보안 권장사항

1. **최소 권한 원칙**: GitHub Actions IAM User에는 필요한 최소 권한만 부여
2. **Secrets Rotation**: 주기적으로 비밀번호/키 변경
3. **환경 분리**: dev/production 환경별 별도 secrets 사용
4. **감사 로깅**: GitHub Actions 실행 로그 모니터링

---

## 트러블슈팅

### KUBECONFIG 오류
```
error: error loading config file "/home/runner/.kube/config": yaml: line X: ...
```
→ Base64 인코딩이 올바르게 되었는지 확인:
```bash
echo "$KUBECONFIG_SECRET" | base64 -d | head -10
```

### ECR 로그인 실패
```
Error: Cannot perform an interactive login from a non TTY device
```
→ AWS credentials가 올바르게 설정되었는지 확인

### DB 연결 실패
```
Communications link failure
```
→ Security Group에서 K3s → RDS 3306 포트가 열려있는지 확인
