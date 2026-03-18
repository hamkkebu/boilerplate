variable "gcp_project_id" {
  description = "GCP project ID"
  type        = string
}

variable "gcp_region" {
  description = "GCP region (Always Free: us-west1, us-central1, us-east1)"
  type        = string
  default     = "us-west1"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "hamkkebu"
}

variable "state_bucket_name" {
  description = "GCS bucket name for Terraform state"
  type        = string
  default     = "hamkkebu-terraform-state"
}
