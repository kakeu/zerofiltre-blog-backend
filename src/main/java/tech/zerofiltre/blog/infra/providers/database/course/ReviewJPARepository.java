package tech.zerofiltre.blog.infra.providers.database.course;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.zerofiltre.blog.infra.providers.database.course.model.ReviewJPA;

public interface ReviewJPARepository extends JpaRepository<ReviewJPA, Long> {
    ReviewJPA findByUserIdAndChapterId(long reviewAuthorId, long chapterId);
}
