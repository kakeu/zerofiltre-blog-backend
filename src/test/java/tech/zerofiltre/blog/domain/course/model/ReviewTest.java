package tech.zerofiltre.blog.domain.course.model;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.ReviewProvider;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.doubles.FoundAdminUserProviderSpy;
import tech.zerofiltre.blog.doubles.FoundReviewProviderSpy;
import tech.zerofiltre.blog.doubles.NotFoundUserProviderSpy;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


public class ReviewTest {
    private Review review;
    private Review.ReviewBuilder reviewBuilder;
    private ReviewProvider reviewProvider;
    private UserProvider userProvider;
    private CourseProvider courseProvider;
    private EnrollmentProvider enrollmentProvider;

    @BeforeEach
    void setup() {
        reviewProvider = mock(ReviewProvider.class);
        userProvider = mock(UserProvider.class);
        courseProvider = mock(CourseProvider.class);
        enrollmentProvider = mock(EnrollmentProvider.class);


        reviewBuilder = Review.builder()
                .reviewAuthorId(1L);
    }

    @DisplayName("Initialize a review if the user is not found (not connected) then a ResourceNotFoundException is thrown")
    @Test
    void init_throws_ResourceNotFoundException_if_user_not_found() {
        // given
        review = reviewBuilder
                .userProvider(new NotFoundUserProviderSpy())
                .build();

        // when & then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> review.init());
    }

