package tech.zerofiltre.blog.infra.providers.database.purchase.mapper;

import org.mapstruct.Mapper;
import tech.zerofiltre.blog.domain.purchase.model.Purchase;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.CourseJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.course.mapper.LessonJPAMapper;
import tech.zerofiltre.blog.infra.providers.database.purchase.model.PurchaseJPA;
import tech.zerofiltre.blog.infra.providers.database.user.mapper.UserJPAMapper;

@Mapper(uses = {CourseJPAMapper.class, UserJPAMapper.class, LessonJPAMapper.class})
public interface PurchaseJPAMapper {

    PurchaseJPA toJPA(Purchase purchase);

    Purchase fromJPA(PurchaseJPA purchaseJPA);
}
