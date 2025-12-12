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

output "private_subnet_ids" {
  description = "Private subnet IDs"
  value       = aws_subnet.private[*].id
}

output "nat_gateway_ip" {
  description = "NAT Gateway public IP"
  value       = aws_eip.nat.public_ip
}

# ============================================
# EKS Outputs
# ============================================
output "eks_cluster_name" {
  description = "EKS cluster name"
  value       = module.eks.cluster_name
}

output "eks_cluster_endpoint" {
  description = "EKS cluster endpoint"
  value       = module.eks.cluster_endpoint
}

output "eks_kubeconfig_command" {
  description = "Command to update kubeconfig"
  value       = module.eks.kubeconfig_command
}

output "alb_controller_role_arn" {
  description = "IAM role ARN for AWS Load Balancer Controller"
  value       = module.eks.alb_controller_role_arn
}

# ============================================
# EKS Cluster Security Group (서비스별 RDS에서 참조)
# ============================================
output "eks_cluster_security_group_id" {
  description = "EKS cluster security group ID for RDS ingress rules"
  value       = module.eks.cluster_security_group_id
}
