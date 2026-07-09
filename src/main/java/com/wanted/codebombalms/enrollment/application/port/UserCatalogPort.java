package com.wanted.codebombalms.enrollment.application.port;

public interface UserCatalogPort {

    UserEnrollmentEligibility getEnrollmentEligibility(Long userId);

    record UserEnrollmentEligibility(
            boolean activeStudent,
            boolean locked
    ) {
    }
}
