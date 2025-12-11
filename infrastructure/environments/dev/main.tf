# Hamkkebu Development Environment
# AWS 프리티어 기반 K3s + ArgoCD 인프라

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    bucket         = "hamkkebu-terraform-state"
    key            = "environments/dev/terraform.tfstate"
    region         = "ap-northeast-2"
    encrypt        = true
    dynamodb_table = "terraform-state-lock"
  }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "terraform"
    }
  }
}

# 현재 AWS 계정 정보
data "aws_caller_identity" "current" {}

# 가용 영역 조회
data "aws_availability_zones" "available" {
  state = "available"
}

# ============================================
# VPC (기존 또는 신규)
# ============================================
# 기존 VPC 사용 시 data source로 참조
# 신규 생성 시 아래 리소스 활성화

resource "aws_vpc" "main" {
  cidr_block           = var.vpc_cidr
  enable_dns_hostnames = true
  enable_dns_support   = true

  tags = {
    Name = "${var.project_name}-${var.environment}-vpc"
  }
}

resource "aws_internet_gateway" "main" {
  vpc_id = aws_vpc.main.id

  tags = {
    Name = "${var.project_name}-${var.environment}-igw"
  }
}

resource "aws_subnet" "public" {
  count                   = 2
  vpc_id                  = aws_vpc.main.id
  cidr_block              = cidrsubnet(var.vpc_cidr, 8, count.index)
  availability_zone       = data.aws_availability_zones.available.names[count.index]
  map_public_ip_on_launch = true

  tags = {
    Name = "${var.project_name}-${var.environment}-public-${count.index + 1}"
    Type = "public"
  }
}

resource "aws_subnet" "private" {
  count             = 2
  vpc_id            = aws_vpc.main.id
  cidr_block        = cidrsubnet(var.vpc_cidr, 8, count.index + 10)
  availability_zone = data.aws_availability_zones.available.names[count.index]

  tags = {
    Name = "${var.project_name}-${var.environment}-private-${count.index + 1}"
    Type = "private"
  }
}

resource "aws_route_table" "public" {
  vpc_id = aws_vpc.main.id

  route {
    cidr_block = "0.0.0.0/0"
    gateway_id = aws_internet_gateway.main.id
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-public-rt"
  }
}

resource "aws_route_table_association" "public" {
  count          = length(aws_subnet.public)
  subnet_id      = aws_subnet.public[count.index].id
  route_table_id = aws_route_table.public.id
}

# ============================================
# ECR Module
# ============================================
module "ecr" {
  source = "../../modules/ecr"

  project_name               = var.project_name
  environment                = var.environment
  create_github_actions_user = var.create_github_actions_user
}

# ============================================
# EC2 + K3s Module
# ============================================
module "k3s" {
  source = "../../modules/ec2-k3s"

  project_name          = var.project_name
  environment           = var.environment
  aws_region            = var.aws_region
  aws_account_id        = data.aws_caller_identity.current.account_id
  vpc_id                = aws_vpc.main.id
  public_subnet_id      = aws_subnet.public[0].id
  key_pair_name         = var.key_pair_name
  instance_type         = var.k3s_instance_type
  allowed_ssh_cidrs     = var.allowed_ssh_cidrs
  argocd_admin_password = var.argocd_admin_password
  db_host               = var.db_host
  db_port               = var.db_port
}

# ============================================
# RDS Security Group (K3s에서 접근 허용)
# ============================================
resource "aws_security_group" "rds" {
  name        = "${var.project_name}-${var.environment}-rds-sg"
  description = "Security group for RDS"
  vpc_id      = aws_vpc.main.id

  ingress {
    description     = "MySQL from K3s"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = [module.k3s.security_group_id]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-rds-sg"
  }
}

# ============================================
# RDS Subnet Group
# ============================================
resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-${var.environment}-db-subnet-group"
  subnet_ids = aws_subnet.private[*].id

  tags = {
    Name = "${var.project_name}-${var.environment}-db-subnet-group"
  }
}

# ============================================
# RDS Instance (프리티어: db.t2.micro)
# ============================================
resource "aws_db_instance" "main" {
  count = var.create_rds ? 1 : 0

  identifier = "${var.project_name}-${var.environment}-mysql"

  engine               = "mysql"
  engine_version       = "8.0"
  instance_class       = var.db_instance_class
  allocated_storage    = var.db_allocated_storage
  storage_type         = "gp2"
  storage_encrypted    = true

  db_name  = var.db_name
  username = var.db_username
  password = var.db_password

  vpc_security_group_ids = [aws_security_group.rds.id]
  db_subnet_group_name   = aws_db_subnet_group.main.name

  backup_retention_period = 1  # 프리티어는 최대 1일
  backup_window          = "03:00-04:00"
  maintenance_window     = "Mon:04:00-Mon:05:00"

  skip_final_snapshot       = var.environment != "prod"
  final_snapshot_identifier = var.environment == "prod" ? "${var.project_name}-${var.environment}-final-snapshot" : null
  deletion_protection       = var.environment == "prod"

  publicly_accessible = false
  multi_az            = false  # 프리티어는 단일 AZ

  tags = {
    Name = "${var.project_name}-${var.environment}-mysql"
  }
}
