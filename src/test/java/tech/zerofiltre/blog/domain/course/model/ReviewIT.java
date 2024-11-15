package tech.zerofiltre.blog.domain.course.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.course.ChapterProvider;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.ReviewProvider;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.article.DBTagProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBChapterProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBCourseProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBEnrollmentProvider;
import tech.zerofiltre.blog.infra.providers.database.course.DBReviewProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.logging.Slf4jLoggerProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static tech.zerofiltre.blog.domain.article.model.Status.DRAFT;

@DataJpaTest
@Import({DBReviewProvider.class, DBChapterProvider.class, DBUserProvider.class, DBCourseProvider.class, Slf4jLoggerProvider.class, DBTagProvider.class, DBEnrollmentProvider.class})
public class ReviewIT {
    @Autowired
    private UserProvider userProvider;

    @Autowired
    private ReviewProvider reviewProvider;

    @Autowired
    private ChapterProvider chapterProvider;

    @Autowired
    private CourseProvider courseProvider;

    @Autowired
    private EnrollmentProvider enrollmentProvider;

    private Review review;
    private User author;
    private User chapterReviewer;
    private Chapter chapter;
    private Course course;
    private Enrollment enrollment;

    public static final String UPDATED_CHAPTER_EXPLANATION = "UPDATED CHAPTER EXPLANATION";
    public static final int UPDATED_CHAPTER_SATISFACTION_SCORE = 5;

    @BeforeEach
    void setup() {

    }

