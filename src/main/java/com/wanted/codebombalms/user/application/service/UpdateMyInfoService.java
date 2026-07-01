package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.ForbiddenException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.user.application.query.UpdateMyInfoResult;
import com.wanted.codebombalms.user.application.usecase.UpdateMyInfoUseCase;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.ProfileEditVerificationRepository;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UpdateMyInfoService implements UpdateMyInfoUseCase {

    private final UserRepository userRepository;
    private final ProfileEditVerificationRepository profileEditVerificationRepository;

    @Override
    public UpdateMyInfoResult update(Long userId, String nickname, String phone) {
        // 1. 본인 조회 (없으면 USER_NOT_FOUND)
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(UserErrorCode.USER_NOT_FOUND));

        // 2. 재인증 게이트 — LOCAL 계정만 적용 (소셜 계정은 비밀번호가 없어 재인증 불가 → 스킵)
        if (!user.isSocialAccount() && !profileEditVerificationRepository.isVerified(userId)) {
            throw new ForbiddenException(UserErrorCode.USER_REVERIFICATION_REQUIRED);
        }

        // 3. 닉네임 변경 감지 + 중복 검사 (기존 닉네임과 다를 때만)
        boolean nicknameChanged = nickname != null && !nickname.equals(user.getNickname());
        if (nicknameChanged && userRepository.existsByNickname(nickname)) {
            throw new ValidationException(UserErrorCode.USER_NICKNAME_DUPLICATED);
        }

        // 4. 부분 수정 적용 (전달된 필드만) + 즉시 flush
        user.updateProfile(nickname, phone);
        try {
            userRepository.saveAndFlush(user); // 즉시 flush — 제약 위반을 이 자리에서 포착
        } catch (DataIntegrityViolationException e) {
            // 사전 체크 통과했어도 동시성 레이스로 닉네임 unique 충돌 가능 → 닉네임 중복으로 정밀 매핑 (원인 보존)
            throw new ValidationException(UserErrorCode.USER_NICKNAME_DUPLICATED, e);
        }

        log.info("개인정보 수정 완료 - userId={}, nicknameChanged={}", userId, nicknameChanged);

        // 5. 토큰 재발급 판단용 정보 반환
        return new UpdateMyInfoResult(nicknameChanged, user.getNickname(), user.getRole());
    }
}
