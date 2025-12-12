#!/bin/bash
# ============================================
# RDS 초기 데이터베이스 생성 스크립트
# ============================================
# 사용법:
# ./init-databases.sh <RDS_HOST> <RDS_PORT> <MASTER_USER> <MASTER_PASSWORD>
#
# 예시:
# ./init-databases.sh hamkkebu-dev-xxx.rds.amazonaws.com 3306 admin MyPassword123
# ============================================

set -e

RDS_HOST=${1:-$DB_HOST}
RDS_PORT=${2:-${DB_PORT:-3306}}
MASTER_USER=${3:-${DB_USERNAME:-admin}}
MASTER_PASSWORD=${4:-$DB_PASSWORD}

if [ -z "$RDS_HOST" ] || [ -z "$MASTER_PASSWORD" ]; then
  echo "Usage: $0 <RDS_HOST> <RDS_PORT> <MASTER_USER> <MASTER_PASSWORD>"
  echo "Or set environment variables: DB_HOST, DB_PORT, DB_USERNAME, DB_PASSWORD"
  exit 1
fi

echo "=========================================="
echo "Hamkkebu Database Initialization"
echo "=========================================="
echo "RDS Host: $RDS_HOST"
echo "RDS Port: $RDS_PORT"
echo "User: $MASTER_USER"
echo "=========================================="

# MySQL 클라이언트 확인
if ! command -v mysql &> /dev/null; then
  echo "Error: mysql client not found. Please install it first."
  exit 1
fi

# 데이터베이스 생성 SQL
SQL_COMMANDS=$(cat <<'SQL'
-- ============================================
-- 데이터베이스 생성
-- ============================================
CREATE DATABASE IF NOT EXISTS hamkkebu_auth
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS hamkkebu_ledger
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE DATABASE IF NOT EXISTS hamkkebu_transaction
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ============================================
-- 데이터베이스 확인
-- ============================================
SHOW DATABASES LIKE 'hamkkebu_%';
SQL
)

echo ""
echo "Creating databases..."
echo "$SQL_COMMANDS" | mysql -h "$RDS_HOST" -P "$RDS_PORT" -u "$MASTER_USER" -p"$MASTER_PASSWORD"

echo ""
echo "=========================================="
echo "Database initialization completed!"
echo "=========================================="
echo ""
echo "Created databases:"
echo "  - hamkkebu_auth"
echo "  - hamkkebu_ledger"
echo "  - hamkkebu_transaction"
echo ""
echo "Connection URLs for each service:"
echo "  auth-service:        jdbc:mysql://$RDS_HOST:$RDS_PORT/hamkkebu_auth"
echo "  ledger-service:      jdbc:mysql://$RDS_HOST:$RDS_PORT/hamkkebu_ledger"
echo "  transaction-service: jdbc:mysql://$RDS_HOST:$RDS_PORT/hamkkebu_transaction"
