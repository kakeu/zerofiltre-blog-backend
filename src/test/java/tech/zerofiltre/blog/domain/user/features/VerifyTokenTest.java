package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.VerificationToken;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class VerifyTokenTest {

    public static final String TOKEN = "token";
    @MockBean
    VerificationTokenProvider verificationTokenProvider;

    LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);

    VerifyToken verifyToken;

    User user = new User();
    VerificationToken verificationToken = new VerificationToken(user, TOKEN, expiryDate);


    @BeforeEach
    void setUp() {
        verifyToken = new VerifyToken(verificationTokenProvider);
    }

    @Test
    void onValidToken_setUserActive_thenSave() throws InvalidTokenException {
        //ARRANGE
        when(verificationTokenProvider.ofToken(TOKEN)).thenReturn(java.util.Optional.ofNullable(verificationToken));

        //ACT
        verifyToken.execute(TOKEN);

        //ASSERT
        verify(verificationTokenProvider, times(1)).ofToken(TOKEN);

    }

    @Test
    void onTokenExpired_ThrowInvalidTokenException_withProperMessage() {
        //ARRANGE
        verificationToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        when(verificationTokenProvider.ofToken(TOKEN)).thenReturn(java.util.Optional.ofNullable(verificationToken));

        //ACT & ASSERT
        assertThatExceptionOfType(InvalidTokenException.class).isThrownBy(() -> verifyToken.execute(TOKEN))
                .withMessage(ConfirmUserRegistration.TOKEN_EXPIRED);

    }

    @Test
    void onTokenNotFound_ThrowInvalidTokenException_withProperMessage() {
        //ARRANGE
        when(verificationTokenProvider.ofToken(TOKEN)).thenReturn(Optional.empty());

        //ACT & ASSERT
        assertThatExceptionOfType(InvalidTokenException.class).isThrownBy(() -> verifyToken.execute(TOKEN))
                .withMessage(ConfirmUserRegistration.INVALID_TOKEN);

    }

    @Test
    void invalidate_token_deletesIt() {

        //ACT
        verifyToken.invalidate(verificationToken);

        //ASSERT
        verify(verificationTokenProvider, times(1)).delete(verificationToken);

    }
}