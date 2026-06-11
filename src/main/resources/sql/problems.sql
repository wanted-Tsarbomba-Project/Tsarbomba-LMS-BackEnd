START TRANSACTION;

-- =====================================================
-- 문제 도메인 기본 더미 데이터
-- 포함: 문제 카테고리, 문제 세트, 문제
-- 제외: 데이터셋, 힌트, 테스트케이스
-- PK 범위:
--   problem_category: 3000 ~ 3999
--   problem_set:      4000 ~ 4999
--   problem:          5000 ~ 5999
--
-- created_by로 사용할 user_id = 30이 반드시 존재해야 합니다.
-- =====================================================

-- =====================================================
-- 1. 문제 카테고리
-- =====================================================
INSERT INTO problem_category
(category_id, category_name, description, status, created_at, updated_at)
VALUES
(3001, '데이터 분석 실습', 'CSV 데이터를 활용해 pandas 기반 데이터 분석을 연습하는 문제 분야입니다.', 'ACTIVE', NOW(), NOW()),
(3002, '매출 데이터 분석', '쇼핑몰 주문 데이터를 활용한 매출 분석 문제 분야입니다.', 'ACTIVE', NOW(), NOW()),
(3003, '교통 데이터 분석', '교통 이용 및 사고 데이터를 활용한 분석 문제 분야입니다.', 'ACTIVE', NOW(), NOW()),
(3004, '학습 데이터 분석', '학습자 활동 데이터를 분석하는 문제 분야입니다.', 'ACTIVE', NOW(), NOW()),
(3005, '생활 데이터 분석', '카페 주문, 운동 기록 등 생활 데이터를 분석하는 문제 분야입니다.', 'ACTIVE', NOW(), NOW())
ON DUPLICATE KEY UPDATE
    category_name = VALUES(category_name),
    description = VALUES(description),
    status = VALUES(status),
    updated_at = NOW();


-- =====================================================
-- 2. 문제 세트
-- =====================================================
INSERT INTO problem_set
(problem_set_id, category_id, title, description, difficulty, status,
 total_problem_count, completed_user_count, started_user_count,
 created_by, created_at, updated_at, deleted_at)
VALUES
(4001, 3003, '서울 교통 사고 데이터 분석 프로젝트',
 '서울 교통 사고 CSV 데이터를 불러와 데이터 확인, 정제, 탐색, 모델 학습까지 진행하는 코드 실행형 문제 세트입니다.',
 'MEDIUM', 'ACTIVE', 6, 2, 5, 30, NOW(), NOW(), NULL),

(4101, 3001, '직원 성과 데이터 기초 분석',
 '직원 성과 데이터를 활용해 DataFrame 기본 정보와 평균값 계산을 연습합니다.',
 'EASY', 'ACTIVE', 2, 1, 3, 30, NOW(), NOW(), NULL),

(4102, 3002, '쇼핑몰 주문 데이터 분석',
 '온라인 쇼핑몰 주문 데이터를 활용해 매출과 카테고리별 판매량을 분석합니다.',
 'EASY', 'ACTIVE', 2, 1, 2, 30, NOW(), NOW(), NULL),

(4103, 3003, '대중교통 이용 데이터 분석',
 '교통 이용 데이터를 활용해 총 요금과 러시아워 평균 이동 시간을 분석합니다.',
 'EASY', 'ACTIVE', 2, 0, 2, 30, NOW(), NOW(), NULL),

(4104, 3005, '카페 주문 데이터 분석',
 '카페 주문 데이터를 활용해 인기 메뉴와 고객 유형별 평균 주문 금액을 분석합니다.',
 'EASY', 'ACTIVE', 2, 0, 2, 30, NOW(), NOW(), NULL),

(4105, 3004, 'LMS 학생 실습 데이터 분석',
 '학생 실습 데이터를 활용해 평균 점수와 학과별 통과 문제 수를 분석합니다.',
 'EASY', 'ACTIVE', 2, 1, 2, 30, NOW(), NOW(), NULL),

(4106, 3005, '운동 기록 데이터 분석',
 '운동 기록 데이터를 활용해 평균 칼로리와 운동 유형별 평균 시간을 분석합니다.',
 'EASY', 'ACTIVE', 2, 0, 1, 30, NOW(), NOW(), NULL),

(4107, 3004, '학생 학습 데이터 분석',
 '학생 학습 데이터를 활용해 학습 시간과 점수 데이터를 분석합니다.',
 'EASY', 'ACTIVE', 2, 0, 1, 30, NOW(), NOW(), NULL)
