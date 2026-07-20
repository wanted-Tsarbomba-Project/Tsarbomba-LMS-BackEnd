package com.wanted.codebombalms.inquiry.domain.model;

// 문의가 속한 서비스 도메인
public enum InquiryDomain {
    ADMIN,
    AUTH,
    BADGE,
    CHATBOT,
    COURSE,
    ENROLLMENT,
    PROBLEMS,
    RANKING,
    RECOMMENDATION,
    USER,
    LECTURE,
    LEARNING,
    ETC;

    // Swagger 문서용 값별 의미 설명. @Schema/@Parameter description에서 재사용한다.
    public static final String SCHEMA_DESCRIPTION =
            "문의 도메인 (ADMIN: 관리자/운영, AUTH: 로그인/인증, BADGE: 뱃지, CHATBOT: 챗봇, "
                    + "COURSE: 강의, ENROLLMENT: 수강신청/수강관리, PROBLEMS: 문제/채점/제출, RANKING: 랭킹, "
                    + "RECOMMENDATION: 추천, USER: 회원/프로필, LECTURE: 강의 영상/자료, LEARNING: 학습 진행, ETC: 기타)";
}
