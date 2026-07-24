package com.wanted.codebombalms.inquiry.application.usecase;

import com.wanted.codebombalms.inquiry.application.query.ActiveInquiryReply;

import java.util.List;

public interface GetActiveInquiryRepliesUseCase {

    List<ActiveInquiryReply> getActiveReplies(Long userId);
}
