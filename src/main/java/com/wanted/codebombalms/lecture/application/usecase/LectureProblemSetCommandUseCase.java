package com.wanted.codebombalms.lecture.application.usecase;

import com.wanted.codebombalms.lecture.application.command.ConfigureLectureProblemSetsCommand;
import com.wanted.codebombalms.lecture.domain.model.LectureProblemSet;
import java.util.List;

public interface LectureProblemSetCommandUseCase {

    List<LectureProblemSet> configureProblemSets(ConfigureLectureProblemSetsCommand command);
}
