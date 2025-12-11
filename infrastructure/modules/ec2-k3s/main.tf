# EC2 + K3s + ArgoCD Module
# 프리티어: t2.micro (750시간/월)

# Latest Amazon Linux 2 AMI
data "aws_ami" "amazon_linux_2" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["amzn2-ami-hvm-*-x86_64-gp2"]
  }

  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

# Security Group for K3s
resource "aws_security_group" "k3s" {
  name        = "${var.project_name}-${var.environment}-k3s-sg"
  description = "Security group for K3s cluster"
  vpc_id      = var.vpc_id

  # SSH
  ingress {
    description = "SSH"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = var.allowed_ssh_cidrs
  }

  # HTTP
  ingress {
    description = "HTTP"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # HTTPS
  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  # K3s API Server
  ingress {
    description = "K3s API Server"
    from_port   = 6443
    to_port     = 6443
    protocol    = "tcp"
    cidr_blocks = var.allowed_ssh_cidrs
  }

  # ArgoCD UI (NodePort)
  ingress {
    description = "ArgoCD UI"
    from_port   = 30080
    to_port     = 30080
    protocol    = "tcp"
    cidr_blocks = var.allowed_ssh_cidrs
  }

  # Service NodePorts (30000-32767)
  ingress {
    description = "NodePort Services"
    from_port   = 30000
    to_port     = 32767
    protocol    = "tcp"
    cidr_blocks = var.allowed_ssh_cidrs
  }

  # All outbound traffic
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-k3s-sg"
    Project     = var.project_name
    Environment = var.environment
  }
}

# IAM Role for EC2 (ECR access)
resource "aws_iam_role" "k3s" {
  name = "${var.project_name}-${var.environment}-k3s-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ec2.amazonaws.com"
      }
    }]
  })

  tags = {
    Name        = "${var.project_name}-${var.environment}-k3s-role"
    Project     = var.project_name
    Environment = var.environment
  }
}

# ECR Read-Only Policy
resource "aws_iam_role_policy_attachment" "ecr_readonly" {
  role       = aws_iam_role.k3s.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

# SSM Policy for Session Manager (선택적 SSH 대안)
resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.k3s.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# Instance Profile
resource "aws_iam_instance_profile" "k3s" {
  name = "${var.project_name}-${var.environment}-k3s-profile"
  role = aws_iam_role.k3s.name

  tags = {
    Name        = "${var.project_name}-${var.environment}-k3s-profile"
    Project     = var.project_name
    Environment = var.environment
  }
}

# EC2 Instance
resource "aws_instance" "k3s_master" {
  ami                    = data.aws_ami.amazon_linux_2.id
  instance_type          = var.instance_type
  key_name               = var.key_pair_name
  vpc_security_group_ids = [aws_security_group.k3s.id]
  subnet_id              = var.public_subnet_id
  iam_instance_profile   = aws_iam_instance_profile.k3s.name

  user_data = base64encode(templatefile("${path.module}/user-data.sh", {
    aws_region          = var.aws_region
    aws_account_id      = var.aws_account_id
    argocd_password     = var.argocd_admin_password
    project_name        = var.project_name
    db_host             = var.db_host
    db_port             = var.db_port
  }))

  root_block_device {
    volume_size           = 20  # 프리티어 최대
    volume_type           = "gp2"
    encrypted             = true
    delete_on_termination = true
  }

  tags = {
    Name        = "${var.project_name}-${var.environment}-k3s-master"
    Project     = var.project_name
    Environment = var.environment
    Role        = "k3s-master"
  }

  lifecycle {
    ignore_changes = [ami]  # AMI 업데이트로 인한 재생성 방지
  }
}

# Elastic IP for stable access
resource "aws_eip" "k3s" {
  domain   = "vpc"
  instance = aws_instance.k3s_master.id

  tags = {
    Name        = "${var.project_name}-${var.environment}-k3s-eip"
    Project     = var.project_name
    Environment = var.environment
  }

  depends_on = [aws_instance.k3s_master]
}
