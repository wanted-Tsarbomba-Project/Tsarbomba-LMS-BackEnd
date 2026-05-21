package com.wanted.codebombalms.user.domain.model;

import java.time.LocalDateTime;

public class User {

    private Long userId;
    private UserRole role;
    private String email;
    private String password;
    private String name;
    private String nickname;
    private String phone;
    private AuthProvider provider;
    private String providerId;
    private boolean emailVerified;
    private boolean isLocked;
    private String bio;
    private String career;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deletedAt;

    private User() {}

    // ===== Getter =====
    public Long getUserId()           { return userId; }
    public UserRole getRole()         { return role; }
    public String getEmail()          { return email; }
    public String getPassword()       { return password; }
    public String getName()           { return name; }
    public String getNickname()       { return nickname; }
    public String getPhone()          { return phone; }
    public AuthProvider getProvider() { return provider; }
    public String getProviderId()     { return providerId; }
    public boolean isEmailVerified()  { return emailVerified; }
    public boolean isLocked()         { return isLocked; }
    public String getBio()            { return bio; }
    public String getCareer()         { return career; }
    public LocalDateTime getCreatedAt()  { return createdAt; }
    public LocalDateTime getUpdatedAt()  { return updatedAt; }
    public LocalDateTime getDeletedAt()  { return deletedAt; }

    // ===== 신규 로컬 회원 생성 =====
    public static User createLocalUser(
            String email,
            String encodedPassword,
            String name,
            String nickname,
            String phone
    ) {
        User user = new User();
        user.role          = UserRole.STUDENT;
        user.email         = email;
        user.password      = encodedPassword;
        user.name          = name;
        user.nickname      = nickname;
        user.phone         = phone;
        user.provider      = AuthProvider.LOCAL;
        user.emailVerified = false;
        user.isLocked      = false;
        return user;
    }

    // ===== 영속성 복원 — Adapter 전용 =====
    public static User restore(
            Long userId, UserRole role, String email, String password,
            String name, String nickname, String phone,
            AuthProvider provider, String providerId,
            boolean emailVerified, boolean isLocked,
            String bio, String career,
            LocalDateTime createdAt, LocalDateTime updatedAt, LocalDateTime deletedAt
    ) {
        User user = new User();
        user.userId        = userId;
        user.role          = role;
        user.email         = email;
        user.password      = password;
        user.name          = name;
        user.nickname      = nickname;
        user.phone         = phone;
        user.provider      = provider;
        user.providerId    = providerId;
        user.emailVerified = emailVerified;
        user.isLocked      = isLocked;
        user.bio           = bio;
        user.career        = career;
        user.createdAt     = createdAt;
        user.updatedAt     = updatedAt;
        user.deletedAt     = deletedAt;
        return user;
    }

    // ===== 도메인 행위 =====
    public void softDelete() { this.deletedAt = LocalDateTime.now(); }
    public void lock()       { this.isLocked = true; }
    public void unlock()     { this.isLocked = false; }
    public boolean isDeleted() { return this.deletedAt != null; }
}