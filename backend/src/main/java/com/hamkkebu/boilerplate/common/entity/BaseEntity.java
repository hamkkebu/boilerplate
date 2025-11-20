package com.hamkkebu.boilerplate.common.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 모든 엔티티의 기본 클래스
 *
 * <p>모든 엔티티가 상속받아야 하는 공통 필드를 정의합니다.</p>
 *
 * <p>제공하는 기능:</p>
 * <ul>
 *   <li>JPA Auditing: 생성일시, 수정일시, 생성자, 수정자 자동 관리</li>
 *   <li>Soft Delete: 논리적 삭제 지원</li>
 * </ul>
 *
 * <p>사용 예시:</p>
 * <pre>
 * {@code
 * @Entity
 * public class User extends BaseEntity {
 *     @Id
 *     @GeneratedValue(strategy = GenerationType.IDENTITY)
 *     private Long id;
 *
 *     private String name;
 *     // ...
 * }
 * }
 * </pre>
 */
@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    /**
     * 생성 일시
     * <p>엔티티가 처음 저장될 때 자동으로 설정됩니다.</p>
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 수정 일시
     * <p>엔티티가 수정될 때마다 자동으로 갱신됩니다.</p>
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 생성자 ID
     * <p>엔티티를 생성한 사용자의 ID를 저장합니다.</p>
     * <p>AuditorAware를 통해 자동으로 설정됩니다.</p>
     */
    @JsonIgnore
    @CreatedBy
    @Column(name = "created_by", updatable = false, length = 50)
    private String createdBy;

    /**
     * 수정자 ID
     * <p>엔티티를 마지막으로 수정한 사용자의 ID를 저장합니다.</p>
     * <p>AuditorAware를 통해 자동으로 설정됩니다.</p>
     */
    @JsonIgnore
    @LastModifiedBy
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * 삭제 여부 (Soft Delete)
     * <p>true: 삭제됨, false: 활성 상태</p>
     * <p>물리적 삭제 대신 논리적 삭제를 위해 사용됩니다.</p>
     */
    @JsonIgnore
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    /**
     * 삭제 일시
     * <p>엔티티가 삭제된 시각을 기록합니다.</p>
     */
    @JsonIgnore
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // ==================== Soft Delete 메서드 ====================

    /**
     * 엔티티를 논리적으로 삭제합니다.
     * <p>isDeleted 플래그를 true로 설정하고, deletedAt에 현재 시각을 기록합니다.</p>
     */
    public void delete() {
        this.isDeleted = true;
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 삭제된 엔티티를 복구합니다.
     * <p>isDeleted 플래그를 false로 설정하고, deletedAt을 null로 초기화합니다.</p>
     */
    public void restore() {
        this.isDeleted = false;
        this.deletedAt = null;
    }

    /**
     * 엔티티가 삭제되었는지 확인합니다.
     *
     * @return 삭제되었으면 true, 아니면 false
     */
    public boolean isDeleted() {
        return Boolean.TRUE.equals(isDeleted);
    }

    /**
     * 엔티티가 활성 상태인지 확인합니다.
     *
     * @return 활성 상태이면 true, 삭제되었으면 false
     */
    public boolean isActive() {
        return !isDeleted();
    }

    // ==================== Persistence Lifecycle ====================

    /**
     * 엔티티가 저장되기 전에 호출됩니다.
     * <p>isDeleted 플래그가 null인 경우 false로 초기화합니다.</p>
     */
    @PrePersist
    protected void onCreate() {
        if (isDeleted == null) {
            isDeleted = false;
        }
    }

    /**
     * 엔티티가 업데이트되기 전에 호출됩니다.
     * <p>현재는 특별한 처리가 없지만, 필요시 확장 가능합니다.</p>
     */
    @PreUpdate
    protected void onUpdate() {
        // 필요시 추가 로직 구현
    }
}
