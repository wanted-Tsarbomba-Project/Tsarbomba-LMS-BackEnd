package com.wanted.codebombalms.global.presentation.api.common;

import java.time.Instant;

/* comment.
*   global 공통 응답 포멧이다.
*   해당 계층은 presentation (외부와의 소통) 계층에 속하며,
*   내부 계층이 Http 응답 구조에 대해 몰라도 되게 만드는 것이 목적이다.
*   Instant -> Java 시간 관련 클래스. 해당 클래스는 시간 뿐만 아니라
*   지역도 나오기 때문에 글로벌 서비스에서 많이 쓰인다.
*  */
public record ApiResponse<T>(
        Instant timestamp,
        int status,
        String code,
        String message,
        T data
) {

    public static <T> ApiResponse<T> success(String code, String message, T data) {
        return new ApiResponse<>(Instant.now(), 200, code, message, data);
    }

    // 생성과 일반 성공을 분리해두면 controller가 REST 의도를 더 명확히 표현할 수 있다.
    public static <T> ApiResponse<T> created(String code, String message, T data) {
        return new ApiResponse<>(Instant.now(), 201, code, message, data);
    }

    public static ApiResponse<Void> success(String code, String message) {
        return new ApiResponse<>(Instant.now(), 200, code, message, null);
    }
    

}
