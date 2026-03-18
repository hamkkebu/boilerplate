# Artifact Registry for hamkkebu services
# GCP Artifact Registry: 500MB 무료 스토리지 (us 리전)
# ECR 대체

locals {
  repositories = [
    "auth-service-backend",
    "auth-service-frontend",
    "ledger-service-backend",
    "ledger-service-frontend",
    "transaction-service-backend",
    "transaction-service-frontend",
  ]
}

# ============================================
# Artifact Registry Repository (Docker)
# ============================================
# GCP는 하나의 레포지토리 안에 여러 이미지를 저장할 수 있음
# ECR처럼 이미지마다 별도 레포가 필요하지 않음
resource "google_artifact_registry_repository" "docker" {
  location      = var.gcp_region
  repository_id = var.project_name
  format        = "DOCKER"
  description   = "Docker images for ${var.project_name} services"
  project       = var.gcp_project_id

  # 프리티어 비용 최적화: 오래된 이미지 자동 정리
  cleanup_policies {
    id     = "keep-minimum-versions"
    action = "KEEP"

    most_recent_versions {
      keep_count = 3
    }
  }

  cleanup_policies {
    id     = "delete-old-untagged"
    action = "DELETE"

    condition {
      tag_state = "UNTAGGED"
      older_than = "86400s" # 1일
    }
  }

  labels = {
    project     = var.project_name
    environment = var.environment
    managed-by  = "terraform"
  }
}

# ============================================
# GitHub Actions용 Service Account
# ============================================
resource "google_service_account" "github_actions" {
  count = var.create_github_actions_sa ? 1 : 0

  account_id   = "${var.project_name}-${var.environment}-gh-actions"
  display_name = "${var.project_name} GitHub Actions (${var.environment})"
  project      = var.gcp_project_id
}

# Artifact Registry 쓰기 권한
resource "google_project_iam_member" "github_actions_ar_writer" {
  count = var.create_github_actions_sa ? 1 : 0

  project = var.gcp_project_id
  role    = "roles/artifactregistry.writer"
  member  = "serviceAccount:${google_service_account.github_actions[0].email}"
}

# Workload Identity Federation (GitHub Actions OIDC)
resource "google_iam_workload_identity_pool" "github" {
  count = var.create_github_actions_sa ? 1 : 0

  workload_identity_pool_id = "${var.project_name}-github-pool"
  display_name              = "GitHub Actions Pool"
  description               = "Workload Identity Pool for GitHub Actions"
  project                   = var.gcp_project_id
}

resource "google_iam_workload_identity_pool_provider" "github" {
  count = var.create_github_actions_sa ? 1 : 0

  workload_identity_pool_id          = google_iam_workload_identity_pool.github[0].workload_identity_pool_id
  workload_identity_pool_provider_id = "github-provider"
  display_name                       = "GitHub OIDC Provider"
  project                            = var.gcp_project_id

  attribute_mapping = {
    "google.subject"       = "assertion.sub"
    "attribute.actor"      = "assertion.actor"
    "attribute.repository" = "assertion.repository"
  }

  oidc {
    issuer_uri = "https://token.actions.githubusercontent.com"
  }
}

# Service Account에 Workload Identity 바인딩
resource "google_service_account_iam_member" "github_actions_wif" {
  count = var.create_github_actions_sa ? 1 : 0

  service_account_id = google_service_account.github_actions[0].name
  role               = "roles/iam.workloadIdentityUser"
  member             = "principalSet://iam.googleapis.com/${google_iam_workload_identity_pool.github[0].name}/attribute.repository/${var.github_repository}"
}
