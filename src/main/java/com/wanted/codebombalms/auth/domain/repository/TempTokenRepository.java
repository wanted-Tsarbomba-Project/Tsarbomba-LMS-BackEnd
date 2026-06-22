package com.wanted.codebombalms.auth.domain.repository;

import com.wanted.codebombalms.auth.domain.model.OAuthTempData;

import java.util.Optional;

public interface TempTokenRepository {

    void save(String tempToken, OAuthTempData data);

    Optional<OAuthTempData> find(String tempToken);

    Optional<OAuthTempData> findAndDelete(String tempToken);
}
