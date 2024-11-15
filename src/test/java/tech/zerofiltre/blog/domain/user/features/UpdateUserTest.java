package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.error.ForbiddenActionException;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.SocialLink;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UpdateUserTest {

    public static final String NEW_WEBSITE = "new website";
    public static final String NEW_BIO = "new bio";
    public static final String NEW_PROFESSION = "new profession";
    public static final String NEW_PROFILE_PICTURE = "new profile picture";
    public static final String NEW_LAST_NAME = "new last name";
    public static final String NEW_FIRST_NAME = "new first name";
    public static final String NEW_LINK = "new link";
    public static final String NEW_LANGUAGE = "new language";
    User patchUser = new User();
    User currentUser = new User();
    private UpdateUser updateUser;
    @MockBean
    private UserProvider userProvider;

    @BeforeEach
    void setUp() {
        updateUser = new UpdateUser(userProvider);
        patchUser.setId(10);
        SocialLink socialLink = new SocialLink(SocialLink.Platform.STACKOVERFLOW, NEW_LINK);
        patchUser.setSocialLinks(Collections.singleton(socialLink));

    }


    @Test
    void updateUser_savesUserProperly() throws ForbiddenActionException, UserNotFoundException {
        //ARRANGE
        User foundUser = ZerofiltreUtils.createMockUser(false);
        currentUser.setId(1);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(foundUser));
        when(userProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        User patchUser = new User();
        patchUser.setId(1);
        patchUser.setWebsite(NEW_WEBSITE);
        patchUser.setBio(NEW_BIO);
        patchUser.setProfession(NEW_PROFESSION);
        patchUser.setProfilePicture(NEW_PROFILE_PICTURE);
        patchUser.setFullName(NEW_FIRST_NAME);
        SocialLink socialLink = new SocialLink(SocialLink.Platform.STACKOVERFLOW, NEW_LINK);
        patchUser.setSocialLinks(Collections.singleton(socialLink));
        patchUser.setLanguage(NEW_LANGUAGE);


        //ACT
        User patchedUser = updateUser.patch(currentUser, patchUser);

        //ASSERT
        verify(userProvider, times(1)).save(any());
        assertThat(patchedUser).isNotNull();

        assertThat(patchedUser.getLoginFrom()).isNotNull();
        assertThat(patchedUser.getWebsite()).isEqualTo(NEW_WEBSITE);
        assertThat(patchedUser.getBio()).isEqualTo(NEW_BIO);
        assertThat(patchedUser.getProfession()).isEqualTo(NEW_PROFESSION);
        assertThat(patchedUser.getProfilePicture()).isEqualTo(NEW_PROFILE_PICTURE);
        assertThat(patchedUser.getFullName()).isEqualTo(NEW_FIRST_NAME);
        assertThat(patchedUser.getLanguage()).isEqualTo(NEW_LANGUAGE);
        Set<SocialLink> socialLinks = patchedUser.getSocialLinks();
        assertThat(socialLinks.size()).isEqualTo(1);
        assertThat(socialLinks.stream().anyMatch(link ->
                link.getLink().equals(NEW_LINK) && link.getPlatform().equals(SocialLink.Platform.STACKOVERFLOW)
        )).isTrue();

    }

    @Test
    void updateUser_updatesSocialLinks() throws ForbiddenActionException, UserNotFoundException {
        //ARRANGE
        User foundUser = ZerofiltreUtils.createMockUser(false);

        assertThat(foundUser.getSocialLinks().size()).isEqualTo(3);
        currentUser.setId(1);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(foundUser));
        when(userProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        User patchUser = new User();
        patchUser.setId(1);
        SocialLink socialLink = new SocialLink(SocialLink.Platform.TWITTER, NEW_LINK);
        patchUser.setSocialLinks(Collections.singleton(socialLink));


        //ACT
        User patchedUser = updateUser.patch(currentUser, patchUser);

        //ASSERT
        verify(userProvider, times(1)).save(any());
        assertThat(patchedUser).isNotNull();


        Set<SocialLink> socialLinks = patchedUser.getSocialLinks();
        assertThat(socialLinks.size()).isEqualTo(1);
        assertThat(socialLinks.stream().anyMatch(link ->
                link.getLink().equals(NEW_LINK) && link.getPlatform().equals(SocialLink.Platform.TWITTER)
        )).isTrue();


    }

    @Test
    @DisplayName("Updating a non existing user throws a UserNotFoundException")
    void updateUser_ThrowsUserNotFoundException() {

        //ARRANGE
        currentUser.setId(10);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.empty());

        //ACT && ASSERT
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> updateUser.patch(currentUser, patchUser));
    }

    @Test
    @DisplayName("Updating a user without being an admin nor the current user throws ForbiddenActionException")
    void updateUser_ThrowsForbiddenActionException_IfNotTheCurrentUser() {

        //ARRANGE
        currentUser.setId(12);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(patchUser));


        //ACT && ASSERT
        assertThatExceptionOfType(ForbiddenActionException.class)
                .isThrownBy(() -> updateUser.patch(currentUser, patchUser));
    }

    @Test
    @DisplayName("Updating a user without being the current user, but an administrator works")
    void updateUser_savesProperly_IfNotTheCurrentUserButIsAdmin() {

        //ARRANGE
        currentUser.setId(12);
        currentUser.setRoles(Collections.singleton("ROLE_ADMIN"));
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(patchUser));


        //ACT && ASSERT
        assertThatNoException().isThrownBy(() -> updateUser.patch(currentUser, patchUser));
        verify(userProvider, times(1)).save(patchUser);
    }
}