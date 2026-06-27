package com.wanted.codebombalms.learning.application.port;

import java.util.List;
import java.util.Map;

public interface LearningUserPort {

    String findUserName(Long userId);

    Map<Long, String> findUserNames(List<Long> userIds);
}
