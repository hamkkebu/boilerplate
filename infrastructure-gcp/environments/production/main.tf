# Hamkkebu Production Environment (GCP)
# GKE Autopilot + Cloud SQL + Cloud Load Balancing

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
    prefix = "environments/production"
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
# Subnets
# ============================================
resource "google_compute_subnetwork" "main" {
  name          = "${var.project_name}-${var.environment}-subnet"
  ip_cidr_range = var.subnet_cidr
  region        = var.gcp_region
  network       = google_compute_network.main.id
  project       = var.gcp_project_id

  # GKE Pod/Service IP ranges
  secondary_ip_range {
    range_name    = "pods"
    ip_cidr_range = var.pods_cidr
  }

  secondary_ip_range {
    range_name    = "services"
    ip_cidr_range = var.services_cidr
  }
}

# ============================================
# Cloud Router + NAT
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
    enable = true
    filter = "ERRORS_ONLY"
  }
}

# ============================================
# Artifact Registry Module
# ============================================
module "artifact_registry" {
  source = "../../modules/artifact-registry"

  project_name             = var.project_name
  environment              = var.environment
  gcp_project_id           = var.gcp_project_id
  gcp_region               = var.gcp_region
  create_github_actions_sa = var.create_github_actions_sa
  github_repository        = var.github_repository
}

# ============================================
# GKE Autopilot Cluster
# ============================================
resource "google_container_cluster" "main" {
  name     = "${var.project_name}-${var.environment}"
  location = var.gcp_region
  project  = var.gcp_project_id

  # Autopilot 모드 (노드 자동 관리)
  enable_autopilot = true

  network    = google_compute_network.main.name
  subnetwork = google_compute_subnetwork.main.name

  ip_allocation_policy {
    cluster_secondary_range_name  = "pods"
    services_secondary_range_name = "services"
  }

  # Release channel
  release_channel {
    channel = "REGULAR"
  }

  # Private cluster (노드에 외부 IP 없음)
  private_cluster_config {
    enable_private_nodes    = true
    enable_private_endpoint = false
    master_ipv4_cidr_block  = var.master_cidr
  }

  # 삭제 보호
  deletion_protection = var.deletion_protection
}

# ============================================
# Cloud SQL은 각 서비스별 레포지토리에서 관리
# - auth-service/infrastructure-gcp/
# - ledger-service/infrastructure-gcp/
# - transaction-service/infrastructure-gcp/
# ============================================
