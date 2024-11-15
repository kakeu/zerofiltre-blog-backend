package tech.zerofiltre.blog.infra.providers.database.course.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import tech.zerofiltre.blog.domain.course.model.CompletedLesson;
import tech.zerofiltre.blog.infra.providers.database.course.model.CompletedLessonJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.EnrollmentJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.LessonJPA;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.UserJPAMapper;

@Mapper(uses = UserJPAMapper.class)
public interface CompletedLessonJPAMapper {

    @Mapping(target = "enrollment", source = "enrollmentId", qualifiedByName = "enrollmentFromId")
    @Mapping(target = "lesson", source = "lessonId", qualifiedByName = "lessonFromId")
    @Mapping(target = "id.enrollmentId", source = "enrollmentId")
    @Mapping(target = "id.lessonId", source = "lessonId")
    CompletedLessonJPA toJPA(CompletedLesson completedLesson);

    @Mapping(target = "enrollmentId", source = "enrollment.id")
    @Mapping(target = "lessonId", source = "lesson.id")
    CompletedLesson fromJPA(CompletedLessonJPA completedLessonJPA);

    @Named("enrollmentFromId")
    default EnrollmentJPA enrollmentFromId(long enrollmentId) {
        EnrollmentJPA enrollmentJPA = new EnrollmentJPA();
        enrollmentJPA.setId(enrollmentId);
        return enrollmentJPA;
    }

    @Named("lessonFromId")
    default LessonJPA lessonFromId(long lessonId) {
        LessonJPA result = new LessonJPA();
        result.setId(lessonId);
        return result;
    }
}
