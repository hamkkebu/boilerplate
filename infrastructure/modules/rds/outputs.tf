output "security_group_id" {
  description = "RDS security group ID"
  value       = aws_security_group.rds.id
}

output "db_subnet_group_name" {
  description = "DB subnet group name"
  value       = aws_db_subnet_group.main.name
}

output "endpoints" {
  description = "RDS endpoints for each service"
  value = {
    for name, instance in aws_db_instance.services : name => {
      endpoint = instance.endpoint
      address  = instance.address
      port     = instance.port
    }
  }
}

output "connection_strings" {
  description = "JDBC connection strings for each service"
  sensitive   = true
  value = {
    for name, instance in aws_db_instance.services : name => 
      "jdbc:mysql://${instance.address}:${instance.port}/${instance.db_name}?useSSL=false&allowPublicKeyRetrieval=true"
  }
}
