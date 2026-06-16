package com.wanted.codebombalms.auth.application.usecase;

public interface DuplicateCheckUseCase {

    /**
     * 이메일 사용 가능 여부를 확인한다.
     * @return true: 사용 가능, false: 이미 사용 중
     */
    boolean isEmailAvailable(String email);

    /**
     * 닉네임 사용 가능 여부를 확인한다.
     * @return true: 사용 가능, false: 이미 사용 중
     */
    boolean isNicknameAvailable(String nickname);
}
