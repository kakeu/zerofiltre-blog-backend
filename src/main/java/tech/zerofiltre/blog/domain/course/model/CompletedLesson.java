package tech.zerofiltre.blog.domain.course.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CompletedLesson {

    private long enrollmentId;

    private long lessonId;

    private LocalDateTime completedAt;
}
