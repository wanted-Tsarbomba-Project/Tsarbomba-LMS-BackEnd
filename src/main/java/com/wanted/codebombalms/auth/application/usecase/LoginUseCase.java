package com.wanted.codebombalms.auth.application.usecase;

import com.wanted.codebombalms.auth.application.command.LoginCommand;
import com.wanted.codebombalms.auth.application.dto.TokenPair;

public interface LoginUseCase {

    TokenPair login(LoginCommand command);
}