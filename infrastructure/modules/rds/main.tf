# RDS Module - Multiple MySQL instances for each service

# ============================================
# DB Subnet Group
# ============================================
resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-${var.environment}-db-subnet-group"
  subnet_ids = var.private_subnet_ids

  tags = {
    Name = "${var.project_name}-${var.environment}-db-subnet-group"
  }
}

# ============================================
# Security Group for RDS
# ============================================
resource "aws_security_group" "rds" {
  name        = "${var.project_name}-${var.environment}-rds-sg"
  description = "Security group for RDS instances"
  vpc_id      = var.vpc_id

  ingress {
    description     = "MySQL from allowed security groups"
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = var.allowed_security_group_ids
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-rds-sg"
  }
}

# ============================================
# RDS Instances (one per service)
# ============================================
resource "aws_db_instance" "services" {
  for_each = { for s in var.services : s.name => s }

  identifier = "${var.project_name}-${var.environment}-${each.value.name}"

  engine               = "mysql"
  engine_version       = var.engine_version
  instance_class       = var.instance_class
  allocated_storage    = var.allocated_storage
  storage_type         = "gp3"
  storage_encrypted    = true

  db_name  = each.value.db_name
  username = each.value.username
  password = each.value.password

  vpc_security_group_ids = [aws_security_group.rds.id]
  db_subnet_group_name   = aws_db_subnet_group.main.name

  backup_retention_period = var.backup_retention_period
  backup_window          = "03:00-04:00"
  maintenance_window     = "Mon:04:00-Mon:05:00"

  skip_final_snapshot       = var.environment != "prod"
  final_snapshot_identifier = var.environment == "prod" ? "${var.project_name}-${var.environment}-${each.value.name}-final" : null
  deletion_protection       = var.environment == "prod"

  publicly_accessible = false
  multi_az            = var.environment == "prod"

  tags = {
    Name    = "${var.project_name}-${var.environment}-${each.value.name}"
    Service = each.value.name
  }
}
