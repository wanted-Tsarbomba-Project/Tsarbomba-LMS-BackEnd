package com.wanted.codebombalms.inquiry.domain.repository;

import com.wanted.codebombalms.inquiry.domain.model.Inquiry;

import java.util.Optional;

public interface InquiryRepository {

    Optional<Inquiry> findById(Long inquiryId);

    Optional<Inquiry> findByIdAndUserId(Long inquiryId, Long userId);

    Inquiry save(Inquiry inquiry);
}
