package com.wanted.codebombalms.inquiry.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.inquiry.application.command.ReplyInquiryCommand;
import com.wanted.codebombalms.inquiry.application.command.UpdateInquiryClassificationCommand;
import com.wanted.codebombalms.inquiry.application.command.UpdateInquiryFilterCommand;
import com.wanted.codebombalms.inquiry.application.usecase.ReplyInquiryUseCase;
import com.wanted.codebombalms.inquiry.application.usecase.UpdateInquiryClassificationUseCase;
import com.wanted.codebombalms.inquiry.application.usecase.UpdateInquiryFilterUseCase;
import com.wanted.codebombalms.inquiry.domain.exception.InquiryErrorCode;
import com.wanted.codebombalms.inquiry.domain.model.Inquiry;
import com.wanted.codebombalms.inquiry.domain.model.InquiryAiCorrection;
import com.wanted.codebombalms.inquiry.domain.model.InquiryCorrectionField;
import com.wanted.codebombalms.inquiry.domain.repository.InquiryAiCorrectionRepository;
import com.wanted.codebombalms.inquiry.domain.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminInquiryCommandService implements
        UpdateInquiryClassificationUseCase,
        UpdateInquiryFilterUseCase,
        ReplyInquiryUseCase {

    private final InquiryRepository inquiryRepository;
    private final InquiryAiCorrectionRepository inquiryAiCorrectionRepository;

    @Override
    // 문의를 조회해 도메인/심각도를 수정하고, 값이 바뀐 필드만 보정 이력에 upsert한다.
    public Inquiry updateClassification(UpdateInquiryClassificationCommand command) {
        validateClassificationCommand(command);

        Inquiry inquiry = findInquiry(command.inquiryId());
        String previousDomain = inquiry.getDomain().name();
        String previousSeverity = inquiry.getSeverity().name();

        inquiry.updateClassification(command.domain(), command.severity(), LocalDateTime.now());
        Inquiry saved = inquiryRepository.save(inquiry);

        upsertCorrectionIfChanged(
                saved.getInquiryId(), command.adminId(), InquiryCorrectionField.DOMAIN,
                previousDomain, command.domain().name(), command.reason()
        );
        upsertCorrectionIfChanged(
                saved.getInquiryId(), command.adminId(), InquiryCorrectionField.SEVERITY,
                previousSeverity, command.severity().name(), command.reason()
        );

        return saved;
    }

    @Override
    // 문의를 조회해 필터링 상태를 바꾸고, 값이 바뀌었으면 보정 이력에 upsert한다.
    public Inquiry updateFilter(UpdateInquiryFilterCommand command) {
        validateFilterCommand(command);

        Inquiry inquiry = findInquiry(command.inquiryId());
        String previousFiltered = String.valueOf(inquiry.isFiltered());

        inquiry.updateFilter(command.filtered(), LocalDateTime.now());
        Inquiry saved = inquiryRepository.save(inquiry);

        upsertCorrectionIfChanged(
                saved.getInquiryId(), command.adminId(), InquiryCorrectionField.IS_FILTERED,
                previousFiltered, String.valueOf(command.filtered()), command.reason()
        );

        return saved;
    }

    @Override
    // 문의를 조회해 관리자 답변을 등록한다. 상태/노출 여부 변경은 도메인 모델이 함께 처리한다.
    public Inquiry reply(ReplyInquiryCommand command) {
        Inquiry inquiry = findInquiry(command.inquiryId());

        inquiry.reply(command.content(), command.adminId(), LocalDateTime.now());

        return inquiryRepository.save(inquiry);
    }

    private Inquiry findInquiry(Long inquiryId) {
        return inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new NotFoundException(InquiryErrorCode.INQUIRY_NOT_FOUND));
    }

    // AI 값과 관리자 값이 다를 때만 inquiry_id + field_name 기준으로 마지막 보정값을 upsert한다.
    private void upsertCorrectionIfChanged(
            Long inquiryId,
            Long adminId,
            InquiryCorrectionField fieldName,
            String aiValue,
            String correctedValue,
            String reason
    ) {
        if (aiValue.equals(correctedValue)) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        InquiryAiCorrection correction = inquiryAiCorrectionRepository.findByInquiryIdAndFieldName(inquiryId, fieldName)
                .map(existing -> {
                    existing.updateCorrection(correctedValue, reason, adminId, now);
                    return existing;
                })
                .orElseGet(() -> InquiryAiCorrection.create(
                        inquiryId, adminId, fieldName, aiValue, correctedValue, reason, now
                ));

        inquiryAiCorrectionRepository.save(correction);
    }

    private void validateClassificationCommand(UpdateInquiryClassificationCommand command) {
        if (command.domain() == null || command.severity() == null || isBlank(command.reason())) {
            throw new ValidationException(InquiryErrorCode.INVALID_CLASSIFICATION_REQUEST);
        }
    }

    private void validateFilterCommand(UpdateInquiryFilterCommand command) {
        if (command.filtered() == null || isBlank(command.reason())) {
            throw new ValidationException(InquiryErrorCode.INVALID_FILTER_REQUEST);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
