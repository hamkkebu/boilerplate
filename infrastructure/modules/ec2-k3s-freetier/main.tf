# ============================================
# EC2 + K3s Free Tier Module
# ============================================
# t4g.small (ARM64/Graviton) - 신규 계정 Free Tier
# t2.micro (x86_64) - 기존 계정 Free Tier
# ============================================

# Amazon Linux 2023 AMI (아키텍처 자동 선택)
data "aws_ami" "amazon_linux_2023" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-kernel-*-${local.ami_architecture}"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# 인스턴스 타입에 따른 아키텍처 자동 결정 + K3s 설치 스크립트
locals {
  # t4g, c6g, m6g 등 'g'로 끝나는 타입은 ARM64 (Graviton)
  is_graviton      = can(regex("^[a-z]+[0-9]+g", var.instance_type))
  ami_architecture = local.is_graviton ? "arm64" : "x86_64"

  # K3s 설치 스크립트 (외부 파일 참조)
  k3s_install_script = templatefile("${path.module}/scripts/user-data.sh", {
    ecr_registry_url = var.ecr_registry_url
    aws_region       = var.aws_region
  })
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
