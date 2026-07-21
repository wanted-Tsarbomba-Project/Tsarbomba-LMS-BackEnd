package com.wanted.codebombalms.inquiry.presentation.api.response;

import com.wanted.codebombalms.inquiry.application.query.ActiveInquiryReply;

import java.util.List;

// 미확인 답변이 없으면 replies는 빈 배열로 응답한다.
public record ActiveInquiryReplyListResponse(
        List<ActiveInquiryReplyResponse> replies
) {

    public static ActiveInquiryReplyListResponse from(List<ActiveInquiryReply> replies) {
        return new ActiveInquiryReplyListResponse(
                replies.stream()
                        .map(ActiveInquiryReplyResponse::from)
                        .toList()
        );
    }
}
