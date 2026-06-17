package com.wanted.codebombalms.user.infrastructure.persistence;

import com.wanted.codebombalms.user.domain.model.AuthProvider;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@SQLRestriction("deleted_at IS NULL")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 20)
    private String name;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider;

    @Column(length = 100)
    private String providerId;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(nullable = false)
    private boolean isLocked = false;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(columnDefinition = "TEXT")
    private String career;


    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime deletedAt;

    // ===== 도메인 → 엔티티 (저장할 때) =====
    public static UserJpaEntity from(User user) {
        UserJpaEntity e = new UserJpaEntity();
        e.userId        = user.getUserId();
        e.role          = user.getRole();
        e.email         = user.getEmail();
        e.password      = user.getPassword();
        e.name          = user.getName();
        e.nickname      = user.getNickname();
        e.phone         = user.getPhone();
        e.provider      = user.getProvider();
        e.providerId    = user.getProviderId();
        e.emailVerified = user.isEmailVerified();
        e.isLocked      = user.isLocked();
        e.bio           = user.getBio();
        e.career        = user.getCareer();
        e.deletedAt     = user.getDeletedAt();
        return e;
    }

    // ===== 엔티티 → 도메인 (조회할 때) =====
    public User toDomain() {
        return User.restore(
                userId, role, email, password, name, nickname, phone,
                provider, providerId, emailVerified, isLocked,
                bio, career, createdAt, updatedAt, deletedAt
        );
    }

    // ===== 도메인 상태 반영 (업데이트할 때) =====
    public void applyDomain(User user) {
        this.role          = user.getRole();
        this.email         = user.getEmail();
        this.password      = user.getPassword();
        this.name          = user.getName();
        this.nickname      = user.getNickname();
        this.phone         = user.getPhone();
        this.provider      = user.getProvider();
        this.providerId    = user.getProviderId();
        this.emailVerified = user.isEmailVerified();
        this.isLocked      = user.isLocked();
        this.bio           = user.getBio();
        this.career        = user.getCareer();
        this.deletedAt     = user.getDeletedAt();
    }
}
