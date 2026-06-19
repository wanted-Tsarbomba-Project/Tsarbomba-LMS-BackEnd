package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.user.application.query.StudentPageResult;
import com.wanted.codebombalms.user.application.query.StudentSummary;
import com.wanted.codebombalms.user.application.usecase.GetStudentsUseCase;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
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
    public StudentPageResult getStudents(int page, int size) {
        long startedAt = System.nanoTime();

        // 1. 학생 목록 조회 (가입 최신순) — 인덱스 부재 시 풀스캔 + filesort 발생 구간
        List<User> users = userRepository.findAllByRole(UserRole.STUDENT, page, size);

        // 2. 전체 학생 수
        long totalElements = userRepository.countByRole(UserRole.STUDENT);

        long elapsedNanos = System.nanoTime() - startedAt;
        userMetrics.recordStudentListQuery(elapsedNanos);
        log.info("event=user_student_list_queried page={} size={} resultCount={} durationMs={}",
                page, size, users.size(), elapsedNanos / 1_000_000);

        // 3. 전체 페이지 수 (size = 0 방어)
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        // 4. 변환
        List<StudentSummary> content = users.stream()
                .map(StudentSummary::from)
                .toList();

        return new StudentPageResult(content, totalElements, totalPages);
    }
}
