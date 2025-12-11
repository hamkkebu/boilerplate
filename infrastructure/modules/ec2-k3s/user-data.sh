#!/bin/bash
set -e

# 로그 설정
exec > >(tee /var/log/user-data.log | logger -t user-data -s 2>/dev/console) 2>&1

echo "============================================"
echo "Starting K3s + ArgoCD Installation"
echo "Time: $(date)"
echo "============================================"

# 변수 설정
AWS_REGION="${aws_region}"
AWS_ACCOUNT_ID="${aws_account_id}"
ARGOCD_PASSWORD="${argocd_password}"
PROJECT_NAME="${project_name}"
DB_HOST="${db_host}"
DB_PORT="${db_port}"

# 시스템 업데이트 (Amazon Linux 2023은 dnf 사용)
echo ">>> Updating system packages..."
dnf update -y
dnf install -y docker git jq curl

# Docker 시작
echo ">>> Starting Docker..."
systemctl start docker
systemctl enable docker
usermod -aG docker ec2-user

# K3s 설치 (경량 Kubernetes)
echo ">>> Installing K3s..."
curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="--disable traefik --write-kubeconfig-mode 644" sh -

# kubectl 설정
echo ">>> Configuring kubectl..."
mkdir -p /root/.kube
cp /etc/rancher/k3s/k3s.yaml /root/.kube/config
chmod 600 /root/.kube/config

# ec2-user용 kubectl 설정
mkdir -p /home/ec2-user/.kube
cp /etc/rancher/k3s/k3s.yaml /home/ec2-user/.kube/config
chown -R ec2-user:ec2-user /home/ec2-user/.kube
chmod 600 /home/ec2-user/.kube/config

# 환경 변수 설정
echo 'export KUBECONFIG=/etc/rancher/k3s/k3s.yaml' >> /home/ec2-user/.bashrc
echo 'alias k=kubectl' >> /home/ec2-user/.bashrc

# K3s 준비 대기
echo ">>> Waiting for K3s to be ready..."
until kubectl get nodes 2>/dev/null | grep -q "Ready"; do
    echo "Waiting for K3s node to be ready..."
    sleep 5
done
echo "K3s is ready!"

# ArgoCD 네임스페이스 생성 및 설치
echo ">>> Installing ArgoCD..."
kubectl create namespace argocd || true

# ArgoCD 설치
kubectl apply -n argocd -f https://raw.githubusercontent.com/argoproj/argo-cd/stable/manifests/install.yaml

# ArgoCD 준비 대기
echo ">>> Waiting for ArgoCD to be ready..."
kubectl wait --for=condition=available --timeout=600s deployment/argocd-server -n argocd

# ArgoCD 서비스를 NodePort로 변경 (30080)
echo ">>> Configuring ArgoCD NodePort service..."
kubectl patch svc argocd-server -n argocd -p '{
  "spec": {
    "type": "NodePort",
    "ports": [
      {
        "name": "http",
        "port": 80,
        "targetPort": 8080,
        "nodePort": 30080
      },
      {
        "name": "https",
        "port": 443,
        "targetPort": 8080,
        "nodePort": 30443
      }
    ]
  }
}'

# ArgoCD CLI 설치
echo ">>> Installing ArgoCD CLI..."
curl -sSL -o /usr/local/bin/argocd https://github.com/argoproj/argo-cd/releases/latest/download/argocd-linux-amd64
chmod +x /usr/local/bin/argocd

# ArgoCD 초기 비밀번호 가져오기 및 변경
echo ">>> Configuring ArgoCD admin password..."
sleep 30  # ArgoCD secret 생성 대기

ARGOCD_INITIAL_PASSWORD=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" | base64 -d)

# ArgoCD 로그인 및 비밀번호 변경
argocd login localhost:30080 --insecure --username admin --password "$ARGOCD_INITIAL_PASSWORD" || true
argocd account update-password --current-password "$ARGOCD_INITIAL_PASSWORD" --new-password "$ARGOCD_PASSWORD" || true

# hamkkebu 네임스페이스 생성
echo ">>> Creating hamkkebu namespace..."
kubectl create namespace hamkkebu || true

# ECR 이미지 풀 시크릿 설정
echo ">>> Setting up ECR pull secret..."
ECR_TOKEN=$(aws ecr get-login-password --region $AWS_REGION)
kubectl create secret docker-registry ecr-registry-secret \
  --docker-server=$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com \
  --docker-username=AWS \
  --docker-password="$ECR_TOKEN" \
  -n hamkkebu || true

# ECR 자격 증명 자동 갱신 스크립트 (12시간마다 갱신 - ECR 토큰은 12시간 유효)
echo ">>> Setting up ECR credential refresh cron..."
cat > /usr/local/bin/ecr-cred-refresh.sh << 'SCRIPT'
#!/bin/bash
AWS_REGION="${aws_region}"
AWS_ACCOUNT_ID="${aws_account_id}"
export KUBECONFIG=/etc/rancher/k3s/k3s.yaml

ECR_TOKEN=$(aws ecr get-login-password --region $AWS_REGION)
kubectl delete secret ecr-registry-secret -n hamkkebu 2>/dev/null || true
kubectl create secret docker-registry ecr-registry-secret \
  --docker-server=$AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com \
  --docker-username=AWS \
  --docker-password="$ECR_TOKEN" \
  -n hamkkebu
SCRIPT

chmod +x /usr/local/bin/ecr-cred-refresh.sh

# Cron job 설정 (6시간마다 실행)
echo "0 */6 * * * root /usr/local/bin/ecr-cred-refresh.sh >> /var/log/ecr-cred-refresh.log 2>&1" > /etc/cron.d/ecr-cred-refresh
chmod 644 /etc/cron.d/ecr-cred-refresh

# ConfigMap for common database configuration
echo ">>> Creating common ConfigMap..."
kubectl create configmap hamkkebu-common-config \
  --from-literal=DB_HOST="$DB_HOST" \
  --from-literal=DB_PORT="$DB_PORT" \
  -n hamkkebu || true

# External Secrets Operator (ESO) 설치
echo ">>> Installing External Secrets Operator..."
helm repo add external-secrets https://charts.external-secrets.io || true
helm repo update

# Helm 설치 (K3s에 기본 포함 안됨)
echo ">>> Installing Helm..."
curl -fsSL https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash

# ESO 설치
helm upgrade --install external-secrets external-secrets/external-secrets \
  --namespace external-secrets \
  --create-namespace \
  --set installCRDs=true \
  --wait

echo ">>> Waiting for External Secrets Operator to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/external-secrets -n external-secrets

# SecretStore 생성 (Kubernetes backend - Dev용)
echo ">>> Creating SecretStore for Kubernetes backend..."
kubectl apply -f - <<EOF
apiVersion: external-secrets.io/v1beta1
kind: SecretStore
metadata:
  name: hamkkebu-secret-store
  namespace: hamkkebu
spec:
  provider:
    kubernetes:
      remoteNamespace: hamkkebu
      server:
        caProvider:
          type: ConfigMap
          name: kube-root-ca.crt
          key: ca.crt
      auth:
        serviceAccount:
          name: default
EOF

echo ">>> External Secrets Operator installed successfully!"

echo "============================================"
echo "K3s + ArgoCD Installation Complete!"
echo "============================================"
echo ""
echo "ArgoCD UI: https://<PUBLIC_IP>:30080"
echo "Username: admin"
echo "Password: <configured password>"
echo ""
echo "To get kubeconfig:"
echo "  cat /etc/rancher/k3s/k3s.yaml"
echo ""
echo "============================================"
