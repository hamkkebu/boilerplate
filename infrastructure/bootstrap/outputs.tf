output "state_bucket_name" {
  description = "S3 bucket name for Terraform state"
  value       = aws_s3_bucket.terraform_state.id
}

output "state_bucket_arn" {
  description = "S3 bucket ARN"
  value       = aws_s3_bucket.terraform_state.arn
}

output "lock_table_name" {
  description = "DynamoDB table name for state locking"
  value       = aws_dynamodb_table.terraform_lock.name
}

output "lock_table_arn" {
  description = "DynamoDB table ARN"
  value       = aws_dynamodb_table.terraform_lock.arn
}

output "next_steps" {
  description = "Next steps after bootstrap"
  value       = <<-EOT

    Bootstrap complete! Next steps:

    1. cd ../environments/dev
    2. terraform init
    3. cp terraform.tfvars.example terraform.tfvars
    4. Edit terraform.tfvars with your values
    5. terraform plan
    6. terraform apply

  EOT
}
