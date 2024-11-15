package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.SocialLink;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import({DBUserProvider.class})
class UpdateUserIT {

    public static final String NEW_WEBSITE = "new website";
    public static final String NEW_BIO = "new bio";
    public static final String NEW_PROFESSION = "new profession";
    public static final String NEW_PROFILE_PICTURE = "new profile picture";
    public static final String NEW_FIRST_NAME = "new first name";
    public static final String NEW_LINK = "new link";
    public static final String NEW_LANGUAGE = "new language";
    public static final String TOKEN = "torna-7878-osdo";
    private UpdateUser updateUser;

    @Autowired
    private UserProvider userProvider;

    @BeforeEach
    void init() {
        updateUser = new UpdateUser(userProvider);
    }

    @Test
    @DisplayName("Patching a user updates only the date that are meant to be updated. E.g: loginFrom should not be overridden")
    void patchUser_UpdatesProperly() throws UserNotFoundException, ForbiddenActionException {
        //ARRANGE
        User existingUser = ZerofiltreUtils.createMockUser(true);
        existingUser = userProvider.save(existingUser);

        //loginFrom will not be set
        User patchUser = new User();
        patchUser.setWebsite(NEW_WEBSITE);
        patchUser.setBio(NEW_BIO);
        patchUser.setProfession(NEW_PROFESSION);
        patchUser.setProfilePicture(NEW_PROFILE_PICTURE);
        patchUser.setFullName(NEW_FIRST_NAME);
        patchUser.setId(existingUser.getId());
        SocialLink socialLink = new SocialLink(SocialLink.Platform.STACKOVERFLOW, NEW_LINK);
        patchUser.setSocialLinks(Collections.singleton(socialLink));
        patchUser.setLanguage(NEW_LANGUAGE);

        //ACT
        updateUser.patch(existingUser, patchUser);
        User resultUser = userProvider.userOfId(existingUser.getId()).orElseThrow();

        //ASSERT

        //loginFrom has not been touched
        assertThat(resultUser.getLoginFrom()).isEqualTo(SocialLink.Platform.LINKEDIN);

        //All the other fields have been updated
        assertThat(resultUser.getWebsite()).isEqualTo(NEW_WEBSITE);
        assertThat(resultUser.getBio()).isEqualTo(NEW_BIO);
        assertThat(resultUser.getProfession()).isEqualTo(NEW_PROFESSION);
        assertThat(resultUser.getProfilePicture()).isEqualTo(NEW_PROFILE_PICTURE);
        assertThat(resultUser.getFullName()).isEqualTo(NEW_FIRST_NAME);
        assertThat(resultUser.getLanguage()).isEqualTo(NEW_LANGUAGE);
        Set<SocialLink> socialLinks = resultUser.getSocialLinks();
        assertThat(socialLinks.size()).isEqualTo(1);
        assertThat(socialLinks.stream().anyMatch(link ->
                link.getLink().equals(NEW_LINK) && link.getPlatform().equals(SocialLink.Platform.STACKOVERFLOW)
        )).isTrue();

    }

}
