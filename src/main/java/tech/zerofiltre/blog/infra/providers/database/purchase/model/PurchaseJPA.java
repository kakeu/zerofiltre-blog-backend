package tech.zerofiltre.blog.infra.providers.database.purchase.model;

import lombok.*;
import tech.zerofiltre.blog.infra.providers.database.BaseEntityJPA;
import tech.zerofiltre.blog.infra.providers.database.course.model.CourseJPA;
import tech.zerofiltre.blog.infra.providers.database.user.model.UserJPA;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "purchase", uniqueConstraints = {@UniqueConstraint(name = "UniquePurchasePerUserIdAndCourseId", columnNames = {"user_id", "course_id"})})
@EqualsAndHashCode(callSuper = true)
public class PurchaseJPA extends BaseEntityJPA {
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "user_id")
    private UserJPA user;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinColumn(name = "course_id")
    private CourseJPA course;

    private LocalDateTime at;
}