    @DisplayName("Initialize a review if the user is found and has active enrollment then save the review")
    @Test
    void init_saves_review_if_user_has_active_enrollment() throws ResourceNotFoundException, ForbiddenActionException {
        // given
        User reviewer = ZerofiltreUtils.createMockUser(false);
        when(userProvider.userOfId(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(courseProvider.courseIdOfChapterId(1)).thenReturn(2L);
        when(enrollmentProvider.enrollmentOf(reviewer.getId(), 2L, true)).thenReturn(Optional.of(new Enrollment()));


        review = reviewBuilder
                .reviewAuthorId(reviewer.getId())
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .enrollmentProvider(enrollmentProvider)
                .reviewProvider(reviewProvider)
                .chapterId(1L)
                .chapterExplanations("Great chapter")
                .chapterSatisfactionScore(5)
                .chapterUnderstandingScore(5)
                .recommendCourse(true)
                .overallChapterSatisfaction(5)
                .build();

        when(reviewProvider.save(any())).thenReturn(review);

        // when
        Review result = review.init();

        // then
        assertThat(result).isNotNull();
        assertThat(result.getReviewAuthorId()).isEqualTo(reviewer.getId());
        assertThatCode(() -> review.init()).doesNotThrowAnyException();
    }

    @DisplayName("Thows ForbiddenActionException if user has no active enrollment")
    @Test
    void init_throws_ForbiddenActionException_if_user_has_no_access() throws ResourceNotFoundException, ForbiddenActionException {
        // given
        User reviewer = ZerofiltreUtils.createMockUser(false);
        when(userProvider.userOfId(reviewer.getId())).thenReturn(Optional.of(reviewer));
        when(courseProvider.courseIdOfChapterId(1)).thenReturn(2L);
        when(enrollmentProvider.enrollmentOf(reviewer.getId(), 2L, true))
                .thenReturn(Optional.empty());

        review = reviewBuilder
                .reviewAuthorId(reviewer.getId())
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .enrollmentProvider(enrollmentProvider)
                .chapterExplanations("Great chapter")
                .chapterSatisfactionScore(5)
                .chapterUnderstandingScore(5)
                .recommendCourse(true)
                .overallChapterSatisfaction(5)
                .build();

        // when & then
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> review.init())
                .withMessage("User is not enrolled to this course, so you cannot review it");

    }

    @DisplayName("Find a review by id throws ResourceNotFoundException if review does not exist")
    @Test
    void find_by_id_throws_ResourceNotFoundException_if_review_does_not_exist() {
        review = reviewBuilder
                .userProvider(new NotFoundUserProviderSpy())
                .reviewProvider(reviewProvider)
                .build();

        when(reviewProvider.findById(anyLong())).thenReturn(Optional.empty());

        // When then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> review.findById(1L));

    }

    @DisplayName("Find a review by id returns review when it exists")
    @Test
    void find_by_id_return_the_review_if_it_exists() throws ResourceNotFoundException {
        // given
        review = reviewBuilder
                .userProvider(null)
                .reviewProvider(reviewProvider)
                .reviewAuthorId(0)
                .chapterId(1)
                .chapterExplanations("Great chapter")
                .chapterSatisfactionScore(5)
                .chapterUnderstandingScore(5)
                .recommendCourse(true)
                .overallChapterSatisfaction(5)
                .build();

        long reviewId = 1L;
        when(reviewProvider.findById(reviewId)).thenReturn(Optional.of(review));

        // when
        Review reviewFound = review.findById(reviewId);

        // then
        assertThat(reviewFound).isNotNull();
        assertThat(reviewFound.getId()).isEqualTo(review.getId());
        assertThat(reviewFound.getChapterExplanations()).isEqualTo("Great chapter");
    }

    @DisplayName("Delete review throws ResourceNotFoundException when user does not exist")
    @Test
    void delete_throws_ResourceNotFoundException_when_user_does_not_exist() {
        // given
        User user = ZerofiltreUtils.createMockUser(false);
        long reviewId = 1L;

        review = reviewBuilder
                .reviewAuthorId(user.getId())
                .id(reviewId)
                .userProvider(userProvider)
                .build();

        when(userProvider.userOfId(user.getId())).thenReturn(Optional.empty());

        // when then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> review.delete())
                .withMessage("User does not exist");

        verify(reviewProvider, never()).deleteById(reviewId);
    }

    @DisplayName("Delete review throws ResourceNotFoundException when review does not exist")
    @Test
    void delete_review_hrows_esourceNotFoundException_when_review_does_not_exist() {
        // given
        User user = ZerofiltreUtils.createMockUser(false);
        long reviewId = 1L;

        review = reviewBuilder
                .id(reviewId)
                .reviewProvider(reviewProvider)
                .userProvider(userProvider)
                .reviewAuthorId(user.getId())
                .build();

        when(userProvider.userOfId(user.getId())).thenReturn(Optional.of(user));
        when(reviewProvider.findById(reviewId)).thenReturn(Optional.empty());

        // when then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> review.delete())
                .withMessage("The review with id: " + reviewId + " does not exist");

        verify(reviewProvider, never()).deleteById(reviewId);
    }

    @DisplayName("Delete review, you're not an admin throw a ForbiddenActionException")
    @Test
    void delete_review_throws_ForbiddenActionException_when_you_are_not_admin() {
        // given
        User user = ZerofiltreUtils.createMockUser(false);

        long reviewId = 1L;
        review = reviewBuilder
                .id(reviewId)
                .reviewAuthorId(user.getId())
                .userProvider(userProvider)
                .reviewProvider(reviewProvider)
                .build();

        when(userProvider.userOfId(user.getId())).thenReturn(Optional.of(user));
        when(reviewProvider.findById(reviewId)).thenReturn(Optional.of(review));

        // when then
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> review.delete())
                .withMessage("You are not allowed to delete this review");

        verify(reviewProvider, never()).deleteById(reviewId);
    }

    @DisplayName("Delete review works if user is admin and review exists")
    @Test
    void delete_review_works_if_user_is_admin() throws ForbiddenActionException, ResourceNotFoundException {
        // given
        User user = ZerofiltreUtils.createMockUser(true);
        FoundReviewProviderSpy reviewProviderSpy = new FoundReviewProviderSpy();

        long reviewId = 1L;
        review = reviewBuilder
                .id(reviewId)
                .reviewProvider(reviewProviderSpy)
                .userProvider(new FoundAdminUserProviderSpy())
                .reviewAuthorId(user.getId())
                .chapterId(1)
                .chapterExplanations("Great chapter")
                .chapterSatisfactionScore(5)
                .chapterUnderstandingScore(5)
                .recommendCourse(true)
                .overallChapterSatisfaction(5)
                .build();

        // When
        review.delete();

        // Then
        Assertions.assertThat(reviewProviderSpy.deleteCalled).isTrue();
    }

