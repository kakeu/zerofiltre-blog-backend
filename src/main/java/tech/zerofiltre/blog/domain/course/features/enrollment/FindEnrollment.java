package tech.zerofiltre.blog.domain.course.features.enrollment;

import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class FindEnrollment {

    private final EnrollmentProvider enrollmentProvider;
    private final CourseProvider courseProvider;
    private final ChapterProvider chapterProvider;

    public FindEnrollment(EnrollmentProvider enrollmentProvider, CourseProvider courseProvider, ChapterProvider chapterProvider) {
        this.enrollmentProvider = enrollmentProvider;
        this.courseProvider = courseProvider;
        this.chapterProvider = chapterProvider;
    }

    public Page<Course> of(FinderRequest request) {
        Page<Enrollment> enrollments = enrollmentProvider.of(request.getPageNumber(), request.getPageSize(), request.getUser().getId(), request.getFilter(), request.getTag());
        List<Course> courses = enrollments.getContent().stream().map(Enrollment::getCourse).collect(Collectors.toList());
        Page<Course> result = new Page<>();
        result.setPageSize(enrollments.getPageSize());
        courses.forEach(course -> {
            course.setEnrolledCount(getEnrolledCount(course.getId()));
            course.setLessonsCount(getLessonsCount(course.getId()));
        });
        result.setContent(courses);
        result.setHasNext(enrollments.getHasNext());
        result.setHasPrevious(enrollments.getHasPrevious());
        result.setTotalNumberOfPages(enrollments.getTotalNumberOfPages());
        result.setNumberOfElements(enrollments.getNumberOfElements());
        result.setTotalNumberOfElements(enrollments.getTotalNumberOfElements());
        result.setPageNumber(enrollments.getPageNumber());
        return result;
    }

    public Enrollment of(long courseId, long userId, User executor) throws ResourceNotFoundException, ForbiddenActionException {
        if (!executor.isAdmin() && executor.getId() != userId) {
            throw new ForbiddenActionException("You are only allow to look for your enrollments");
        }
        return enrollmentProvider.enrollmentOf(userId, courseId, true)
                .map(enrollment -> {
                    enrollment.getCourse().setEnrolledCount(getEnrolledCount(courseId));
                    enrollment.getCourse().setLessonsCount(getLessonsCount(courseId));
                    return enrollment;
                }).orElseThrow(() -> new ResourceNotFoundException("Enrollment not found", courseId + "/" + userId));
    }

    private int getLessonsCount(long courseId) {
        return chapterProvider.ofCourseId(courseId)
                .stream().mapToInt(chapter -> chapter.getLessons() == null ? 0 : chapter.getLessons().size()).sum();
    }

    private int getEnrolledCount(long courseId) {
        return courseProvider.getEnrolledCount(courseId);
    }

}
