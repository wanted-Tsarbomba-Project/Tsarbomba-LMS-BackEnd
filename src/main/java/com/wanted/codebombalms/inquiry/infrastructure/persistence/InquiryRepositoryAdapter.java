package com.wanted.codebombalms.inquiry.infrastructure.persistence;

import com.wanted.codebombalms.inquiry.domain.model.Inquiry;
import com.wanted.codebombalms.inquiry.domain.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class InquiryRepositoryAdapter implements InquiryRepository {

    private final SpringDataInquiryRepository springDataRepository;

    @Override
    public Optional<Inquiry> findById(Long inquiryId) {
        return springDataRepository.findById(inquiryId)
                .map(InquiryMapper::toDomain);
    }

    @Override
    public Optional<Inquiry> findByIdAndUserId(Long inquiryId, Long userId) {
        return springDataRepository.findByInquiryIdAndUserId(inquiryId, userId)
                .map(InquiryMapper::toDomain);
    }

    @Override
    public Inquiry save(Inquiry inquiry) {
        InquiryJpaEntity saved = springDataRepository.save(InquiryMapper.toEntity(inquiry));

        return InquiryMapper.toDomain(saved);
    }
}
