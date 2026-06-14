package com.wanted.codebombalms.admin.permission.infrastructure.persistence;

import com.wanted.codebombalms.admin.permission.domain.model.AdminPermissionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminPermissionRepositoryAdapter 단위 테스트")
class AdminPermissionRepositoryAdapterTest {

    @Mock
    private SpringDataAdminPermissionRepository springDataRepository;

    @InjectMocks
    private AdminPermissionRepositoryAdapter adminPermissionRepositoryAdapter;

    @Test
    @DisplayName("동시 grant로 unique 충돌이 발생해도 권한 row가 존재하면 멱등 성공으로 처리한다.")
    void concurrent_grant_unique_conflict_is_idempotent_success() {
        // given
        given(springDataRepository.existsByAdminUserIdAndPermissionType(
                2L,
                AdminPermissionType.USER_MANAGEMENT
        )).willReturn(false, true);
        given(springDataRepository.saveAndFlush(any(AdminPermissionJpaEntity.class)))
                .willThrow(new DataIntegrityViolationException("duplicate permission"));

        // when & then
        assertDoesNotThrow(() -> adminPermissionRepositoryAdapter.grant(
                2L,
                AdminPermissionType.USER_MANAGEMENT,
                1L
        ));
        verify(springDataRepository).saveAndFlush(any(AdminPermissionJpaEntity.class));
    }
}
