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
# EKS
# ============================================
variable "eks_cluster_version" {
  description = "Kubernetes version for EKS"
  type        = string
  default     = "1.29"
}

variable "eks_node_instance_types" {
  description = "EC2 instance types for EKS worker nodes"
  type        = list(string)
  default     = ["t4g.medium"]
}

variable "eks_node_desired_size" {
  description = "Desired number of worker nodes"
  type        = number
  default     = 2
}

variable "eks_node_min_size" {
  description = "Minimum number of worker nodes"
  type        = number
  default     = 1
}

variable "eks_node_max_size" {
  description = "Maximum number of worker nodes"
  type        = number
  default     = 3
}

# ============================================
# RDS
# ============================================
variable "db_instance_class" {
  description = "RDS instance class"
  type        = string
  default     = "db.t4g.micro"
}

variable "db_allocated_storage" {
  description = "RDS allocated storage in GB"
  type        = number
  default     = 20
}

variable "db_backup_retention_period" {
  description = "Backup retention period (days)"
  type        = number
  default     = 7
}

variable "db_username" {
  description = "Database master username"
  type        = string
  default     = "admin"
}

variable "db_password_auth" {
  description = "Database password for auth-service"
  type        = string
  sensitive   = true
}

variable "db_password_ledger" {
  description = "Database password for ledger-service"
  type        = string
  sensitive   = true
}

variable "db_password_transaction" {
  description = "Database password for transaction-service"
  type        = string
  sensitive   = true
}

# ============================================
# ECR / GitHub Actions
# ============================================
variable "create_github_actions_user" {
  description = "Whether to create IAM user for GitHub Actions"
  type        = bool
  default     = true
}
