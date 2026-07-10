package com.wanted.codebombalms.user.domain.repository;

import com.wanted.codebombalms.user.domain.model.UserAgreement;

import java.util.List;

public interface UserAgreementRepository {

    List<UserAgreement> saveAll(List<UserAgreement> agreements);
}
