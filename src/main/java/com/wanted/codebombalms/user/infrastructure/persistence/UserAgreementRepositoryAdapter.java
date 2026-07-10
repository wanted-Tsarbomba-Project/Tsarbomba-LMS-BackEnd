package com.wanted.codebombalms.user.infrastructure.persistence;

import com.wanted.codebombalms.user.domain.model.UserAgreement;
import com.wanted.codebombalms.user.domain.repository.UserAgreementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class UserAgreementRepositoryAdapter implements UserAgreementRepository {

    private final SpringDataUserAgreementRepository springDataUserAgreementRepository;

    @Override
    public List<UserAgreement> saveAll(List<UserAgreement> agreements) {
        List<UserAgreementJpaEntity> entities = agreements.stream()
                .map(UserAgreementJpaEntity::from)
                .toList();

        return springDataUserAgreementRepository.saveAll(entities).stream()
                .map(UserAgreementJpaEntity::toDomain)
                .toList();
    }
}