ON DUPLICATE KEY UPDATE
    category_id = VALUES(category_id),
    title = VALUES(title),
    description = VALUES(description),
    difficulty = VALUES(difficulty),
    status = VALUES(status),
    total_problem_count = VALUES(total_problem_count),
    completed_user_count = VALUES(completed_user_count),
    started_user_count = VALUES(started_user_count),
    created_by = VALUES(created_by),
    updated_at = NOW(),
    deleted_at = VALUES(deleted_at);


-- =====================================================
-- 3. 문제
-- answer 컬럼 제거 반영
-- =====================================================
INSERT INTO problem
(problem_id, problem_set_id, title, content, problem_type, difficulty,
 explanation, point, attempt_limit, is_retriable, status,
 problem_order, created_at, updated_at)
VALUES
(5001, 4001, '데이터 살펴보기',
 '서울 교통 사고 CSV 데이터를 불러온 뒤 DataFrame의 행과 열 개수를 result 변수에 담으세요.',
 'CODE', 'EASY', 'df.shape를 사용하면 데이터의 행과 열 개수를 튜플로 확인할 수 있습니다.',
 10, 999, TRUE, 'ACTIVE', 1, NOW(), NOW()),

(5002, 4001, '데이터 정제하기',
 '분석에 필요한 컬럼만 선택한 DataFrame을 result 변수에 담으세요.',
 'CODE', 'MEDIUM', '분석에 필요한 컬럼명을 리스트로 만든 뒤 df[cols] 형태로 선택하면 됩니다.',
 15, 999, TRUE, 'ACTIVE', 2, NOW(), NOW()),

(5003, 4001, '데이터 탐색하기',
 '사고유형_대범주 컬럼을 기준으로 사고 유형별 발생 건수를 result 변수에 담으세요.',
 'CODE', 'MEDIUM', 'value_counts()를 사용하면 범주형 데이터의 빈도수를 확인할 수 있습니다.',
 15, 999, TRUE, 'ACTIVE', 3, NOW(), NOW()),

(5004, 4001, '데이터 준비하기',
 '모델 학습을 위해 Feature와 Target을 분리하고, 각각의 shape을 result 변수에 담으세요.',
 'CODE', 'MEDIUM', 'X는 사고내용을 제외한 Feature, y는 사고내용 Target으로 분리합니다.',
 20, 999, TRUE, 'ACTIVE', 4, NOW(), NOW()),

(5005, 4001, '모델 학습하기',
 'LogisticRegression 모델을 학습한 뒤 학습된 model 객체를 result 변수에 담으세요.',
 'CODE', 'HARD', '문자형 Feature는 pd.get_dummies()로 변환한 뒤 LogisticRegression을 학습할 수 있습니다.',
 20, 999, TRUE, 'ACTIVE', 5, NOW(), NOW()),

(5006, 4001, '모델 평가 및 예측하기',
 'LogisticRegression 모델을 학습한 뒤 검증 데이터 정확도를 result 변수에 담으세요.',
 'CODE', 'HARD', 'accuracy_score를 사용하면 예측 결과와 실제 정답을 비교해 정확도를 계산할 수 있습니다.',
 20, 999, TRUE, 'ACTIVE', 6, NOW(), NOW()),

(5101, 4101, '데이터 행 개수 확인',
 'employee_performance.csv 데이터의 행 개수를 result 변수에 담으세요.',
 'CODE', 'EASY', 'len(df)를 사용하면 DataFrame의 행 개수를 확인할 수 있습니다.',
 10, 999, TRUE, 'ACTIVE', 1, NOW(), NOW()),

(5102, 4101, 'value 평균 계산',
 'value 컬럼의 평균값을 result 변수에 담으세요.',
 'CODE', 'EASY', 'df["value"].mean()을 사용하면 평균값을 계산할 수 있습니다.',
 10, 999, TRUE, 'ACTIVE', 2, NOW(), NOW()),

(5111, 4102, '총 매출 계산',
 'quantity, unit_price, discount_rate를 활용해 전체 매출 합계를 result 변수에 담으세요.',
 'CODE', 'EASY', '수량 * 단가 * (1 - 할인율)을 계산한 뒤 합계를 구하면 됩니다.',
 10, 999, TRUE, 'ACTIVE', 1, NOW(), NOW()),

(5112, 4102, '카테고리별 판매량 분석',
 'category별 quantity 합계를 result 변수에 담으세요.',
 'CODE', 'EASY', 'groupby("category")["quantity"].sum()을 사용하면 됩니다.',
 10, 999, TRUE, 'ACTIVE', 2, NOW(), NOW()),

