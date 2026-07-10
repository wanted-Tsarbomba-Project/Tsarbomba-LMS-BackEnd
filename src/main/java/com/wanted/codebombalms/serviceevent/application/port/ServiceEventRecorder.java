package com.wanted.codebombalms.serviceevent.application.port;

import com.wanted.codebombalms.serviceevent.domain.model.ServiceEventEnvelope;

/**
 * 도메인 서비스가 비즈니스 이벤트를 기록할 때 쓰는 발행 포트
 * 사용법 (도메인 코드에 1줄):
 *   serviceEventRecorder.record(ServiceEventEnvelope.business(ENROLL_CREATED, userId, courseId));
 * 트랜잭션 커밋 후(AFTER_COMMIT)에만 기록되므로 롤백된 행위는 남지 않는다.
 */
public interface ServiceEventRecorder {

    void record(ServiceEventEnvelope envelope);
}
