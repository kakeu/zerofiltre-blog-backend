package tech.zerofiltre.blog.domain.user.features;

import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.VerificationToken;

public class SavePasswordReset {

    private final VerifyToken verifyToken;
    private final UserProvider userProvider;


    public SavePasswordReset(VerificationTokenProvider verificationTokenProvider, UserProvider userProvider) {
        this.verifyToken = new VerifyToken(verificationTokenProvider);
        this.userProvider = userProvider;
    }

    public void execute(String encodedPassword, String token) throws InvalidTokenException {
        VerificationToken verificationToken = verifyToken.execute(token);
        User user = verificationToken.getUser();
        user.setPassword(encodedPassword);
        userProvider.save(user);
        verifyToken.invalidate(verificationToken);

    }


}
