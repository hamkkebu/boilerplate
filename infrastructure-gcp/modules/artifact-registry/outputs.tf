output "repository_url" {
  description = "Artifact Registry repository URL"
  value       = "${var.gcp_region}-docker.pkg.dev/${var.gcp_project_id}/${google_artifact_registry_repository.docker.repository_id}"
}

output "repository_id" {
  description = "Artifact Registry repository ID"
  value       = google_artifact_registry_repository.docker.repository_id
}

output "repository_name" {
  description = "Artifact Registry repository name"
  value       = google_artifact_registry_repository.docker.name
}

# 서비스별 이미지 URL
output "image_urls" {
  description = "Map of service names to full image URLs"
  value = {
    for repo in local.repositories :
    repo => "${var.gcp_region}-docker.pkg.dev/${var.gcp_project_id}/${google_artifact_registry_repository.docker.repository_id}/${repo}"
  }
}

output "github_actions_sa_email" {
  description = "GitHub Actions service account email"
  value       = var.create_github_actions_sa ? google_service_account.github_actions[0].email : null
}

output "workload_identity_provider" {
  description = "Workload Identity Provider name (for GitHub Actions)"
  value       = var.create_github_actions_sa ? google_iam_workload_identity_pool_provider.github[0].name : null
}

output "docker_push_command" {
  description = "Example docker push command"
  value       = "docker push ${var.gcp_region}-docker.pkg.dev/${var.gcp_project_id}/${google_artifact_registry_repository.docker.repository_id}/<image-name>:<tag>"
}
