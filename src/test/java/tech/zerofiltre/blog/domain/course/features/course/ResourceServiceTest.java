package tech.zerofiltre.blog.domain.course.features.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.article.model.Status;
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
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;


@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {


    ResourceService resourceService;
    @Mock
    ResourceProvider resourceProvider;
    @Mock
    LessonProvider lessonProvider;
    @Mock
    private ChapterProvider chapterProvider;
    @Mock
    private CourseProvider courseProvider;

    @BeforeEach
    void init() {
        resourceService = new ResourceService(resourceProvider, lessonProvider, chapterProvider, courseProvider);
    }


    @Test
    void createResource_throwsResourceNotFoundException_whenLessonIsNotFound() {
        //given
        Resource resource = Resource.builder()
                .lessonId(1L)
                .build();
        Mockito.when(lessonProvider.lessonOfId(resource.getLessonId())).thenReturn(Optional.empty());

        //when & then
        assertThrows(ResourceNotFoundException.class, () -> resourceService.createResource(resource, new User()));
    }

    @Test
    void createResource_isForbidden_whenUserIsNotAuthor_NorAdmin() {
        //given
        User user = ZerofiltreUtils.createMockUser(false);
        user.setId(999);
        Course course = ZerofiltreUtils.createMockCourse(true, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        Chapter chapter = ZerofiltreUtils.createMockChapter(true, chapterProvider, Collections.emptyList(), course.getId());
        Lesson lesson = Lesson.builder()
                .id(1L)
                .chapterId(chapter.getId())
                .build();

        Resource resource = Resource.builder()
                .lessonId(1L)
                .build();

        Mockito.when(lessonProvider.lessonOfId(resource.getLessonId())).thenReturn(Optional.of(lesson));
        Mockito.when(chapterProvider.chapterOfId(lesson.getChapterId())).thenReturn(Optional.of(chapter));
        Mockito.when(courseProvider.courseOfId(chapter.getCourseId())).thenReturn(Optional.of(course));

        //when & then
        assertThrows(ForbiddenActionException.class, () -> resourceService.createResource(resource, user));
    }

    @Test
    void createResourceIsOk_ifUserIsAdmin_ButNotAuthor() {
        //given
        User user = ZerofiltreUtils.createMockUser(true);
        user.setId(999);
        Course course = ZerofiltreUtils.createMockCourse(true, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        Chapter chapter = ZerofiltreUtils.createMockChapter(true, chapterProvider, Collections.emptyList(), course.getId());
        Lesson lesson = Lesson.builder()
                .id(1L)
                .chapterId(chapter.getId())
                .build();

        Resource resource = Resource.builder()
                .lessonId(1L)
                .build();

        Mockito.when(lessonProvider.lessonOfId(resource.getLessonId())).thenReturn(Optional.of(lesson));
        Mockito.when(chapterProvider.chapterOfId(lesson.getChapterId())).thenReturn(Optional.of(chapter));
        Mockito.when(courseProvider.courseOfId(chapter.getCourseId())).thenReturn(Optional.of(course));

        //when & then
        assertDoesNotThrow(() -> resourceService.createResource(resource, user));
    }

    @Test
    void createResourceIsOk_ifUserIsAuthor_ButNotAdmin() {
        //given
        User user = ZerofiltreUtils.createMockUser(false);
        user.setId(999);
        Course course = ZerofiltreUtils.createMockCourse(true, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        Chapter chapter = ZerofiltreUtils.createMockChapter(true, chapterProvider, Collections.emptyList(), course.getId());
        Lesson lesson = Lesson.builder()
                .id(1L)
                .chapterId(chapter.getId())
                .build();

        Resource resource = Resource.builder()
                .lessonId(1L)
                .build();

        Mockito.when(lessonProvider.lessonOfId(resource.getLessonId())).thenReturn(Optional.of(lesson));
        Mockito.when(chapterProvider.chapterOfId(lesson.getChapterId())).thenReturn(Optional.of(chapter));
        Mockito.when(courseProvider.courseOfId(chapter.getCourseId())).thenReturn(Optional.of(course));

        //when & then
        assertDoesNotThrow(() -> resourceService.createResource(resource, user));
    }

    @Test
    void createResource_SavesResource_properly() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        User user = ZerofiltreUtils.createMockUser(false);
        user.setId(999);
        Course course = ZerofiltreUtils.createMockCourse(true, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        Chapter chapter = ZerofiltreUtils.createMockChapter(true, chapterProvider, Collections.emptyList(), course.getId());
        Lesson lesson = Lesson.builder()
                .id(1L)
                .chapterId(chapter.getId())
                .build();

        Resource resource = Resource.builder()
                .lessonId(1L)
                .build();

        Mockito.when(lessonProvider.lessonOfId(resource.getLessonId())).thenReturn(Optional.of(lesson));
        Mockito.when(chapterProvider.chapterOfId(lesson.getChapterId())).thenReturn(Optional.of(chapter));
        Mockito.when(courseProvider.courseOfId(chapter.getCourseId())).thenReturn(Optional.of(course));

        //when
        resourceService.createResource(resource, user);

        //then
        Mockito.verify(resourceProvider, Mockito.times(1)).save(resource);
    }

    @Test
    void deleteResource_throwsResourceNotFoundException_whenResourceIsNotFound() {
        //given
        Mockito.when(resourceProvider.resourceOfId(1L)).thenReturn(Optional.empty());

        //when & then
        assertThrows(ResourceNotFoundException.class, () -> resourceService.deleteResource(1L, new User()));
    }

    @Test
    void deleteResource_isForbidden_whenUserIsNotAuthor_NorAdmin() {
        //given
        User user = ZerofiltreUtils.createMockUser(false);
        user.setId(999);
        Course course = ZerofiltreUtils.createMockCourse(true, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        Chapter chapter = ZerofiltreUtils.createMockChapter(true, chapterProvider, Collections.emptyList(), course.getId());
        Lesson lesson = Lesson.builder()
                .id(1L)
                .chapterId(chapter.getId())
                .build();

        Resource resource = Resource.builder()
                .lessonId(1L)
                .build();

        Mockito.when(resourceProvider.resourceOfId(1L)).thenReturn(Optional.of(resource));
        Mockito.when(lessonProvider.lessonOfId(resource.getLessonId())).thenReturn(Optional.of(lesson));
        Mockito.when(chapterProvider.chapterOfId(lesson.getChapterId())).thenReturn(Optional.of(chapter));
        Mockito.when(courseProvider.courseOfId(chapter.getCourseId())).thenReturn(Optional.of(course));

        //when & then
        assertThrows(ForbiddenActionException.class, () -> resourceService.deleteResource(1L, user));
    }

    @Test
    void deleteResource_isOk_whenUserIsAuthor() {
        //given
        User user = ZerofiltreUtils.createMockUser(false);
        user.setId(999);
        Course course = ZerofiltreUtils.createMockCourse(true, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        Chapter chapter = ZerofiltreUtils.createMockChapter(true, chapterProvider, Collections.emptyList(), course.getId());
        Lesson lesson = Lesson.builder()
                .id(1L)
                .chapterId(chapter.getId())
                .build();

        Resource resource = Resource.builder()
                .lessonId(1L)
                .build();

        Mockito.when(resourceProvider.resourceOfId(1L)).thenReturn(Optional.of(resource));
        Mockito.when(lessonProvider.lessonOfId(resource.getLessonId())).thenReturn(Optional.of(lesson));
        Mockito.when(chapterProvider.chapterOfId(lesson.getChapterId())).thenReturn(Optional.of(chapter));
        Mockito.when(courseProvider.courseOfId(chapter.getCourseId())).thenReturn(Optional.of(course));

        //when & then
        assertDoesNotThrow(() -> resourceService.deleteResource(1L, user));
    }

    @Test
    void deleteResource_isOk_whenUserIsAdmin() {
        //given
        User user = ZerofiltreUtils.createMockUser(true);
        user.setId(999);
        Course course = ZerofiltreUtils.createMockCourse(true, Status.DRAFT, new User(), Collections.emptyList(), Collections.emptyList());
        Chapter chapter = ZerofiltreUtils.createMockChapter(true, chapterProvider, Collections.emptyList(), course.getId());
        Lesson lesson = Lesson.builder()
                .id(1L)
                .chapterId(chapter.getId())
                .build();

        Resource resource = Resource.builder()
                .lessonId(1L)
                .build();

        Mockito.when(resourceProvider.resourceOfId(1L)).thenReturn(Optional.of(resource));
        Mockito.when(lessonProvider.lessonOfId(resource.getLessonId())).thenReturn(Optional.of(lesson));
        Mockito.when(chapterProvider.chapterOfId(lesson.getChapterId())).thenReturn(Optional.of(chapter));
        Mockito.when(courseProvider.courseOfId(chapter.getCourseId())).thenReturn(Optional.of(course));

        //when & then
        assertDoesNotThrow(() -> resourceService.deleteResource(1L, user));
    }

}