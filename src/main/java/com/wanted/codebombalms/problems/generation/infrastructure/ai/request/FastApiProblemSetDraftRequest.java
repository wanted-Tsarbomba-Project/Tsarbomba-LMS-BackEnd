package com.wanted.codebombalms.problems.generation.infrastructure.ai.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.wanted.codebombalms.problems.generation.application.command.GenerateProblemSetDraftCommand;

import java.util.List;

public record FastApiProblemSetDraftRequest(
        String question,

        @JsonProperty("operator_id")
        Long operatorId,

        @JsonProperty("dataset_url")
        String datasetUrl,

        @JsonProperty("data_file_name")
        String dataFileName,

        String topic,

        @JsonProperty("category_name")
        String categoryName,

        String difficulty,

        @JsonProperty("problem_count")
        int problemCount,

        @JsonProperty("sub_problem_count")
        int subProblemCount,

        List<Object> history
) {

    public static FastApiProblemSetDraftRequest from(GenerateProblemSetDraftCommand command) {
        return new FastApiProblemSetDraftRequest(
                command.question(),
                command.operatorId(),
                command.datasetUrl(),
                command.dataFileName(),
                command.topic(),
                command.categoryName(),
                command.difficulty(),
                command.problemCount(),
                command.subProblemCount(),
                List.of()
        );
    }
}
