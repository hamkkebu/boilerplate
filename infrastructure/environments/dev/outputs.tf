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
# K3s / EC2 Outputs
# ============================================
output "k3s_public_ip" {
  description = "K3s master public IP (Elastic IP)"
  value       = module.k3s.instance_public_ip
}

output "k3s_private_ip" {
  description = "K3s master private IP"
  value       = module.k3s.instance_private_ip
}

output "argocd_url" {
  description = "ArgoCD UI URL"
  value       = module.k3s.argocd_url
}

output "ssh_command" {
  description = "SSH command to connect to K3s master"
  value       = module.k3s.ssh_command
}

output "kubeconfig_command" {
  description = "Command to get kubeconfig"
  value       = module.k3s.kubeconfig_command
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

output "private_subnet_ids" {
  description = "Private subnet IDs"
  value       = aws_subnet.private[*].id
}

# ============================================
# RDS Outputs
# ============================================
output "rds_endpoint" {
  description = "RDS endpoint"
  value       = var.create_rds ? aws_db_instance.main[0].endpoint : null
}

output "rds_address" {
  description = "RDS address (hostname only)"
  value       = var.create_rds ? aws_db_instance.main[0].address : null
}

output "rds_port" {
  description = "RDS port"
  value       = var.create_rds ? aws_db_instance.main[0].port : null
}
