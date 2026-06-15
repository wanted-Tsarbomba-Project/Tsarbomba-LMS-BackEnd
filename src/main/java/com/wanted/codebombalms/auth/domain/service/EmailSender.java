package com.wanted.codebombalms.auth.domain.service;

public interface EmailSender {

    void sendVerificationCode(String to, String code);
    
    void sendPasswordResetCode(String to, String code);
}
