package com.wanted.codebombalms.learning.application.usecase;

import com.wanted.codebombalms.submission.application.command.SubmitCodeCommand;
import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase.SubmissionView;

public interface LectureProblemSubmissionUseCase {

    SubmissionView submit(Long lectureProblemSetId, Long problemId, SubmitCodeCommand command);
}
