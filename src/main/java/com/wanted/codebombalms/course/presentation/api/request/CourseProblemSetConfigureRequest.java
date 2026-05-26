package com.wanted.codebombalms.course.presentation.api.request;

import com.wanted.codebombalms.course.application.command.ConfigureCourseProblemSetsCommand;
import com.wanted.codebombalms.course.domain.model.CourseProblemSetRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CourseProblemSetConfigureRequest(
        @NotEmpty List<@Valid ProblemSetRequest> problemSets
) {

    public ConfigureCourseProblemSetsCommand toCommand(Long courseId) {
        return new ConfigureCourseProblemSetsCommand(
                courseId,
                problemSets.stream()
                        .map(ProblemSetRequest::toCommand)
                        .toList()
        );
    }

    public record ProblemSetRequest(
            @NotNull Long problemSetId,
            @NotNull CourseProblemSetRole role,
            @NotEmpty List<@Valid ProblemStepRequest> steps
    ) {
        private ConfigureCourseProblemSetsCommand.ProblemSetCommand toCommand() {
            return new ConfigureCourseProblemSetsCommand.ProblemSetCommand(
                    problemSetId,
                    role,
                    steps.stream()
                            .map(ProblemStepRequest::toCommand)
                            .toList()
            );
        }
    }

    public record ProblemStepRequest(
            @NotNull Long problemId,
            Long lectureId,
            @NotNull Long stepOrder
    ) {
        private ConfigureCourseProblemSetsCommand.ProblemStepCommand toCommand() {
            return new ConfigureCourseProblemSetsCommand.ProblemStepCommand(
                    problemId,
                    lectureId,
                    stepOrder
            );
        }
    }
}
