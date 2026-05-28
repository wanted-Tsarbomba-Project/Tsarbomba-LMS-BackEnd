package com.wanted.codebombalms.reward.point.application.service;

import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import com.wanted.codebombalms.reward.point.application.usecase.GrantProblemPointUseCase;
import com.wanted.codebombalms.reward.point.domain.exception.RewardErrorCode;
import com.wanted.codebombalms.reward.point.domain.model.PointHistory;
import com.wanted.codebombalms.reward.point.domain.model.UserPoint;
import com.wanted.codebombalms.reward.point.domain.repository.PointHistoryRepository;
import com.wanted.codebombalms.reward.point.domain.repository.UserPointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RewardPointGrantService implements GrantProblemPointUseCase {

    private static final String PROBLEM_CORRECT_REASON = "문제 정답 제출";

    private final UserPointRepository userPointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @Override
    @Transactional
    public void grant(Long userId, Long problemId, Long submissionId, Integer point) {
        validatePoint(point);
        validateNotAlreadyGranted(userId, problemId);

        UserPoint userPoint = userPointRepository.findByUserIdForUpdate(userId)
                .map(existingPoint -> existingPoint.addPoint(point))
                .orElseGet(() -> UserPoint.create(userId, point));

        userPointRepository.save(userPoint);

        pointHistoryRepository.save(PointHistory.create(
                userId,
                problemId,
                submissionId,
                point,
                PROBLEM_CORRECT_REASON
        ));
    }

    private void validatePoint(Integer point) {
        if (point == null || point <= 0) {
            throw new ValidationException(RewardErrorCode.REWARD_POINT_GRANT_FAILED);
        }
    }

    private void validateNotAlreadyGranted(Long userId, Long problemId) {
        if (pointHistoryRepository.existsByUserIdAndProblemId(userId, problemId)) {
            throw new ConflictException(RewardErrorCode.REWARD_POINT_ALREADY_GRANTED);
        }
    }
}

