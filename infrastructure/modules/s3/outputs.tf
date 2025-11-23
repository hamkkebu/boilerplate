output "app_bucket_id" {
  description = "ID of the application bucket"
  value       = aws_s3_bucket.app.id
}

output "app_bucket_name" {
  description = "Name of the application bucket"
  value       = aws_s3_bucket.app.bucket
}

output "app_bucket_arn" {
  description = "ARN of the application bucket"
  value       = aws_s3_bucket.app.arn
}

output "logs_bucket_id" {
  description = "ID of the logs bucket"
  value       = aws_s3_bucket.logs.id
}

output "logs_bucket_name" {
  description = "Name of the logs bucket"
  value       = aws_s3_bucket.logs.bucket
}

output "logs_bucket_arn" {
  description = "ARN of the logs bucket"
  value       = aws_s3_bucket.logs.arn
}
