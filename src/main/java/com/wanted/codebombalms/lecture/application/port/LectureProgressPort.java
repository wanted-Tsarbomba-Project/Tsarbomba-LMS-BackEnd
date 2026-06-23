package com.wanted.codebombalms.lecture.application.port;

import java.util.List;

public interface LectureProgressPort {

    boolean areLecturesCompleted(Long userId, List<Long> lectureIds);
}
