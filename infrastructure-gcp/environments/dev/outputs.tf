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

output "workload_identity_provider" {
  description = "Workload Identity Provider for GitHub Actions"
  value       = module.artifact_registry.workload_identity_provider
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
  value       = google_compute_subnetwork.public.name
}

# ============================================
# K3s Outputs
# ============================================
output "k3s_instance_name" {
  description = "K3s GCE instance name"
  value       = module.k3s.instance_name
}

output "k3s_public_ip" {
  description = "K3s GCE external IP"
  value       = module.k3s.instance_public_ip
}

output "k3s_ssh_command" {
  description = "SSH command to connect to K3s"
  value       = module.k3s.ssh_command
}

output "k3s_gcloud_ssh_command" {
  description = "gcloud SSH command"
  value       = module.k3s.gcloud_ssh_command
}

output "k3s_kubeconfig_command" {
  description = "Command to get kubeconfig"
  value       = module.k3s.k3s_kubeconfig_command
}

# ============================================
# Service별 DB URL (VM 내부 Docker MySQL)
# ============================================
output "auth_service_db_url" {
  description = "Auth service JDBC URL"
  value       = module.k3s.auth_service_db_url
}

output "ledger_service_db_url" {
  description = "Ledger service JDBC URL"
  value       = module.k3s.ledger_service_db_url
}

output "transaction_service_db_url" {
  description = "Transaction service JDBC URL"
  value       = module.k3s.transaction_service_db_url
}

# ============================================
# 비용 요약
# ============================================
output "cost_summary" {
  description = "Estimated monthly cost"
  value       = <<-EOT
    ============================================
    GCP Always Free 예상 비용
    ============================================
    - GCE (e2-micro): $0 (Always Free, US 리전 1 인스턴스)
    - Boot Disk (30GB standard): $0 (Always Free)
    - Artifact Registry: $0 (500MB 무료, us 리전)
    - Cloud NAT: ~$1/월 (NAT gateway 비용)
    - 데이터 전송: $0 (1GB/월 무료)
    ============================================
    총 예상 비용: ~$1/월

    * Cloud NAT 비용 절감 팁:
    - 필요할 때만 NAT gateway 활성화
    - 또는 VPC 없이 default network 사용 시 NAT 불필요

    * 참고: GCP Always Free는 기간 제한 없이 무료!
      (AWS Free Tier는 12개월 한정)
    ============================================
  EOT
}
