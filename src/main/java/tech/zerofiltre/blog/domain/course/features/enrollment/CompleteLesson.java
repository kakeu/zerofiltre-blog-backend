package tech.zerofiltre.blog.domain.course.features.enrollment;

import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.course.model.*;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;

import java.time.LocalDateTime;
import java.util.List;

public class CompleteLesson {

    private final EnrollmentProvider enrollmentProvider;
    private final LessonProvider lessonProvider;
    private final ChapterProvider chapterProvider;
    private final CourseProvider courseProvider;

    public CompleteLesson(EnrollmentProvider enrollmentProvider, LessonProvider lessonProvider, ChapterProvider chapterProvider, CourseProvider courseProvider) {
        this.enrollmentProvider = enrollmentProvider;
        this.lessonProvider = lessonProvider;
        this.chapterProvider = chapterProvider;
        this.courseProvider = courseProvider;
    }

    public Enrollment execute(long courseId, long lessonId, long currentUserId, boolean completeLesson) throws ZerofiltreException {
        Enrollment existingEnrollment = enrollmentProvider.enrollmentOf(currentUserId, courseId, true)
                .orElseThrow(() -> new ResourceNotFoundException("There is no enrollment regarding the courseId and userId you submit", "Course Id = " + courseId + " " + "UserId = " + currentUserId));

        if (existingEnrollment.getCompletedLessons().stream().anyMatch(lesson -> lesson.getLessonId() == lessonId) == completeLesson && completeLesson)
            return computeCounts(existingEnrollment);

        Lesson lesson = lessonProvider.lessonOfId(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("Lesson of id " + lessonId + " does not exist", String.valueOf(lessonId)));

        ForbiddenActionException forbiddenActionException = new ForbiddenActionException("Lesson not part of this enrollment");

        Chapter chapter = chapterProvider.chapterOfId(lesson.getChapterId())
                .orElseThrow(() -> forbiddenActionException);

        if (chapter.getCourseId() != courseId) throw forbiddenActionException;

        existingEnrollment.setCompleted(false);

        if (completeLesson) {
            CompletedLesson completedLesson = new CompletedLesson();
            completedLesson.setLessonId(lesson.getId());
            completedLesson.setCompletedAt(LocalDateTime.now());
            completedLesson.setEnrollmentId(existingEnrollment.getId());
            existingEnrollment.getCompletedLessons().add(completedLesson);

            List<Long> allLessonIdNotCompleted = lessonProvider.listNotCompletedLessons(existingEnrollment.getId());
            if (allLessonIdNotCompleted.size() == 1 && allLessonIdNotCompleted.get(0) == lessonId) {
                existingEnrollment.setCompleted(true);
            }
        } else {
            existingEnrollment.getCompletedLessons().removeIf(existingLesson -> existingLesson.getLessonId() == lesson.getId());
        }

        Enrollment result = enrollmentProvider.save(existingEnrollment);
        return computeCounts(result);
    }

    private Enrollment computeCounts(Enrollment result) {
        Course resultCourse = result.getCourse();
        resultCourse.setEnrolledCount(getEnrolledCount(resultCourse.getId()));
        resultCourse.setLessonsCount(getLessonsCount(resultCourse.getId()));
        return result;
    }

    private int getLessonsCount(long courseId) {
        return chapterProvider.ofCourseId(courseId)
                .stream().mapToInt(chapter -> chapter.getLessons() == null ? 0 : chapter.getLessons().size()).sum();
    }


    private int getEnrolledCount(long courseId) {
        return courseProvider.getEnrolledCount(courseId);
    }

}
