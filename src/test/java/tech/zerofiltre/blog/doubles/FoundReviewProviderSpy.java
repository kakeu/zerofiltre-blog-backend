package tech.zerofiltre.blog.doubles;

import tech.zerofiltre.blog.domain.course.ReviewProvider;
import tech.zerofiltre.blog.domain.course.model.Review;

import java.util.List;
import java.util.Optional;

public class FoundReviewProviderSpy implements ReviewProvider {
    public boolean deleteCalled;

    @Override
    public Review save(Review review) {
        return review;
    }

    @Override
    public Optional<Review> findById(long id) {
        Review review = Review.builder().id(id).build();
        return Optional.ofNullable(review);
    }

    @Override
    public List<Review> findAll() {
        return null;
    }

    @Override
    public void deleteById(long id) {
        deleteCalled = true;
    }

    @Override
    public Optional<Review> findByAuthorIdAndChapterId(long authorId, long chapterId) {
        return Optional.empty();
    }
}
