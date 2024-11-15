package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import tech.zerofiltre.blog.domain.sandbox.model.Sandbox;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;
import tech.zerofiltre.blog.infra.providers.notification.user.model.SandboxCreatedApplicationEvent;
import tech.zerofiltre.blog.infra.security.config.EmailValidator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class SandboxCreatedEventListener implements ApplicationListener<SandboxCreatedApplicationEvent> {

    private final MessageSource messages;
    private final ZerofiltreEmailSender emailSender;
    private final ITemplateEngine emailTemplateEngine;
    private final InfraProperties infraProperties;


    @Override
    public void onApplicationEvent(SandboxCreatedApplicationEvent event) {
        User user = event.getUser();
        boolean validEmail = user.getEmail() != null && EmailValidator.validateEmail(user.getEmail());
        String emailAddress = validEmail ? user.getEmail() : user.getPaymentEmail();
        if (emailAddress != null) {

            String subjectCode = "message.created.sandbox.subject";

            String template = "sandbox_created_notification.html";

            Map<String, Object> templateModel = new HashMap<>();
            Sandbox sandbox = event.getSandbox();
            String sandboxDoc = Sandbox.Type.K8S.equals(sandbox.getType()) ? infraProperties.getSandboxK8sDoc() : "https://github.com/Zerofiltre-Courses/bootcamp-devops-dev/tree/main";

            templateModel.put("fullName", user.getFullName());
            templateModel.put("sandboxUsername", sandbox.getUsername());
            templateModel.put("sandboxPassword", sandbox.getPassword());
            templateModel.put("onboardingLink", sandboxDoc);

            String subject = messages.getMessage(subjectCode, null, event.getLocale());
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);
            thymeleafContext.setLocale(event.getLocale());
            String emailContent = emailTemplateEngine.process(template, thymeleafContext);

            Email email = new Email();
            email.setSubject(subject);
            email.setContent(emailContent);
            email.setRecipients(Collections.singletonList(emailAddress));
            emailSender.send(email, true);
        }
    }
}
