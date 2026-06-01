package com.wanted.codebombalms.auth.application.usecase;

import com.wanted.codebombalms.auth.application.command.SignupCommand;

public interface SignupUseCase {
    Long signup(SignupCommand command);

}
