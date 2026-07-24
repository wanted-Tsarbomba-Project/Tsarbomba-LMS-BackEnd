package com.wanted.codebombalms.learning.application.usecase;

import com.wanted.codebombalms.problems.explanation.application.usecase.ViewProblemExplanationUseCase.ExplanationView;
import com.wanted.codebombalms.submission.application.command.SubmitCodeCommand;
import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase.SubmissionView;

public interface LectureProblemSubmissionUseCase {

    SubmissionView submit(Long lectureProblemSetId, Long problemId, SubmitCodeCommand command);

    ExplanationView viewExplanation(Long userId, Long lectureProblemSetId, Long problemId);
}
