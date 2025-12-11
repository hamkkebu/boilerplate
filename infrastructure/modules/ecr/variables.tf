variable "project_name" {
  description = "Project name for resource naming"
  type        = string
  default     = "hamkkebu"
}

variable "environment" {
  description = "Environment (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "create_github_actions_user" {
  description = "Whether to create IAM user for GitHub Actions"
  type        = bool
  default     = true
}
