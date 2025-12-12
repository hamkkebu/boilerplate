# ============================================
# ECR Outputs
# ============================================
output "ecr_registry_url" {
  description = "ECR registry URL"
  value       = module.ecr.registry_url
}

output "ecr_repository_urls" {
  description = "Map of ECR repository URLs"
  value       = module.ecr.repository_urls
}

output "github_actions_user" {
  description = "GitHub Actions IAM user name"
  value       = module.ecr.github_actions_user_name
}

# ============================================
# VPC Outputs
# ============================================
output "vpc_id" {
  description = "VPC ID"
  value       = aws_vpc.main.id
}

output "public_subnet_ids" {
  description = "Public subnet IDs"
  value       = aws_subnet.public[*].id
}

# ============================================
# K3s Outputs
# ============================================
output "k3s_instance_id" {
  description = "K3s EC2 instance ID"
  value       = module.k3s.instance_id
}

output "k3s_public_ip" {
  description = "K3s EC2 public IP"
  value       = module.k3s.instance_public_ip
}

output "k3s_ssh_command" {
  description = "SSH command to connect to K3s"
  value       = module.k3s.ssh_command
}

output "k3s_kubeconfig_command" {
  description = "Command to get kubeconfig"
  value       = module.k3s.k3s_kubeconfig_command
}

# ============================================
# RDS Outputs
# ============================================
output "rds_endpoint" {
  description = "RDS endpoint"
  value       = module.rds.db_instance_endpoint
}

output "rds_address" {
  description = "RDS address (host only)"
  value       = module.rds.db_instance_address
}

output "rds_port" {
  description = "RDS port"
  value       = module.rds.db_instance_port
}

# ============================================
# Service별 DB URL (K8s Secret에 사용)
# ============================================
output "auth_service_db_url" {
  description = "Auth service JDBC URL"
  value       = module.rds.auth_service_db_url
}

output "ledger_service_db_url" {
  description = "Ledger service JDBC URL"
  value       = module.rds.ledger_service_db_url
}

output "transaction_service_db_url" {
  description = "Transaction service JDBC URL"
  value       = module.rds.transaction_service_db_url
}

# ============================================
# DB 초기화 안내
# ============================================
output "db_init_instructions" {
  description = "Instructions to create databases"
  value       = module.rds.db_init_instructions
}

# ============================================
# 비용 요약
# ============================================
output "cost_summary" {
  description = "Estimated monthly cost"
  value       = <<-EOT
    ============================================
    AWS Free Tier 예상 비용 (12개월간)
    ============================================
    - EC2 (t2.micro): $0 (750시간/월 무료)
    - RDS (db.t2.micro): $0 (750시간/월 무료)
    - EBS (20GB x 2): $0 (30GB까지 무료)
    - 데이터 전송: $0 (15GB/월 무료)
    ============================================
    총 예상 비용: $0/월 (Free Tier 기간)

    * Free Tier 종료 후 예상 비용:
    - EC2: ~$10/월
    - RDS: ~$15/월
    - Total: ~$25/월
    ============================================
  EOT
}
