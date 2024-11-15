package tech.zerofiltre.blog.infra.providers.notification.user;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.sandbox.model.SandboxCreatedEvent;
import tech.zerofiltre.blog.domain.user.UserNotificationProvider;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.UserActionEvent;
import tech.zerofiltre.blog.infra.providers.notification.user.model.ArticleSubmittedEvent;
import tech.zerofiltre.blog.infra.providers.notification.user.model.CheckoutStartedEvent;
import tech.zerofiltre.blog.infra.providers.notification.user.model.SandboxCreatedApplicationEvent;
import tech.zerofiltre.blog.infra.providers.notification.user.model.UserActionApplicationEvent;

@Component
@RequiredArgsConstructor
public class AppPublisherNotificationProvider implements UserNotificationProvider {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void notify(UserActionEvent userActionEvent) {
        ApplicationEvent event;
        if (Action.CHECKOUT_STARTED == userActionEvent.getAction()) {

            event = new CheckoutStartedEvent(
                    userActionEvent.getUser(),
                    userActionEvent.getLocale(),
                    userActionEvent.getAppUrl()
            );


        } else if (Action.ARTICLE_SUBMITTED == userActionEvent.getAction()) {
            event = new ArticleSubmittedEvent(
                    userActionEvent.getUser(),
                    userActionEvent.getLocale(),
                    userActionEvent.getAppUrl(),
                    userActionEvent.getArticle()
            );

        } else if (Action.SANDBOX_CREATED == userActionEvent.getAction()) {
            event = new SandboxCreatedApplicationEvent(
                    userActionEvent.getUser(),
                    userActionEvent.getLocale(),
                    userActionEvent.getAppUrl(),
                    ((SandboxCreatedEvent) userActionEvent).getSandbox()
            );

        } else {
            event = new UserActionApplicationEvent(
                    userActionEvent.getUser(),
                    userActionEvent.getLocale(),
                    userActionEvent.getAppUrl(),
                    userActionEvent.getCurrentToken(),
                    userActionEvent.getAction());

        }
        eventPublisher.publishEvent(event);

    }
}

