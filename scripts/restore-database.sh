#!/bin/bash

################################################################################
# MySQL Database Restore Script
################################################################################
# 이 스크립트는 백업된 MySQL 데이터베이스를 복원합니다.
#
# 기능:
# - 압축된 백업 파일 복원
# - 복원 전 확인 프롬프트
# - 로그 기록
#
# 사용법:
#   ./restore-database.sh <backup_file.sql.gz>
#
# 예시:
#   ./restore-database.sh backups/mysql/hamkkebu_boilerplate_db_20250119_030000.sql.gz
################################################################################

# 설정
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-password}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-hamkkebu_boilerplate_db}"

# 로그 디렉토리
LOG_DIR="${LOG_DIR:-$(pwd)/logs}"
LOG_FILE="${LOG_DIR}/restore-$(date +"%Y%m%d_%H%M%S").log"

################################################################################
# Functions
################################################################################

log() {
    echo "[$(date +"%Y-%m-%d %H:%M:%S")] $1" | tee -a "${LOG_FILE}"
}

error() {
    echo "[$(date +"%Y-%m-%d %H:%M:%S")] ERROR: $1" | tee -a "${LOG_FILE}" >&2
}

# 사용법 출력
usage() {
    echo "Usage: $0 <backup_file.sql.gz>"
    echo ""
    echo "Example:"
    echo "  $0 backups/mysql/hamkkebu_boilerplate_db_20250119_030000.sql.gz"
    echo ""
    echo "Environment Variables:"
    echo "  DB_USER      - Database user (default: root)"
    echo "  DB_PASSWORD  - Database password (default: password)"
    echo "  DB_HOST      - Database host (default: localhost)"
    echo "  DB_PORT      - Database port (default: 3306)"
    echo "  DB_NAME      - Database name (default: hamkkebu_boilerplate_db)"
    exit 1
}

# 백업 파일 검증
verify_backup_file() {
    local backup_file=$1

    if [ ! -f "${backup_file}" ]; then
        error "Backup file not found: ${backup_file}"
        exit 1
    fi

    # gzip 파일 무결성 확인
    gzip -t "${backup_file}" 2>/dev/null

    if [ $? -ne 0 ]; then
        error "Backup file is corrupted: ${backup_file}"
        exit 1
    fi

    log "Backup file verified: ${backup_file}"
}

# 복원 확인
confirm_restore() {
    local backup_file=$1

    echo ""
    echo "============================================"
    echo "DATABASE RESTORE CONFIRMATION"
    echo "============================================"
    echo "WARNING: This will replace all data in the database!"
    echo ""
    echo "Database: ${DB_NAME}"
    echo "Host: ${DB_HOST}:${DB_PORT}"
    echo "Backup File: ${backup_file}"
    echo "File Size: $(du -h "${backup_file}" | cut -f1)"
    echo ""
    echo "============================================"
    echo ""
    read -p "Are you sure you want to proceed? (yes/no): " confirm

    if [ "${confirm}" != "yes" ]; then
        log "Restore cancelled by user"
        exit 0
    fi
}

# 데이터베이스 복원
restore_database() {
    local backup_file=$1

    log "Starting database restore from: ${backup_file}"

    # 압축 해제하면서 mysql로 복원
    gunzip -c "${backup_file}" | mysql \
        --user="${DB_USER}" \
        --password="${DB_PASSWORD}" \
        --host="${DB_HOST}" \
        --port="${DB_PORT}" \
        "${DB_NAME}"

    if [ $? -eq 0 ]; then
        log "Database restore completed successfully"
    else
        error "Database restore failed"
        exit 1
    fi
}

# 복원 후 검증
verify_restore() {
    log "Verifying database restore..."

    # 테이블 수 확인
    TABLE_COUNT=$(mysql \
        --user="${DB_USER}" \
        --password="${DB_PASSWORD}" \
        --host="${DB_HOST}" \
        --port="${DB_PORT}" \
        --skip-column-names \
        --execute="SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='${DB_NAME}';" \
        2>/dev/null)

    if [ -z "${TABLE_COUNT}" ] || [ "${TABLE_COUNT}" -eq 0 ]; then
        error "Database verification failed: No tables found"
        exit 1
    fi

    log "Database verification successful: ${TABLE_COUNT} tables found"
}

################################################################################
# Main
################################################################################

main() {
    # 인자 확인
    if [ $# -ne 1 ]; then
        usage
    fi

    local backup_file=$1

    # 로그 디렉토리 생성
    mkdir -p "${LOG_DIR}"

    log "============================================"
    log "Database Restore Started"
    log "============================================"

    # 백업 파일 검증
    verify_backup_file "${backup_file}"

    # 복원 확인
    confirm_restore "${backup_file}"

    # 복원 실행
    restore_database "${backup_file}"

    # 복원 검증
    verify_restore

    log "============================================"
    log "Database Restore Completed Successfully"
    log "============================================"
}

# 스크립트 실행
main "$@"

exit 0
