package com.wanted.codebombalms.course.application.usecase;

import com.wanted.codebombalms.course.application.command.ConfigureCourseProblemSetsCommand;
import com.wanted.codebombalms.course.domain.model.CourseProblemSet;
import java.util.List;

public interface CourseProblemCommandUseCase {

    List<CourseProblemSet> configureProblemSets(ConfigureCourseProblemSetsCommand command);
}
