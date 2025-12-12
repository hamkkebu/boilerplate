variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "aws_region" {
  description = "AWS region"
  type        = string
}

variable "vpc_id" {
  description = "VPC ID"
  type        = string
}

variable "subnet_id" {
  description = "Subnet ID for EC2 instance"
  type        = string
}

variable "instance_type" {
  description = "EC2 instance type (t2.micro for Free Tier)"
  type        = string
  default     = "t2.micro"
}

variable "root_volume_size" {
  description = "Root EBS volume size in GB (Free Tier: 30GB)"
  type        = number
  default     = 20
}

variable "key_pair_name" {
  description = "EC2 Key Pair name for SSH access"
  type        = string
  default     = null
}

variable "allowed_ssh_cidrs" {
  description = "CIDR blocks allowed to SSH"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "ecr_registry_url" {
  description = "ECR registry URL (e.g., 123456789012.dkr.ecr.ap-northeast-2.amazonaws.com)"
  type        = string
  default     = ""
}

variable "use_elastic_ip" {
  description = "Whether to assign an Elastic IP (incurs cost if instance is stopped)"
  type        = bool
  default     = false
}
