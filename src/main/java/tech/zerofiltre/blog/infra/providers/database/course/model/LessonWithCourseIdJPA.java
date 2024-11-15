package tech.zerofiltre.blog.infra.providers.database.course.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class LessonWithCourseIdJPA {
    private LessonJPA lesson;
    private Long courseId;
}
