package com.wanted.codebombalms.admin.permission.infrastructure.persistence;

import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "admin_permissions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_admin_permission_user_type",
                        columnNames = {"admin_user_id", "permission_type"}
                )
        }
)
@Getter
@NoArgsConstructor
public class AdminPermissionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_permission_id")
    private Long adminPermissionId;

    @Column(name = "admin_user_id", nullable = false)
    private Long adminUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "permission_type", nullable = false, length = 50)
    private AdminPermissionType permissionType;

    @Column(name = "granted_by", nullable = false)
    private Long grantedBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public AdminPermissionJpaEntity(
            Long adminPermissionId,
            Long adminUserId,
            AdminPermissionType permissionType,
            Long grantedBy,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.adminPermissionId = adminPermissionId;
        this.adminUserId = adminUserId;
        this.permissionType = permissionType;
        this.grantedBy = grantedBy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
