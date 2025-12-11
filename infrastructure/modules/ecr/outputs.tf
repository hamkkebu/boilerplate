output "repository_urls" {
  description = "Map of repository names to URLs"
  value       = { for k, v in aws_ecr_repository.services : k => v.repository_url }
}

output "repository_arns" {
  description = "Map of repository names to ARNs"
  value       = { for k, v in aws_ecr_repository.services : k => v.arn }
}

output "ecr_push_policy_arn" {
  description = "ARN of the ECR push policy for GitHub Actions"
  value       = aws_iam_policy.ecr_push.arn
}

output "github_actions_user_name" {
  description = "Name of the GitHub Actions IAM user"
  value       = var.create_github_actions_user ? aws_iam_user.github_actions[0].name : null
}

output "github_actions_user_arn" {
  description = "ARN of the GitHub Actions IAM user"
  value       = var.create_github_actions_user ? aws_iam_user.github_actions[0].arn : null
}

output "registry_url" {
  description = "ECR registry URL (without repository name)"
  value       = split("/", values(aws_ecr_repository.services)[0].repository_url)[0]
}
