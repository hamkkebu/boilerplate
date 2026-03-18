output "state_bucket_name" {
  description = "GCS bucket name for Terraform state"
  value       = google_storage_bucket.terraform_state.name
}

output "state_bucket_url" {
  description = "GCS bucket URL"
  value       = google_storage_bucket.terraform_state.url
}

output "ssh_public_key" {
  description = "SSH public key for GCE instances"
  value       = tls_private_key.gce.public_key_openssh
}

output "private_key_path" {
  description = "Path to the private key file"
  value       = local_file.private_key.filename
}

output "next_steps" {
  description = "Next steps after bootstrap"
  value       = <<-EOT

    Bootstrap complete! Next steps:

    1. Save the private key: ${local_file.private_key.filename}
       (Move it to a secure location, e.g., ~/.ssh/)

    2. cd ../environments/dev
    3. terraform init
    4. cp terraform.tfvars.example terraform.tfvars
    5. Edit terraform.tfvars with your values
    6. terraform plan
    7. terraform apply

  EOT
}
