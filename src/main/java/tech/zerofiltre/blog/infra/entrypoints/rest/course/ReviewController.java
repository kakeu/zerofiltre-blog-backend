package tech.zerofiltre.blog.infra.entrypoints.rest.course;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import tech.zerofiltre.blog.domain.course.CourseProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.ReviewProvider;
import tech.zerofiltre.blog.domain.course.model.Review;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;
import tech.zerofiltre.blog.infra.entrypoints.rest.course.model.ReviewVM;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewProvider reviewProvider;
    private final SecurityContextManager securityContextManager;
    private final UserProvider userProvider;
    private final CourseProvider courseProvider;
    private final EnrollmentProvider enrollmentProvider;

    public ReviewController(ReviewProvider reviewProvider, SecurityContextManager securityContextManager,
                            UserProvider userProvider, CourseProvider courseProvider, EnrollmentProvider enrollmentProvider) {
        this.reviewProvider = reviewProvider;
        this.securityContextManager = securityContextManager;
        this.userProvider = userProvider;
        this.courseProvider = courseProvider;
        this.enrollmentProvider = enrollmentProvider;
    }

    private Review.ReviewBuilder toBuildReview(ReviewVM reviewVM, User user) {
        return  Review.builder()
                .reviewProvider(reviewProvider)
                .userProvider(userProvider)
                .courseProvider(courseProvider)
                .enrollmentProvider(enrollmentProvider)
                .chapterExplanations(reviewVM.getChapterExplanations())
                .chapterSatisfactionScore(reviewVM.getChapterSatisfactionScore())
                .chapterUnderstandingScore(reviewVM.getChapterUnderstandingScore())
                .recommendCourse(reviewVM.isRecommendCourse())
                .overallChapterSatisfaction(reviewVM.getOverallChapterSatisfaction())
                .whyRecommendingThisCourse(reviewVM.getWhyRecommendingThisCourse())
                .chapterImpressions(reviewVM.getChapterImpressions())
                .improvementSuggestion(reviewVM.getImprovementSuggestion())
                .reasonFavoriteLearningToolOfTheChapter(reviewVM.getReasonFavoriteLearningToolOfTheChapter())
                .favoriteLearningToolOfTheChapter(reviewVM.getFavoriteLearningToolOfTheChapter())
                .chapterId(reviewVM.getChapterId())
                .reviewAuthorId(user.getId());
    }

    @PostMapping
    public Review createOrUpdateReview(@RequestBody @Valid ReviewVM reviewVM) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        Review review = toBuildReview(reviewVM, user).build();
        return review.init();
    }

    @PatchMapping("/{id}")
    public Review updateReview(@PathVariable("id") long id, @RequestBody @Valid ReviewVM reviewVM) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        Review review = toBuildReview(reviewVM, user).id(id).build();
        return review.update();
    }

    @GetMapping("/{id}")
    public Review getReview(@PathVariable long id) throws ResourceNotFoundException {
        Review review = Review.builder()
                .reviewProvider(reviewProvider)
                .id(id)
                .build();
        return review.findById(id);
    }

    @GetMapping
    public List<Review> getAllReviews() {
        return reviewProvider.findAll();
    }

    @DeleteMapping("/{id}")
    public void deleteReview(@PathVariable long id) throws ResourceNotFoundException, ForbiddenActionException {
        User user = securityContextManager.getAuthenticatedUser();
        Review review = Review.builder()
                .reviewProvider(reviewProvider)
                .id(id)
                .reviewAuthorId(user.getId())
                .userProvider(userProvider)
                .build();
        review.delete();
    }
}

