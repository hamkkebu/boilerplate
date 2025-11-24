variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "ap-northeast-2"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "hamkkebu"
}

variable "environment" {
  description = "Environment (dev, staging, prod)"
  type        = string
}

variable "terraform_state_bucket" {
  description = "S3 bucket name for terraform state"
  type        = string
}

variable "cloudfront_price_class" {
  description = "CloudFront price class"
  type        = string
  default     = "PriceClass_200"
}