    @Test
    void init_review_is_Ok_when_has_enrollement() throws ZerofiltreException {
        author = ZerofiltreUtils.createMockUser(false);
        author.setEmail("author@zerofiltre.tech");
        author.setPseudoName("pseudo");
        author = userProvider.save(author);

        course = ZerofiltreUtils.createMockCourse(false, DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = ZerofiltreUtils.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        chapterReviewer = ZerofiltreUtils.createMockUser(false);
        chapterReviewer.setId(4L);
        chapterReviewer = userProvider.save(chapterReviewer);

        enrollment = ZerofiltreUtils.createMockEnrollment(false, chapterReviewer, course);
        enrollment = enrollmentProvider.save(enrollment);

        review = Review.builder()
                .userProvider(userProvider)
                .reviewProvider(reviewProvider)
                .reviewAuthorId(chapterReviewer.getId())
                .chapterId(chapter.getId())
                .courseProvider(courseProvider)
                .enrollmentProvider(enrollmentProvider)
                .chapterExplanations("Great chapter")
                .chapterSatisfactionScore(5)
                .chapterUnderstandingScore(5)
                .recommendCourse(true)
                .overallChapterSatisfaction(5)
                .favoriteLearningToolOfTheChapter(null)
                .build()
                .init();

        assertThat(review).isNotNull();
        assertThat(review.getChapterId()).isEqualTo(chapter.getId());
        assertThat(review.getReviewAuthorId()).isEqualTo(chapterReviewer.getId());
        assertThat(review.getFavoriteLearningToolOfTheChapter()).isNotNull();
    }


    @Test
    void get_review_is_Ok() throws ZerofiltreException {
        // given
        author = ZerofiltreUtils.createMockUser(false);
        author.setEmail("author@zerofiltre.tech");
        author.setPseudoName("pseudo");
        author = userProvider.save(author);

        chapterReviewer = ZerofiltreUtils.createMockUser(false);
        chapterReviewer.setId(4L);
        chapterReviewer = userProvider.save(chapterReviewer);

        course = ZerofiltreUtils.createMockCourse(false, DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = ZerofiltreUtils.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        enrollment = ZerofiltreUtils.createMockEnrollment(false, chapterReviewer, course);
        enrollment = enrollmentProvider.save(enrollment);

        List<String> learningTool = new ArrayList<>();
        learningTool.add("Vidéos explicatives");
        learningTool.add("Exercices pratiques");

        String chapterImpression = "La methodologie est claire et digeste";
        String improvementSuggestion = "Faites davantage de contenu de qualité";
        String whyDoYouRecommendIt = "Ce cours est magnifique";
        String reasons = "Les vidéos sont bient editées";

        review = Review.builder()
                .userProvider(userProvider)
                .reviewProvider(reviewProvider)
                .chapterId(chapter.getId())
                .reviewAuthorId(chapterReviewer.getId())
                .courseProvider(courseProvider)
                .enrollmentProvider(enrollmentProvider)
                .chapterExplanations("Great chapter")
                .chapterSatisfactionScore(5)
                .chapterUnderstandingScore(5)
                .recommendCourse(true)
                .overallChapterSatisfaction(5)
                .chapterImpressions(chapterImpression)
                .whyRecommendingThisCourse(whyDoYouRecommendIt)
                .favoriteLearningToolOfTheChapter(learningTool)
                .improvementSuggestion(improvementSuggestion)
                .reasonFavoriteLearningToolOfTheChapter(reasons)
                .build()
                .init();

        // when
        Optional<Review> foundReview = reviewProvider.findById(review.getId());

        // then
        assertThat(foundReview).isNotEmpty();
        assertThat(foundReview.get().getReviewAuthorId()).isEqualTo(chapterReviewer.getId());
        assertThat(foundReview.get().getChapterExplanations()).isEqualTo("Great chapter");
        assertThat(foundReview.get().getFavoriteLearningToolOfTheChapter()).isEqualTo(learningTool);
        assertThat(foundReview.get().getChapterImpressions()).isEqualTo(chapterImpression);
        assertThat(foundReview.get().getImprovementSuggestion()).isEqualTo(improvementSuggestion);
        assertThat(foundReview.get().getWhyRecommendingThisCourse()).isEqualTo(whyDoYouRecommendIt);
        assertThat(foundReview.get().getReasonFavoriteLearningToolOfTheChapter()).isEqualTo(reasons);
    }

    @Test
    void delete_review_is_Ok_when_admin_user() throws ZerofiltreException {
        User authorAndAdmin = ZerofiltreUtils.createMockUser(true);
        authorAndAdmin.setEmail("authorAndAdmin@zerofiltre.tech");
        authorAndAdmin.setPseudoName("pseudo");
        authorAndAdmin = userProvider.save(authorAndAdmin);

        chapterReviewer = ZerofiltreUtils.createMockUser(true);
        chapterReviewer.setId(4L);
        chapterReviewer = userProvider.save(chapterReviewer);

        course = ZerofiltreUtils.createMockCourse(false, DRAFT, authorAndAdmin, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = ZerofiltreUtils.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        enrollment = ZerofiltreUtils.createMockEnrollment(false, chapterReviewer, course);
        enrollment = enrollmentProvider.save(enrollment);

        review = Review.builder()
                .id(2L)
                .userProvider(userProvider)
                .reviewProvider(reviewProvider)
                .courseProvider(courseProvider)
                .enrollmentProvider(enrollmentProvider)
                .chapterId(chapter.getId())
                .reviewAuthorId(chapterReviewer.getId())
                .chapterExplanations("Great chapter")
                .chapterSatisfactionScore(5)
                .chapterUnderstandingScore(5)
                .recommendCourse(true)
                .overallChapterSatisfaction(5)
                .build()
                .init();

        assertThat(review).isNotNull();

        review.delete();

        Optional<Review> reviewFound = reviewProvider.findById(2L);
        assertThat(reviewFound).isEmpty();
    }

    @Test
    void user_has_active_enrollment_update_review_is_Ok() throws ZerofiltreException {
        // given
        User author = ZerofiltreUtils.createMockUser(false);
        author.setEmail("authorAndAdmin@zerofiltre.tech");
        author.setPseudoName("pseudo");
        author = userProvider.save(author);

        chapterReviewer = ZerofiltreUtils.createMockUser(false);
        chapterReviewer.setId(4L);
        chapterReviewer = userProvider.save(chapterReviewer);

        course = ZerofiltreUtils.createMockCourse(false, DRAFT, author, Collections.emptyList(), Collections.emptyList());
        course = courseProvider.save(course);

        chapter = ZerofiltreUtils.createMockChapter(false, chapterProvider, Collections.emptyList(), course.getId());
        chapter = chapterProvider.save(chapter);

        enrollment = ZerofiltreUtils.createMockEnrollment(false, chapterReviewer, course);
        enrollment = enrollmentProvider.save(enrollment);

        review = Review.builder()
                .userProvider(userProvider)
                .reviewProvider(reviewProvider)
                .enrollmentProvider(enrollmentProvider)
                .courseProvider(courseProvider)
                .chapterId(chapter.getId())
                .reviewAuthorId(chapterReviewer.getId())
                .chapterExplanations("Great chapter")
                .chapterSatisfactionScore(5)
                .chapterUnderstandingScore(5)
                .recommendCourse(true)
                .overallChapterSatisfaction(5)
                .build();

        review = reviewProvider.save(review);

        // Update
        Review updatedReview = review
                .toBuilder()
                .userProvider(userProvider)
                .reviewProvider(reviewProvider)
                .enrollmentProvider(enrollmentProvider)
                .courseProvider(courseProvider)
                .chapterExplanations(UPDATED_CHAPTER_EXPLANATION)
                .chapterSatisfactionScore(UPDATED_CHAPTER_SATISFACTION_SCORE)
                .build();

        updatedReview = updatedReview.update();

        // Then
        assertThat(updatedReview).isNotNull();
        assertThat(updatedReview.getChapterExplanations()).isEqualTo(UPDATED_CHAPTER_EXPLANATION);
        assertThat(updatedReview.getChapterSatisfactionScore()).isEqualTo(UPDATED_CHAPTER_SATISFACTION_SCORE);
    }
}

