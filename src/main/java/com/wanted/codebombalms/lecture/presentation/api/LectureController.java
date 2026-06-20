package com.wanted.codebombalms.lecture.presentation.api;

import com.wanted.codebombalms.lecture.application.command.CreateLectureCommand;
import com.wanted.codebombalms.lecture.application.command.UpdateLectureCommand;
import com.wanted.codebombalms.lecture.application.command.UploadLectureMaterialCommand;
import com.wanted.codebombalms.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.codebombalms.lecture.application.usecase.LectureMaterialUseCase;
import com.wanted.codebombalms.lecture.application.usecase.LectureQueryUseCase;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.lecture.presentation.api.request.LectureCreateRequest;
import com.wanted.codebombalms.lecture.presentation.api.request.LectureUpdateRequest;
import com.wanted.codebombalms.lecture.presentation.api.response.LectureDetailResponse;
import com.wanted.codebombalms.lecture.presentation.api.response.LectureMaterialDownloadUrlResponse;
import com.wanted.codebombalms.lecture.presentation.api.response.LectureMaterialResponse;
import com.wanted.codebombalms.lecture.presentation.api.response.LectureResponse;
import com.wanted.codebombalms.global.domain.common.error.exception.ExternalServiceException;
import com.wanted.codebombalms.global.presentation.api.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(name = "강의", description = "강의 API")
public class LectureController {

    private static final Logger log = LoggerFactory.getLogger(LectureController.class);

    private final LectureCommandUseCase lectureCommandUseCase;
    private final LectureQueryUseCase lectureQueryUseCase;
    private final LectureMaterialUseCase lectureMaterialUseCase;

    @GetMapping("/courses/{courseId}/lectures")
    @Operation(summary = "강좌별 강의 목록 조회")
    public ResponseEntity<ApiResponse<?>> findLecturesByCourseId(@PathVariable Long courseId) {
        log.info("[LectureController] find lectures - courseId: {}", courseId);

        return ResponseEntity.ok(ApiResponse.success(
                LectureResponseCode.RETRIEVED,
                LectureResponseMessage.RETRIEVED,
                lectureQueryUseCase.findLecturesByCourseId(courseId)
                        .stream()
                        .map(LectureResponse::from)
                        .toList()
        ));
    }

    @GetMapping("/lectures/{lectureId}")
    @Operation(summary = "강의 단건 조회")
    public ResponseEntity<ApiResponse<?>> findLectureById(@PathVariable Long lectureId) {
        log.info("[LectureController] find lecture - lectureId: {}", lectureId);

        return ResponseEntity.ok(ApiResponse.success(
                LectureResponseCode.RETRIEVED,
                LectureResponseMessage.RETRIEVED,
                LectureDetailResponse.from(lectureQueryUseCase.findLectureById(lectureId))
        ));
    }

    @PostMapping("/courses/{courseId}/lectures")
    @Operation(summary = "강의 생성")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<?>> createLecture(
            @PathVariable Long courseId,
            @Valid @RequestBody LectureCreateRequest request
    ) {
        log.info("[LectureController] create lecture - courseId: {}, title: {}", courseId, request.title());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        LectureResponseCode.CREATED,
                        LectureResponseMessage.CREATED,
                        LectureDetailResponse.from(lectureCommandUseCase.createLecture(new CreateLectureCommand(
                                courseId,
                                request.title(),
                                request.description(),
                                request.videoUrl(),
                                request.thumbnailUrl(),
                                request.lectureOrder(),
                                request.status()
                        )))
                ));
    }

    @PutMapping("/lectures/{lectureId}")
    @Operation(summary = "강의 수정")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<?>> updateLecture(
            @PathVariable Long lectureId,
            @Valid @RequestBody LectureUpdateRequest request
    ) {
        log.info("[LectureController] update lecture - lectureId: {}", lectureId);

        return ResponseEntity.ok(ApiResponse.success(
                LectureResponseCode.UPDATED,
                LectureResponseMessage.UPDATED,
                LectureDetailResponse.from(lectureCommandUseCase.updateLecture(new UpdateLectureCommand(
                        lectureId,
                        request.title(),
                        request.description(),
                        request.videoUrl(),
                        request.thumbnailUrl(),
                        request.lectureOrder(),
                        request.status()
                )))
        ));
    }

    @DeleteMapping("/lectures/{lectureId}")
    @Operation(summary = "강의 삭제")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<Void> deleteLecture(@PathVariable Long lectureId) {
        log.info("[LectureController] delete lecture - lectureId: {}", lectureId);

        lectureCommandUseCase.deleteLecture(lectureId);

        return ResponseEntity.noContent().build();
    }
    @PostMapping(value = "/lectures/{lectureId}/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "강의자료 업로드")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<ApiResponse<?>> uploadMaterial(
            @PathVariable Long lectureId,
            @RequestParam("material") MultipartFile material
    ) {
        byte[] materialBytes = readMaterialBytes(material);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(
                        LectureResponseCode.MATERIAL_UPLOADED,
                        LectureResponseMessage.MATERIAL_UPLOADED,
                        LectureMaterialResponse.from(lectureMaterialUseCase.uploadMaterial(
                                new UploadLectureMaterialCommand(
                                        lectureId,
                                        material.getOriginalFilename(),
                                        material.getContentType(),
                                        material.getSize(),
                                        materialBytes
                                )
                        ))
                ));
    }

    @GetMapping("/lectures/{lectureId}/materials")
    @Operation(summary = "강의자료 목록 조회")
    public ResponseEntity<ApiResponse<?>> findMaterials(@PathVariable Long lectureId) {
        return ResponseEntity.ok(ApiResponse.success(
                LectureResponseCode.RETRIEVED,
                LectureResponseMessage.RETRIEVED,
                lectureMaterialUseCase.findMaterials(lectureId)
                        .stream()
                        .map(LectureMaterialResponse::from)
                        .toList()
        ));
    }

    @PostMapping("/lecture-materials/{lectureMaterialId}/download-url")
    @Operation(summary = "강의자료 다운로드 URL 발급")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<?>> issueMaterialDownloadUrl(
            @PathVariable Long lectureMaterialId,
            @AuthenticationPrincipal Long userId,
            Authentication authentication
    ) {
        String downloadUrl = lectureMaterialUseCase.issueDownloadUrl(
                lectureMaterialId,
                userId,
                isOperator(authentication)
        );

        return ResponseEntity.ok(ApiResponse.success(
                LectureResponseCode.MATERIAL_DOWNLOAD_URL_ISSUED,
                LectureResponseMessage.MATERIAL_DOWNLOAD_URL_ISSUED,
                new LectureMaterialDownloadUrlResponse(downloadUrl)
        ));
    }

    @DeleteMapping("/lecture-materials/{lectureMaterialId}")
    @Operation(summary = "강의자료 삭제")
    @PreAuthorize("hasRole('OPERATOR')")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Long lectureMaterialId) {
        lectureMaterialUseCase.deleteMaterial(lectureMaterialId);
        return ResponseEntity.noContent().build();
    }

    private byte[] readMaterialBytes(MultipartFile material) {
        try {
            return material.getBytes();
        } catch (IOException e) {
            throw new ExternalServiceException(
                    LectureErrorCode.LECTURE_MATERIAL_UPLOAD_FAILED,
                    e
            );
        }
    }

    private boolean isOperator(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_OPERATOR".equals(authority.getAuthority()));
    }
}
