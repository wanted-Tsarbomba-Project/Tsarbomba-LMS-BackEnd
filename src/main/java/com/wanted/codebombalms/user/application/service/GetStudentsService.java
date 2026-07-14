package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.user.application.query.StudentPageResult;
import com.wanted.codebombalms.user.application.query.StudentSummary;
import com.wanted.codebombalms.user.application.usecase.GetStudentsUseCase;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.domain.repository.UserPage;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import com.wanted.codebombalms.user.infrastructure.metrics.UserMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStudentsService implements GetStudentsUseCase {

    private final UserRepository userRepository;
    private final UserMetrics userMetrics;

    @Override
    public StudentPageResult getStudents(int page, int size, String keyword) {
        long startedAt = System.nanoTime();

        // 1. 학생 목록 + 전체 건수 조회 (가입 최신순, keyword 있으면 이름 중간 매칭)
        UserPage userPage = userRepository.findAllByRoleAndKeyword(UserRole.STUDENT, keyword, page, size);

        long elapsedNanos = System.nanoTime() - startedAt;
        userMetrics.recordStudentListQuery(elapsedNanos);
        log.info("event=user_student_list_queried page={} size={} hasKeyword={} resultCount={} durationMs={}",
                page, size, keyword != null && !keyword.isBlank(), userPage.content().size(), elapsedNanos / 1_000_000);

        // 2. 전체 페이지 수 (size = 0 방어)
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) userPage.totalElements() / size);

        // 3. 변환
        List<StudentSummary> content = userPage.content().stream()
                .map(StudentSummary::from)
                .toList();

        return new StudentPageResult(content, userPage.totalElements(), totalPages);
    }
}
