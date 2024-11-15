package tech.zerofiltre.blog.domain.course.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.ReviewProvider;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.logging.LoggerProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(value = {"reviewProvider", "userProvider", "loggerProvider", "ReviewId", "chapterId",
        "enrollmentProvider", "courseProvider"})
@Builder(toBuilder = true)
public class Review {

    private static final String USER_DOES_NOT_EXIST = "User does not exist";
    public static final String DOES_NOT_EXIST = " does not exist";
    public static final String THE_REVIEW_WITH_ID = "The review with id: ";
    private long id;
    private String chapterExplanations;
    private int chapterSatisfactionScore;
    private int chapterUnderstandingScore;
    private boolean recommendCourse;
    private int overallChapterSatisfaction;
    private String chapterImpressions;
    private String whyRecommendingThisCourse;
    private List<String> favoriteLearningToolOfTheChapter = new ArrayList<>();
    private String reasonFavoriteLearningToolOfTheChapter;
    private String improvementSuggestion;
    private long reviewAuthorId;
    private long chapterId;
    private long courseId;
    private ReviewProvider reviewProvider;
    private UserProvider userProvider;
    private LoggerProvider loggerProvider;
    private EnrollmentProvider enrollmentProvider;
    private CourseProvider courseProvider;

    public Review findById(long id) throws ResourceNotFoundException {
        return setProviders(reviewProvider.findById(id).orElseThrow(
                () -> new ResourceNotFoundException(THE_REVIEW_WITH_ID + this.id + DOES_NOT_EXIST, String.valueOf(id))
        ));
    }

    public Review init() throws ResourceNotFoundException, ForbiddenActionException {
        Optional<User> reviewer = userProvider.userOfId(this.reviewAuthorId);
        if (reviewer.isEmpty())
            throw new ResourceNotFoundException(USER_DOES_NOT_EXIST, String.valueOf(this.reviewAuthorId));

        long courseId = courseProvider.courseIdOfChapterId(this.chapterId);
        boolean doesUserHasActiveEnrollment = userHasActiveEnrollmentInCourse(reviewer.get().getId(), courseId);
        if (doesUserHasActiveEnrollment) {
            Optional<Review> existingReview = reviewProvider.findByAuthorIdAndChapterId(this.reviewAuthorId, this.chapterId);
            if (existingReview.isEmpty()) {
                this.courseId = courseId;
                return setProviders(reviewProvider.save(this));
            } else {
                Review updatedReview = updateExistingReview(existingReview.get());
                return setProviders(reviewProvider.save(updatedReview));
            }
        } else {
            throw new ForbiddenActionException("User is not enrolled to this course, so you cannot review it");
        }

    }

    public Review update() throws ForbiddenActionException, ResourceNotFoundException {
        User currentUser = userProvider.userOfId(this.reviewAuthorId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_DOES_NOT_EXIST, String.valueOf(this.reviewAuthorId)));

        Optional<Review> existingReview = reviewProvider.findById(this.id);
        if (existingReview.isEmpty())
            throw (new ResourceNotFoundException(THE_REVIEW_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id)));

        checkReviewAccessConditions(existingReview.get(), currentUser, false);
        long courseId = courseProvider.courseIdOfChapterId(this.chapterId);
        boolean doesUserHasActiveEnrollment = userHasActiveEnrollmentInCourse(currentUser.getId(), courseId);
        if (currentUser.isPro() || doesUserHasActiveEnrollment) {
            Review updatedReview = updateExistingReview(existingReview.get());
            return setProviders(reviewProvider.save(updatedReview));
        } else {
            throw new ForbiddenActionException("User is not authorized to update this review");
        }
    }

    private Review updateExistingReview(Review existingReview) {
        return existingReview.toBuilder()
                .chapterExplanations(this.chapterExplanations)
                .chapterSatisfactionScore(this.chapterSatisfactionScore)
                .chapterUnderstandingScore(this.chapterUnderstandingScore)
                .recommendCourse(this.recommendCourse)
                .overallChapterSatisfaction(this.overallChapterSatisfaction)
                .improvementSuggestion(this.improvementSuggestion)
                .whyRecommendingThisCourse(this.whyRecommendingThisCourse)
                .favoriteLearningToolOfTheChapter(this.favoriteLearningToolOfTheChapter)
                .reasonFavoriteLearningToolOfTheChapter(this.reasonFavoriteLearningToolOfTheChapter)
                .build();
    }

    public void delete() throws ForbiddenActionException, ResourceNotFoundException {
        User currentUser = userProvider.userOfId(this.reviewAuthorId)
                .orElseThrow(() -> new ResourceNotFoundException(USER_DOES_NOT_EXIST, String.valueOf(this.reviewAuthorId)));

        Optional<Review> existingReview = reviewProvider.findById(this.id);
        if (existingReview.isEmpty())
            throw (new ResourceNotFoundException(THE_REVIEW_WITH_ID + id + DOES_NOT_EXIST, String.valueOf(id)));

        checkReviewAccessConditions(existingReview.get(), currentUser, true);
        reviewProvider.deleteById(this.id);
    }

    private void checkReviewAccessConditions(Review existingReview, User currentUser, boolean isDeletion) throws ForbiddenActionException {
        if (currentUser.isAdmin()) {
            return;
        }

        boolean isNotAuthor = currentUser.getId() != existingReview.getReviewAuthorId();
        if (!isDeletion && isNotAuthor) {
            throw new ForbiddenActionException("You are not allowed to update this review");
        }

        if (isDeletion) {
            throw new ForbiddenActionException("You are not allowed to delete this review");
        }
    }

    private boolean userHasActiveEnrollmentInCourse(long reviewerId, long courseId) {
        return enrollmentProvider.enrollmentOf(reviewerId, courseId, true).isPresent();
    }

    private Review setProviders(Review review) {
        review.reviewProvider = this.reviewProvider;
        review.userProvider = this.userProvider;
        review.loggerProvider = this.loggerProvider;
        return review;
    }
}

