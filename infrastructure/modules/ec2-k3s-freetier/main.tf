# ============================================
# EC2 + K3s Free Tier Module
# ============================================
# t2.micro/t3.micro 단일 인스턴스에 K3s 설치
# Free Tier 적합 (12개월 750시간/월)
# ============================================

# 최신 Amazon Linux 2023 AMI
data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-kernel-*-x86_64"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# EC2 Security Group
resource "aws_security_group" "k3s" {
  name_prefix = "${var.project_name}-${var.environment}-k3s-"
  description = "Security group for K3s EC2 instance"
  vpc_id      = var.vpc_id

  # SSH
  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = var.allowed_ssh_cidrs
    description = "SSH access"
  }

  # Kubernetes API
  ingress {
    from_port   = 6443
    to_port     = 6443
    protocol    = "tcp"
    cidr_blocks = var.allowed_ssh_cidrs
    description = "Kubernetes API"
  }

  # HTTP
  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTP"
  }

  # HTTPS
  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "HTTPS"
  }

  # NodePort range (for services)
  ingress {
    from_port   = 30000
    to_port     = 32767
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
    description = "NodePort range"
  }

  # All outbound
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "All outbound traffic"
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-k3s-sg"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# IAM Role for EC2 (ECR 접근용)
resource "aws_iam_role" "k3s" {
  name_prefix = "${var.project_name}-${var.environment}-k3s-"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "${var.project_name}-${var.environment}-k3s-role"
  }
}

# ECR 읽기 권한
resource "aws_iam_role_policy_attachment" "ecr_readonly" {
  role       = aws_iam_role.k3s.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

# SSM 접근 권한 (Session Manager)
resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.k3s.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# Instance Profile
resource "aws_iam_instance_profile" "k3s" {
  name_prefix = "${var.project_name}-${var.environment}-k3s-"
  role        = aws_iam_role.k3s.name
}

# K3s 설치 스크립트
locals {
  k3s_install_script = <<-EOF
    #!/bin/bash
    set -e

    # 시스템 업데이트
    dnf update -y

    # Docker 설치 (ECR에서 이미지 pull용)
    dnf install -y docker
    systemctl enable docker
    systemctl start docker

    # K3s 설치 (Docker 대신 containerd 사용)
    curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="--disable traefik --write-kubeconfig-mode 644" sh -

    # kubectl alias
    echo 'alias k=kubectl' >> /home/ec2-user/.bashrc
    echo 'export KUBECONFIG=/etc/rancher/k3s/k3s.yaml' >> /home/ec2-user/.bashrc

    # ECR credential helper 설치
    dnf install -y amazon-ecr-credential-helper

    # containerd에 ECR 인증 설정
    mkdir -p /etc/rancher/k3s
    cat > /etc/rancher/k3s/registries.yaml <<'REGISTRIES'
    mirrors:
      "${var.ecr_registry_url}":
        endpoint:
          - "https://${var.ecr_registry_url}"
    configs:
      "${var.ecr_registry_url}":
        auth:
          username: AWS
          password: ""
    REGISTRIES

    # AWS CLI 설치
    dnf install -y aws-cli

    # ECR 로그인 스크립트 생성 (cronjob용)
    cat > /usr/local/bin/ecr-login.sh <<'ECRLOGIN'
    #!/bin/bash
    PASSWORD=$(aws ecr get-login-password --region ${var.aws_region})
    cat > /etc/rancher/k3s/registries.yaml <<REGISTRIES
    mirrors:
      "${var.ecr_registry_url}":
        endpoint:
          - "https://${var.ecr_registry_url}"
    configs:
      "${var.ecr_registry_url}":
        auth:
          username: AWS
          password: "$PASSWORD"
    REGISTRIES
    systemctl restart k3s
    ECRLOGIN
    chmod +x /usr/local/bin/ecr-login.sh

    # 매 6시간마다 ECR 토큰 갱신
    echo "0 */6 * * * root /usr/local/bin/ecr-login.sh" > /etc/cron.d/ecr-login

    # 초기 ECR 로그인
    /usr/local/bin/ecr-login.sh

    # 완료 표시
    touch /var/log/k3s-setup-complete
  EOF
}

# EC2 Instance
resource "aws_instance" "k3s" {
  ami                         = data.aws_ami.amazon_linux_2023.id
  instance_type               = var.instance_type
  subnet_id                   = var.subnet_id
  vpc_security_group_ids      = [aws_security_group.k3s.id]
  iam_instance_profile        = aws_iam_instance_profile.k3s.name
  associate_public_ip_address = true
  key_name                    = var.key_pair_name

  root_block_device {
    volume_size           = var.root_volume_size
    volume_type           = "gp3"
    delete_on_termination = true
    encrypted             = true
  }

  user_data = base64encode(local.k3s_install_script)

  tags = {
    Name = "${var.project_name}-${var.environment}-k3s"
  }

  lifecycle {
    ignore_changes = [ami]
  }
}

# Elastic IP (고정 IP)
resource "aws_eip" "k3s" {
  count    = var.use_elastic_ip ? 1 : 0
  instance = aws_instance.k3s.id
  domain   = "vpc"

  tags = {
    Name = "${var.project_name}-${var.environment}-k3s-eip"
  }
}
