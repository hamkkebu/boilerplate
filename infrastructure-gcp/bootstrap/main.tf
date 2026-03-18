# Terraform State Backend Bootstrap (GCP)
# GCS 버킷을 생성합니다. (GCS는 내장 잠금 지원으로 DynamoDB 불필요)
# 한 번만 실행하고, 이후에는 environments/dev에서 작업합니다.

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    google = {
      source  = "hashicorp/google"
      version = "~> 5.0"
    }
  }

  # Bootstrap은 로컬 state 사용
  backend "local" {
    path = "terraform.tfstate"
  }
}

provider "google" {
  project = var.gcp_project_id
  region  = var.gcp_region
}

# ============================================
# GCS Bucket for Terraform State
# ============================================
resource "google_storage_bucket" "terraform_state" {
  name     = var.state_bucket_name
  location = var.gcp_region
  project  = var.gcp_project_id

  # 버전 관리 (S3 versioning 대응)
  versioning {
    enabled = true
  }

  # 공개 접근 차단
  uniform_bucket_level_access = true

  # 암호화 (기본 Google 관리 키)
  encryption {
    default_kms_key_name = ""
  }

  labels = {
    project    = var.project_name
    managed-by = "terraform-bootstrap"
  }

  lifecycle {
    prevent_destroy = true
  }
}

# ============================================
# SSH Key Pair (로컬 생성)
# ============================================
resource "tls_private_key" "gce" {
  algorithm = "RSA"
  rsa_bits  = 4096
}

# 프라이빗 키를 로컬 파일로 저장
resource "local_file" "private_key" {
  content         = tls_private_key.gce.private_key_pem
  filename        = "${path.module}/${var.project_name}-dev-key.pem"
  file_permission = "0400"
}

# 퍼블릭 키를 로컬 파일로 저장 (GCE 메타데이터에 등록할 때 사용)
resource "local_file" "public_key" {
  content  = tls_private_key.gce.public_key_openssh
  filename = "${path.module}/${var.project_name}-dev-key.pub"
}

# ============================================
# 필요한 GCP API 활성화
# ============================================
resource "google_project_service" "required_apis" {
  for_each = toset([
    "compute.googleapis.com",
    "artifactregistry.googleapis.com",
    "iam.googleapis.com",
    "cloudresourcemanager.googleapis.com",
  ])

  project = var.gcp_project_id
  service = each.value

  disable_on_destroy = false
}
