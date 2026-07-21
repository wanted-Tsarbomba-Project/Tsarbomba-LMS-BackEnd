package com.wanted.codebombalms.inquiry.domain.model;

import java.time.LocalDateTime;

// AI 최초 분석값과 관리자 최종 보정값을 inquiry_id + field_name 기준으로 하나만 유지하는 도메인 모델
public class InquiryAiCorrection {

    private final Long correctionId;
    private final Long inquiryId;
    private Long adminId;
    private final InquiryCorrectionField fieldName;
    private final String aiValue;
    private String correctedValue;
    private String reason;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private InquiryAiCorrection(
            Long correctionId,
            Long inquiryId,
            Long adminId,
            InquiryCorrectionField fieldName,
            String aiValue,
            String correctedValue,
            String reason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.correctionId = correctionId;
        this.inquiryId = inquiryId;
        this.adminId = adminId;
        this.fieldName = fieldName;
        this.aiValue = aiValue;
        this.correctedValue = correctedValue;
        this.reason = reason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // 해당 필드의 첫 보정을 생성한다.
    public static InquiryAiCorrection create(
            Long inquiryId,
            Long adminId,
            InquiryCorrectionField fieldName,
            String aiValue,
            String correctedValue,
            String reason,
            LocalDateTime now
    ) {
        return new InquiryAiCorrection(
                null,
                inquiryId,
                adminId,
                fieldName,
                aiValue,
                correctedValue,
                reason,
                now,
                now
        );
    }

    // DB에서 조회한 값으로 도메인 모델을 복원한다.
    public static InquiryAiCorrection restore(
            Long correctionId,
            Long inquiryId,
            Long adminId,
            InquiryCorrectionField fieldName,
            String aiValue,
            String correctedValue,
            String reason,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        return new InquiryAiCorrection(
                correctionId,
                inquiryId,
                adminId,
                fieldName,
                aiValue,
                correctedValue,
                reason,
                createdAt,
                updatedAt
        );
    }

    // 같은 필드의 재보정. ai_value는 최초값을 유지하고 최종 보정값만 갱신한다.
    public void updateCorrection(String correctedValue, String reason, Long adminId, LocalDateTime updatedAt) {
        this.correctedValue = correctedValue;
        this.reason = reason;
        this.adminId = adminId;
        this.updatedAt = updatedAt;
    }

    public Long getCorrectionId() {
        return correctionId;
    }

    public Long getInquiryId() {
        return inquiryId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public InquiryCorrectionField getFieldName() {
        return fieldName;
    }

    public String getAiValue() {
        return aiValue;
    }

    public String getCorrectedValue() {
        return correctedValue;
    }

    public String getReason() {
        return reason;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
