package tech.zerofiltre.blog.infra.providers.database.payment.model;

import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.BaseEntityJPA;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserJPA;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment", uniqueConstraints = {@UniqueConstraint(name = "UniquePaymentPerReference", columnNames = {"reference"})})
@EqualsAndHashCode(callSuper = true)
public class PaymentJPA extends BaseEntityJPA {
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "user_id")
    private UserJPA user;

    private String reference;

    private LocalDateTime at;

    private String status;

    private String recurringInterval;
}
