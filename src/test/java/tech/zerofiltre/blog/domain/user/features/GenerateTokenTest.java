package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.user.JwtTokenProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.model.JwtToken;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.VerificationToken;
import tech.zerofiltre.blog.infra.security.model.Token;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class GenerateTokenTest {

    public static final String TOKEN = "token";
    @MockBean
    VerificationTokenProvider verificationTokenProvider;
    LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);
    private GenerateToken generateToken;
    @MockBean
    private JwtTokenProvider jwtTokenProvider;
    @MockBean
    private UserProvider userProvider;

    @BeforeEach
    void setUp() {
        generateToken = new GenerateToken(verificationTokenProvider, jwtTokenProvider, userProvider);
    }

    @Test
    void execute_generates_ProperlyFromRefreshToken() throws InvalidTokenException {
        //ARRANGE
        User user = new User();
        VerificationToken verificationToken = new VerificationToken(user, TOKEN, expiryDate);
        when(verificationTokenProvider.ofToken(TOKEN)).thenReturn(java.util.Optional.of(verificationToken));
        when(jwtTokenProvider.generate(user))
                .thenReturn(new JwtToken("accessToken", expiryDate.toEpochSecond(ZoneOffset.UTC)));

        //ACT
        Token token = generateToken.byRefreshToken(TOKEN);

        assertThat(token).isNotNull();

        assertThat(token.getRefreshToken()).isNotNull();
        assertThat(token.getAccessToken()).isNotNull();
        assertThat(token.getTokenType()).isNotNull();
        assertThat(token.getTokenType()).isNotEmpty();
        assertThat(token.getRefreshTokenExpiryDateInSeconds()).isEqualTo(expiryDate.toEpochSecond(ZoneOffset.UTC));
        assertThat(token.getAccessTokenExpiryDateInSeconds()).isEqualTo(expiryDate.toEpochSecond(ZoneOffset.UTC));
    }


    @Test
    void execute_generates_ProperlyFromUser() {
        //ARRANGE
        User user = new User();
        VerificationToken verificationToken = new VerificationToken(user, TOKEN, expiryDate);
        when(verificationTokenProvider.generate(user)).thenReturn(verificationToken);
        when(jwtTokenProvider.generate(user))
                .thenReturn(new JwtToken("accessToken", expiryDate.toEpochSecond(ZoneOffset.UTC)));

        //ACT
        Token token = generateToken.byUser(user);

        assertThat(token).isNotNull();

        assertThat(token.getRefreshToken()).isNotNull();
        assertThat(token.getAccessToken()).isNotNull();
        assertThat(token.getTokenType()).isNotNull();
        assertThat(token.getTokenType()).isNotEmpty();
        assertThat(token.getRefreshTokenExpiryDateInSeconds()).isEqualTo(expiryDate.toEpochSecond(ZoneOffset.UTC));
        assertThat(token.getAccessTokenExpiryDateInSeconds()).isEqualTo(expiryDate.toEpochSecond(ZoneOffset.UTC));
    }
}