package com.wanted.codebombalms.auth.domain.repository;

public interface OAuthStateRepository {

    void save(String state);

    boolean validateAndDelete(String state);
}
