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

variable "gcp_project_id" {
  description = "GCP project ID"
  type        = string
}

variable "gcp_region" {
  description = "GCP region (Always Free: us-west1, us-central1, us-east1)"
  type        = string
  default     = "us-west1"
}

variable "gcp_zone" {
  description = "GCP zone"
  type        = string
  default     = "us-west1-b"
}

# ============================================
# VPC
# ============================================
variable "subnet_cidr" {
  description = "Subnet CIDR range"
  type        = string
  default     = "10.0.0.0/24"
}

# ============================================
# GCE + K3s (Always Free)
# ============================================
variable "gce_machine_type" {
  description = "GCE machine type (e2-micro for Always Free)"
  type        = string
  default     = "e2-micro"
}

variable "gce_boot_disk_size" {
  description = "Boot disk size in GB (Always Free: 30GB standard)"
  type        = number
  default     = 30
}

variable "ssh_user" {
  description = "SSH user name"
  type        = string
  default     = "hamkkebu"
}

variable "ssh_public_key" {
  description = "SSH public key (from bootstrap output)"
  type        = string
}

variable "allowed_ssh_cidrs" {
  description = "CIDR blocks allowed to SSH"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "use_static_ip" {
  description = "Whether to assign a static external IP"
  type        = bool
  default     = false
}

# ============================================
# Database (Docker MySQL on VM)
# ============================================
variable "db_root_password" {
  description = "MySQL root password (Docker MySQL on the VM)"
  type        = string
  sensitive   = true
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
