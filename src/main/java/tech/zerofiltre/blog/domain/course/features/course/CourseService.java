package tech.zerofiltre.blog.domain.course.features.course;

import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.UnAuthenticatedActionException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.logging.model.LogEntry;
import tech.zerofiltre.blog.domain.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

import static tech.zerofiltre.blog.domain.article.model.Status.PUBLISHED;

public class CourseService {

    public static final String DOES_NOT_EXIST = " does not exist";
    public static final String THE_COURSE_WITH_ID = "The course with id: ";

    private final CourseProvider courseProvider;

    private final TagProvider tagProvider;
    private final LoggerProvider loggerProvider;


    public CourseService(CourseProvider courseProvider, TagProvider tagProvider, LoggerProvider loggerProvider) {
        this.courseProvider = courseProvider;
        this.tagProvider = tagProvider;
        this.loggerProvider = loggerProvider;
    }


    public Course init(String title, User author) {
        Course course = new Course();
        course.setTitle(title);
        course.setAuthor(author);
        course.setCreatedAt(LocalDateTime.now());
        course.setLastSavedAt(course.getCreatedAt());
        return courseProvider.save(course);
    }

    public Course findById(long id, User viewer) throws ResourceNotFoundException, ForbiddenActionException {
        Course foundCourse = courseProvider.courseOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id)));


        if ((viewer == null && Status.PUBLISHED != foundCourse.getStatus())
                || (viewer != null && !viewer.isAdmin() && isNotAuthor(viewer, foundCourse) && foundCourse.getStatus() != Status.PUBLISHED)) {
            throw new ForbiddenActionException("You are not allowed to access this course (that you do not own) as it is not yet published");
        }
        return foundCourse;
    }

    public Course save(Course updatedCourse, User currentEditor) throws ResourceNotFoundException, ForbiddenActionException {

        Status statusToSave = updatedCourse.getStatus();
        LocalDateTime now = LocalDateTime.now();

        long updatedCourseId = updatedCourse.getId();
        Course existingCourse = courseProvider.courseOfId(updatedCourseId)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + updatedCourseId + DOES_NOT_EXIST, String.valueOf(updatedCourseId)));
        if (isNotAuthor(currentEditor, existingCourse) && !currentEditor.isAdmin())
            throw new ForbiddenActionException("You are not allowed to edit this course");

        if (!isAlreadyPublished(existingCourse.getStatus()) && isTryingToPublish(statusToSave) && !currentEditor.isAdmin())
            existingCourse.setStatus(Status.IN_REVIEW);

        if (!isAlreadyPublished(existingCourse.getStatus()) && (!isTryingToPublish(statusToSave) || currentEditor.isAdmin()))
            existingCourse.setStatus(updatedCourse.getStatus());

        if (isAlreadyPublished(existingCourse.getStatus())) {
            if (existingCourse.getPublishedAt() == null) existingCourse.setPublishedAt(now);
            existingCourse.setLastPublishedAt(now);
        }

        existingCourse.setLastSavedAt(now);
        existingCourse.setTitle(updatedCourse.getTitle());
        existingCourse.setSubTitle(updatedCourse.getSubTitle());
        existingCourse.setSummary(updatedCourse.getSummary());
        existingCourse.setThumbnail(updatedCourse.getThumbnail());
        existingCourse.setVideo(updatedCourse.getVideo());
        checkTags(updatedCourse.getTags());
        existingCourse.setTags(updatedCourse.getTags());
        return courseProvider.save(existingCourse);
    }

    public void delete(long id, User deleter) throws ResourceNotFoundException, ForbiddenActionException {

        Course existingCourse = courseProvider.courseOfId(id)
                .orElseThrow(() -> new ResourceNotFoundException(THE_COURSE_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id)));

        if (existingCourse.getStatus().equals(Status.PUBLISHED))
            throw new ForbiddenActionException("You are not allowed to delete this course as it is published");

        if (existingCourse.getEnrolledCount() > 0)
            throw new ForbiddenActionException("You are not allowed to delete this course as it has enrolled users");

        if (isNotAuthor(deleter, existingCourse) && !deleter.isAdmin())
            throw new ForbiddenActionException("You are not allowed to delete this course");
        courseProvider.delete(existingCourse);

        LogEntry logEntry = new LogEntry(LogEntry.Level.INFO, "Deleting course " + id + " for done", null, Course.class);
        loggerProvider.log(logEntry);
    }

    public Page<Course> of(FinderRequest request) throws ForbiddenActionException, UnAuthenticatedActionException {
        User user = request.getUser();

        boolean unAuthenticatedUserGettingNonPublishedCourses = !PUBLISHED.equals(request.getStatus())
                && user == null
                && !request.isYours();

        if (unAuthenticatedUserGettingNonPublishedCourses) {
            throw new UnAuthenticatedActionException("The user token might be expired, try to refresh it. ");
        }

        boolean nonAdminGettingNonPublishedCourses = !PUBLISHED.equals(request.getStatus())
                && (user == null || !user.getRoles().contains("ROLE_ADMIN"))
                && !request.isYours();

        if (nonAdminGettingNonPublishedCourses) {
            throw new ForbiddenActionException("You are not authorize to request courses other than the published ones with this API. " +
                    "Please request with status=published or try /user/courses/* API resources");
        }

        long authorId = request.isYours() ? request.getUser().getId() : 0;
        return courseProvider.courseOf(request.getPageNumber(), request.getPageSize(), request.getStatus(), authorId, request.getFilter(), request.getTag());
    }


    private boolean isNotAuthor(User currentEditor, Course course) {
        return !course.getAuthor().getEmail().equals(currentEditor.getEmail());
    }

    private boolean isAlreadyPublished(Status status) {
        return status.equals(Status.PUBLISHED);
    }


    private boolean isTryingToPublish(Status status) {
        return status == Status.PUBLISHED || status == Status.IN_REVIEW;
    }

    private void checkTags(List<Tag> tags) throws ResourceNotFoundException {
        for (Tag tag : tags) {
            if (tagProvider.tagOfId(tag.getId()).isEmpty())
                throw new ResourceNotFoundException("We can not publish this course. Could not find the related tag with id: " + tag.getId(), String.valueOf(tag.getId()));
        }
    }
}
