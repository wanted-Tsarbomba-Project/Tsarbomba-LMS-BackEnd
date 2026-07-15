package com.wanted.codebombalms.user.domain.repository;

import com.wanted.codebombalms.user.domain.model.User;

import java.util.List;

public record UserPage(
        List<User> content,
        long totalElements
) {
}
