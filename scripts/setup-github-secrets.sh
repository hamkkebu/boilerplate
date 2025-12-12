#!/bin/bash
# ============================================
# GitHub Secrets 자동 설정 스크립트
# ============================================
# Terraform 배포 후 실행하여 GitHub Secrets를 자동 설정합니다.
#
# 필수 조건:
# - gh CLI 설치 및 로그인
# - terraform이 배포된 상태
# - SSH 키가 설정된 상태 (K3s kubeconfig 가져오기용)
#
# 사용법:
# ./scripts/setup-github-secrets.sh [dev|production]
# ============================================

set -e

ENVIRONMENT=${1:-dev}
ORG_NAME="hamkkebu"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
TF_DIR="${SCRIPT_DIR}/../infrastructure/environments/${ENVIRONMENT}"

echo "=========================================="
echo "GitHub Secrets Setup for ${ENVIRONMENT}"
echo "=========================================="

# gh CLI 확인
if ! command -v gh &> /dev/null; then
  echo "Error: gh CLI not found. Please install it first."
  echo "  brew install gh  (macOS)"
  echo "  winget install GitHub.cli  (Windows)"
  exit 1
fi

# gh 로그인 확인
if ! gh auth status &> /dev/null; then
  echo "Error: Not logged in to GitHub. Please run 'gh auth login' first."
  exit 1
fi

# Terraform 디렉토리 확인
if [ ! -d "$TF_DIR" ]; then
  echo "Error: Terraform directory not found: $TF_DIR"
  exit 1
fi

cd "$TF_DIR"

echo ""
echo "[1/4] Fetching Terraform outputs..."

# Terraform outputs 가져오기
RDS_HOST=$(terraform output -raw rds_address 2>/dev/null || echo "")
RDS_PORT=$(terraform output -raw rds_port 2>/dev/null || echo "3306")
K3S_IP=$(terraform output -raw k3s_public_ip 2>/dev/null || echo "")
ECR_URL=$(terraform output -raw ecr_registry_url 2>/dev/null || echo "")

echo "  RDS Host: ${RDS_HOST:-'(not set)'}"
echo "  RDS Port: ${RDS_PORT}"
echo "  K3s IP: ${K3S_IP:-'(not set)'}"
echo "  ECR URL: ${ECR_URL:-'(not set)'}"

# RDS 정보 설정
if [ -n "$RDS_HOST" ]; then
  echo ""
  echo "[2/4] Setting database secrets..."
  gh secret set DB_HOST --body "$RDS_HOST" --org "$ORG_NAME" && echo "  ✓ DB_HOST"
  gh secret set DB_PORT --body "$RDS_PORT" --org "$ORG_NAME" && echo "  ✓ DB_PORT"
else
  echo ""
  echo "[2/4] Skipping database secrets (RDS not deployed)"
fi

# K3s kubeconfig 설정
if [ -n "$K3S_IP" ]; then
  echo ""
  echo "[3/4] Fetching K3s kubeconfig..."

  SSH_KEY="${SSH_KEY:-~/.ssh/hamkkebu-${ENVIRONMENT}.pem}"

  if [ ! -f "$SSH_KEY" ]; then
    echo "  Warning: SSH key not found at $SSH_KEY"
    echo "  Set SSH_KEY environment variable to specify the key location"
    echo "  Example: SSH_KEY=~/.ssh/my-key.pem ./setup-github-secrets.sh"
  else
    # kubeconfig 가져오기
    ssh -o StrictHostKeyChecking=no -i "$SSH_KEY" ec2-user@"$K3S_IP" \
      "sudo cat /etc/rancher/k3s/k3s.yaml" > /tmp/kubeconfig.yaml 2>/dev/null

    if [ -s /tmp/kubeconfig.yaml ]; then
      # Public IP로 변경
      sed -i.bak "s/127.0.0.1/$K3S_IP/g" /tmp/kubeconfig.yaml

      # Base64 인코딩
      if [[ "$OSTYPE" == "darwin"* ]]; then
        KUBECONFIG_BASE64=$(cat /tmp/kubeconfig.yaml | base64)
      else
        KUBECONFIG_BASE64=$(cat /tmp/kubeconfig.yaml | base64 -w 0)
      fi

      gh secret set KUBECONFIG --body "$KUBECONFIG_BASE64" --org "$ORG_NAME" && echo "  ✓ KUBECONFIG"

      # 정리
      rm -f /tmp/kubeconfig.yaml /tmp/kubeconfig.yaml.bak
    else
      echo "  Warning: Failed to fetch kubeconfig from K3s"
    fi
  fi
else
  echo ""
  echo "[3/4] Skipping kubeconfig (K3s not deployed)"
fi

# 수동 설정 안내
echo ""
echo "[4/4] Manual configuration required..."
echo ""
echo "  The following secrets must be set manually:"
echo ""
echo "  # AWS Credentials (from Terraform ECR module output)"
echo "  gh secret set AWS_ACCESS_KEY_ID --body '<value>' --org $ORG_NAME"
echo "  gh secret set AWS_SECRET_ACCESS_KEY --body '<value>' --org $ORG_NAME"
echo "  gh secret set AWS_ACCOUNT_ID --body '<value>' --org $ORG_NAME"
echo ""
echo "  # Database Password (from terraform.tfvars)"
echo "  gh secret set DB_USERNAME --body 'admin' --org $ORG_NAME"
echo "  gh secret set DB_PASSWORD --body '<your-password>' --org $ORG_NAME"
echo ""
echo "  # Kafka (K8s internal service name)"
echo "  gh secret set KAFKA_BOOTSTRAP_SERVERS --body 'kafka:9092' --org $ORG_NAME"
echo ""
echo "  # Keycloak (auth-service repository only)"
echo "  gh secret set KEYCLOAK_AUTH_SERVER_URL --body 'http://keycloak:8080' --repo $ORG_NAME/auth-service"
echo "  gh secret set KEYCLOAK_REALM --body 'hamkkebu' --repo $ORG_NAME/auth-service"
echo ""

echo "=========================================="
echo "Setup completed!"
echo "=========================================="
