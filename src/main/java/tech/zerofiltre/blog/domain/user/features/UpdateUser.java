package tech.zerofiltre.blog.domain.user.features;

import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;

public class UpdateUser {

    private final UserProvider userProvider;

    public UpdateUser(UserProvider userProvider) {
        this.userProvider = userProvider;
    }

    public User patch(User currentUser, User user) throws ForbiddenActionException, UserNotFoundException {

        if (currentUser.getId() != user.getId() && !currentUser.getRoles().contains("ROLE_ADMIN"))
            throw new ForbiddenActionException("You can only save your own account");

        User foundUser = userProvider.userOfId(user.getId())
                .orElseThrow(() -> new UserNotFoundException("We could not find the user you want to save", String.valueOf(user.getId())));

        foundUser.setFullName(user.getFullName());
        foundUser.setProfilePicture(user.getProfilePicture());
        foundUser.setProfession(user.getProfession());
        foundUser.setBio(user.getBio());
        foundUser.setLanguage(user.getLanguage());
        foundUser.setWebsite(user.getWebsite());
        foundUser.setSocialLinks(user.getSocialLinks());
        userProvider.save(foundUser);
        return foundUser;
    }

    public User execute(String email, User currentUser) throws UserNotFoundException {

        User foundUser = userProvider.userOfId(currentUser.getId())
                .orElseThrow(() -> new UserNotFoundException("We could not find the user you want to save", String.valueOf(currentUser.getId())));

        foundUser.setEmail(email);
        userProvider.save(foundUser);
        return foundUser;
    }
}
