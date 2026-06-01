package com.wanted.codebombalms.learning.application.usecase;

import com.wanted.codebombalms.learning.application.command.RecordLectureProgressCommand;
import com.wanted.codebombalms.learning.domain.model.LectureProgress;

public interface LectureProgressCommandUseCase {

    LectureProgress recordProgress(RecordLectureProgressCommand command);
}
