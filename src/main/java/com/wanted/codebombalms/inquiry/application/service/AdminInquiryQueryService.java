package com.wanted.codebombalms.inquiry.application.service;

import com.wanted.codebombalms.admin.operation.common.application.PageResult;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.inquiry.application.query.AdminInquiryDetail;
import com.wanted.codebombalms.inquiry.application.query.AdminInquiryListItem;
import com.wanted.codebombalms.inquiry.application.query.GetAdminInquiriesQuery;
import com.wanted.codebombalms.inquiry.application.query.InquiryQueryRepository;
import com.wanted.codebombalms.inquiry.application.usecase.GetAdminInquiriesUseCase;
import com.wanted.codebombalms.inquiry.application.usecase.GetAdminInquiryDetailUseCase;
import com.wanted.codebombalms.inquiry.domain.exception.InquiryErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInquiryQueryService implements GetAdminInquiriesUseCase, GetAdminInquiryDetailUseCase {

    private final InquiryQueryRepository inquiryQueryRepository;

    @Override
    // isFiltered/domain/severity/status 조건으로 관리자 문의 목록을 페이지 조회한다.
    public PageResult<AdminInquiryListItem> getInquiries(GetAdminInquiriesQuery query) {
        validatePage(query.page(), query.size());

        return inquiryQueryRepository.findAdminInquiries(query);
    }

    @Override
    // 문의 ID로 상세 정보를 조회하고 없으면 예외를 던진다.
    public AdminInquiryDetail getInquiryDetail(Long inquiryId) {
        return inquiryQueryRepository.findAdminInquiryDetail(inquiryId)
                .orElseThrow(() -> new NotFoundException(InquiryErrorCode.INQUIRY_NOT_FOUND));
    }

    private void validatePage(int page, int size) {
        if (page < 0 || size <= 0 || size > 100) {
            throw new ValidationException(InquiryErrorCode.INVALID_PAGE_REQUEST);
        }
    }
}
