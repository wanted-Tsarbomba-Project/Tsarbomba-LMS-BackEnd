package com.wanted.codebombalms.auth.application.usecase;

import com.wanted.codebombalms.auth.application.command.LoginCommand;
import com.wanted.codebombalms.auth.application.dto.TokenPair;
import jakarta.servlet.http.HttpServletRequest;

public interface LoginUseCase {

    TokenPair login(LoginCommand command, HttpServletRequest request);
}