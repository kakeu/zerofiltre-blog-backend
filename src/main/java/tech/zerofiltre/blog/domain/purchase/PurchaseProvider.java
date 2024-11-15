package tech.zerofiltre.blog.domain.purchase;

import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.purchase.model.Purchase;

import java.util.Optional;

public interface PurchaseProvider {
    void delete(long userId, long courseId);

    Optional<Purchase> purchaseOf(long userId, long courseId);

    Purchase save(Purchase purchase) throws ZerofiltreException;
}
