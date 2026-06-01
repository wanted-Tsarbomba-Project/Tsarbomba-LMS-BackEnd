package com.wanted.codebombalms.submission.domain.model;

import java.util.List;

public record CodeSubmissionPage(
        List<CodeSubmissionListItem> submissions,
        int currentPage,
        int totalPage,
        long totalCount
) {
}
