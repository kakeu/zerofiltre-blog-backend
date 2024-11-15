package tech.zerofiltre.blog.infra.providers.database.purchase;

import org.springframework.data.jpa.repository.JpaRepository;
import tech.zerofiltre.blog.infra.providers.database.purchase.model.PurchaseJPA;

import java.util.Optional;

public interface PurchaseJPARepository extends JpaRepository<PurchaseJPA, Long> {
    Optional<PurchaseJPA> findByUserIdAndCourseId(long userId, long courseId);

    void deleteByUserIdAndCourseId(long userId, long courseId);
}
