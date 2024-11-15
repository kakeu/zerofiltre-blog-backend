package tech.zerofiltre.blog.domain.course.features.enrollment;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.FinderRequest;
import tech.zerofiltre.blog.domain.Page;
import tech.zerofiltre.blog.domain.article.model.Status;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Course;
import tech.zerofiltre.blog.domain.course.model.Enrollment;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.PurchaseProvider;
import tech.zerofiltre.blog.domain.purchase.model.Purchase;
import tech.zerofiltre.blog.domain.sandbox.SandboxProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.course.DBChapterProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBEnrollmentProvider;
import tech.zerofiltre.blog.infra.providers.database.purchase.DBPurchaseProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@DataJpaTest
@Import({DBEnrollmentProvider.class, DBCourseProvider.class, DBUserProvider.class, DBChapterProvider.class, DBCourseProvider.class, DBPurchaseProvider.class})
class SuspendIT {

    Suspend suspend;

    Enroll enroll;

    @Autowired
    EnrollmentProvider enrollmentProvider;

    @Autowired
    DBUserProvider dbUserProvider;

    @Autowired
    DBCourseProvider dbCourseProvider;

    @Autowired
    ChapterProvider chapterProvider;

    @Autowired
    PurchaseProvider purchaseProvider;

    FindEnrollment findEnrollment;
    @Mock
    SandboxProvider sandboxProvider;


    @BeforeEach
    void init() throws ZerofiltreException {
        doNothing().when(sandboxProvider).destroy(any(), any());
        suspend = new Suspend(enrollmentProvider, chapterProvider, purchaseProvider, sandboxProvider);
        enroll = new Enroll(enrollmentProvider, dbCourseProvider, dbUserProvider, chapterProvider, sandboxProvider, null);
        findEnrollment = new FindEnrollment(enrollmentProvider, dbCourseProvider, chapterProvider);
    }

    @Test
    void suspendSavesEnrollmentProperly() throws ZerofiltreException {
        User author = ZerofiltreUtils.createMockUser(false);
        author = dbUserProvider.save(author);

        User user = ZerofiltreUtils.createMockUser(true);
        user.setEmail("test@gmail.grok");
        user.setPseudoName("tester");
        user = dbUserProvider.save(user);

        Course course = ZerofiltreUtils.createMockCourse(false, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        course = dbCourseProvider.save(course);
        enroll.execute(user.getId(), course.getId());
        LocalDateTime beforeSuspend = LocalDateTime.now();
        Enrollment enrollment = suspend.execute(user.getId(), course.getId());
        LocalDateTime afterSuspend = LocalDateTime.now();

        assertThat(enrollment).isNotNull();
        assertThat(enrollment.getUser().getId()).isEqualTo(user.getId());
        assertThat(enrollment.getUser().getEmail()).isEqualTo(user.getEmail());
        assertThat(enrollment.getUser().getPseudoName()).isEqualTo(user.getPseudoName());
        assertThat(enrollment.getCourse().getId()).isEqualTo(course.getId());

        assertThat(enrollment.getId()).isNotZero();

        org.assertj.core.api.Assertions.assertThat(enrollment.getSuspendedAt()).isNotNull();
        org.assertj.core.api.Assertions.assertThat(enrollment.getSuspendedAt()).isAfterOrEqualTo(beforeSuspend);
        org.assertj.core.api.Assertions.assertThat(enrollment.getEnrolledAt()).isBefore(enrollment.getSuspendedAt());
        org.assertj.core.api.Assertions.assertThat(enrollment.getSuspendedAt()).isBeforeOrEqualTo(afterSuspend);
        Assertions.assertThat(enrollment.isActive()).isFalse();
    }

    @Test
    void suspendAllDeactivatesTheProperEnrollments() throws ZerofiltreException {
        User user = ZerofiltreUtils.createMockUser(false);
        User author = ZerofiltreUtils.createMockUser(false);
        user.setPlan(User.Plan.PRO);
        user = dbUserProvider.save(user);

        author.setEmail("test@mail.com");
        author.setPseudoName("test@mail.com");
        author = dbUserProvider.save(author);

        Course courseBasic = ZerofiltreUtils.createMockCourse(true, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        courseBasic = dbCourseProvider.save(courseBasic);

        Course coursePro1 = ZerofiltreUtils.createMockCourse(true, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        coursePro1 = dbCourseProvider.save(coursePro1);

        Course coursePro2 = ZerofiltreUtils.createMockCourse(true, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList());
        coursePro2 = dbCourseProvider.save(coursePro2);


        Enrollment enrollmentForLife = new Enrollment();
        enrollmentForLife.setUser(user);
        enrollmentForLife.setCourse(courseBasic);
        enrollmentForLife.setForLife(true);
        enrollmentProvider.save(enrollmentForLife);

        Enrollment enrollmentNotForLife1 = new Enrollment();
        enrollmentNotForLife1.setUser(user);
        enrollmentNotForLife1.setCourse(coursePro1);
        enrollmentNotForLife1.setForLife(false);
        enrollmentProvider.save(enrollmentNotForLife1);

        Enrollment enrollmentNotForLife2 = new Enrollment();
        enrollmentNotForLife2.setUser(user);
        enrollmentNotForLife2.setCourse(coursePro2);
        enrollmentNotForLife2.setForLife(false);
        enrollmentProvider.save(enrollmentNotForLife2);


        FinderRequest request = new FinderRequest();
        request.setPageNumber(0);
        request.setPageSize(5);
        request.setUser(user);


        Page<Course> enrollments = findEnrollment.of(request);
        Assertions.assertThat(enrollments.getContent()).hasSize(3);

        //when
        suspend.all(user.getId(), false);


        //then
        enrollments = findEnrollment.of(request);
        Assertions.assertThat(enrollments.getContent()).hasSize(1);


    }

    @Test
    void suspendAllDeletesPurchaseProperly() throws ZerofiltreException {
        User user = ZerofiltreUtils.createMockUser(false);
        User author = ZerofiltreUtils.createMockUser(false);
        user = dbUserProvider.save(user);

        author.setEmail("test@mail.com");
        author.setPseudoName("test@mail.com");
        author = dbUserProvider.save(author);

        Course courseBasic = ZerofiltreUtils.createMockCourse(true, Status.PUBLISHED, author, Collections.emptyList(), Collections.emptyList(), true);
        courseBasic = dbCourseProvider.save(courseBasic);

        Enrollment enrollmentForLife = new Enrollment();
        enrollmentForLife.setUser(user);
        enrollmentForLife.setCourse(courseBasic);
        enrollmentForLife.setForLife(true);
        enrollmentProvider.save(enrollmentForLife);

        Purchase purchase = new Purchase(user, courseBasic);
        purchase = purchaseProvider.save(purchase);

        FinderRequest request = new FinderRequest();
        request.setPageNumber(0);
        request.setPageSize(5);
        request.setUser(user);


        Page<Course> enrollments = findEnrollment.of(request);
        Assertions.assertThat(enrollments.getContent()).hasSize(1);
        assertThat(purchase.getId()).isNotZero();

        //when
        suspend.execute(user.getId(), courseBasic.getId());


        //then
        enrollments = findEnrollment.of(request);
        Assertions.assertThat(enrollments.getContent()).hasSize(0);
        Optional<Purchase> deletePurchase = purchaseProvider.purchaseOf(user.getId(), courseBasic.getId());
        assertThat(deletePurchase).isEmpty();


    }
}
