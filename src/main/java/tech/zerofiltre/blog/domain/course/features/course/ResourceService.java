package tech.zerofiltre.blog.domain.course.features.course;

import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.LessonProvider;
import tech.zerofiltre.blog.domain.course.ResourceProvider;
import tech.zerofiltre.blog.domain.course.model.Chapter;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Lesson;
import tech.zerofiltre.blog.domain.course.model.Resource;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.model.User;

import static tech.zerofiltre.blog.domain.error.ErrorMessages.DOES_NOT_EXIST;

public class ResourceService {

    private final ResourceProvider resourceProvider;
    private final LessonProvider lessonProvider;
    private final ChapterProvider chapterProvider;
    private final CourseProvider courseProvider;

    public ResourceService(ResourceProvider resourceProvider, LessonProvider lessonProvider, ChapterProvider chapterProvider, CourseProvider courseProvider) {
        this.resourceProvider = resourceProvider;
        this.lessonProvider = lessonProvider;
        this.chapterProvider = chapterProvider;
        this.courseProvider = courseProvider;
    }

    public Resource createResource(Resource resource, User user) throws ResourceNotFoundException, ForbiddenActionException {
        long lessonId = resource.getLessonId();
        Lesson lesson = lessonProvider.lessonOfId(lessonId)
                .orElseThrow(() -> new ResourceNotFoundException("The lesson of id " + lessonId + DOES_NOT_EXIST, String.valueOf(lessonId)));
        Chapter chapter = chapterProvider.chapterOfId(lesson.getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException("The chapter of id " + lesson.getChapterId() + DOES_NOT_EXIST, String.valueOf(lesson.getChapterId())));
        Course course = courseProvider.courseOfId(chapter.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("The course of id " + chapter.getCourseId() + DOES_NOT_EXIST, String.valueOf(chapter.getCourseId())));
        if (course.getAuthor().getId() != user.getId() && !user.isAdmin()) {
            throw new ForbiddenActionException("You are not allowed to create a resource for this lesson");
        }
        return resourceProvider.save(resource);
    }

    public void deleteResource(long resourceId, User user) throws ResourceNotFoundException, ForbiddenActionException {
        Resource resource = resourceProvider.resourceOfId(resourceId)
                .orElseThrow(() -> new ResourceNotFoundException("The resource of id " + resourceId + DOES_NOT_EXIST, String.valueOf(resourceId)));
        Lesson lesson = lessonProvider.lessonOfId(resource.getLessonId())
                .orElseThrow(() -> new ResourceNotFoundException("The lesson of id " + resource.getLessonId() + DOES_NOT_EXIST, String.valueOf(resource.getLessonId())));
        Chapter chapter = chapterProvider.chapterOfId(lesson.getChapterId())
                .orElseThrow(() -> new ResourceNotFoundException("The chapter of id " + lesson.getChapterId() + DOES_NOT_EXIST, String.valueOf(lesson.getChapterId())));
        Course course = courseProvider.courseOfId(chapter.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("The course of id " + chapter.getCourseId() + DOES_NOT_EXIST, String.valueOf(chapter.getCourseId())));
        if (course.getAuthor().getId() != user.getId() && !user.isAdmin()) {
            throw new ForbiddenActionException("You are not allowed to delete a resource for this lesson");
        }
        resourceProvider.delete(resourceId);

    }
}
