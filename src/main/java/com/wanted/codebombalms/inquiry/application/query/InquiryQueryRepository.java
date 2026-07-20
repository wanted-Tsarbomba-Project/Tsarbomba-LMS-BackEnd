package com.wanted.codebombalms.inquiry.application.query;

import com.wanted.codebombalms.admin.operation.common.application.PageResult;

import java.util.Optional;

public interface InquiryQueryRepository {

    PageResult<AdminInquiryListItem> findAdminInquiries(GetAdminInquiriesQuery query);

    Optional<AdminInquiryDetail> findAdminInquiryDetail(Long inquiryId);
}
