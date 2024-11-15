package tech.zerofiltre.blog.domain.course.features.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
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
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.article.DBTagProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBChapterProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBLessonProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBResourceProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.logging.Slf4jLoggerProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import({DBCourseProvider.class, DBUserProvider.class, DBChapterProvider.class, DBLessonProvider.class, DBResourceProvider.class, Slf4jLoggerProvider.class, DBTagProvider.class})
public class ResourceServiceIT {

    private ResourceService resourceService;
    @Autowired
    private ResourceProvider resourceProvider;
    @Autowired
    private LessonProvider lessonProvider;
    @Autowired
    private ChapterProvider chapterProvider;
    @Autowired
    private CourseProvider courseProvider;

    @Autowired
    private UserProvider userProvider;

    @BeforeEach
    void setUp() {
        resourceService = new ResourceService(resourceProvider, lessonProvider, chapterProvider, courseProvider);
    }

    @Test
    void should_create_resource() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        User user = ZerofiltreUtils.createMockUser(false);
        user = userProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Chapter chapter = ZerofiltreUtils.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        Lesson lesson = Lesson.builder()
                .chapterId(chapter.getId())
                .title("title")
                .build();
        lesson = lessonProvider.save(lesson);

        Resource resource = Resource.builder()
                .name("name")
                .url("url")
                .lessonId(lesson.getId())
                .build();

        //when
        Resource savedResource = resourceService.createResource(resource, user);

        //then
        assertThat(savedResource.getId()).isNotZero();
    }


    @Test
    void should_delete_resource() throws ForbiddenActionException, ResourceNotFoundException {
        //given
        User user = ZerofiltreUtils.createMockUser(false);
        user = userProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.DRAFT, user, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        Chapter chapter = ZerofiltreUtils.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        Lesson lesson = Lesson.builder()
                .chapterId(chapter.getId())
                .title("title")
                .build();
        lesson = lessonProvider.save(lesson);

        Resource resource = Resource.builder()
                .name("name")
                .url("url")
                .lessonId(lesson.getId())
                .build();
        resource = resourceProvider.save(resource);
        assertThat(resource.getId()).isNotZero();

        //when
        resourceService.deleteResource(resource.getId(), user);

        //then
        Optional<Resource> notFoundResource = resourceProvider.resourceOfId(resource.getId());
        assertThat(notFoundResource).isEmpty();
    }


}
