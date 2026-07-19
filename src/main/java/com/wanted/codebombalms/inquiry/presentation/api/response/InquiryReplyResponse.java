package com.wanted.codebombalms.inquiry.presentation.api.response;

import com.wanted.codebombalms.inquiry.domain.model.Inquiry;
import com.wanted.codebombalms.inquiry.domain.model.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
// 답변 등록 결과로 바뀐 상태와 답변 내용을 응답에 담는다.
public class InquiryReplyResponse {

    private Long inquiryId;
    private InquiryStatus status;
    private String adminReply;
    private Long repliedBy;
    private LocalDateTime repliedAt;

    public static InquiryReplyResponse from(Inquiry inquiry) {
        return new InquiryReplyResponse(
                inquiry.getInquiryId(),
                inquiry.getStatus(),
                inquiry.getAdminReply(),
                inquiry.getRepliedBy(),
                inquiry.getRepliedAt()
        );
    }
}
