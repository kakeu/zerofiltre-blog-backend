package tech.zerofiltre.blog.infra.providers.database.course;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.zerofiltre.blog.domain.course.ReviewProvider;
import tech.zerofiltre.blog.domain.course.model.Review;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.ReviewJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.course.model.ReviewJPA;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@Transactional
@RequiredArgsConstructor
public class DBReviewProvider implements ReviewProvider {
    private final ReviewJPARepository reviewJPARepository;
    ReviewJPAMapper reviewJPAMapper = Mappers.getMapper(ReviewJPAMapper.class);

    @Override
    public Review save(Review review) {
        ReviewJPA reviewJPA = reviewJPAMapper.toJPA(review);
        ReviewJPA savedReview = reviewJPARepository.save(reviewJPA);
        return reviewJPAMapper.fromJPA(savedReview);
    }

    @Override
    public Optional<Review> findById(long id) {
        return reviewJPARepository.findById(id).map(reviewJPAMapper::fromJPA);
    }

    @Override
    public List<Review> findAll() {
        return reviewJPARepository.findAll().stream().map(reviewJPAMapper::fromJPA).collect(Collectors.toList());
    }

    @Override
    public void deleteById(long id) {
        reviewJPARepository.deleteById(id);
    }

    @Override
    public Optional<Review> findByAuthorIdAndChapterId(long authorId, long chapterId) {
        ReviewJPA reviewJPA = reviewJPARepository.findByUserIdAndChapterId(authorId, chapterId);
        return Optional.ofNullable(reviewJPAMapper.fromJPA(reviewJPA));
    }
}
