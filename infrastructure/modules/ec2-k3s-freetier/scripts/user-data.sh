#!/bin/bash
# ============================================
# K3s EC2 Instance Setup Script
# ============================================
# 이 스크립트는 EC2 user-data로 실행됩니다.
# Terraform templatefile()을 통해 변수가 주입됩니다.
#
# 필요한 변수:
# - ecr_registry_url: ECR 레지스트리 URL
# - aws_region: AWS 리전
# ============================================

set -e
exec > >(tee /var/log/user-data.log) 2>&1

echo "=========================================="
echo "Starting K3s setup at $(date)"
echo "=========================================="

# ============================================
# 1. 시스템 업데이트
# ============================================
echo "[1/7] Updating system packages..."
dnf update -y

# ============================================
# 2. 필수 패키지 설치
# ============================================
echo "[2/7] Installing required packages..."
dnf install -y docker aws-cli jq mysql

# Docker 서비스 시작
systemctl enable docker
systemctl start docker

# ============================================
# 3. K3s 설치
# ============================================
echo "[3/7] Installing K3s..."
curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="--disable traefik --write-kubeconfig-mode 644" sh -

# K3s 서비스 대기
echo "Waiting for K3s to be ready..."
until kubectl get nodes 2>/dev/null | grep -q "Ready"; do
  sleep 5
done
echo "K3s is ready!"

# ============================================
# 4. kubectl 설정 (ec2-user용)
# ============================================
echo "[4/7] Configuring kubectl for ec2-user..."
cat >> /home/ec2-user/.bashrc <<'BASHRC'
alias k=kubectl
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
BASHRC

# ============================================
# 5. ECR 인증 설정
# ============================================
echo "[5/7] Setting up ECR authentication..."

# ECR 로그인 스크립트 생성
cat > /usr/local/bin/ecr-login.sh <<'ECRLOGIN'
#!/bin/bash
set -e

ECR_REGISTRY="${ecr_registry_url}"
AWS_REGION="${aws_region}"

if [ -z "$ECR_REGISTRY" ]; then
  echo "ECR registry URL not configured, skipping..."
  exit 0
fi

echo "Logging into ECR: $ECR_REGISTRY"
PASSWORD=$(aws ecr get-login-password --region $AWS_REGION)

# K3s registries.yaml 업데이트
cat > /etc/rancher/k3s/registries.yaml <<REGISTRIES
mirrors:
  "$ECR_REGISTRY":
    endpoint:
      - "https://$ECR_REGISTRY"
configs:
  "$ECR_REGISTRY":
    auth:
      username: AWS
      password: "$PASSWORD"
REGISTRIES

# K3s 재시작 (무중단)
systemctl restart k3s

echo "ECR login completed at $(date)"
ECRLOGIN

chmod +x /usr/local/bin/ecr-login.sh

# ============================================
# 6. Cron Job 설정 (ECR 토큰 갱신)
# ============================================
echo "[6/7] Setting up ECR token refresh cron job..."

# 매 6시간마다 ECR 토큰 갱신 (토큰 유효기간: 12시간)
cat > /etc/cron.d/ecr-login <<'CRON'
# ECR token refresh every 6 hours
0 */6 * * * root /usr/local/bin/ecr-login.sh >> /var/log/ecr-login.log 2>&1
CRON

chmod 644 /etc/cron.d/ecr-login

# ============================================
# 7. 초기 ECR 로그인 실행
# ============================================
echo "[7/7] Running initial ECR login..."
/usr/local/bin/ecr-login.sh || echo "Initial ECR login failed (may be expected if ECR not ready)"

# ============================================
# 완료
# ============================================
echo "=========================================="
echo "K3s setup completed at $(date)"
echo "=========================================="

# 완료 표시 파일 생성
touch /var/log/k3s-setup-complete

# 시스템 정보 출력
echo ""
echo "System Info:"
echo "- K3s Version: $(k3s --version)"
echo "- Kubectl Version: $(kubectl version --client -o json | jq -r '.clientVersion.gitVersion')"
echo "- Docker Version: $(docker --version)"
echo ""
echo "Next Steps:"
echo "1. SSH into this instance"
echo "2. Run: kubectl get nodes"
echo "3. Deploy your applications"
