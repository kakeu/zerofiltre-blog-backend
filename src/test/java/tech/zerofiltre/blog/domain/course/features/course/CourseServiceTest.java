package tech.zerofiltre.blog.domain.course.features.course;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tech.zerofiltre.blog.domain.article.TagProvider;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.article.model.Tag;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.doubles.*;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.article.model.Status.*;
import static tech.zerofiltre.blog.util.ZerofiltreUtils.TEST_COURSE_TITLE;

class CourseServiceTest {

    public static final String THIS_IS_MY_TITLE = "This is my title";

    private CourseProvider courseProvider;
    private TagProvider tagProvider;
    private ChapterProvider chapterProvider;
    private LoggerProvider loggerProvider;
    private Course course;
    private User editor;

    @BeforeEach
    void init() {
        course = new Course();
        editor = ZerofiltreUtils.createMockUser(false);
        chapterProvider = new FoundChapterProviderSpy();
    }


    @Test
    void should_init_course_properly() {
        //GIVEN
        courseProvider = new NotFoundCourseProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
        User user = ZerofiltreUtils.createMockUser(false);
        user.setId(1);
        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course course = courseService.init(THIS_IS_MY_TITLE, user);

        //THEN
        assertThat(course.getTitle()).isEqualTo(THIS_IS_MY_TITLE);
        assertThat(course.getStatus()).isEqualTo(DRAFT);
        LocalDateTime createdAt = course.getCreatedAt();
        assertThat(createdAt).isAfterOrEqualTo(beforeSave);
        assertThat(course.getLastSavedAt()).isEqualTo(createdAt);
        assertThat(course.getLastPublishedAt()).isNull();
        assertThat(course.getPublishedAt()).isNull();
        assertThat(course.getAuthor().getId()).isEqualTo(user.getId());
        assertThat(course.getAuthor().getEmail()).isEqualTo(user.getEmail());
        assertThat(((NotFoundCourseProviderSpy) courseProvider).registerCourseCalled).isTrue();
    }

    @Test
    void should_find_the_corresponding_courseBy_Id() throws ResourceNotFoundException, ForbiddenActionException {
        //GIVEN
        courseProvider = new Found_Published_WithUnknownAuthor_CourseProviderSpy();
        chapterProvider = new FoundChapterProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
        User user = ZerofiltreUtils.createMockUser(false);

        //WHEN
        Course found = courseService.findById(1, user);

        //THEN
        assertThat(((Found_Published_WithUnknownAuthor_CourseProviderSpy) courseProvider).courseOfIdCalled).isTrue();
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(45);
        assertThat(found.getTitle()).isEqualTo(TEST_COURSE_TITLE);
        assertThat(found.getStatus()).isEqualTo(PUBLISHED);
        assertThat(found.getAuthor().getId()).isEqualTo(21);
    }

    @Test
    void should_ThrowException_WhenCourseNotFound() {
        //GIVEN
        courseProvider = new NotFoundCourseProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.findById(1, editor));

