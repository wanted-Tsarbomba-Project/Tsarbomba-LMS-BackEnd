package com.wanted.codebombalms.inquiry.infrastructure.persistence;

import com.wanted.codebombalms.inquiry.domain.model.InquiryAiCorrection;
import com.wanted.codebombalms.inquiry.domain.model.InquiryCorrectionField;
import com.wanted.codebombalms.inquiry.domain.repository.InquiryAiCorrectionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InquiryAiCorrectionRepositoryAdapter implements InquiryAiCorrectionRepository {

    private final SpringDataInquiryAiCorrectionRepository springDataRepository;

    @Override
    public Optional<InquiryAiCorrection> findByInquiryIdAndFieldName(Long inquiryId, InquiryCorrectionField fieldName) {
        return springDataRepository.findByInquiryIdAndFieldName(inquiryId, fieldName)
                .map(InquiryAiCorrectionMapper::toDomain);
    }

    @Override
    public InquiryAiCorrection save(InquiryAiCorrection correction) {
        InquiryAiCorrectionJpaEntity saved = springDataRepository.save(InquiryAiCorrectionMapper.toEntity(correction));

        return InquiryAiCorrectionMapper.toDomain(saved);
    }
}
