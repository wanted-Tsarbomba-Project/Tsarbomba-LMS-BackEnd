package com.wanted.codebombalms.inquiry.domain.model;

import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.inquiry.domain.exception.InquiryErrorCode;

import java.time.LocalDateTime;

// 사용자 문의와 AI 분석/관리자 처리 결과를 함께 갖는 도메인 모델
public class Inquiry {

    private final Long inquiryId;
    private final Long userId;
    private String title;
    private final String content;
    private InquiryStatus status;
    private InquirySeverity severity;
    private InquiryDomain domain;
    private final String sourceUrl;
    private String estimatedUrl;
    private String adminReply;
    private Long repliedBy;
    private LocalDateTime repliedAt;
    private String aiSummary;
    private String aiRecommendedAction;
    private boolean filtered;
    private boolean replyVisible;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Inquiry(
            Long inquiryId,
            Long userId,
            String title,
            String content,
            InquiryStatus status,
            InquirySeverity severity,
            InquiryDomain domain,
            String sourceUrl,
            String estimatedUrl,
            String adminReply,
            Long repliedBy,
            LocalDateTime repliedAt,
            String aiSummary,
            String aiRecommendedAction,
            boolean filtered,
            boolean replyVisible,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.inquiryId = inquiryId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.status = status;
        this.severity = severity;
        this.domain = domain;
        this.sourceUrl = sourceUrl;
        this.estimatedUrl = estimatedUrl;
        this.adminReply = adminReply;
        this.repliedBy = repliedBy;
        this.repliedAt = repliedAt;
        this.aiSummary = aiSummary;
        this.aiRecommendedAction = aiRecommendedAction;
        this.filtered = filtered;
        this.replyVisible = replyVisible;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // DB에서 조회한 값으로 도메인 모델을 복원한다.
    public static Inquiry restore(
            Long inquiryId,
            Long userId,
            String title,
            String content,
            InquiryStatus status,
            InquirySeverity severity,
            InquiryDomain domain,
            String sourceUrl,
            String estimatedUrl,
            String adminReply,
            Long repliedBy,
            LocalDateTime repliedAt,
            String aiSummary,
            String aiRecommendedAction,
            boolean filtered,
            boolean replyVisible,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new Inquiry(
                inquiryId,
                userId,
                title,
                content,
                status,
                severity,
                domain,
                sourceUrl,
                estimatedUrl,
                adminReply,
                repliedBy,
                repliedAt,
                aiSummary,
                aiRecommendedAction,
                filtered,
                replyVisible,
                createdAt,
                updatedAt
        );
    }

    // 관리자가 AI 분류(도메인/심각도)를 보정한다.
    public void updateClassification(InquiryDomain domain, InquirySeverity severity, LocalDateTime updatedAt) {
        this.domain = domain;
        this.severity = severity;
        this.updatedAt = updatedAt;
    }

    // 관리자가 필터링 처리 또는 복구를 수행한다.
    public void updateFilter(boolean filtered, LocalDateTime updatedAt) {
        this.filtered = filtered;
        this.updatedAt = updatedAt;
    }

    // 관리자 답변을 등록하고 상태를 답변 완료로 변경한다.
    public void reply(String content, Long adminId, LocalDateTime repliedAt) {
        if (content == null || content.isBlank()) {
            throw new ValidationException(InquiryErrorCode.INVALID_REPLY_REQUEST);
        }

        this.adminReply = content;
        this.repliedBy = adminId;
        this.repliedAt = repliedAt;
        this.status = InquiryStatus.ANSWERED;
        this.replyVisible = true;
        this.updatedAt = repliedAt;
    }

    public Long getInquiryId() {
        return inquiryId;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public InquiryStatus getStatus() {
        return status;
    }

    public InquirySeverity getSeverity() {
        return severity;
    }

    public InquiryDomain getDomain() {
        return domain;
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public String getEstimatedUrl() {
        return estimatedUrl;
    }

    public String getAdminReply() {
        return adminReply;
    }

    public Long getRepliedBy() {
        return repliedBy;
    }

    public LocalDateTime getRepliedAt() {
        return repliedAt;
    }

    public String getAiSummary() {
        return aiSummary;
    }

    public String getAiRecommendedAction() {
        return aiRecommendedAction;
    }

    public boolean isFiltered() {
        return filtered;
    }

    public boolean isReplyVisible() {
        return replyVisible;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
