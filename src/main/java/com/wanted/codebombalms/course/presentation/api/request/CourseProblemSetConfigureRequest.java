package com.wanted.codebombalms.course.presentation.api.request;

import com.wanted.codebombalms.lecture.application.command.ConfigureLectureProblemSetsCommand;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSetRole;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CourseProblemSetConfigureRequest(
        @NotEmpty List<@Valid ProblemSetRequest> problemSets
) {

    public ConfigureLectureProblemSetsCommand toCommand(Long courseId) {
        return new ConfigureLectureProblemSetsCommand(
                courseId,
                problemSets.stream()
                        .map(ProblemSetRequest::toCommand)
                        .toList()
        );
    }

    public record ProblemSetRequest(
            Long lectureId,
            @NotNull Long problemSetId,
            @NotNull LectureProblemSetRole role,
            @NotNull Integer displayOrder
    ) {
        private ConfigureLectureProblemSetsCommand.ProblemSetCommand toCommand() {
            return new ConfigureLectureProblemSetsCommand.ProblemSetCommand(
                    lectureId,
                    problemSetId,
                    role,
                    displayOrder
            );
        }
    }
}
