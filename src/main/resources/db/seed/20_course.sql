INSERT INTO course (
    course_id,
    instructor_id,
    course_category_id,
    title,
    description,
    thumbnail_url,
    status,
    created_at,
    updated_at,
    deleted_at
) VALUES
      (
          2000,
          8,
          1000,
          '처음 시작하는 pandas 데이터 분석',
          'Python pandas를 활용해 CSV 데이터를 불러오고, 기본 탐색과 조건 필터링을 실습하는 입문 강좌입니다.',
          'https://placehold.co/640x360?text=Pandas+Data+Analysis',
          'ACTIVE',
          '2026-05-01 09:10:00',
          '2026-05-01 09:10:00',
          NULL
      ),
      (
          2001,
          8,
          1000,
          '실무 데이터를 활용한 기초 리포트 작성',
          '수집된 데이터를 정리하고 주요 지표를 계산해 리포트 형태로 정리하는 강좌입니다.',
          'https://placehold.co/640x360?text=Data+Report',
          'ACTIVE',
          '2026-04-01 09:10:00',
          '2026-04-01 09:10:00',
          NULL
      )
    ON DUPLICATE KEY UPDATE
                         course_id = course_id;
