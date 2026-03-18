variable "project_name" {
  description = "Project name"
  type        = string
  default     = "hamkkebu"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "production"
}

variable "gcp_project_id" {
  description = "GCP project ID"
  type        = string
}

variable "gcp_region" {
  description = "GCP region"
  type        = string
  default     = "asia-northeast3" # 서울
}

# ============================================
# VPC
# ============================================
variable "subnet_cidr" {
  description = "Primary subnet CIDR"
  type        = string
  default     = "10.1.0.0/24"
}

variable "pods_cidr" {
  description = "GKE Pods secondary CIDR"
  type        = string
  default     = "10.2.0.0/16"
}

variable "services_cidr" {
  description = "GKE Services secondary CIDR"
  type        = string
  default     = "10.3.0.0/20"
}

variable "master_cidr" {
  description = "GKE master CIDR (private cluster)"
  type        = string
  default     = "172.16.0.0/28"
}

# ============================================
# GKE
# ============================================
variable "deletion_protection" {
  description = "Enable GKE deletion protection"
  type        = bool
  default     = true
}

# ============================================
# Artifact Registry / GitHub Actions
# ============================================
variable "create_github_actions_sa" {
  description = "Whether to create Service Account for GitHub Actions"
  type        = bool
  default     = true
}

variable "github_repository" {
  description = "GitHub repository (owner/repo) for Workload Identity"
  type        = string
  default     = ""
}
