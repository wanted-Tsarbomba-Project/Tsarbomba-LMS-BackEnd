package com.wanted.codebombalms.inquiry.infrastructure.persistence;

import com.wanted.codebombalms.inquiry.domain.model.InquiryCorrectionField;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SpringDataInquiryAiCorrectionRepository extends JpaRepository<InquiryAiCorrectionJpaEntity, Long> {

    Optional<InquiryAiCorrectionJpaEntity> findByInquiryIdAndFieldName(Long inquiryId, InquiryCorrectionField fieldName);

    // updatedAt만으로는 동시각 row 간 순서가 안정적이지 않아, 고유 식별자(correctionId)를 2차 정렬 기준으로 둔다.
    List<InquiryAiCorrectionJpaEntity> findAllByOrderByUpdatedAtDescCorrectionIdDesc(Pageable pageable);
}
