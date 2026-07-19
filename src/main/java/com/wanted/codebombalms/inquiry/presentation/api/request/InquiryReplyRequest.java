package com.wanted.codebombalms.inquiry.presentation.api.request;

import com.wanted.codebombalms.inquiry.application.command.ReplyInquiryCommand;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
// 관리자 문의 답변 등록 요청을 받는다.
public class InquiryReplyRequest {

    private String content;

    // path variable의 문의 ID와 인증된 관리자 ID를 더해 command로 변환한다.
    public ReplyInquiryCommand toCommand(Long inquiryId, Long adminId) {
        return new ReplyInquiryCommand(inquiryId, adminId, content);
    }
}
