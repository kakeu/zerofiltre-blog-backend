package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.VerificationToken;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ConfirmUserRegistrationTest {


    public static final String TOKEN = "token";
    @MockBean
    VerificationTokenProvider verificationTokenProvider;
    LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);

    @MockBean
    UserProvider userProvider;

    ConfirmUserRegistration confirmUserRegistration;

    User user = new User();
    VerificationToken verificationToken = new VerificationToken(user, TOKEN,expiryDate);


    @BeforeEach
    void setUp() {
        confirmUserRegistration = new ConfirmUserRegistration(verificationTokenProvider, userProvider);
    }

    @Test
    void onValidToken_setUserActive_thenSaveAndDeleteToken() throws InvalidTokenException {
        //ARRANGE
        when(verificationTokenProvider.ofToken(TOKEN)).thenReturn(java.util.Optional.ofNullable(verificationToken));
        when(userProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        assertThat(user.isActive()).isFalse();

        //ACT
        user = confirmUserRegistration.execute(TOKEN);

        //ASSERT
        assertThat(user.isActive()).isTrue();
        verify(userProvider, times(1)).save(any());
        verify(verificationTokenProvider, times(1)).delete(any());

    }

    @Test
    void onTokenExpired_ThrowInvalidTokenException_withProperMessage() {
        //ARRANGE
        verificationToken.setExpiryDate(LocalDateTime.now().minusHours(1));
        when(verificationTokenProvider.ofToken(TOKEN)).thenReturn(java.util.Optional.ofNullable(verificationToken));

        //ACT & ASSERT
        assertThatExceptionOfType(InvalidTokenException.class).isThrownBy(() -> confirmUserRegistration.execute(TOKEN))
                .withMessage(ConfirmUserRegistration.TOKEN_EXPIRED);

    }

    @Test
    void onTokenNotFound_ThrowInvalidTokenException_withProperMessage() {
        //ARRANGE
        when(verificationTokenProvider.ofToken(TOKEN)).thenReturn(Optional.empty());

        //ACT & ASSERT
        assertThatExceptionOfType(InvalidTokenException.class).isThrownBy(() -> confirmUserRegistration.execute(TOKEN))
                .withMessage(ConfirmUserRegistration.INVALID_TOKEN);

    }
}