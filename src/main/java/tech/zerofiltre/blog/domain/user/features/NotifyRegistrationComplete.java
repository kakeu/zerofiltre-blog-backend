package tech.zerofiltre.blog.domain.user.features;

import tech.zerofiltre.blog.domain.user.UserNotificationProvider;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.UserActionEvent;

import java.util.Locale;

public class NotifyRegistrationComplete {
    private final UserNotificationProvider userNotificationProvider;

    public NotifyRegistrationComplete(UserNotificationProvider userNotificationProvider) {
        this.userNotificationProvider = userNotificationProvider;
    }

    public void execute(User user, String token, Locale locale, String applicationURL) {
        UserActionEvent registrationCompleteEvent = new UserActionEvent(
                applicationURL,
                locale,
                user,
                token,null, Action.REGISTRATION_COMPLETE);
        userNotificationProvider.notify(registrationCompleteEvent);

    }
}
