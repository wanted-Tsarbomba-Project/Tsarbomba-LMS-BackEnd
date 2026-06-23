package com.wanted.codebombalms.lecture.application.usecase;

import com.wanted.codebombalms.lecture.application.command.UploadLectureMaterialCommand;
import com.wanted.codebombalms.lecture.domain.model.LectureMaterial;
import java.util.List;

public interface LectureMaterialUseCase {

    LectureMaterial uploadMaterial(UploadLectureMaterialCommand command);

    List<LectureMaterial> findMaterials(Long lectureId);

    String issueDownloadUrl(Long lectureMaterialId, Long userId, boolean operator);

    void deleteMaterial(Long lectureMaterialId);
}