    @DisplayName("Update review throws ResourceNotFoundException when user does not exist")
    @Test
    void update_review_throws_ResourceNotFoundException_when_user_does_not_exist() {
        // given
        review = reviewBuilder
                .id(1L)
                .reviewAuthorId(1L)
                .userProvider(userProvider)
                .reviewProvider(reviewProvider)
                .chapterId(1)
                .chapterExplanations("Great chapter")
                .chapterSatisfactionScore(5)
                .chapterUnderstandingScore(5)
                .recommendCourse(true)
                .overallChapterSatisfaction(5)
                .build();

        when(userProvider.userOfId(1L)).thenReturn(Optional.empty());

        // when then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> review.update())
                .withMessage("User does not exist");

        verify(reviewProvider, never()).save(review);
    }

    @DisplayName("Update review throws ResourceNotFoundException when review does not exist")
    @Test
    void update_review_throws_ResourceNotFoundException_when_review_does_not_exist() {
        // given
        User user = ZerofiltreUtils.createMockUser(false);

        review = reviewBuilder
                .id(1L)
                .reviewAuthorId(user.getId())
                .userProvider(userProvider)
                .reviewProvider(reviewProvider)
                .chapterId(1)
                .chapterExplanations("Great chapter")
                .chapterSatisfactionScore(5)
                .chapterUnderstandingScore(5)
                .recommendCourse(true)
                .overallChapterSatisfaction(5)
                .build();

        when(userProvider.userOfId(user.getId())).thenReturn(Optional.of(user));
        when(reviewProvider.findById(review.getId())).thenReturn(Optional.empty());

        // when then
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> review.update())
                .withMessage("The review with id: " + review.getId() + " does not exist");

        verify(reviewProvider, never()).save(review);
    }

    @DisplayName("Update works if the updater is the owner of the review")
    @Test
    void update_works_if_the_updater_is_the_owner_of_the_review() throws ForbiddenActionException, ResourceNotFoundException {
        // given
        User user = ZerofiltreUtils.createMockProUser(false, true);
        review = reviewBuilder
                .id(1L)
                .reviewAuthorId(user.getId())
                .userProvider(userProvider)
                .reviewProvider(reviewProvider)
                .courseProvider(courseProvider)
                .enrollmentProvider(enrollmentProvider)
                .chapterId(1)
                .chapterExplanations("Great chapter")
                .chapterSatisfactionScore(5)
                .chapterUnderstandingScore(5)
                .recommendCourse(true)
                .overallChapterSatisfaction(5)
                .build();

        when(userProvider.userOfId(user.getId())).thenReturn(Optional.of(user));
        when(reviewProvider.findById(review.getId())).thenReturn(Optional.of(review));
        when(reviewProvider.save(any())).thenReturn(review);
        // when

        Review updatedReview = review.update();

        // then
        assertThat(updatedReview).isNotNull();
        assertThat(updatedReview.getId()).isEqualTo(review.getId());
    }

    @DisplayName("Must update the existing review when the same user try to give another review on the same chapter")
    @Test
    void must_update_the_existing_review_when_the_same_user_try_give_another_review() throws ForbiddenActionException, ResourceNotFoundException {
        // given
        User user = ZerofiltreUtils.createMockProUser(false, true);
        review = reviewBuilder
                .id(2L)
                .userProvider(userProvider)
                .reviewProvider(reviewProvider)
                .courseProvider(courseProvider)
                .enrollmentProvider(enrollmentProvider)
                .build();

        Review existingReview = reviewBuilder
                .id(2L)
                .reviewAuthorId(user.getId())
                .chapterId(1)
                .chapterExplanations("Great chapter")
                .chapterSatisfactionScore(5)
                .chapterUnderstandingScore(5)
                .recommendCourse(true)
                .overallChapterSatisfaction(5)
                .build();

        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));
        when(reviewProvider.findById(anyLong())).thenReturn(Optional.of(existingReview));
        when(reviewProvider.save(any())).thenReturn(existingReview);

        // when
        review.update();

        // then
        ArgumentCaptor<Review> argumentCaptor = ArgumentCaptor.forClass(Review.class);
        verify(reviewProvider, times(1)).save(argumentCaptor.capture());
        Review capturedReview = argumentCaptor.getValue();
        assertThat(capturedReview.getId()).isEqualTo(existingReview.getId());
    }
}