package com.wanted.codebombalms.inquiry.application.usecase;

import com.wanted.codebombalms.admin.operation.common.application.PageResult;
import com.wanted.codebombalms.inquiry.application.query.AdminInquiryListItem;
import com.wanted.codebombalms.inquiry.application.query.GetAdminInquiriesQuery;

public interface GetAdminInquiriesUseCase {

    PageResult<AdminInquiryListItem> getInquiries(GetAdminInquiriesQuery query);
}
