package com.wanted.codebombalms.inquiry.infrastructure.persistence;

import com.wanted.codebombalms.inquiry.domain.model.InquiryCorrectionField;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "inquiry_ai_correction",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_inquiry_ai_correction_field",
                columnNames = {"inquiry_id", "field_name"}
        )
)
@Getter
@NoArgsConstructor
public class InquiryAiCorrectionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "correction_id")
    private Long correctionId;

    @Column(name = "inquiry_id", nullable = false)
    private Long inquiryId;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Enumerated(EnumType.STRING)
    @Column(name = "field_name", nullable = false, length = 50)
    private InquiryCorrectionField fieldName;

    @Column(name = "ai_value", length = 500)
    private String aiValue;

    @Column(name = "corrected_value", nullable = false, length = 500)
    private String correctedValue;

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public InquiryAiCorrectionJpaEntity(
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

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
