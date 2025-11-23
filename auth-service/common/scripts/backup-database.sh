#!/bin/bash

################################################################################
# MySQL Database Backup Script
################################################################################
# 이 스크립트는 MySQL 데이터베이스를 백업합니다.
#
# 기능:
# - 전체 데이터베이스 백업 (구조 + 데이터)
# - 타임스탬프가 포함된 백업 파일명
# - 압축 (gzip)
# - 오래된 백업 파일 자동 삭제 (30일 이상)
# - 로그 기록
#
# 사용법:
#   ./backup-database.sh
#
# Cron 예시 (매일 새벽 3시):
#   0 3 * * * /path/to/backup-database.sh >> /var/log/db-backup.log 2>&1
################################################################################

# 설정
DB_USER="${DB_USER:-root}"
DB_PASSWORD="${DB_PASSWORD:-password}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-3306}"
DB_NAME="${DB_NAME:-hamkkebu_boilerplate_db}"

# 백업 디렉토리
BACKUP_DIR="${BACKUP_DIR:-$(pwd)/backups/mysql}"
LOG_DIR="${LOG_DIR:-$(pwd)/logs}"

# 백업 파일명 (타임스탬프 포함)
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/${DB_NAME}_${TIMESTAMP}.sql"
BACKUP_FILE_GZ="${BACKUP_FILE}.gz"

# 로그 파일
LOG_FILE="${LOG_DIR}/backup-$(date +"%Y%m%d").log"

# 보관 기간 (일)
RETENTION_DAYS=30

################################################################################
# Functions
################################################################################

log() {
    echo "[$(date +"%Y-%m-%d %H:%M:%S")] $1" | tee -a "${LOG_FILE}"
}

error() {
    echo "[$(date +"%Y-%m-%d %H:%M:%S")] ERROR: $1" | tee -a "${LOG_FILE}" >&2
}

# 디렉토리 생성
create_directories() {
    mkdir -p "${BACKUP_DIR}"
    mkdir -p "${LOG_DIR}"
}

# MySQL 백업
backup_database() {
    log "Starting database backup: ${DB_NAME}"
    log "Backup file: ${BACKUP_FILE_GZ}"

    # mysqldump 실행
    mysqldump \
        --user="${DB_USER}" \
        --password="${DB_PASSWORD}" \
        --host="${DB_HOST}" \
        --port="${DB_PORT}" \
        --single-transaction \
        --routines \
        --triggers \
        --events \
        --set-gtid-purged=OFF \
        "${DB_NAME}" > "${BACKUP_FILE}"

    if [ $? -eq 0 ]; then
        log "Database dump completed successfully"
    else
        error "Database dump failed"
        rm -f "${BACKUP_FILE}"
        exit 1
    fi

    # 압축
    log "Compressing backup file..."
    gzip "${BACKUP_FILE}"

    if [ $? -eq 0 ]; then
        log "Compression completed successfully"
        log "Backup file size: $(du -h "${BACKUP_FILE_GZ}" | cut -f1)"
    else
        error "Compression failed"
        exit 1
    fi
}

# 오래된 백업 파일 삭제
cleanup_old_backups() {
    log "Cleaning up backups older than ${RETENTION_DAYS} days..."

    find "${BACKUP_DIR}" -name "*.sql.gz" -type f -mtime +${RETENTION_DAYS} -delete

    if [ $? -eq 0 ]; then
        log "Old backups cleaned up successfully"
    else
        error "Failed to clean up old backups"
    fi
}

# 백업 파일 검증
verify_backup() {
    if [ -f "${BACKUP_FILE_GZ}" ]; then
        # 파일 크기 확인 (최소 1KB)
        MIN_SIZE=1024
        FILE_SIZE=$(stat -f%z "${BACKUP_FILE_GZ}" 2>/dev/null || stat -c%s "${BACKUP_FILE_GZ}" 2>/dev/null)

        if [ "${FILE_SIZE}" -lt "${MIN_SIZE}" ]; then
            error "Backup file size is too small (${FILE_SIZE} bytes)"
            exit 1
        fi

        # gzip 파일 무결성 확인
        gzip -t "${BACKUP_FILE_GZ}" 2>/dev/null

        if [ $? -eq 0 ]; then
            log "Backup file integrity verified"
        else
            error "Backup file is corrupted"
            exit 1
        fi
    else
        error "Backup file not found: ${BACKUP_FILE_GZ}"
        exit 1
    fi
}

# 백업 상태 요약
print_summary() {
    log "============================================"
    log "Backup Summary"
    log "============================================"
    log "Database: ${DB_NAME}"
    log "Backup File: ${BACKUP_FILE_GZ}"
    log "File Size: $(du -h "${BACKUP_FILE_GZ}" | cut -f1)"
    log "Total Backups: $(ls -1 "${BACKUP_DIR}"/*.sql.gz 2>/dev/null | wc -l | tr -d ' ')"
    log "Disk Usage: $(du -sh "${BACKUP_DIR}" | cut -f1)"
    log "============================================"
}

################################################################################
# Main
################################################################################

main() {
    log "============================================"
    log "Database Backup Started"
    log "============================================"

    # 디렉토리 생성
    create_directories

    # 백업 실행
    backup_database

    # 백업 검증
    verify_backup

    # 오래된 백업 정리
    cleanup_old_backups

    # 요약 출력
    print_summary

    log "Database Backup Completed Successfully"
}

# 스크립트 실행
main

exit 0
