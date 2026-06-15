package com.wanted.codebombalms.recommendation.infrastructure.scheduler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.OptionalInt;
import java.util.function.IntSupplier;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 다중 서버 환경에서 추천 생성 배치가 동시에 실행되지 않도록 DB named lock으로 보호합니다. */
@Component
@RequiredArgsConstructor
public class RecommendationBatchLockExecutor {

    private static final int LOCK_TIMEOUT_SECONDS = 0;
    private static final int LOCK_QUERY_TIMEOUT_SECONDS = 5;

    private final DataSource dataSource;

    /** lock 획득에 성공한 서버에서만 작업을 실행하고, 실패하면 빈 결과를 반환합니다. */
    public OptionalInt executeIfLockAcquired(String lockName, IntSupplier task) {
        try (Connection connection = dataSource.getConnection()) {
            if (!tryAcquireLock(connection, lockName)) {
                return OptionalInt.empty();
            }

            Exception taskException = null;
            try {
                return OptionalInt.of(task.getAsInt());
            } catch (Exception e) {
                taskException = e;
                throw e;
            } finally {
                try {
                    releaseLock(connection, lockName);
                } catch (Exception releaseException) {
                    if (taskException != null) {
                        taskException.addSuppressed(releaseException);
                    } else {
                        throw releaseException;
                    }
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("추천 생성 배치 락 처리 중 예외가 발생했습니다.", e);
        }
    }

    /** MySQL named lock을 같은 커넥션에서 획득합니다. */
    private boolean tryAcquireLock(Connection connection, String lockName) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("SELECT GET_LOCK(?, ?)")) {
            statement.setQueryTimeout(LOCK_QUERY_TIMEOUT_SECONDS);
            statement.setString(1, lockName);
            statement.setInt(2, LOCK_TIMEOUT_SECONDS);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) == 1;
            }
        }
    }

    /** MySQL named lock은 커넥션 단위라 획득한 커넥션에서 직접 해제합니다. */
    private void releaseLock(Connection connection, String lockName) throws Exception {
        try (PreparedStatement statement = connection.prepareStatement("SELECT RELEASE_LOCK(?)")) {
            statement.setQueryTimeout(LOCK_QUERY_TIMEOUT_SECONDS);
            statement.setString(1, lockName);
            statement.executeQuery();
        }
    }
}
