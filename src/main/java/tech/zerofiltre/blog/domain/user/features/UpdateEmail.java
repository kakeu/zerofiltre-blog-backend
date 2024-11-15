package tech.zerofiltre.blog.domain.user.features;

import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;

public class UpdateEmail {

    private final UserProvider userProvider;

    public UpdateEmail(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    public User execute(String email, User currentUser) throws UserNotFoundException {

        User foundUser = userProvider.userOfId(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("We could not find the user you want to save", String.valueOf(currentUser.getId())));

        foundUser.setEmail(email);
        userProvider.save(foundUser);
        return foundUser;
    }
}
