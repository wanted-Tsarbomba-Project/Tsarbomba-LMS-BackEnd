package com.wanted.codebombalms.problems.set.application.port;

import com.wanted.codebombalms.problems.set.domain.model.ProblemTestCaseModification;
import com.wanted.codebombalms.problems.set.domain.model.ProblemTestCaseRegistration;

import java.util.List;

public interface ManageProblemSetTestCasesPort {

    int createTestCases(Long problemId, List<ProblemTestCaseRegistration> testCases);

    /**
     * 문제에 속한 테스트케이스를 요청 목록 기준으로 동기화한다.
     *
     * 기존 ID가 포함된 항목은 수정하고, ID가 없는 항목은 신규 생성한다.
     * 요청 목록에서 제외된 기존 활성 테스트케이스는 비활성화하며,
     * 테스트 순서는 요청 배열 순서대로 다시 지정한다.
     *
     * @param problemId 동기화할 문제 ID
     * @param testCases 요청된 테스트케이스 목록
     * @return 요청 목록에 포함된 테스트케이스 수
     */

    int synchronizeTestCases(Long problemId, List<ProblemTestCaseModification> testCases);

    int deactivateActiveTestCasesByProblemIds(List<Long> problemIds);
}
