package com.wanted.codebombalms.inquiry.infrastructure.persistence;

import com.wanted.codebombalms.inquiry.domain.model.InquiryCorrectionField;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataInquiryAiCorrectionRepository extends JpaRepository<InquiryAiCorrectionJpaEntity, Long> {

    Optional<InquiryAiCorrectionJpaEntity> findByInquiryIdAndFieldName(Long inquiryId, InquiryCorrectionField fieldName);

    List<InquiryAiCorrectionJpaEntity> findAllByOrderByUpdatedAtDesc(Pageable pageable);
}
