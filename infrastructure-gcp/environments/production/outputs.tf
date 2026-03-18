# ============================================
# Artifact Registry Outputs
# ============================================
output "artifact_registry_url" {
  description = "Artifact Registry repository URL"
  value       = module.artifact_registry.repository_url
}

output "image_urls" {
  description = "Map of service image URLs"
  value       = module.artifact_registry.image_urls
}

output "github_actions_sa_email" {
  description = "GitHub Actions service account email"
  value       = module.artifact_registry.github_actions_sa_email
}

# ============================================
# VPC Outputs
# ============================================
output "vpc_name" {
  description = "VPC network name"
  value       = google_compute_network.main.name
}

output "subnet_name" {
  description = "Subnet name"
  value       = google_compute_subnetwork.main.name
}

output "nat_ip" {
  description = "Cloud NAT IP"
  value       = google_compute_router_nat.main.name
}

# ============================================
# GKE Outputs
# ============================================
output "gke_cluster_name" {
  description = "GKE cluster name"
  value       = google_container_cluster.main.name
}

output "gke_cluster_endpoint" {
  description = "GKE cluster endpoint"
  value       = google_container_cluster.main.endpoint
}

output "gke_kubeconfig_command" {
  description = "Command to update kubeconfig"
  value       = "gcloud container clusters get-credentials ${google_container_cluster.main.name} --region ${var.gcp_region} --project ${var.gcp_project_id}"
}

# ============================================
# 비용 요약
# ============================================
output "cost_summary" {
  description = "Estimated monthly cost"
  value       = <<-EOT
    ============================================
    GCP Production 예상 비용
    ============================================
    - GKE Autopilot: ~$73/월 (관리비) + 워크로드 사용량
    - Cloud SQL: 별도 (서비스별 레포에서 관리)
    - Artifact Registry: ~$0 (500MB 무료)
    - Cloud NAT: ~$1/월
    - Cloud Load Balancing: ~$18/월
    ============================================
    공통 인프라 예상: ~$92/월
    (AWS EKS ~$73/월 + NAT ~$32/월 = ~$105/월 대비 절감)
    ============================================
  EOT
}
