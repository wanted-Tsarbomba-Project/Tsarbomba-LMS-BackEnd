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
        user.emailVerified = true;
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

    public void updateProfile(String nickname, String phone) {
        if (nickname != null) this.nickname = nickname;
        if (phone != null)    this.phone = phone;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // ===== 탈퇴(소프트딜리트) 묘비 처리용 상수 =====
    private static final int EMAIL_MAX_LENGTH = 100;          // users.email 컬럼 길이
    private static final String DELETED_NICKNAME_PREFIX = "탈퇴회원_";
    private static final String DELETED_EMAIL_MARKER = "#deleted#";

    public void softDelete() {
        if (isDeleted()) return;  // 이미 탈퇴 처리됨 — 중복 묘비화 방지
        this.deletedAt = LocalDateTime.now();
        this.nickname  = DELETED_NICKNAME_PREFIX + this.userId;      // 닉네임 슬롯 해방
        this.email     = tombstoneEmail(this.email, this.userId);   // 이메일 슬롯 해방(원본 보존)
    }

    /**
     * 이메일 묘비화: "원본#deleted#{userId}" 로 바꿔 unique 슬롯을 풀되 원본을 보존.
     * 컬럼 길이(100) 초과 시 원본 앞부분만 잘라 보존 (userId는 끝에 유지해 고유성 보장).
     */
    private static String tombstoneEmail(String email, Long userId) {
        String suffix  = DELETED_EMAIL_MARKER + userId;             // 예: "#deleted#42"
        int    baseMax = EMAIL_MAX_LENGTH - suffix.length();
        String base    = email.length() > baseMax ? email.substring(0, baseMax) : email;
        return base + suffix;
    }    public void lock()       { this.isLocked = true; }
    public void unlock()     { this.isLocked = false; }
    public boolean isDeleted() { return this.deletedAt != null; }


    // ===== 신규 소셜 회원 생성 (구글) =====
    public static User createSocialUser(
            String email,
            String name,
            String nickname,
            String phone,
            AuthProvider provider
    ) {
        User user = new User();
        user.role          = UserRole.STUDENT;
        user.email         = email;
        user.password      = null;          // 소셜 계정은 비밀번호 없음
        user.name          = name;
        user.nickname      = nickname;
        user.phone         = phone;
        user.provider      = provider;
        user.emailVerified = true;          // 구글에서 검증된 이메일
        user.isLocked      = false;
        return user;
    }
}
