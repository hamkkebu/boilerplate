#!/bin/bash
# ============================================
# K3s GCE Instance Startup Script
# ============================================
# 이 스크립트는 GCE startup-script로 실행됩니다.
# Terraform templatefile()을 통해 변수가 주입됩니다.
#
# 필요한 변수:
# - artifact_registry_url: Artifact Registry URL
# - gcp_region: GCP 리전
# - gcp_project_id: GCP 프로젝트 ID
# - db_root_password: MySQL root 비밀번호
# ============================================

set -e
exec > >(tee /var/log/startup-script.log) 2>&1

echo "=========================================="
echo "Starting K3s setup at $(date)"
echo "=========================================="

# ============================================
# 1. 시스템 업데이트
# ============================================
echo "[1/8] Updating system packages..."
apt-get update -y
apt-get upgrade -y

# ============================================
# 2. 필수 패키지 설치
# ============================================
echo "[2/8] Installing required packages..."
apt-get install -y \
  docker.io \
  jq \
  curl \
  mysql-client

# Docker 서비스 시작
systemctl enable docker
systemctl start docker

# ============================================
# 3. K3s 설치
# ============================================
echo "[3/8] Installing K3s..."
curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="--disable traefik --write-kubeconfig-mode 644" sh -

# K3s 서비스 대기
echo "Waiting for K3s to be ready..."
until kubectl get nodes 2>/dev/null | grep -q "Ready"; do
  sleep 5
done
echo "K3s is ready!"

# ============================================
# 4. kubectl 설정
# ============================================
echo "[4/8] Configuring kubectl..."
# hamkkebu 사용자가 있으면 설정
if id "hamkkebu" &>/dev/null; then
  cat >> /home/hamkkebu/.bashrc <<'BASHRC'
alias k=kubectl
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
BASHRC
fi

# root 사용자에도 설정
cat >> /root/.bashrc <<'BASHRC'
alias k=kubectl
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml
BASHRC

# ============================================
# 5. Artifact Registry 인증 설정
# ============================================
echo "[5/8] Setting up Artifact Registry authentication..."

ARTIFACT_REGISTRY_URL="${artifact_registry_url}"
GCP_REGION="${gcp_region}"

cat > /usr/local/bin/ar-login.sh <<'ARLOGIN'
#!/bin/bash
set -e

ARTIFACT_REGISTRY_URL="${artifact_registry_url}"
GCP_REGION="${gcp_region}"

if [ -z "$ARTIFACT_REGISTRY_URL" ]; then
  echo "Artifact Registry URL not configured, skipping..."
  exit 0
fi

echo "Logging into Artifact Registry: $ARTIFACT_REGISTRY_URL"

# GCE 메타데이터 서버에서 access token 획득
TOKEN=$(curl -s -H "Metadata-Flavor: Google" \
  "http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token" | jq -r '.access_token')

# K3s registries.yaml 업데이트
REGISTRY_HOST=$(echo "$ARTIFACT_REGISTRY_URL" | cut -d'/' -f1)

cat > /etc/rancher/k3s/registries.yaml <<REGISTRIES
mirrors:
  "$REGISTRY_HOST":
    endpoint:
      - "https://$REGISTRY_HOST"
configs:
  "$REGISTRY_HOST":
    auth:
      username: oauth2accesstoken
      password: "$TOKEN"
REGISTRIES

# K3s 재시작 (무중단)
systemctl restart k3s

echo "Artifact Registry login completed at $(date)"
ARLOGIN

chmod +x /usr/local/bin/ar-login.sh

# ============================================
# 6. Cron Job 설정 (토큰 갱신)
# ============================================
echo "[6/8] Setting up token refresh cron job..."

# 매 30분마다 토큰 갱신 (GCP access token 유효기간: 1시간)
cat > /etc/cron.d/ar-login <<'CRON'
# Artifact Registry token refresh every 30 minutes
*/30 * * * * root /usr/local/bin/ar-login.sh >> /var/log/ar-login.log 2>&1
CRON

chmod 644 /etc/cron.d/ar-login

# ============================================
# 7. MySQL Docker 컨테이너 실행
# ============================================
echo "[7/8] Starting MySQL in Docker..."

DB_ROOT_PASSWORD="${db_root_password}"

# MySQL 데이터 디렉토리 생성
mkdir -p /var/lib/mysql-data

# MySQL Docker 실행
docker run -d \
  --name hamkkebu-mysql \
  --restart unless-stopped \
  -p 3306:3306 \
  -e MYSQL_ROOT_PASSWORD="$DB_ROOT_PASSWORD" \
  -e TZ=Asia/Seoul \
  -v /var/lib/mysql-data:/var/lib/mysql \
  mysql:8.0 \
  --character-set-server=utf8mb4 \
  --collation-server=utf8mb4_unicode_ci \
  --default-time-zone='+09:00'

# MySQL 준비 대기
echo "Waiting for MySQL to be ready..."
for i in $(seq 1 60); do
  if docker exec hamkkebu-mysql mysqladmin ping -h localhost -u root -p"$DB_ROOT_PASSWORD" --silent 2>/dev/null; then
    echo "MySQL is ready!"
    break
  fi
  sleep 5
done

# 데이터베이스 생성
echo "Creating databases..."
docker exec hamkkebu-mysql mysql -u root -p"$DB_ROOT_PASSWORD" -e "
CREATE DATABASE IF NOT EXISTS hamkkebu_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS hamkkebu_ledger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS hamkkebu_transaction CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
"

echo "Databases created successfully!"

# ============================================
# 8. 초기 Artifact Registry 로그인 실행
# ============================================
echo "[8/8] Running initial Artifact Registry login..."
/usr/local/bin/ar-login.sh || echo "Initial AR login failed (may be expected if AR not ready)"

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
echo "- MySQL Version: $(docker exec hamkkebu-mysql mysql --version 2>/dev/null || echo 'starting...')"
echo ""
echo "Next Steps:"
echo "1. SSH into this instance"
echo "2. Run: kubectl get nodes"
echo "3. Deploy your applications"
