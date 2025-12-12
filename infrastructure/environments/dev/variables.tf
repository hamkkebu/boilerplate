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
# EC2 + K3s (Free Tier)
# ============================================
variable "ec2_instance_type" {
  description = "EC2 instance type (t2.micro for Free Tier)"
  type        = string
  default     = "t2.micro"
}

variable "ec2_root_volume_size" {
  description = "EC2 root volume size in GB (Free Tier: 30GB total)"
  type        = number
  default     = 20
}

variable "ec2_key_pair_name" {
  description = "EC2 Key Pair name for SSH access"
  type        = string
  default     = null
}

variable "allowed_ssh_cidrs" {
  description = "CIDR blocks allowed to SSH"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "use_elastic_ip" {
  description = "Whether to assign an Elastic IP"
  type        = bool
  default     = false
}

# ============================================
# RDS (Free Tier)
# ============================================
variable "rds_instance_class" {
  description = "RDS instance class (db.t2.micro for Free Tier)"
  type        = string
  default     = "db.t2.micro"
}

variable "rds_engine_version" {
  description = "MySQL engine version"
  type        = string
  default     = "8.0"
}

variable "rds_allocated_storage" {
  description = "RDS allocated storage in GB (Free Tier: 20GB)"
  type        = number
  default     = 20
}

variable "rds_max_allocated_storage" {
  description = "RDS max allocated storage for autoscaling (0 to disable)"
  type        = number
  default     = 0
}

variable "rds_initial_db_name" {
  description = "Initial database name"
  type        = string
  default     = "hamkkebu"
}

variable "rds_master_username" {
  description = "RDS master username"
  type        = string
  default     = "admin"
}

variable "rds_master_password" {
  description = "RDS master password"
  type        = string
  sensitive   = true
}

variable "rds_backup_retention_period" {
  description = "RDS backup retention period in days"
  type        = number
  default     = 7
}

variable "allowed_db_cidrs" {
  description = "CIDR blocks allowed to access RDS (for local development)"
  type        = list(string)
  default     = []
}

# ============================================
# ECR / GitHub Actions
# ============================================
variable "create_github_actions_user" {
  description = "Whether to create IAM user for GitHub Actions"
  type        = bool
  default     = true
}
