package tech.zerofiltre.blog.domain.course.features.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.SectionProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.course.model.Section;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.article.DBTagProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBChapterProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBEnrollmentProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBSectionProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.logging.Slf4jLoggerProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;

@DataJpaTest
@Import({DBCourseProvider.class, DBUserProvider.class, DBSectionProvider.class, DBTagProvider.class, DBChapterProvider.class,
        Slf4jLoggerProvider.class, DBEnrollmentProvider.class})
class CourseServiceIT {

    public static final String UPDATED_TITLE = "This is the updated title";
    public static final String UPDATED_SUB_TITLE = "This is the updated sub title";
    public static final String UPDATED_SUMMARY = "This is the updated summary";
    public static final String UPDATED_VIDEO = "updated video";
    public static final String UPDATED_THUMBNAIL = "updated thumbnail";

    @Autowired
    private CourseProvider courseProvider;

    @Autowired
    private SectionProvider sectionProvider;

    @Autowired
    EnrollmentProvider enrollmentProvider;

    @Autowired
    private UserProvider userProvider;

    @Autowired
    private TagProvider tagProvider;

    @Autowired
    private LoggerProvider loggerProvider;

    @Autowired
    private ChapterProvider chapterProvider;

    private Course course;

    private User author;

    private List<Section> sections;
    private List<Tag> tags;

    @BeforeEach
    void init() {
        sections = new ArrayList<>();
        tags = new ArrayList<>();
    }


    @Test
    void save_and_init_are_OK() throws ForbiddenActionException, ResourceNotFoundException {
        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        ZerofiltreUtils.createMockTags(false)
                .forEach(tag -> tags.add(tagProvider.save(tag)));

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);

        course = courseService.init("some title", author);

        Section section1 = ZerofiltreUtils.createMockSection(course.getId(), sectionProvider, courseProvider, false);
        section1.init(author);

        course.setTags(tags);
        course.setSections(List.of(section1));
        course.setTitle(UPDATED_TITLE);
        course.setStatus(Status.PUBLISHED);
        course.setThumbnail(UPDATED_THUMBNAIL);
        course.setVideo(UPDATED_VIDEO);
        course.setSummary(UPDATED_SUMMARY);
        course.setSubTitle(UPDATED_SUB_TITLE);

        course = courseService.save(course, author);

        assertThat(course.getId()).isNotZero();
        assertThat(course.getTitle()).isEqualTo(UPDATED_TITLE);
        assertThat(course.getSubTitle()).isEqualTo(UPDATED_SUB_TITLE);
        assertThat(course.getSummary()).isEqualTo(UPDATED_SUMMARY);
        assertThat(course.getVideo()).isEqualTo(UPDATED_VIDEO);
        assertThat(course.getThumbnail()).isEqualTo(UPDATED_THUMBNAIL);
        assertThat(course.getStatus()).isEqualTo(Status.IN_REVIEW);

        assertThat(
                course.getSections().stream().allMatch(section -> sections.stream().anyMatch(s -> s.getId() == section.getId()))
        ).isTrue();

        assertThat(
                course.getTags().stream().allMatch(tag -> tags.stream().anyMatch(tag1 -> tag1.getId() == tag.getId()))
        ).isTrue();
    }

    @Test
    void getCourseById_isOK() throws ResourceNotFoundException, ForbiddenActionException {
        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);


        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
        Course course = courseService.init("some title", author);

        assertThat(courseService.findById(course.getId(), author)).isNotNull();
    }

    @Test
    @Disabled("to fix")
    void deleteCourse_isOK() throws ResourceNotFoundException, ForbiddenActionException {
        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        ZerofiltreUtils.createMockSections(sectionProvider, courseProvider, false)
                .forEach(section -> sections.add(sectionProvider.save(section)));

        ZerofiltreUtils.createMockTags(false)
                .forEach(tag -> tags.add(tagProvider.save(tag)));

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
        course = courseService.init("some title", author);

        courseService.delete(course.getId(), author);

        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.findById(course.getId(), author));
    }

    @Test
    void enroll_increases_EnrolledCount() throws ZerofiltreException {
        author = ZerofiltreUtils.createMockUser(false);
        author = userProvider.save(author);

        User student1 = ZerofiltreUtils.createMockUser(false);
        student1.setEmail("bable@gma.fr");
        student1.setPseudoName("bable");
        student1 = userProvider.save(student1);

        User student2 = ZerofiltreUtils.createMockUser(false);
        student2.setEmail("poseidon@gma.fr");
        student2.setPseudoName("poseidon");
        student2 = userProvider.save(student2);

        User student3 = ZerofiltreUtils.createMockUser(false);
        student3.setEmail("chaka@gma.fr");
        student3.setPseudoName("zulu");
        student3 = userProvider.save(student3);

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
        course = courseService.init("some title", author);
        course.setStatus(Status.PUBLISHED);
        course = courseService.save(course, author);


        Enrollment enrollment1 = new Enrollment();
        enrollment1.setCourse(course);
        enrollment1.setUser(student1);
        enrollmentProvider.save(enrollment1);

        Enrollment enrollment2 = new Enrollment();
        enrollment2.setCourse(course);
        enrollment2.setUser(student2);
        enrollmentProvider.save(enrollment2);

        course = courseProvider.courseOfId(course.getId()).get();

        long enrolledCount = course.getEnrolledCount();

        assertThat(enrolledCount).isEqualTo(2);


        Enrollment suspendedEnrollment = new Enrollment();
        suspendedEnrollment.setCourse(course);
        suspendedEnrollment.setUser(student3);
        enrollmentProvider.save(suspendedEnrollment);

        course = courseProvider.courseOfId(course.getId()).get();
        enrolledCount = course.getEnrolledCount();

        assertThat(enrolledCount).isEqualTo(3);
    }
}
