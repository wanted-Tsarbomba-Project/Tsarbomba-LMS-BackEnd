package com.wanted.codebombalms.user.application.service;

import com.wanted.codebombalms.user.application.query.StudentPageResult;
import com.wanted.codebombalms.user.application.query.StudentSummary;
import com.wanted.codebombalms.user.application.usecase.GetStudentsUseCase;
import com.wanted.codebombalms.user.domain.model.User;
import com.wanted.codebombalms.user.domain.model.UserRole;
import com.wanted.codebombalms.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetStudentsService implements GetStudentsUseCase {

    private final UserRepository userRepository;

    @Override
    public StudentPageResult getStudents(int page, int size) {
        // 1. 학생 목록 조회 (가입 최신순)
        List<User> users = userRepository.findAllByRole(UserRole.STUDENT, page, size);

        // 2. 전체 학생 수
        long totalElements = userRepository.countByRole(UserRole.STUDENT);

        // 3. 전체 페이지 수 (size = 0 방어)
        int totalPages = size <= 0 ? 0 : (int) Math.ceil((double) totalElements / size);

        // 4. 변환
        List<StudentSummary> content = users.stream()
                .map(StudentSummary::from)
                .toList();

        return new StudentPageResult(content, totalElements, totalPages);
    }
}
