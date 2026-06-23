package com.wanted.codebombalms.reward.point.application.port;

public interface PointGrantedEventPort {

    void publish(Long userId, Integer totalPoint);
}
