package com.wanted.codebombalms.lecture.application.usecase;

import com.wanted.codebombalms.lecture.application.command.CreateLectureCommand;
import com.wanted.codebombalms.lecture.application.command.UpdateLectureCommand;
import com.wanted.codebombalms.lecture.application.command.UpdateLectureOrdersCommand;
import com.wanted.codebombalms.lecture.domain.model.Lecture;

public interface LectureCommandUseCase {

    Lecture createLecture(CreateLectureCommand command);

    Lecture updateLecture(UpdateLectureCommand command);

    void updateLectureOrders(UpdateLectureOrdersCommand command);

    void deleteLecture(Long lectureId);
}
