package tech.zerofiltre.blog.infra.providers.database.payment.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.payment.model.Payment;
import tech.zerofiltre.blog.infra.providers.database.payment.model.PaymentJPA;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.UserJPAMapper;

import java.util.List;

@Mapper(uses = {UserJPAMapper.class})
public interface PaymentJPAMapper {

    PaymentJPA toJPA(Payment payment);

    Payment fromJPA(PaymentJPA paymentJPA);

    List<PaymentJPA> toJPA(List<Payment> payments);

    List<Payment> fromJPA(List<PaymentJPA> paymentJPA);
}
