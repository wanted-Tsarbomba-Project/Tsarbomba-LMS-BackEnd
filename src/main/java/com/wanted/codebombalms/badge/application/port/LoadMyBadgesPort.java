package com.wanted.codebombalms.badge.application.port;

import com.wanted.codebombalms.badge.application.query.MyBadgeRow;

import java.util.List;

public interface LoadMyBadgesPort {

    List<MyBadgeRow> loadMyBadges(Long userId);
}
