package tech.zerofiltre.blog.domain.course;

import tech.zerofiltre.blog.domain.course.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewProvider {
    Review save(Review review);

    Optional<Review> findById(long id);

    List<Review> findAll();

    void deleteById(long id);

    Optional<Review> findByAuthorIdAndChapterId(long authorId, long chapterId);
}
