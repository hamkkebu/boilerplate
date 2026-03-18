# Hamkkebu Development Environment (GCP)
# GCE (K3s) + Docker MySQL - GCP Always Free 구성

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }

  backend "gcs" {
    bucket = "hamkkebu-terraform-state"
    prefix = "environments/dev"
  }
}

provider "google" {
  project = var.gcp_project_id
  region  = var.gcp_region

  default_labels = {
    project     = var.project_name
    environment = var.environment
    managed-by  = "terraform"
  }
}

# ============================================
# VPC Network
# ============================================
resource "google_compute_network" "main" {
  name                    = "${var.project_name}-${var.environment}-vpc"
  auto_create_subnetworks = false
  project                 = var.gcp_project_id
}

# ============================================
# Subnet (단일 서브넷)
# ============================================
resource "google_compute_subnetwork" "public" {
  name          = "${var.project_name}-${var.environment}-subnet"
  ip_cidr_range = var.subnet_cidr
  region        = var.gcp_region
  network       = google_compute_network.main.id
  project       = var.gcp_project_id

  # GKE용 Secondary IP Range (향후 확장용)
  # secondary_ip_range {
  #   range_name    = "pods"
  #   ip_cidr_range = "10.1.0.0/16"
  # }
}

# ============================================
# Cloud Router + NAT (외부 접근)
# ============================================
resource "google_compute_router" "main" {
  name    = "${var.project_name}-${var.environment}-router"
  region  = var.gcp_region
  network = google_compute_network.main.id
}

resource "google_compute_router_nat" "main" {
  name                               = "${var.project_name}-${var.environment}-nat"
  router                             = google_compute_router.main.name
  region                             = var.gcp_region
  nat_ip_allocate_option             = "AUTO_ONLY"
  source_subnetwork_ip_ranges_to_nat = "ALL_SUBNETWORKS_ALL_IP_RANGES"

  log_config {
    enable = false
    filter = "ERRORS_ONLY"
  }
}

# ============================================
# Artifact Registry Module (이미지 저장소)
# ============================================
module "artifact_registry" {
  source = "../../modules/artifact-registry"

  project_name           = var.project_name
  environment            = var.environment
  gcp_project_id         = var.gcp_project_id
  gcp_region             = var.gcp_region
  create_github_actions_sa = var.create_github_actions_sa
  github_repository      = var.github_repository
}

# ============================================
# GCE + K3s Module (Always Free: e2-micro)
# ============================================
module "k3s" {
  source = "../../modules/gce-k3s-freetier"

  project_name          = var.project_name
  environment           = var.environment
  gcp_project_id        = var.gcp_project_id
  gcp_region            = var.gcp_region
  gcp_zone              = var.gcp_zone
  network_name          = google_compute_network.main.name
  subnet_name           = google_compute_subnetwork.public.name
  machine_type          = var.gce_machine_type
  boot_disk_size        = var.gce_boot_disk_size
  ssh_user              = var.ssh_user
  ssh_public_key        = var.ssh_public_key
  allowed_ssh_cidrs     = var.allowed_ssh_cidrs
  artifact_registry_url = module.artifact_registry.repository_url
  use_static_ip         = var.use_static_ip
  db_root_password      = var.db_root_password
}
