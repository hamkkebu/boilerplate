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

variable "gcp_project_id" {
  description = "GCP project ID"
  type        = string
}

variable "gcp_region" {
  description = "GCP region for Artifact Registry"
  type        = string
}

variable "create_github_actions_sa" {
  description = "Whether to create a Service Account for GitHub Actions"
  type        = bool
  default     = true
}

variable "github_repository" {
  description = "GitHub repository (owner/repo) for Workload Identity Federation"
  type        = string
  default     = ""
}
