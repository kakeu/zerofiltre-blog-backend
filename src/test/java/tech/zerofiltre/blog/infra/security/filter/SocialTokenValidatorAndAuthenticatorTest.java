package tech.zerofiltre.blog.infra.security.filter;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataIntegrityViolationException;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.user.SocialLoginProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.SocialLink;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.infra.entrypoints.rest.SecurityContextManager;

import java.util.Optional;

import static org.mockito.Mockito.*;

class SocialTokenValidatorAndAuthenticatorTest {

    AutoCloseable closeable;
    @Mock
    private SocialLoginProvider socialLoginProvider;
    @Mock
    private UserProvider userProvider;
    @Mock
    private MetricsProvider metricsProvider;
    @Mock
    private SecurityContextManager securityContextManager;
    @InjectMocks
    private SocialTokenValidatorAndAuthenticator<SocialLoginProvider> authenticator;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void validateAndAuthenticate_FindsUserBySocialId() {
        // Arrange
        String token = "valid-token";
        User user = new User();
        user.setEmail("test@example.com");
        user.setLoginFrom(SocialLink.Platform.GITHUB);
        when(socialLoginProvider.isValid(token)).thenReturn(true);
        when(socialLoginProvider.userOfToken(token)).thenReturn(Optional.of(user));

        when(userProvider.save(any())).thenReturn(user);

        // Act
        authenticator.validateAndAuthenticate(token);

        // Assert
        verify(userProvider).userOfSocialId(any());
    }

    @Test
    void validateAndAuthenticate_FindsUserByEmail_onDataIntegrityViolationException() {
        //Arrange
        String token = "valid-token";
        User user = new User();
        user.setEmail("XXXXXXXXXXXXXXXX");
        user.setLoginFrom(SocialLink.Platform.GITHUB);
        when(socialLoginProvider.isValid(token)).thenReturn(true);
        when(socialLoginProvider.userOfToken(token)).thenReturn(Optional.of(user));

        when(userProvider.userOfSocialId(any())).thenReturn(Optional.empty());
        when(userProvider.save(any())).thenThrow(new DataIntegrityViolationException("")).thenReturn(user);
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(user));

        // Act
        authenticator.validateAndAuthenticate(token);

        // Assert
        verify(userProvider).userOfSocialId(any());
        verify(userProvider).userOfEmail(any());

    }
}
