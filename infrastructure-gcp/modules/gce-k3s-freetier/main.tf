# ============================================
# GCE + K3s Free Tier Module
# ============================================
# e2-micro (Always Free: 1 인스턴스, us-west1/us-central1/us-east1)
# 30GB 표준 영구 디스크 무료
# MySQL은 Docker로 VM 내부에서 실행 (Cloud SQL 무료 티어 없음)
# ============================================

# 최신 Ubuntu 22.04 LTS 이미지
data "google_compute_image" "ubuntu" {
  family  = "ubuntu-2204-lts"
  project = "ubuntu-os-cloud"
}

locals {
  # startup script (외부 파일 참조)
  startup_script = templatefile("${path.module}/scripts/startup.sh", {
    artifact_registry_url = var.artifact_registry_url
    gcp_region            = var.gcp_region
    gcp_project_id        = var.gcp_project_id
    db_root_password      = var.db_root_password
  })
}

# ============================================
# Firewall Rules
# ============================================
resource "google_compute_firewall" "k3s_ssh" {
  name    = "${var.project_name}-${var.environment}-k3s-ssh"
  network = var.network_name

  allow {
    protocol = "tcp"
    ports    = ["22"]
  }

  source_ranges = var.allowed_ssh_cidrs
  target_tags   = ["${var.project_name}-k3s"]
  description   = "SSH access to K3s instance"
}

resource "google_compute_firewall" "k3s_k8s_api" {
  name    = "${var.project_name}-${var.environment}-k3s-api"
  network = var.network_name

  allow {
    protocol = "tcp"
    ports    = ["6443"]
  }

  source_ranges = var.allowed_ssh_cidrs
  target_tags   = ["${var.project_name}-k3s"]
  description   = "Kubernetes API access"
}

resource "google_compute_firewall" "k3s_http" {
  name    = "${var.project_name}-${var.environment}-k3s-http"
  network = var.network_name

  allow {
    protocol = "tcp"
    ports    = ["80", "443"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["${var.project_name}-k3s"]
  description   = "HTTP/HTTPS access"
}

resource "google_compute_firewall" "k3s_nodeport" {
  name    = "${var.project_name}-${var.environment}-k3s-nodeport"
  network = var.network_name

  allow {
    protocol = "tcp"
    ports    = ["30000-32767"]
  }

  source_ranges = ["0.0.0.0/0"]
  target_tags   = ["${var.project_name}-k3s"]
  description   = "NodePort range for services"
}

# ============================================
# Service Account (Artifact Registry 접근용)
# ============================================
resource "google_service_account" "k3s" {
  account_id   = "${var.project_name}-${var.environment}-k3s"
  display_name = "${var.project_name} ${var.environment} K3s Instance"
  project      = var.gcp_project_id
}

# Artifact Registry 읽기 권한
resource "google_project_iam_member" "k3s_artifact_reader" {
  project = var.gcp_project_id
  role    = "roles/artifactregistry.reader"
  member  = "serviceAccount:${google_service_account.k3s.email}"
}

# ============================================
# GCE Instance (Always Free: e2-micro)
# ============================================
resource "google_compute_instance" "k3s" {
  name         = "${var.project_name}-${var.environment}-k3s"
  machine_type = var.machine_type
  zone         = var.gcp_zone

  tags = ["${var.project_name}-k3s"]

  boot_disk {
    initialize_params {
      image = data.google_compute_image.ubuntu.self_link
      size  = var.boot_disk_size
      type  = "pd-standard" # 표준 영구 디스크 (Always Free: 30GB)
    }
  }

  network_interface {
    network    = var.network_name
    subnetwork = var.subnet_name

    access_config {
      # Ephemeral public IP (고정 IP는 use_static_ip로 제어)
    }
  }

  service_account {
    email  = google_service_account.k3s.email
    scopes = ["cloud-platform"]
  }

  metadata = {
    ssh-keys = "${var.ssh_user}:${var.ssh_public_key}"
  }

  metadata_startup_script = local.startup_script

  labels = {
    project     = var.project_name
    environment = var.environment
    managed-by  = "terraform"
  }

  # AMI 변경 무시 (AWS lifecycle ignore_changes와 동일)
  lifecycle {
    ignore_changes = [boot_disk[0].initialize_params[0].image]
  }
}

# ============================================
# Static IP (고정 IP, 선택사항)
# ============================================
resource "google_compute_address" "k3s" {
  count = var.use_static_ip ? 1 : 0

  name   = "${var.project_name}-${var.environment}-k3s-ip"
  region = var.gcp_region

  labels = {
    project     = var.project_name
    environment = var.environment
  }
}
