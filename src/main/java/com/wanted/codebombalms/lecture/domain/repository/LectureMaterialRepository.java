package com.wanted.codebombalms.lecture.domain.repository;

import com.wanted.codebombalms.lecture.domain.model.LectureMaterial;
import java.util.List;
import java.util.Optional;

public interface LectureMaterialRepository {

    LectureMaterial save(LectureMaterial material);

    List<LectureMaterial> findByLectureIdAndDeletedAtIsNull(Long lectureId);

    Optional<LectureMaterial> findByLectureMaterialIdAndDeletedAtIsNull(Long lectureMaterialId);
}
