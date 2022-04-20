package tech.zerofiltre.blog.infra.entrypoints.rest.user;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.mockito.*;
import org.springframework.boot.test.mock.mockito.*;
import org.springframework.context.*;
import org.springframework.core.env.*;
import org.springframework.security.crypto.password.*;
import org.springframework.test.context.junit.jupiter.*;
import org.springframework.test.util.*;
import tech.zerofiltre.blog.domain.article.*;
import tech.zerofiltre.blog.domain.error.*;
import tech.zerofiltre.blog.domain.logging.*;
import tech.zerofiltre.blog.domain.user.*;
import tech.zerofiltre.blog.domain.user.model.*;
import tech.zerofiltre.blog.domain.user.use_cases.*;
import tech.zerofiltre.blog.infra.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.*;
import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.*;
import tech.zerofiltre.blog.infra.providers.api.github.*;
import tech.zerofiltre.blog.infra.providers.logging.*;
import tech.zerofiltre.blog.infra.security.model.*;

import javax.servlet.http.*;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static tech.zerofiltre.blog.domain.user.model.SocialLink.Platform.*;

@ExtendWith(SpringExtension.class)
class UserControllerTest {

    public static final String TOKEN = "token";
    public static final String EMAIL = "email";
    public static final String LAST_NAME = "lastName";
    public static final String FIRST_NAME = "firstName";
    public static final String BIO = "bio";
    public static final String GITHUBLINK = "Github link";
    public static final String WEBSITE = "website";
    @MockBean
    MessageSource sources;

    @Mock
    RetrieveSocialToken retrieveSocialToken;

    @MockBean
    PasswordEncoder passwordEncoder;

    @MockBean
    UserProvider userProvider;

    @MockBean
    private ReactionProvider reactionProvider;

    @MockBean
    AvatarProvider profilePictureGenerator;

    @MockBean
    GithubLoginProvider githubLoginProvider;

    @MockBean
    ArticleProvider articleProvider;

    @MockBean
    JwtAuthenticationTokenProperties jwTokenConfiguration;


    LoggerProvider loggerProvider = new Slf4jLoggerProvider();

    @MockBean
    PasswordVerifierProvider passwordVerifierProvider;

    @MockBean
    UserNotificationProvider userNotificationProvider;

    @MockBean
    VerificationTokenProvider verificationTokenProvider;

    @MockBean
    Environment environment;

    @MockBean
    HttpServletRequest request;

    @MockBean
    InfraProperties infraProperties;

    UserController userController;

    RegisterUserVM userVM = new RegisterUserVM();
    UpdatePasswordVM updatePasswordVM = new UpdatePasswordVM();


    @MockBean
    private SecurityContextManager securityContextManager;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;


    @BeforeEach
    void setUp() throws ResourceNotFoundException {
        userController = new UserController(
                userProvider, userNotificationProvider, articleProvider, verificationTokenProvider, sources,
                passwordEncoder, securityContextManager, passwordVerifierProvider,
                jwTokenConfiguration, infraProperties, githubLoginProvider, profilePictureGenerator, verificationTokenProvider, reactionProvider, jwtTokenProvider, loggerProvider);

        when(infraProperties.getEnv()).thenReturn("dev");
    }

    @Test
    void registerUser_MustRegisterUser_ThenNotifyRegistrationComplete() throws ResourceAlreadyExistException {
        //ARRANGE
        userVM.setEmail(EMAIL);
        userVM.setFullName(FIRST_NAME);
        when(request.getLocale()).thenReturn(Locale.FRANCE);
        VerificationToken t = new VerificationToken(new User(), TOKEN);
        when(verificationTokenProvider.generate(any())).thenReturn(t);
        when(verificationTokenProvider.generate(any(), anyLong())).thenReturn(t);
        when(jwtTokenProvider.generate(any())).thenReturn(new JwtToken(TOKEN, 784587));
        when(userProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        //ACT
        userController.registerUser(userVM, request);

        //ASSERT
        verify(userProvider, times(1)).userOfEmail(any());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userProvider, times(1)).save(captor.capture());
        User user = captor.getValue();
        assertThat(user.getEmail()).isEqualTo(EMAIL);
        assertThat(user.getFullName()).isEqualTo(FIRST_NAME);
        assertThat(user.getLanguage()).isEqualTo(Locale.FRANCE.getLanguage());
        verify(userNotificationProvider, times(1)).notify(any());
        verify(verificationTokenProvider, times(1)).generate(any(), anyLong());
        verify(jwtTokenProvider, times(1)).generate(any());
    }

