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

output "key_pair_name" {
  description = "EC2 Key Pair name"
  value       = aws_key_pair.ec2.key_name
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
       - key_pair_name is already set to: ${aws_key_pair.ec2.key_name}
    6. terraform plan
    7. terraform apply

  EOT
}
