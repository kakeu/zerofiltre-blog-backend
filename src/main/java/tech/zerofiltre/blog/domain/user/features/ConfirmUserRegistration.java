package tech.zerofiltre.blog.domain.user.features;

import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.VerificationToken;

public class ConfirmUserRegistration {
    public static final String TOKEN_EXPIRED = "The token you've provided is expired";
    public static final String INVALID_TOKEN = "We couldn't find a token corresponding to the one you've provided";
    private final VerifyToken verifyToken;

    private final UserProvider userProvider;


    public ConfirmUserRegistration(VerificationTokenProvider verificationTokenProvider, UserProvider userProvider) {
        this.userProvider = userProvider;
        verifyToken = new VerifyToken(verificationTokenProvider);
    }

    public User execute(String token) throws InvalidTokenException {
        VerificationToken verificationToken = verifyToken.execute(token);
        User user = verificationToken.getUser();
        user.setActive(true);
        User result = userProvider.save(user);
        verifyToken.invalidate(verificationToken);
        return result;
    }
}
