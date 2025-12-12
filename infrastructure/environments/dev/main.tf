# Hamkkebu Development Environment
# EC2 (K3s) + RDS (단일 인스턴스, 다중 DB) - AWS Free Tier 구성

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
# VPC (간소화 - Public Subnet만 사용)
# ============================================
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

# ============================================
# Public Subnets (EC2 + RDS용)
# ============================================
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

# ============================================
# Route Table (Public)
# ============================================
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
# ECR Module (이미지 저장소)
# ============================================
module "ecr" {
  source = "../../modules/ecr"

  project_name               = var.project_name
  environment                = var.environment
  create_github_actions_user = var.create_github_actions_user
}

# ============================================
# EC2 + K3s Module (Free Tier: t2.micro)
# ============================================
module "k3s" {
  source = "../../modules/ec2-k3s-freetier"

  project_name     = var.project_name
  environment      = var.environment
  aws_region       = var.aws_region
  vpc_id           = aws_vpc.main.id
  subnet_id        = aws_subnet.public[0].id
  instance_type    = var.ec2_instance_type
  root_volume_size = var.ec2_root_volume_size
  key_pair_name    = var.ec2_key_pair_name
  allowed_ssh_cidrs = var.allowed_ssh_cidrs
  ecr_registry_url = module.ecr.registry_url
  use_elastic_ip   = var.use_elastic_ip
}

# ============================================
# RDS Module (Free Tier: db.t2.micro, 단일 인스턴스)
# ============================================
module "rds" {
  source = "../../modules/rds-freetier"

  project_name               = var.project_name
  environment                = var.environment
  vpc_id                     = aws_vpc.main.id
  subnet_ids                 = aws_subnet.public[*].id
  allowed_security_group_ids = [module.k3s.security_group_id]
  allowed_cidrs              = var.allowed_db_cidrs

  instance_class        = var.rds_instance_class
  engine_version        = var.rds_engine_version
  allocated_storage     = var.rds_allocated_storage
  max_allocated_storage = var.rds_max_allocated_storage
  initial_db_name       = var.rds_initial_db_name
  master_username       = var.rds_master_username
  master_password       = var.rds_master_password

  backup_retention_period = var.rds_backup_retention_period
  skip_final_snapshot     = true
  deletion_protection     = false
}
