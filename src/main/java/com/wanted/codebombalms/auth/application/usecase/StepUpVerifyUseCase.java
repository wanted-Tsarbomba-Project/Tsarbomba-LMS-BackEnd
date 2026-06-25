package com.wanted.codebombalms.auth.application.usecase;

import com.wanted.codebombalms.auth.application.command.StepUpVerifyCommand;
import com.wanted.codebombalms.auth.application.dto.LoginResult;
import jakarta.servlet.http.HttpServletRequest;

public interface StepUpVerifyUseCase {

    LoginResult verify(StepUpVerifyCommand command, HttpServletRequest request);
}
