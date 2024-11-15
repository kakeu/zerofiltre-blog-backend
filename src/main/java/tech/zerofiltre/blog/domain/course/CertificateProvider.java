package tech.zerofiltre.blog.domain.course;

import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;


public interface CertificateProvider {

    Certificate get(User user, long courseId) throws ZerofiltreException;
}
