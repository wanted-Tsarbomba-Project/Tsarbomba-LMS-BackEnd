package com.wanted.codebombalms.learning.application.usecase;

import com.wanted.codebombalms.learning.application.command.RecordLectureProblemProgressCommand;
import com.wanted.codebombalms.learning.domain.model.LectureProblemProgress;

public interface LectureProblemProgressCommandUseCase {

    LectureProblemProgress recordProgress(RecordLectureProblemProgressCommand command);
}
