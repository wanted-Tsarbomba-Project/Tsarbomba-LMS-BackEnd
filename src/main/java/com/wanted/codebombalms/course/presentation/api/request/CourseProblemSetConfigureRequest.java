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
            Long lectureId,
            @NotNull Long problemSetId,
            @NotNull CourseProblemSetRole role,
            @NotNull Integer displayOrder
    ) {
        private ConfigureCourseProblemSetsCommand.ProblemSetCommand toCommand() {
            return new ConfigureCourseProblemSetsCommand.ProblemSetCommand(
                    lectureId,
                    problemSetId,
                    role,
                    displayOrder
            );
        }
    }
}
