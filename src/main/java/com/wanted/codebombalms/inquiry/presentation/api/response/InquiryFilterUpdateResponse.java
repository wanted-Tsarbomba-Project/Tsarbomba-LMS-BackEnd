package com.wanted.codebombalms.inquiry.presentation.api.response;

import com.wanted.codebombalms.inquiry.domain.model.Inquiry;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
// 필터링 처리/복구 결과로 바뀐 상태만 응답에 담는다.
public class InquiryFilterUpdateResponse {

    private Long inquiryId;
    private boolean filtered;

    public static InquiryFilterUpdateResponse from(Inquiry inquiry) {
        return new InquiryFilterUpdateResponse(
                inquiry.getInquiryId(),
                inquiry.isFiltered()
        );
    }
}
