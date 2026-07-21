package com.wanted.codebombalms.inquiry.presentation.api;

public class InquiryResponseMessage {

    private InquiryResponseMessage() {}

    public static final String RETRIEVED               = "문의 조회에 성공했습니다.";
    public static final String CLASSIFICATION_UPDATED   = "문의 분류가 수정되었습니다.";
    public static final String FILTER_UPDATED           = "문의 필터링 상태가 변경되었습니다.";
    public static final String REPLIED                  = "문의 답변이 등록되었습니다.";
    public static final String ACTIVE_REPLIES_RETRIEVED = "미확인 문의 답변을 조회했습니다.";
    public static final String REPLY_VISIBILITY_UPDATED = "문의 답변 노출 상태가 변경되었습니다.";
    public static final String CREATED                  = "문의가 접수되었습니다.";
}
