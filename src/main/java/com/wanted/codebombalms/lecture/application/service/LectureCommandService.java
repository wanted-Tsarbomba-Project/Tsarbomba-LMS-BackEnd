package com.wanted.codebombalms.lecture.application.service;

import com.wanted.codebombalms.course.domain.model.Course;
import com.wanted.codebombalms.lecture.application.command.CreateLectureCommand;
import com.wanted.codebombalms.lecture.application.command.UpdateLectureCommand;
import com.wanted.codebombalms.lecture.application.policy.LectureCreationPolicy;
import com.wanted.codebombalms.lecture.application.port.CourseCatalogPort;
import com.wanted.codebombalms.lecture.application.usecase.LectureCommandUseCase;
import com.wanted.codebombalms.lecture.domain.exception.LectureErrorCode;
import com.wanted.codebombalms.lecture.domain.model.Lecture;
import com.wanted.codebombalms.lecture.domain.model.LectureStatus;
import com.wanted.codebombalms.lecture.domain.repository.LectureRepository;
import com.wanted.codebombalms.global.domain.common.error.exception.ConflictException;
import com.wanted.codebombalms.global.domain.common.error.exception.NotFoundException;
import com.wanted.codebombalms.global.domain.common.error.exception.ValidationException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
@Transactional
public class LectureCommandService implements LectureCommandUseCase {

    private static final Logger log = LoggerFactory.getLogger(LectureCommandService.class);
    private static final int YOUTUBE_VIDEO_ID_LENGTH = 11;

    private final LectureRepository lectureRepository;
    private final CourseCatalogPort courseCatalogPort;
    private final LectureCreationPolicy lectureCreationPolicy;

    @Override
    public Lecture createLecture(CreateLectureCommand command) {
        log.info("[LectureCommandService] create lecture - courseId: {}, title: {}", command.courseId(), command.title());

        Course course = courseCatalogPort.findCourse(command.courseId());
        lectureCreationPolicy.validate(course);
        validateLectureOrder(command.courseId(), null, command.lectureOrder());
        validateYoutubeVideoUrl(command.videoUrl());

        Lecture lecture = Lecture.create(
                course,
                command.title(),
                command.description(),
                command.videoUrl(),
                command.thumbnailUrl(),
                command.lectureOrder(),
                command.status()
        );

        return lectureRepository.save(lecture);
    }

    @Override
    public Lecture updateLecture(UpdateLectureCommand command) {
        log.info("[LectureCommandService] update lecture - lectureId: {}", command.lectureId());

        Lecture lecture = lectureRepository.findByLectureIdAndDeletedAtIsNull(command.lectureId())
                .orElseThrow(() -> new NotFoundException(LectureErrorCode.LECTURE_NOT_FOUND));

        if (command.status() == LectureStatus.DELETED) {
            throw new ValidationException(LectureErrorCode.LECTURE_DELETE_STATUS_REQUIRES_DELETE);
        }
        validateLectureOrder(
                lecture.getCourse().getCourseId(),
                lecture.getLectureId(),
                command.lectureOrder()
        );
        validateYoutubeVideoUrl(command.videoUrl());

        lecture.update(
                command.title(),
                command.description(),
                command.videoUrl(),
                command.thumbnailUrl(),
                command.lectureOrder(),
                command.status()
        );

        return lectureRepository.save(lecture);
    }

    @Override
    public void deleteLecture(Long lectureId) {
        log.info("[LectureCommandService] delete lecture - lectureId: {}", lectureId);

        Lecture lecture = lectureRepository.findByLectureIdAndDeletedAtIsNull(lectureId)
                .orElseThrow(() -> new NotFoundException(LectureErrorCode.LECTURE_NOT_FOUND));

        lecture.delete();
        lectureRepository.save(lecture);
    }

    private void validateLectureOrder(Long courseId, Long lectureId, Integer lectureOrder) {
        if (lectureOrder == null) {
            return;
        }

        boolean duplicated = lectureRepository.findByCourseIdAndDeletedAtIsNullOrderByLectureOrderAsc(courseId)
                .stream()
                .filter(lecture -> lectureId == null || !lecture.getLectureId().equals(lectureId))
                .anyMatch(lecture -> lecture.getLectureOrder().equals(lectureOrder));

        if (duplicated) {
            throw new ConflictException(LectureErrorCode.LECTURE_ORDER_DUPLICATED);
        }
    }

    private void validateYoutubeVideoUrl(String videoUrl) {
        if (videoUrl == null) {
            return;
        }
        if (videoUrl.isBlank() || !isValidYoutubeVideoUrl(videoUrl)) {
            throw new ValidationException(LectureErrorCode.INVALID_YOUTUBE_VIDEO_URL);
        }
    }

    private boolean isValidYoutubeVideoUrl(String videoUrl) {
        try {
            URI uri = new URI(videoUrl);
            String scheme = uri.getScheme();
            String host = uri.getHost();

            if (!isHttpScheme(scheme) || host == null) {
                return false;
            }

            String normalizedHost = host.toLowerCase();
            if ("youtu.be".equals(normalizedHost)) {
                return hasSingleVideoIdPath(uri.getPath());
            }

            if (!isYoutubeHost(normalizedHost)) {
                return false;
            }

            String path = uri.getPath();
            if ("/watch".equals(path)) {
                return hasSingleValidWatchVideoId(uri.getRawQuery());
            }
            if (path != null && path.startsWith("/embed/")) {
                return hasPrefixedVideoIdPath(path, "embed");
            }
            if (path != null && path.startsWith("/shorts/")) {
                return hasPrefixedVideoIdPath(path, "shorts");
            }
            return false;
        } catch (URISyntaxException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isHttpScheme(String scheme) {
        return "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
    }

    private boolean isYoutubeHost(String host) {
        return "youtube.com".equals(host) || "www.youtube.com".equals(host) || "m.youtube.com".equals(host);
    }

    private boolean hasSingleValidWatchVideoId(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return false;
        }

        int videoIdCount = 0;
        String videoId = null;
        for (String parameter : rawQuery.split("&")) {
            String[] keyValue = parameter.split("=", 2);
            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
            if (!"v".equals(key)) {
                continue;
            }

            videoIdCount++;
            videoId = keyValue.length > 1
                    ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8)
                    : "";
        }
        return videoIdCount == 1 && isValidYoutubeVideoId(videoId);
    }

    private boolean hasSingleVideoIdPath(String path) {
        if (path == null || !path.startsWith("/")) {
            return false;
        }

        String[] segments = path.substring(1).split("/", -1);
        return segments.length == 1 && isValidYoutubeVideoId(segments[0]);
    }

    private boolean hasPrefixedVideoIdPath(String path, String prefix) {
        String[] segments = path.substring(1).split("/", -1);
        return segments.length == 2 && prefix.equals(segments[0]) && isValidYoutubeVideoId(segments[1]);
    }

    private boolean isValidYoutubeVideoId(String videoId) {
        if (videoId == null || videoId.length() != YOUTUBE_VIDEO_ID_LENGTH) {
            return false;
        }
        for (int i = 0; i < videoId.length(); i++) {
            char ch = videoId.charAt(i);
            if (!Character.isLetterOrDigit(ch) && ch != '_' && ch != '-') {
                return false;
            }
        }
        return true;
    }
}
