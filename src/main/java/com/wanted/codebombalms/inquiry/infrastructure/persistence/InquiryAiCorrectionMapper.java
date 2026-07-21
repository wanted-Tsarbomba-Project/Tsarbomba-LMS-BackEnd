package com.wanted.codebombalms.inquiry.infrastructure.persistence;

import com.wanted.codebombalms.inquiry.domain.model.InquiryAiCorrection;

public class InquiryAiCorrectionMapper {

    private InquiryAiCorrectionMapper() {
    }

    public static InquiryAiCorrection toDomain(InquiryAiCorrectionJpaEntity entity) {
        return InquiryAiCorrection.restore(
                entity.getCorrectionId(),
                entity.getInquiryId(),
                entity.getAdminId(),
                entity.getFieldName(),
                entity.getAiValue(),
                entity.getCorrectedValue(),
                entity.getReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static InquiryAiCorrectionJpaEntity toEntity(InquiryAiCorrection domain) {
        return new InquiryAiCorrectionJpaEntity(
                domain.getCorrectionId(),
                domain.getInquiryId(),
                domain.getAdminId(),
                domain.getFieldName(),
                domain.getAiValue(),
                domain.getCorrectedValue(),
                domain.getReason(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
