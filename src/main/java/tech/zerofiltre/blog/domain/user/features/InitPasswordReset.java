package tech.zerofiltre.blog.domain.user.features;

import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.logging.MessageSourceProvider;
import tech.zerofiltre.blog.domain.user.UserNotificationProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.UserActionEvent;

import java.util.Locale;

public class InitPasswordReset {
    private final UserProvider userProvider;
    private final UserNotificationProvider userNotificationProvider;
    private final VerificationTokenProvider verificationTokenProvider;
    private final MessageSourceProvider messageSourceProvider;

    public InitPasswordReset(UserProvider userProvider, UserNotificationProvider userNotificationProvider, VerificationTokenProvider verificationTokenProvider, MessageSourceProvider messageSourceProvider) {
        this.userProvider = userProvider;
        this.userNotificationProvider = userNotificationProvider;
        this.verificationTokenProvider = verificationTokenProvider;
        this.messageSourceProvider = messageSourceProvider;
    }

    public void execute(String email, String appUrl, Locale locale) throws UserNotFoundException, ForbiddenActionException{
        User user = userProvider.userOfEmail(email)
                .orElseThrow(() -> new UserNotFoundException("We were unable to find a user with the corresponding email: " + email, email));

        if(isSocialAccount(user)){
            throw new ForbiddenActionException(messageSourceProvider.getMessage("ZBLOG_013", null, locale) + email);
        }

        String token = verificationTokenProvider.generate(user,86400).getToken();
        userNotificationProvider.notify(new UserActionEvent(appUrl, locale, user, token,null, Action.PASSWORD_RESET));

    }

    private boolean isSocialAccount(User user){
        return user.getLoginFrom() != null;
    }
}