(5121, 4103, '총 교통 요금 계산',
 'transport_usage_data.csv의 fare 합계를 result 변수에 담으세요.',
 'CODE', 'EASY', 'df["fare"].sum()을 사용하면 전체 요금 합계를 계산할 수 있습니다.',
 10, 999, TRUE, 'ACTIVE', 1, NOW(), NOW()),

(5122, 4103, '러시아워 평균 이동 시간',
 '러시아워 데이터만 필터링한 뒤 duration_min 평균을 result 변수에 담으세요.',
 'CODE', 'EASY', 'df[df["is_rush_hour"] == True]로 러시아워 데이터만 필터링합니다.',
 10, 999, TRUE, 'ACTIVE', 2, NOW(), NOW()),

(5131, 4104, '인기 메뉴 확인',
 '카페 주문 데이터에서 메뉴별 주문 횟수를 result 변수에 담으세요.',
 'CODE', 'EASY', 'value_counts()를 사용하면 메뉴별 주문 횟수를 확인할 수 있습니다.',
 10, 999, TRUE, 'ACTIVE', 1, NOW(), NOW()),

(5132, 4104, '고객 유형별 평균 주문 금액',
 'customer_type별 price 평균을 result 변수에 담으세요.',
 'CODE', 'EASY', 'groupby("customer_type")["price"].mean()을 사용합니다.',
 10, 999, TRUE, 'ACTIVE', 2, NOW(), NOW()),

(5141, 4105, '학생 평균 점수 계산',
 'score 컬럼의 평균값을 result 변수에 담으세요.',
 'CODE', 'EASY', 'df["score"].mean()을 사용하면 평균 점수를 계산할 수 있습니다.',
 10, 999, TRUE, 'ACTIVE', 1, NOW(), NOW()),

(5142, 4105, '학과별 통과 문제 수',
 'department별 passed_count 합계를 result 변수에 담으세요.',
 'CODE', 'EASY', 'groupby("department")["passed_count"].sum()을 사용합니다.',
 10, 999, TRUE, 'ACTIVE', 2, NOW(), NOW()),

(5151, 4106, '평균 소모 칼로리',
 'fitness_record_data.csv의 calories 평균을 result 변수에 담으세요.',
 'CODE', 'EASY', 'df["calories"].mean()을 사용합니다.',
 10, 999, TRUE, 'ACTIVE', 1, NOW(), NOW()),

(5152, 4106, '운동 유형별 평균 시간',
 'workout_type별 duration_min 평균을 result 변수에 담으세요.',
 'CODE', 'EASY', 'groupby("workout_type")["duration_min"].mean()을 사용합니다.',
 10, 999, TRUE, 'ACTIVE', 2, NOW(), NOW()),

(5161, 4107, '평균 학습 시간 계산',
 'study_hours 컬럼의 평균값을 result 변수에 담으세요.',
 'CODE', 'EASY', 'df["study_hours"].mean()을 사용합니다.',
 10, 999, TRUE, 'ACTIVE', 1, NOW(), NOW()),

(5162, 4107, '레벨별 평균 점수',
 'level별 score 평균을 result 변수에 담으세요.',
 'CODE', 'EASY', 'groupby("level")["score"].mean()을 사용합니다.',
 10, 999, TRUE, 'ACTIVE', 2, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    problem_set_id = VALUES(problem_set_id),
    title = VALUES(title),
    content = VALUES(content),
    problem_type = VALUES(problem_type),
    difficulty = VALUES(difficulty),
    explanation = VALUES(explanation),
    point = VALUES(point),
    attempt_limit = VALUES(attempt_limit),
    is_retriable = VALUES(is_retriable),
    status = VALUES(status),
    problem_order = VALUES(problem_order),
    updated_at = NOW();

COMMIT;


-- =====================================================
-- 생성 결과 확인
-- =====================================================
SELECT category_id, category_name, status
FROM problem_category
WHERE category_id BETWEEN 3001 AND 3005
ORDER BY category_id;

SELECT problem_set_id, category_id, title, total_problem_count, status
FROM problem_set
WHERE problem_set_id IN (4001, 4101, 4102, 4103, 4104, 4105, 4106, 4107)
ORDER BY problem_set_id;

SELECT problem_id, problem_set_id, title, problem_order, status
FROM problem
WHERE problem_set_id IN (4001, 4101, 4102, 4103, 4104, 4105, 4106, 4107)
ORDER BY problem_set_id, problem_order;
