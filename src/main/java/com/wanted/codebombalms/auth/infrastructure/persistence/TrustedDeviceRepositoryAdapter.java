package com.wanted.codebombalms.auth.infrastructure.persistence;

import com.wanted.codebombalms.auth.domain.model.TrustedDevice;
import com.wanted.codebombalms.auth.domain.repository.TrustedDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class TrustedDeviceRepositoryAdapter implements TrustedDeviceRepository {

    private final SpringDataTrustedDeviceRepository springDataTrustedDeviceRepository;

    @Override
    public TrustedDevice save(TrustedDevice device) {
        TrustedDeviceJpaEntity entity = TrustedDeviceJpaEntity.from(device);
        return springDataTrustedDeviceRepository.save(entity).toDomain();
    }

    @Override
    public Optional<TrustedDevice> findByUserIdAndDeviceFp(Long userId, String deviceFp) {
        return springDataTrustedDeviceRepository.findByUserIdAndDeviceFp(userId, deviceFp)
                .map(TrustedDeviceJpaEntity::toDomain);
    }

    @Override
    public List<TrustedDevice> findAllByUserId(Long userId) {
        return springDataTrustedDeviceRepository.findAllByUserId(userId).stream()
                .map(TrustedDeviceJpaEntity::toDomain)
                .toList();
    }

    @Override
    public Optional<TrustedDevice> findByTrustedDeviceIdAndUserId(Long trustedDeviceId, Long userId) {
        return springDataTrustedDeviceRepository.findByTrustedDeviceIdAndUserId(trustedDeviceId, userId)
                .map(TrustedDeviceJpaEntity::toDomain);
    }

    @Override
    public void deleteById(Long trustedDeviceId) {
        springDataTrustedDeviceRepository.deleteById(trustedDeviceId);
    }
}
