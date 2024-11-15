package tech.zerofiltre.blog.domain.course.features.enrollment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.course.*;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.database.user.UserJPARepository;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static tech.zerofiltre.blog.domain.article.model.Status.PUBLISHED;

@DataJpaTest
class FindEnrollmentIT {

    private FindEnrollment findEnrollment;

    @Autowired
    private EnrollmentJPARepository enrollmentJPARepository;

    @Autowired
    private UserJPARepository userJPARepository;

    @Autowired
    private CourseJPARepository courseJPARepository;
    @Autowired
    private ChapterJPARepository chapterJPARepository;

    @Test
    void findEnrollment_returns_properPage_forInActiveEnrollments() throws ZerofiltreException {
        //given
        User user = init2Enrollments(true, false);


        //when
        FinderRequest request = new FinderRequest(0, 2, null, user);
        request.setFilter(FinderRequest.Filter.INACTIVE);
        Page<Course> courses = findEnrollment.of(request);

        //then
        assertThat(courses).isNotNull();
        assertThat(courses.getContent()).isNotNull();
        assertThat(courses.getContent().size()).isEqualTo(1);
        assertThat(courses.getHasNext()).isFalse();
        assertThat(courses.getHasPrevious()).isFalse();
        assertThat(courses.getNumberOfElements()).isEqualTo(1);
        assertThat(courses.getPageNumber()).isZero();
        assertThat(courses.getPageSize()).isEqualTo(2);
        assertThat(courses.getTotalNumberOfElements()).isEqualTo(1);
        assertThat(courses.getTotalNumberOfPages()).isEqualTo(1);
    }

    @Test
    void findEnrollment_returns_properPage_forActiveEnrollments() throws ZerofiltreException {
        //given
        User user = init2Enrollments(false, false);


        //when
        FinderRequest request = new FinderRequest(0, 2, null, user);
        Page<Course> courses = findEnrollment.of(request);

        //then
        assertThat(courses).isNotNull();
        assertThat(courses.getContent()).isNotNull();
        assertThat(courses.getContent().size()).isEqualTo(2);

        courses.getContent().forEach(course -> assertThat(course.getEnrolledCount()).isOne());

        assertThat(courses.getHasNext()).isFalse();
        assertThat(courses.getHasPrevious()).isFalse();
        assertThat(courses.getNumberOfElements()).isEqualTo(2);
        assertThat(courses.getPageNumber()).isZero();
        assertThat(courses.getPageSize()).isEqualTo(2);
        assertThat(courses.getTotalNumberOfElements()).isEqualTo(2);
        assertThat(courses.getTotalNumberOfPages()).isEqualTo(1);
    }

    @Test
    void findEnrollment_returns_completedAndActiveEnrollments() throws ZerofiltreException {
        //given
        User user = init2Enrollments(false, true);


        //when
        FinderRequest request = new FinderRequest(0, 2, null, user);
        request.setFilter(FinderRequest.Filter.COMPLETED);
        Page<Course> courses = findEnrollment.of(request);

        //then
        assertThat(courses).isNotNull();
        assertThat(courses.getContent()).isNotNull();
        assertThat(courses.getContent().size()).isEqualTo(1);
        assertThat(courses.getHasNext()).isFalse();
        assertThat(courses.getHasPrevious()).isFalse();
        assertThat(courses.getNumberOfElements()).isEqualTo(1);
        assertThat(courses.getPageNumber()).isZero();
        assertThat(courses.getPageSize()).isEqualTo(2);
        assertThat(courses.getTotalNumberOfElements()).isEqualTo(1);
        assertThat(courses.getTotalNumberOfPages()).isEqualTo(1);
    }

    @Test
    void findEnrollment_returns_nothingOnNotMatch() throws ZerofiltreException {
        //given
        User user = init2Enrollments(false, false);


        //when
        FinderRequest request = new FinderRequest(0, 2, null, user);
        request.setFilter(FinderRequest.Filter.COMPLETED);
        Page<Course> courses = findEnrollment.of(request);

        //then
        assertThat(courses).isNotNull();
        assertThat(courses.getContent()).isNotNull();
        assertThat(courses.getContent().size()).isEqualTo(0);
        assertThat(courses.getHasNext()).isFalse();
        assertThat(courses.getHasPrevious()).isFalse();
        assertThat(courses.getNumberOfElements()).isEqualTo(0);
        assertThat(courses.getPageNumber()).isZero();
        assertThat(courses.getPageSize()).isEqualTo(2);
        assertThat(courses.getTotalNumberOfElements()).isEqualTo(0);
        assertThat(courses.getTotalNumberOfPages()).isEqualTo(0);
    }

    @Test
    void findEnrollment_returns_uncompletedAndActiveEnrollment() throws ZerofiltreException {
        //given
        User user = init2Enrollments(false, false);


        //when
        FinderRequest request = new FinderRequest(0, 2, null, user);
        Page<Course> courses = findEnrollment.of(request);

        //then
        assertThat(courses).isNotNull();
        assertThat(courses.getContent()).isNotNull();
        assertThat(courses.getContent().size()).isEqualTo(2);
        assertThat(courses.getHasNext()).isFalse();
        assertThat(courses.getHasPrevious()).isFalse();
        assertThat(courses.getNumberOfElements()).isEqualTo(2);
        assertThat(courses.getPageNumber()).isZero();
        assertThat(courses.getPageSize()).isEqualTo(2);
        assertThat(courses.getTotalNumberOfElements()).isEqualTo(2);
        assertThat(courses.getTotalNumberOfPages()).isEqualTo(1);
    }

    private User init2Enrollments(boolean withThe2ndOneInactive, boolean withThe2ndOneCompleted) throws ZerofiltreException {
        EnrollmentProvider enrollmentProvider = new DBEnrollmentProvider(enrollmentJPARepository);
        UserProvider userProvider = new DBUserProvider(userJPARepository);
        CourseProvider courseProvider = new DBCourseProvider(courseJPARepository, userJPARepository);
        ChapterProvider chapterProvider = new DBChapterProvider(chapterJPARepository);
        findEnrollment = new FindEnrollment(enrollmentProvider, courseProvider, chapterProvider);


        User author = ZerofiltreUtils.createMockUser(false);
        author.setPseudoName("author");
        author.setEmail("author@gmail.fr");
        User user = ZerofiltreUtils.createMockUser(false);
        user.setPseudoName("enrolled");
        user.setEmail("susbscriber@gamil.fr");


        author = userProvider.save(author);
        user = userProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);
        Course susPendedCourse = ZerofiltreUtils.createMockCourse(false, PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        susPendedCourse = courseProvider.save(susPendedCourse);

        Enrollment enrollment = ZerofiltreUtils.createMockEnrollment(false, user, course);
        enrollmentProvider.save(enrollment);

        Enrollment sencondEnrollment = ZerofiltreUtils.createMockEnrollment(false, user, susPendedCourse);
        sencondEnrollment.setActive(!withThe2ndOneInactive);
        sencondEnrollment.setCompleted(withThe2ndOneCompleted);
        enrollmentProvider.save(sencondEnrollment);
        return user;
    }
}
