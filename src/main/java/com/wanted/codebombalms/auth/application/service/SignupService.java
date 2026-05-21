package com.wanted.codebombalms.auth.application.service;

import com.wanted.codebombalms.auth.application.command.SignupCommand;
import com.wanted.codebombalms.auth.application.usecase.SignupUseCase;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.user.domain.exception.UserErrorCode;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SignupService implements SignupUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Long signup(SignupCommand command) {

        if (userRepository.existsByEmail(command.email())) {
            throw new ConflictException(UserErrorCode.USER_EMAIL_DUPLICATED);
        }

        if (userRepository.existsByNickname(command.nickname())) {
            throw new ConflictException(UserErrorCode.USER_NICKNAME_DUPLICATED);
        }

        String encodedPassword = passwordEncoder.encode(command.rawpassword());

        User user = User.createLocalUser(
                command.email(),
                encodedPassword,
                command.name(),
                command.nickname(),
                command.phone()
        );

        User saved = userRepository.save(user);

        return saved.getUserId();
    }
}