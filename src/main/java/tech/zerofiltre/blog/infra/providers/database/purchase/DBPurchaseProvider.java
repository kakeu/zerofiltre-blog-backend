package tech.zerofiltre.blog.infra.providers.database.purchase;

import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.PurchaseProvider;
import tech.zerofiltre.blog.domain.purchase.model.Purchase;
import tech.zerofiltre.blog.infra.providers.database.purchase.mapper.PurchaseJPAMapper;

import java.util.Optional;

@Component
@Transactional
@RequiredArgsConstructor
public class DBPurchaseProvider implements PurchaseProvider {

    private final PurchaseJPARepository repository;
    private final PurchaseJPAMapper mapper = Mappers.getMapper(PurchaseJPAMapper.class);


    @Override
    public void delete(long userId, long courseId) {
        repository.deleteByUserIdAndCourseId(userId, courseId);
    }


    @Override
    public Optional<Purchase> purchaseOf(long userId, long courseId) {
        return repository.findByUserIdAndCourseId(userId, courseId)
                .map(mapper::fromJPA);
    }

    @Override
    public Purchase save(Purchase purchase) throws ZerofiltreException {
        return mapper.fromJPA(repository.save(mapper.toJPA(purchase)));
    }
}
