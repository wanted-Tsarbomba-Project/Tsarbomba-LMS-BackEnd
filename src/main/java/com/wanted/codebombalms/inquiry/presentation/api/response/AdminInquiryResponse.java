package com.wanted.codebombalms.inquiry.presentation.api.response;

import com.wanted.codebombalms.inquiry.application.query.AdminInquiryListItem;
import com.wanted.codebombalms.inquiry.domain.model.InquiryDomain;
import com.wanted.codebombalms.inquiry.domain.model.InquirySeverity;
import com.wanted.codebombalms.inquiry.domain.model.InquiryStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
// 목록 한 행을 AI 요약 기준으로만 응답에 담는다.
public class AdminInquiryResponse {

    private Long inquiryId;
    private String title;
    private String summary;
    private InquiryDomain domain;
    private InquirySeverity severity;
    private InquiryStatus status;
    private boolean filtered;
    private LocalDateTime createdAt;

    public static AdminInquiryResponse from(AdminInquiryListItem item) {
        return new AdminInquiryResponse(
                item.inquiryId(),
                item.title(),
                item.summary(),
                item.domain(),
                item.severity(),
                item.status(),
                item.filtered(),
                item.createdAt()
        );
    }
}
