package com.wanted.codebombalms.domain.auth.controller;

import com.wanted.codebombalms.domain.auth.dto.request.SignupRequest;
import com.wanted.codebombalms.domain.auth.service.AuthService;
import com.wanted.codebombalms.global.common.ResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<ResponseDTO> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new ResponseDTO(HttpStatus.CREATED, "회원가입 성공", null));
    }
}