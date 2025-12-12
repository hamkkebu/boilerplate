# ============================================
# RDS Free Tier Module (단일 인스턴스, 다중 DB)
# ============================================
# db.t2.micro/db.t3.micro 단일 인스턴스
# Free Tier 적합 (12개월 750시간/월)
# 다중 데이터베이스 지원 (auth, ledger, transaction)
# ============================================

# DB Subnet Group
resource "aws_db_subnet_group" "main" {
  name_prefix = "${var.project_name}-${var.environment}-"
  subnet_ids  = var.subnet_ids
  description = "DB subnet group for ${var.project_name}-${var.environment}"

  tags = {
    Name = "${var.project_name}-${var.environment}-db-subnet-group"
  }
}

# Security Group for RDS
resource "aws_security_group" "rds" {
  name_prefix = "${var.project_name}-${var.environment}-rds-"
  description = "Security group for RDS"
  vpc_id      = var.vpc_id

  # MySQL from K3s/EKS
  ingress {
    from_port       = 3306
    to_port         = 3306
    protocol        = "tcp"
    security_groups = var.allowed_security_group_ids
    description     = "MySQL from K3s/EKS"
  }

  # MySQL from specific CIDRs (optional)
  dynamic "ingress" {
    for_each = length(var.allowed_cidrs) > 0 ? [1] : []
    content {
      from_port   = 3306
      to_port     = 3306
      protocol    = "tcp"
      cidr_blocks = var.allowed_cidrs
      description = "MySQL from allowed CIDRs"
    }
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
    description = "All outbound"
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-rds-sg"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# RDS Instance (Free Tier)
resource "aws_db_instance" "main" {
  identifier_prefix = "${var.project_name}-${var.environment}-"

  # Engine
  engine               = "mysql"
  engine_version       = var.engine_version
  instance_class       = var.instance_class
  parameter_group_name = aws_db_parameter_group.main.name

  # Storage (Free Tier: 20GB)
  allocated_storage     = var.allocated_storage
  max_allocated_storage = var.max_allocated_storage
  storage_type          = "gp2"
  storage_encrypted     = true

  # Database
  db_name  = var.initial_db_name
  username = var.master_username
  password = var.master_password

  # Network
  db_subnet_group_name   = aws_db_subnet_group.main.name
  vpc_security_group_ids = [aws_security_group.rds.id]
  publicly_accessible    = false
  port                   = 3306

  # Backup (Free Tier: 20GB backup storage)
  backup_retention_period = var.backup_retention_period
  backup_window           = "03:00-04:00"
  maintenance_window      = "Mon:04:00-Mon:05:00"
  skip_final_snapshot     = var.skip_final_snapshot
  final_snapshot_identifier = var.skip_final_snapshot ? null : "${var.project_name}-${var.environment}-final-snapshot"

  # Performance & Monitoring
  performance_insights_enabled = false  # Free Tier 제외
  monitoring_interval          = 0       # Enhanced Monitoring 비활성화 (비용 절감)

  # Deletion Protection
  deletion_protection = var.deletion_protection

  tags = {
    Name = "${var.project_name}-${var.environment}-rds"
  }

  lifecycle {
    ignore_changes = [password]
  }
}

# Parameter Group
resource "aws_db_parameter_group" "main" {
  name_prefix = "${var.project_name}-${var.environment}-"
  family      = "mysql8.0"
  description = "Parameter group for ${var.project_name}-${var.environment}"

  # UTF8MB4 설정
  parameter {
    name  = "character_set_server"
    value = "utf8mb4"
  }

  parameter {
    name  = "character_set_client"
    value = "utf8mb4"
  }

  parameter {
    name  = "collation_server"
    value = "utf8mb4_unicode_ci"
  }

  # 타임존
  parameter {
    name  = "time_zone"
    value = "Asia/Seoul"
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-db-params"
  }

  lifecycle {
    create_before_destroy = true
  }
}

# ============================================
# 초기 DB 생성 스크립트 (참고용)
# ============================================
# RDS 생성 후 아래 SQL로 추가 데이터베이스 생성:
#
# CREATE DATABASE IF NOT EXISTS hamkkebu_auth
#   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
#
# CREATE DATABASE IF NOT EXISTS hamkkebu_ledger
#   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
#
# CREATE DATABASE IF NOT EXISTS hamkkebu_transaction
#   CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
#
# -- 서비스별 사용자 생성 (선택사항)
# CREATE USER 'auth_user'@'%' IDENTIFIED BY 'password';
# GRANT ALL PRIVILEGES ON hamkkebu_auth.* TO 'auth_user'@'%';
#
# CREATE USER 'ledger_user'@'%' IDENTIFIED BY 'password';
# GRANT ALL PRIVILEGES ON hamkkebu_ledger.* TO 'ledger_user'@'%';
#
# CREATE USER 'transaction_user'@'%' IDENTIFIED BY 'password';
# GRANT ALL PRIVILEGES ON hamkkebu_transaction.* TO 'transaction_user'@'%';
#
# FLUSH PRIVILEGES;
# ============================================
