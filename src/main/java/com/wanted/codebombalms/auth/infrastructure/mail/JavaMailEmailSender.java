package com.wanted.codebombalms.auth.infrastructure.mail;

import com.wanted.codebombalms.auth.domain.service.EmailSender;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JavaMailEmailSender implements EmailSender {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromAddress;

    @Override
    public void sendVerificationCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("[Code-Bomba LMS] 이메일 인증 코드");
        message.setText(buildBody(code));

        mailSender.send(message);
        log.info("[EmailSender] 인증 코드 발송 완료 - to: {}", to);
    }

    private String buildBody(String code) {
        return """
                Code-Bomba LMS 이메일 인증 코드입니다.

                인증 코드: %s

                코드 유효 시간: 3분
                본 메일을 요청하지 않으셨다면 무시해주세요.
                """.formatted(code);
    }
}