    @Test
    void resendRegistrationConfirm_mustNotify() {
        //ARRANGE
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(new User()));

        //ACT
        userController.resendRegistrationConfirm("email", request);

        //ASSERT
        verify(userProvider, times(1)).userOfEmail(any());
        verify(userNotificationProvider, times(1)).notify(any());

    }

    @Test
    void confirmRegistration_mustCheckToken_ThenSaveUser() throws InvalidTokenException {
        //ARRANGE
        when(verificationTokenProvider.ofToken(any())).thenReturn(Optional.of(new VerificationToken(new User(), "")));

        //ACT
        userController.registrationConfirm("token", request);

        //ASSERT
        verify(verificationTokenProvider, times(1)).ofToken(any());
        verify(userProvider, times(1)).save(any());

    }

    @Test
    void resetPassword_mustCheckUser_ThenNotify() {
        //ARRANGE
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(new User()));

        //ACT
        userController.initPasswordReset("email", request);

        //ASSERT
        verify(userProvider, times(1)).userOfEmail(EMAIL);
        verify(userNotificationProvider, times(1)).notify(any());

    }

    @Test
    void verifyToken_mustCheckToken() throws InvalidTokenException {
        //ARRANGE
        when(verificationTokenProvider.ofToken(any())).thenReturn(Optional.of(new VerificationToken(new User(), "")));


        //ACT
        userController.verifyTokenForPasswordReset(TOKEN);

        //ASSERT
        verify(verificationTokenProvider, times(1)).ofToken(any());

    }

    @Test
    void updatePassword_mustCheckUserAndPassword_thenSave() throws BlogException {
        //ARRANGE
        User user = new User();
        user.setEmail(EMAIL);
        when(securityContextManager.getAuthenticatedUser()).thenReturn(user);
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(user));
        when(passwordVerifierProvider.isValid(any(), any())).thenReturn(true);
        when(userProvider.save(any())).thenReturn(user);

        //ACT
        userController.updatePassword(updatePasswordVM, request);

        //ASSERT
        verify(userProvider, times(1)).userOfEmail(EMAIL);
        verify(passwordVerifierProvider, times(1)).isValid(user, updatePasswordVM.getOldPassword());
        verify(userProvider, times(1)).save(any());

    }

    @Test
    void getUserProfile_mustBuildPublicUserProfileVM_properly() throws UserNotFoundException {
        //ARRANGE
        User user = new User();
        user.setFullName(FIRST_NAME);
        user.setBio(BIO);
        user.setLanguage(Locale.FRANCE.getLanguage());
        user.setSocialLinks(Collections.singleton(new SocialLink(GITHUB, GITHUBLINK)));
        user.setWebsite(WEBSITE);
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.of(user));

        //ACT
        PublicUserProfileVM result = userController.getUserProfile(23);

        //ASSERT
        assertThat(result).isNotNull();
        assertThat(result.getFullName()).isEqualTo(FIRST_NAME);
        assertThat(result.getBio()).isEqualTo(BIO);
        assertThat(result.getLanguage()).isEqualTo(Locale.FRANCE.getLanguage());
        Set<SocialLink> socialLinks = result.getSocialLinks();
        socialLinks.forEach(socialLink -> {
            assertThat(socialLink.getLink()).isEqualTo(GITHUBLINK);
            assertThat(socialLink.getPlatform()).isEqualTo(GITHUB);
        });
        assertThat(result.getWebsite()).isEqualTo(WEBSITE);

    }

    @Test
    void getUserProfile_throwUserNotFoundException_IfNoUserExists() {
        //ARRANGE
        when(userProvider.userOfId(anyLong())).thenReturn(Optional.empty());

        //ACT & ASSERT
        assertThatExceptionOfType(UserNotFoundException.class)
                .isThrownBy(() -> userController.getUserProfile(23));


    }

    @Test
    void retrieveSocialToken_constructTheToken() throws ResourceNotFoundException {
        //ARRANGE
        ReflectionTestUtils.setField(userController, "retrieveSocialToken", retrieveSocialToken);
        when(retrieveSocialToken.execute(any())).thenReturn(TOKEN);

        //ACT
        Token token = userController.getGithubToken("code");
        assertThat(token.getTokenType()).isEqualTo("token");
        assertThat(token.getAccessToken()).isEqualTo(TOKEN);
        assertThat(token.getRefreshToken()).isEmpty();
        assertThat(token.getAccessTokenExpiryDateInSeconds()).isZero();
        assertThat(token.getRefreshTokenExpiryDateInSeconds()).isZero();

        //ASSERT
        verify(retrieveSocialToken, times(1)).execute("code");

    }
}