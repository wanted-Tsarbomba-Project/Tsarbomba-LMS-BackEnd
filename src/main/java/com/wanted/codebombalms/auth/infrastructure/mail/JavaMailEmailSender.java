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
        log.info("[EmailSender] 인증 코드 발송 완료 - to: {}", maskEmail(to));
    }

    @Override
    public void sendPasswordResetCode(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("[Code-Bomba LMS] 비밀번호 재설정 코드");
        message.setText(buildResetBody(code));

        mailSender.send(message);
        log.info("[EmailSender] 비밀번호 재설정 코드 발송 완료 - to: {}", maskEmail(to));
    }

    // sendPasswordResetCode 메서드 아래에 추가
    @Override
    public void sendStepUpCode(String to, String code, String lockUrl) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject("[Code-Bomba LMS] 새로운 기기 로그인 — 추가 인증 코드");
        message.setText(buildStepUpBody(code, lockUrl));

        mailSender.send(message);
        log.info("[EmailSender] step-up 코드 발송 완료 - to: {}", maskEmail(to));
    }

    private String buildStepUpBody(String code, String lockUrl) {
        return """
                평소와 다른 기기 또는 위치에서 로그인이 시도되었습니다.

                본인이 맞다면 아래 인증 코드를 입력해주세요.
                인증 코드: %s
                (이 코드는 5분간 유효합니다.)

                본인이 시도한 로그인이 아니라면, 아래 링크를 눌러 계정을 즉시 잠그세요.
                %s
                """.formatted(code, lockUrl);
    }

    /** 로그에 이메일 평문(PII) 노출 방지 — 앞 1글자 + *** + 도메인만 남긴다. (예: j***@gmail.com) */
    private String maskEmail(String email) {
        if (email == null) {
            return "null";
        }
        int at = email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }

    private String buildBody(String code) {
        return """
                Code-Bomba LMS 이메일 인증 코드입니다.

                인증 코드: %s

                코드 유효 시간: 3분
                본 메일을 요청하지 않으셨다면 무시해주세요.
                """.formatted(code);
    }

    private String buildResetBody(String code) {
        return """
                Code-Bomba LMS 비밀번호 재설정 코드입니다.

                재설정 코드: %s

                코드 유효 시간: 10분
                본인이 요청하지 않으셨다면 즉시 비밀번호를 변경해주세요.
                """.formatted(code);
    }
}