        //THEN
        assertThat(((NotFoundCourseProviderSpy) courseProvider).courseOfIdCalled).isTrue();
    }

    @Test
    void publishOrSave_should_ThrowForbiddenActionException_when_UserIsNotAuthorNorAdmin() {
        //GIVEN
        courseProvider = new Found_Published_WithUnknownAuthor_CourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();
        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.save(new Course(), new User()));

        //THEN
        assertThat(((Found_Published_WithUnknownAuthor_CourseProviderSpy) courseProvider).registerCourseCalled).isFalse();
    }

    @Test
    void publishOrSave_should_putInReview_ifAuthor_notAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
        course.setStatus(PUBLISHED);

        //WHEN
        Course result = courseService.save(course, editor);

        //THEN
        assertThat(result.getStatus()).isEqualTo(IN_REVIEW);
    }

    @Test
    void publishOrSave_should_publish_ifIsAuthor_andIsAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();
        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
        course.setStatus(PUBLISHED);
        editor.getRoles().add("ROLE_ADMIN");


        //WHEN
        Course result = courseService.save(course, editor);

        //THEN
        assertThat(result.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    void publishOrSave_should_publish_ifNotAuthor_ButAdmin() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithUnknownAuthor_CourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
        course.setStatus(PUBLISHED);
        editor.getRoles().add("ROLE_ADMIN");

        //WHEN
        Course result = courseService.save(course, editor);


        //THEN
        assertThat(result.getStatus()).isEqualTo(PUBLISHED);

    }


    @Test
    void publishOrSave_should_ThrowResourceNotFoundException_ifTagsNotFound() {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();

        tagProvider = new NotFoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
        course.setStatus(PUBLISHED);
        course.setTags(List.of(new Tag(1, "tag1"), new Tag(2, "tag2")));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.save(course, editor));

        //THEN
        assertThat(((Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons) courseProvider).registerCourseCalled).isFalse();
        assertThat(((NotFoundTagProviderSpy) tagProvider).tagOfIdCalled).isTrue();
    }

    @Test
    void publishOrSave_should_setDatesAndStatusProperly_onSave() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);


        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course result = courseService.save(course, editor);

        LocalDateTime afterSave = LocalDateTime.now();

        //THEN
        assertThat(result.getLastSavedAt()).isBetween(beforeSave, afterSave);
        assertThat(result.getPublishedAt()).isNull();
        assertThat(result.getLastPublishedAt()).isNull();
        assertThat(result.getStatus()).isEqualTo(DRAFT);
    }

    @Test
    void publishOrSave_should_setDatesAndStatusProperly_onPublish_AlreadyPublished() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
        course.setStatus(PUBLISHED);


        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course result = courseService.save(course, editor);

        LocalDateTime afterSave = LocalDateTime.now();

        //THEN
        assertThat(result.getLastSavedAt()).isBetween(beforeSave, afterSave);
        assertThat(result.getPublishedAt()).isBefore(beforeSave);
        assertThat(result.getLastPublishedAt()).isEqualTo(result.getLastSavedAt());
        assertThat(result.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    void publishOrSave_should_setDatesAndStatusProperly_onReview() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
        course.setStatus(IN_REVIEW);

        LocalDateTime beforeSave = LocalDateTime.now();

        //WHEN
        Course result = courseService.save(course, editor);

        LocalDateTime afterSave = LocalDateTime.now();

        //THEN
        assertThat(result.getLastSavedAt()).isBetween(beforeSave, afterSave);
        assertThat(result.getPublishedAt()).isNull();
        assertThat(result.getLastPublishedAt()).isNull();
        assertThat(result.getStatus()).isEqualTo(IN_REVIEW);
    }

    @Test
    void publishOrSave_should_setStatusToDraft_whenSaving_InReviewCourse() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_InReview_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
        course.setStatus(DRAFT);

        //WHEN
        Course result = courseService.save(course, editor);


        //THEN
        assertThat(result.getStatus()).isEqualTo(DRAFT);
    }

    @Test
    void publishOrSave_should_setStatusToPublished_whenSaving_PublishedCourse() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
        course.setStatus(PUBLISHED);

        //WHEN
        Course result = courseService.save(course, editor);


        //THEN
        assertThat(result.getStatus()).isEqualTo(PUBLISHED);
    }

    @Test
    void publishOrSave_shouldKeepInDraft_ifNotPublished() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();


        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
        course.setStatus(DRAFT);

        //WHEN
        Course result = courseService.save(course, ZerofiltreUtils.createMockUser(false));


        //THEN
        assertThat(result.getStatus()).isEqualTo(DRAFT);

    }

    @Test
    void publishOrSave_byNonAdmin_shouldKeepInDraft_ifNotPublished() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);

        //WHEN
        Course result = courseService.save(course, editor);


        //THEN
        assertThat(result.getStatus()).isEqualTo(DRAFT);

    }

    @Test
    void publishOrSave_shouldThrow_ResourceNotFoundException_whenTagNotFound() {
        //GIVEN
        courseProvider = new Found_Draft_WithKnownAuthor_CourseProvider_Spy();

        tagProvider = new NotFoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
        course.setTags(List.of(new Tag(15, "tag")));

        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.save(course, editor));


    }

    @Test
    void delete_ShouldThrow_ResourceNotFoundException_WhenCourseNotFound() {
        //GIVEN
        courseProvider = new NotFoundCourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);

        ZerofiltreUtils.createMockUser(true);


        //WHEN
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> courseService.delete(15, new User()));

    }

    @Test
    void delete_ShouldThrow_ForbiddenActionException_WhenUserIsNotAdmin_NorAuthor() {
        //GIVEN
        courseProvider = new Found_Draft_WithUnknownAuthor_CourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.delete(15, new User()));
    }

    @Test
    void delete_should_callTheProvider() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = mock(CourseProvider.class);
        User author = new User();
        author.setEmail(editor.getEmail());
        course = ZerofiltreUtils.createMockCourse(true, Status.DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course.setEnrolledCount(0);
        when(courseProvider.courseOfId(anyLong())).thenReturn(Optional.ofNullable(course));

        tagProvider = new FoundTagProviderSpy();
        loggerProvider = new LoggerProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);

        //WHEN
        courseService.delete(15, editor);

        //THEN
        verify(courseProvider, times(1)).courseOfId(anyLong());
        assertThat(((LoggerProviderSpy) loggerProvider).logCalled).isTrue();
    }

    @Test
    void findByIdOnDraft_ShouldThrow_ForbiddenActionException_ifNotAdmin_NorAuthor() {
        //GIVEN
        courseProvider = new Found_Draft_WithUnknownAuthor_CourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.findById(15, new User()));
    }

    @Test
    void findByIdOnInReview_ShouldThrow_ForbiddenActionException_ifNotAdmin_NorAuthor() {
        //GIVEN
        courseProvider = new Found_InReview_WithUnknownAuthor_CourseProviderSpy();

        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);
        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.findById(15, new User()));
    }

    @Test
    void findById_ReturnCourse_IfUserNull() throws ForbiddenActionException, ResourceNotFoundException {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        tagProvider = new FoundTagProviderSpy();
        chapterProvider = new FoundChapterProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);

        //WHEN
        Course result = courseService.findById(15, null);
        //THEN
        assertThat(result).isNotNull();
    }

    @Test
    void findById_ThrowsException_IfUserNull_AndCourseIsNotPublished() {
        //GIVEN
        courseProvider = new Found_InReview_WithUnknownAuthor_CourseProviderSpy();
        tagProvider = new FoundTagProviderSpy();

        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);

        //WHEN & THEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.findById(15, null));
    }

    @Test
    void deleteThrowsForbiddenActionException_whenEnrolledCount_NotZero_evenForAnAdmin() {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        loggerProvider = new LoggerProviderSpy();
        editor.getRoles().add("ROLE_ADMIN");


        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.delete(15, editor));
    }

    @Test
    void deleteThrowsForbiddenActionException_ifCourseIsPublished_evenForAnAdmin() {
        //GIVEN
        courseProvider = new Found_Published_WithKnownAuthor_CourseProvider_Spy_And_2Lessons();
        loggerProvider = new LoggerProviderSpy();
        editor.getRoles().add("ROLE_ADMIN");


        CourseService courseService = new CourseService(courseProvider, tagProvider, loggerProvider);

        //WHEN
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> courseService.delete(15, editor))
                .withMessage("You are not allowed to delete this course as it is published");
    }
}