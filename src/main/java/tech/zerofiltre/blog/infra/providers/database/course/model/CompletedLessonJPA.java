package tech.zerofiltre.blog.infra.providers.database.course.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "enrollment_completed_lessons")
public class CompletedLessonJPA {

    @EmbeddedId
    private CompletedLessonId id;

    @MapsId("enrollmentId")
    @ManyToOne(fetch = FetchType.LAZY)
    private EnrollmentJPA enrollment;

    @MapsId("lessonId")
    @ManyToOne(fetch = FetchType.LAZY)
    private LessonJPA lesson;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompletedLessonJPA)) return false;

        CompletedLessonJPA that = (CompletedLessonJPA) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }
}

