package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.user.application.usecase.FindEmailUseCase;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FindEmailService implements FindEmailUseCase {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public String findEmail(String name, String phone) {

        // 1. 이름 + 전화번호가 모두 일치하는 활성 회원 조회 (없으면 404)
        User user = userRepository.findByNameAndPhone(name, phone)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 2. 이메일 마스킹 후 반환 (평문 노출 방지)
        return maskEmail(user.getEmail());
    }

    /**
     * 이메일 마스킹 — 로컬 파트 앞 2자만 남기고 나머지는 '*' 처리.
     * (예: user@example.com → us**@example.com)
     * 로컬 파트가 2자 이하면 첫 1자만 노출.
     */
    private String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return email; // 형식 비정상 — 그대로 반환 (방어적 처리)
        }

        String local  = email.substring(0, atIndex);
        String domain = email.substring(atIndex); // '@' 포함

        int visible = local.length() <= 2 ? 1 : 2;
        String masked = local.substring(0, visible)
                + "*".repeat(local.length() - visible);

        return masked + domain;
    }
}
