package com.wanted.codebombalms.problems.set.application.query;

public enum ProblemSetSort {
    DEFAULT,
    POPULAR;

    public boolean isPopular() {
        return this == POPULAR;
    }
}
