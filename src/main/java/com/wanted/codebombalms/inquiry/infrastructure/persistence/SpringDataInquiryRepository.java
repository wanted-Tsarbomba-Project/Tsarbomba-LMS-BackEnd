package com.wanted.codebombalms.inquiry.infrastructure.persistence;

import com.wanted.codebombalms.inquiry.domain.model.InquiryDomain;
import com.wanted.codebombalms.inquiry.domain.model.InquirySeverity;
import com.wanted.codebombalms.inquiry.domain.model.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SpringDataInquiryRepository extends JpaRepository<InquiryJpaEntity, Long> {

    // isFiltered는 항상 조건으로 걸고, domain/severity/status는 값이 있을 때만 조건에 추가한다.
    @Query(
            value = """
                    select new com.wanted.codebombalms.inquiry.infrastructure.persistence.InquiryListProjection(
                        i.inquiryId, i.title, i.aiSummary, i.domain, i.severity, i.status, i.filtered, i.createdAt
                    )
                    from InquiryJpaEntity i
                    where i.filtered = :isFiltered
                      and (:domain is null or i.domain = :domain)
                      and (:severity is null or i.severity = :severity)
                      and (:status is null or i.status = :status)
                    """,
            countQuery = """
                    select count(i)
                    from InquiryJpaEntity i
                    where i.filtered = :isFiltered
                      and (:domain is null or i.domain = :domain)
                      and (:severity is null or i.severity = :severity)
                      and (:status is null or i.status = :status)
                    """
    )
    Page<InquiryListProjection> findAdminInquiries(
            @Param("isFiltered") boolean isFiltered,
            @Param("domain") InquiryDomain domain,
            @Param("severity") InquirySeverity severity,
            @Param("status") InquiryStatus status,
            Pageable pageable
    );
}
