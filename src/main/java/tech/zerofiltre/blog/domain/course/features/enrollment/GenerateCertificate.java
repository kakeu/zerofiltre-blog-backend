package tech.zerofiltre.blog.domain.course.features.enrollment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.course.CertificateProvider;
import tech.zerofiltre.blog.domain.course.EnrollmentProvider;
import tech.zerofiltre.blog.domain.course.model.Certificate;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;

@Slf4j
@Component
@RequiredArgsConstructor
public class GenerateCertificate {

    private final EnrollmentProvider enrollmentProvider;
    private final CertificateProvider certificateProvider;

    public Certificate get(User user, long courseId) throws ZerofiltreException {
        if (enrollmentProvider.isCompleted(user.getId(), courseId)) {
            Certificate certificate = certificateProvider.get(user, courseId);
            enrollmentProvider.setCertificatePath(certificate.getName(), user.getId(), courseId);
            return certificate;
        }
        throw new ZerofiltreException("The certificate cannot be issued. The course has not yet been completed.");
    }

}
