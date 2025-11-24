terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  backend "s3" {
    # Backend configuration should be provided during initialization
    # terraform init -backend-config="bucket=your-terraform-state-bucket"
    key            = "common/terraform.tfstate"
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
      ManagedBy   = "Terraform"
    }
  }
}

# VPC Module
module "vpc" {
  source = "../modules/vpc"

  project_name        = var.project_name
  environment         = var.environment
  vpc_cidr            = var.vpc_cidr
  availability_zones  = var.availability_zones
  public_subnet_cidrs = var.public_subnet_cidrs
  private_subnet_cidrs = var.private_subnet_cidrs
  enable_nat_gateway  = var.enable_nat_gateway
  single_nat_gateway  = var.single_nat_gateway
}

# Security Groups Module
module "security" {
  source = "../modules/security"

  project_name = var.project_name
  environment  = var.environment
  vpc_id       = module.vpc.vpc_id
}

# RDS Module
module "database" {
  source = "../modules/database"

  project_name           = var.project_name
  environment            = var.environment
  vpc_id                 = module.vpc.vpc_id
  database_subnet_ids    = module.vpc.private_subnet_ids
  db_security_group_id   = module.security.db_security_group_id
  db_instance_class      = var.db_instance_class
  db_allocated_storage   = var.db_allocated_storage
  db_engine_version      = var.db_engine_version
  db_name                = var.db_name
  db_username            = var.db_username
  db_password            = var.db_password
  backup_retention_period = var.backup_retention_period
  multi_az               = var.multi_az
}

# Application Load Balancer Module
module "alb" {
  source = "../modules/alb"

  project_name         = var.project_name
  environment          = var.environment
  vpc_id               = module.vpc.vpc_id
  public_subnet_ids    = module.vpc.public_subnet_ids
  alb_security_group_id = module.security.alb_security_group_id
}

# ECS Cluster Module
module "ecs" {
  source = "../modules/ecs"

  project_name = var.project_name
  environment  = var.environment
}

# S3 Buckets Module
module "s3" {
  source = "../modules/s3"

  project_name = var.project_name
  environment  = var.environment
}

# CloudWatch Module
module "cloudwatch" {
  source = "../modules/cloudwatch"

  project_name = var.project_name
  environment  = var.environment
}
