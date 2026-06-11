package com.wanted.codebombalms.learning.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.learning.application.command.RecordLectureProblemProgressCommand;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSet;
import com.wanted.codebombalms.learning.application.port.LearningLectureProblemSetPort;
import com.wanted.codebombalms.learning.application.port.LearningProblemPort;
import com.wanted.codebombalms.learning.application.usecase.LectureProblemProgressCommandUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProblemSetQueryUseCase;
import com.wanted.codebombalms.learning.application.usecase.LectureProblemSubmissionUseCase;
import com.wanted.codebombalms.learning.domain.exception.LearningErrorCode;
import com.wanted.codebombalms.problems.progress.application.query.GetProblemProgressQuery;
import com.wanted.codebombalms.problems.progress.application.usecase.GetProblemProgressUseCase;
import com.wanted.codebombalms.problems.set.application.query.EnterProblemSetQuery;
import com.wanted.codebombalms.problems.set.application.usecase.EnterProblemSetUseCase;
import com.wanted.codebombalms.submission.application.command.SubmitCodeCommand;
import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase;
import com.wanted.codebombalms.submission.application.usecase.SubmissionCommandUseCase.SubmissionView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LectureProblemSetService implements LectureProblemSetQueryUseCase, LectureProblemSubmissionUseCase {

    private final LearningLectureProblemSetPort learningLectureProblemSetPort;
    private final LearningProblemPort learningProblemPort;
    private final EnterProblemSetUseCase enterProblemSetUseCase;
    private final GetProblemProgressUseCase getProblemProgressUseCase;
    private final SubmissionCommandUseCase submissionCommandUseCase;
    private final LectureProblemProgressCommandUseCase lectureProblemProgressCommandUseCase;

    @Override
    @Transactional
    public LectureProblemSetEntryView enterLectureProblemSet(Long userId, Long lectureProblemSetId) {
        LearningLectureProblemSet lectureProblemSet =
                learningLectureProblemSetPort.findLectureProblemSet(lectureProblemSetId);
        var entry = enterProblemSetUseCase.handle(new EnterProblemSetQuery(lectureProblemSet.problemSetId(), userId));

        recordLectureProblemProgress(
                userId,
                lectureProblemSet,
                entry.currentProblemNumber(),
                Boolean.TRUE.equals(entry.isCompleted())
        );

        return new LectureProblemSetEntryView(
                lectureProblemSet.lectureProblemSetId(),
                lectureProblemSet.problemSetId(),
                entry.title(),
                entry.description(),
                entry.currentProblemNumber(),
                entry.currentProblemId(),
                entry.totalProblemCount(),
                entry.solvedProblemCount(),
                entry.isCompleted(),
                entry.problems()
                        .stream()
                        .map(problem -> new ProblemDetailView(
                                problem.problemId(),
                                problem.problemNumber(),
                                problem.title(),
                                problem.content(),
                                problem.problemType(),
                                problem.point(),
                                problem.startCode(),
                                problem.status(),
                                problem.latestSubmissionId()
                        ))
                        .toList()
        );
    }

    @Override
    @Transactional
    public LectureProblemSetProgressView findLectureProblemSetProgress(Long userId, Long lectureProblemSetId) {
        LearningLectureProblemSet lectureProblemSet =
                learningLectureProblemSetPort.findLectureProblemSet(lectureProblemSetId);
        var progress = getProblemProgressUseCase.handle(
                new GetProblemProgressQuery(lectureProblemSet.problemSetId(), userId)
        );
        boolean completed = progress.totalProblemCount() != null
                && progress.totalProblemCount() > 0
                && progress.totalProblemCount().equals(progress.solvedProblemCount());

        recordLectureProblemProgress(
                userId,
                lectureProblemSet,
                progress.currentProblemNumber(),
                completed
        );

        return new LectureProblemSetProgressView(
                lectureProblemSet.lectureProblemSetId(),
                lectureProblemSet.problemSetId(),
                progress.totalProblemCount(),
                progress.currentProblemNumber(),
                progress.currentProblemId(),
                progress.solvedProblemCount(),
                completed,
                progress.problems()
                        .stream()
                        .map(problem -> new ProblemProgressItemView(
                                problem.problemId(),
                                problem.problemNumber(),
                                problem.status()
                        ))
                        .toList()
        );
    }

    @Override
    @Transactional
    public SubmissionView submit(Long lectureProblemSetId, Long problemId, SubmitCodeCommand command) {
        LearningLectureProblemSet lectureProblemSet =
                learningLectureProblemSetPort.findLectureProblemSet(lectureProblemSetId);

        if (!learningProblemPort.existsProblem(problemId)) {
            throw new NotFoundException(LearningErrorCode.PROBLEM_NOT_FOUND);
        }

        if (!learningProblemPort.existsProblemInSet(lectureProblemSet.problemSetId(), problemId)) {
            throw new NotFoundException(LearningErrorCode.PROBLEM_NOT_IN_LECTURE_PROBLEM_SET);
        }

        SubmissionView result = submissionCommandUseCase.handle(problemId, command);
        findLectureProblemSetProgress(command.userId(), lectureProblemSetId);

        return result;
    }

    private void recordLectureProblemProgress(
            Long userId,
            LearningLectureProblemSet lectureProblemSet,
            Integer currentProblemNumber,
            boolean completed
    ) {
        lectureProblemProgressCommandUseCase.recordProgress(new RecordLectureProblemProgressCommand(
                userId,
                lectureProblemSet.lectureProblemSetId(),
                currentProblemNumber,
                completed
        ));
    }
}
