package tech.zerofiltre.blog.domain.user.features;

import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.metrics.model.CounterSpecs;
import tech.zerofiltre.blog.domain.user.PasswordVerifierProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;

public class UpdatePassword {

    private final UserProvider userProvider;
    private final PasswordVerifierProvider passwordVerifierProvider;
    private final MetricsProvider metricsProvider;

    public UpdatePassword(UserProvider userProvider, PasswordVerifierProvider passwordVerifierProvider, MetricsProvider metricsProvider) {
        this.userProvider = userProvider;
        this.passwordVerifierProvider = passwordVerifierProvider;
        this.metricsProvider = metricsProvider;
    }

    public void execute(String email, String oldPassword, String newEncodedPassword) throws UserNotFoundException, InvalidPasswordException {
        User userFromEmail = userProvider.userOfEmail(email)
                .orElseThrow(() -> new UserNotFoundException("We were unable to find a connected user", email));

        if (!passwordVerifierProvider.isValid(userFromEmail, oldPassword)) {
            throw new InvalidPasswordException("The password provided does not match the current user");
        }
        userFromEmail.setPassword(newEncodedPassword);
        userProvider.save(userFromEmail);

        CounterSpecs counterSpecs = new CounterSpecs();
        counterSpecs.setName(CounterSpecs.ZEROFILTRE_PASSWORD_RESETS);
        counterSpecs.setTags("email", email);
        metricsProvider.incrementCounter(counterSpecs);


    }
}
