package com.wanted.codebombalms.course.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class CourseCategory {

    private Long courseCategoryId;
    private String name;
    private CourseCategoryStatus status;
    private Integer displayOrder;
    private LocalDateTime createdAt;

    public static CourseCategory restore(
            Long courseCategoryId,
            String name,
            CourseCategoryStatus status,
            Integer displayOrder,
            LocalDateTime createdAt
    ) {
        return new CourseCategory(courseCategoryId, name, status, displayOrder, createdAt);
    }
}
