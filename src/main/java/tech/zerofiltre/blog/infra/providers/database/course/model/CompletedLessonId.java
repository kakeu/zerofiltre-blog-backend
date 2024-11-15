package tech.zerofiltre.blog.infra.providers.database.course.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

@Data
@Embeddable
public class CompletedLessonId implements Serializable {
    @Column(name = "enrollment_id")
    private long enrollmentId;

    @Column(name = "lesson_id")
    private long lessonId;


}
