package com.wanted.codebombalms.lecture.infrastructure.persistence;

import com.wanted.codebombalms.lecture.domain.model.LectureMaterial;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@Getter
@Table(
        name = "lecture_material",
        indexes = @Index(name = "idx_lecture_material_lecture_id", columnList = "lecture_id")
)
public class LectureMaterialJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lecture_material_id")
    private Long lectureMaterialId;

    @Column(name = "lecture_id", nullable = false)
    private Long lectureId;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public static LectureMaterialJpaEntity from(LectureMaterial material) {
        LectureMaterialJpaEntity entity = new LectureMaterialJpaEntity();
        entity.lectureMaterialId = material.getLectureMaterialId();
        entity.lectureId = material.getLectureId();
        entity.originalFileName = material.getOriginalFileName();
        entity.storedFileName = material.getStoredFileName();
        entity.filePath = material.getFilePath();
        entity.contentType = material.getContentType();
        entity.fileSize = material.getFileSize();
        entity.createdAt = material.getCreatedAt();
        entity.deletedAt = material.getDeletedAt();
        return entity;
    }

    public void apply(LectureMaterial material) {
        this.lectureId = material.getLectureId();
        this.originalFileName = material.getOriginalFileName();
        this.storedFileName = material.getStoredFileName();
        this.filePath = material.getFilePath();
        this.contentType = material.getContentType();
        this.fileSize = material.getFileSize();
        this.deletedAt = material.getDeletedAt();
    }

    public LectureMaterial toDomain() {
        return LectureMaterial.restore(
                lectureMaterialId,
                lectureId,
                originalFileName,
                storedFileName,
                filePath,
                contentType,
                fileSize,
                createdAt,
                deletedAt
        );
    }

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
