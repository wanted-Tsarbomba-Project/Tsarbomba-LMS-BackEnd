package com.wanted.codebombalms.domain.user.repository;

import com.wanted.codebombalms.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 로그인 — 이메일로 회원 조회
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

    // 회원가입 — 이메일 중복 체크
    boolean existsByEmail(String email);

    // 회원가입 — 닉네임 중복 체크
    boolean existsByNickname(String nickname);

    // 내 정보 조회 — userId로 회원 조회
    Optional<User> findByUserIdAndDeletedAtIsNull(Long userId);
}