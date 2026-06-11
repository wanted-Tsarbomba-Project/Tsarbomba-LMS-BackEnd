INSERT INTO course_category (
    course_category_id,
    name,
    status,
    display_order,
    created_at
) VALUES
      (1000, '데이터 분석', 'ACTIVE', 1, NOW()),
      (1001, '머신러닝', 'ACTIVE', 2, NOW()),
      (1002, 'Python', 'ACTIVE', 3, NOW()),
      (1003, 'SQL', 'ACTIVE', 4, NOW()),
      (1004, '통계', 'ACTIVE', 5, NOW()),
      (1005, '시각화', 'ACTIVE', 6, NOW()),
      (1006, '빅데이터', 'ACTIVE', 7, NOW())
    ON DUPLICATE KEY UPDATE
                         course_category_id = course_category_id;
