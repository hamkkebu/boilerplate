variable "project_name" {
  description = "Project name"
  type        = string
}

variable "environment" {
  description = "Environment name"
  type        = string
}

variable "gcp_project_id" {
  description = "GCP project ID"
  type        = string
}

variable "gcp_region" {
  description = "GCP region"
  type        = string
}

variable "gcp_zone" {
  description = "GCP zone (e.g., us-west1-b)"
  type        = string
}

variable "network_name" {
  description = "VPC network name"
  type        = string
}

variable "subnet_name" {
  description = "Subnet name"
  type        = string
}

variable "machine_type" {
  description = "GCE machine type (e2-micro for Always Free)"
  type        = string
  default     = "e2-micro"
}

variable "boot_disk_size" {
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
  description = "SSH public key for instance access"
  type        = string
}

variable "allowed_ssh_cidrs" {
  description = "CIDR blocks allowed to SSH"
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "artifact_registry_url" {
  description = "Artifact Registry URL (e.g., us-west1-docker.pkg.dev/project-id/hamkkebu)"
  type        = string
  default     = ""
}

variable "use_static_ip" {
  description = "Whether to assign a static external IP (비용 발생 가능)"
  type        = bool
  default     = false
}

variable "db_root_password" {
  description = "MySQL root password for Docker MySQL on the VM"
  type        = string
  sensitive   = true
}
