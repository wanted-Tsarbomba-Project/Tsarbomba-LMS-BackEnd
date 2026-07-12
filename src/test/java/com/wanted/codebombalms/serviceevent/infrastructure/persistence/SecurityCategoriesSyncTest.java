package com.wanted.codebombalms.serviceevent.infrastructure.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventCategory;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** SECURITY_CATEGORIES 상수(native query용) ↔ ServiceEventCategory.isSecurity() 동기화 검증 */
@DisplayName("보안 카테고리 상수 동기화 검증")
class SecurityCategoriesSyncTest {

    @Test
    void securityCategoriesConstant_matchesEnumDefinition() {
        Set<String> fromConstant = Arrays.stream(
                        SpringDataServiceEventRepository.SECURITY_CATEGORIES.split(","))
                .map(s -> s.trim().replace("'", ""))
                .collect(Collectors.toSet());

        Set<String> fromEnum = Arrays.stream(ServiceEventCategory.values())
                .filter(ServiceEventCategory::isSecurity)
                .map(ServiceEventCategory::code)
                .collect(Collectors.toSet());

        assertEquals(fromEnum, fromConstant,
                "SECURITY_CATEGORIES 상수가 ServiceEventCategory.isSecurity() 와 불일치 — 두 곳을 함께 갱신할 것");
    }
}
