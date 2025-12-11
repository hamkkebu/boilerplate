variable "project_name" {
  description = "Project name"
  type        = string
  default     = "hamkkebu"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
}

variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

# ============================================
# VPC
# ============================================
variable "vpc_cidr" {
  description = "VPC CIDR block"
  type        = string
  default     = "10.0.0.0/16"
}

# ============================================
# EC2 / K3s
# ============================================
variable "key_pair_name" {
  description = "Name of the EC2 key pair"
  type        = string
}

variable "k3s_instance_type" {
  description = "EC2 instance type for K3s (t2.micro for free tier)"
  type        = string
  default     = "t2.micro"
}

variable "allowed_ssh_cidrs" {
  description = "CIDR blocks allowed for SSH access"
  type        = list(string)
  default     = ["0.0.0.0/0"]  # 프로덕션에서는 특정 IP로 제한 필요
}

variable "argocd_admin_password" {
  description = "ArgoCD admin password"
  type        = string
  sensitive   = true
}

# ============================================
# RDS
# ============================================
variable "create_rds" {
  description = "Whether to create RDS instance"
  type        = bool
  default     = true
}

variable "db_instance_class" {
  description = "RDS instance class (db.t2.micro for free tier)"
  type        = string
  default     = "db.t2.micro"
}

variable "db_allocated_storage" {
  description = "RDS allocated storage in GB (max 20 for free tier)"
  type        = number
  default     = 20
}

variable "db_name" {
  description = "Database name"
  type        = string
  default     = "hamkkebu"
}

variable "db_username" {
  description = "Database master username"
  type        = string
  default     = "admin"
}

variable "db_password" {
  description = "Database master password"
  type        = string
  sensitive   = true
}

variable "db_host" {
  description = "Database host (for existing RDS or external DB)"
  type        = string
  default     = ""
}

variable "db_port" {
  description = "Database port"
  type        = string
  default     = "3306"
}

# ============================================
# ECR / GitHub Actions
# ============================================
variable "create_github_actions_user" {
  description = "Whether to create IAM user for GitHub Actions"
  type        = bool
  default     = true
}
