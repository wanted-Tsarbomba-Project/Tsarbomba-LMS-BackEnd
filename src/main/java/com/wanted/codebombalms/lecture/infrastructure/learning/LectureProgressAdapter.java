package com.wanted.codebombalms.lecture.infrastructure.learning;

import com.wanted.codebombalms.learning.domain.repository.LectureProgressRepository;
import com.wanted.codebombalms.lecture.application.port.LectureProgressPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LectureProgressAdapter implements LectureProgressPort {

    private final LectureProgressRepository lectureProgressRepository;

    @Override
    public boolean areLecturesCompleted(Long userId, List<Long> lectureIds) {
        if (lectureIds.isEmpty()) {
            return true;
        }
        return lectureProgressRepository.countCompletedByUserIdAndLectureIds(userId, lectureIds) == lectureIds.size();
    }
}
