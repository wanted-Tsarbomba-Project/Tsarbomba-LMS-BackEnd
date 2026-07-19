package com.wanted.codebombalms.inquiry.infrastructure.persistence;

import com.wanted.codebombalms.inquiry.domain.model.Inquiry;

public class InquiryMapper {

    private InquiryMapper() {
    }

    public static Inquiry toDomain(InquiryJpaEntity entity) {
        return Inquiry.restore(
                entity.getInquiryId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getStatus(),
                entity.getSeverity(),
                entity.getDomain(),
                entity.getSourceUrl(),
                entity.getEstimatedUrl(),
                entity.getAdminReply(),
                entity.getRepliedBy(),
                entity.getRepliedAt(),
                entity.getAiSummary(),
                entity.getAiRecommendedAction(),
                entity.isFiltered(),
                entity.isReplyVisible(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static InquiryJpaEntity toEntity(Inquiry domain) {
        return new InquiryJpaEntity(
                domain.getInquiryId(),
                domain.getUserId(),
                domain.getTitle(),
                domain.getContent(),
                domain.getStatus(),
                domain.getSeverity(),
                domain.getDomain(),
                domain.getSourceUrl(),
                domain.getEstimatedUrl(),
                domain.getAdminReply(),
                domain.getRepliedBy(),
                domain.getRepliedAt(),
                domain.getAiSummary(),
                domain.getAiRecommendedAction(),
                domain.isFiltered(),
                domain.isReplyVisible(),
                domain.getCreatedAt(),
                domain.getUpdatedAt()
        );
    }
}
