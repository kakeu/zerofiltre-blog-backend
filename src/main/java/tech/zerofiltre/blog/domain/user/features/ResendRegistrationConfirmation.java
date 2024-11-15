package tech.zerofiltre.blog.domain.user.features;

import tech.zerofiltre.blog.domain.user.UserNotificationProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.UserActionEvent;

import java.util.Locale;

public class ResendRegistrationConfirmation {
    private final UserProvider userProvider;
    private final UserNotificationProvider userNotificationProvider;
    private final VerificationTokenProvider verificationTokenProvider;

    public ResendRegistrationConfirmation(UserProvider userProvider, UserNotificationProvider userNotificationProvider, VerificationTokenProvider verificationTokenProvider) {
        this.userProvider = userProvider;
        this.userNotificationProvider = userNotificationProvider;
        this.verificationTokenProvider = verificationTokenProvider;
    }


    public void execute(String email, String appUrl, Locale locale) throws UserNotFoundException {
        User user = userProvider.userOfEmail(email)
                .orElseThrow(() -> new UserNotFoundException("We were unable to find a user with the corresponding email: " + email, email));
        String token = verificationTokenProvider.generate(user, 86400).getToken();
        userNotificationProvider.notify(new UserActionEvent(appUrl, locale, user, token, null, Action.REGISTRATION_COMPLETE));

    }

}

