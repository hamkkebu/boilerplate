output "db_instance_id" {
  description = "RDS instance ID"
  value       = aws_db_instance.main.id
}

output "db_instance_endpoint" {
  description = "RDS instance endpoint (host:port)"
  value       = aws_db_instance.main.endpoint
}

output "db_instance_address" {
  description = "RDS instance address (host only)"
  value       = aws_db_instance.main.address
}

output "db_instance_port" {
  description = "RDS instance port"
  value       = aws_db_instance.main.port
}

output "db_security_group_id" {
  description = "RDS security group ID"
  value       = aws_security_group.rds.id
}

# ============================================
# Service별 접속 URL 예시
# ============================================
output "auth_service_db_url" {
  description = "Auth service JDBC URL example"
  value       = "jdbc:mysql://${aws_db_instance.main.address}:${aws_db_instance.main.port}/hamkkebu_auth?useSSL=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
}

output "ledger_service_db_url" {
  description = "Ledger service JDBC URL example"
  value       = "jdbc:mysql://${aws_db_instance.main.address}:${aws_db_instance.main.port}/hamkkebu_ledger?useSSL=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
}

output "transaction_service_db_url" {
  description = "Transaction service JDBC URL example"
  value       = "jdbc:mysql://${aws_db_instance.main.address}:${aws_db_instance.main.port}/hamkkebu_transaction?useSSL=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8"
}

# ============================================
# DB 초기화 안내
# ============================================
output "db_init_instructions" {
  description = "Instructions to create additional databases"
  value       = <<-EOT
    RDS 생성 후 아래 명령으로 접속하여 추가 데이터베이스를 생성하세요:

    1. MySQL 접속:
       mysql -h ${aws_db_instance.main.address} -P ${aws_db_instance.main.port} -u admin -p

    2. 데이터베이스 생성:
       CREATE DATABASE IF NOT EXISTS hamkkebu_auth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
       CREATE DATABASE IF NOT EXISTS hamkkebu_ledger CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
       CREATE DATABASE IF NOT EXISTS hamkkebu_transaction CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
  EOT
}
