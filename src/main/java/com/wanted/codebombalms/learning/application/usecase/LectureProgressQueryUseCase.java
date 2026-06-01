package com.wanted.codebombalms.learning.application.usecase;

import com.wanted.codebombalms.learning.domain.model.LectureProgress;

public interface LectureProgressQueryUseCase {

    LectureProgress findProgress(Long userId, Long lectureId);
}
