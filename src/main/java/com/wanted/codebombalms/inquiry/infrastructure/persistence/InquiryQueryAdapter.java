package com.wanted.codebombalms.inquiry.infrastructure.persistence;

import com.wanted.codebombalms.admin.operation.common.application.PageResult;
import com.wanted.codebombalms.inquiry.application.query.ActiveInquiryReply;
import com.wanted.codebombalms.inquiry.application.query.AdminInquiryDetail;
import com.wanted.codebombalms.inquiry.application.query.AdminInquiryListItem;
import com.wanted.codebombalms.inquiry.application.query.GetAdminInquiriesQuery;
import com.wanted.codebombalms.inquiry.application.query.InquiryQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InquiryQueryAdapter implements InquiryQueryRepository {

    private final SpringDataInquiryRepository springDataRepository;

    @Override
    // 최신 문의가 먼저 오도록 정렬해 관리자 목록을 페이지 조회한다.
    public PageResult<AdminInquiryListItem> findAdminInquiries(GetAdminInquiriesQuery query) {
        PageRequest pageRequest = PageRequest.of(
                query.page(),
                query.size(),
                Sort.by(Sort.Direction.DESC, "createdAt")
                        .and(Sort.by(Sort.Direction.DESC, "inquiryId"))
        );

        Page<InquiryListProjection> result = springDataRepository.findAdminInquiries(
                query.isFiltered(),
                query.domain(),
                query.severity(),
                query.status(),
                pageRequest
        );

        List<AdminInquiryListItem> content = result.getContent().stream()
                .map(this::toListItem)
                .toList();

        return new PageResult<>(
                content,
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast(),
                result.hasNext(),
                result.hasPrevious()
        );
    }

    @Override
    // 조인이 필요 없는 단일 테이블 조회라 JPA 기본 findById 결과를 그대로 상세 응답으로 변환한다.
    public Optional<AdminInquiryDetail> findAdminInquiryDetail(Long inquiryId) {
        return springDataRepository.findById(inquiryId)
                .map(this::toDetail);
    }

    @Override
    // 로그인 첫 화면에 필요한 미확인 답변만 최신 답변순으로 조회한다.
    public List<ActiveInquiryReply> findActiveRepliesByUserId(Long userId) {
        return springDataRepository.findActiveRepliesByUserId(userId);
    }

    private AdminInquiryListItem toListItem(InquiryListProjection projection) {
        return new AdminInquiryListItem(
                projection.inquiryId(),
                projection.title(),
                projection.aiSummary(),
                projection.domain(),
                projection.severity(),
                projection.status(),
                projection.filtered(),
                projection.createdAt()
        );
    }

    private AdminInquiryDetail toDetail(InquiryJpaEntity entity) {
        return new AdminInquiryDetail(
                entity.getInquiryId(),
                entity.getUserId(),
                entity.getTitle(),
                entity.getContent(),
                entity.getSourceUrl(),
                entity.getEstimatedUrl(),
                entity.getAiSummary(),
                entity.getAiRecommendedAction(),
                entity.getDomain(),
                entity.getSeverity(),
                entity.getStatus(),
                entity.isFiltered(),
                entity.getAdminReply(),
                entity.getRepliedBy(),
                entity.getRepliedAt(),
                entity.getCreatedAt()
        );
    }
}
