package com.wanted.codebombalms.auth.application.usecase;

import com.wanted.codebombalms.auth.application.command.LoginCommand;
import com.wanted.codebombalms.auth.application.dto.LoginResult;
import jakarta.servlet.http.HttpServletRequest;

public interface LoginUseCase {

    LoginResult login(LoginCommand command, HttpServletRequest request, String deviceFp);
}
