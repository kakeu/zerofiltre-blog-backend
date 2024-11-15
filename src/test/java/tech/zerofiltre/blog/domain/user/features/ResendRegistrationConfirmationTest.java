package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.user.UserNotificationProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.VerificationToken;

import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class ResendRegistrationConfirmationTest {


    @MockBean
    UserNotificationProvider userNotificationProvider;

    @MockBean
    UserProvider userProvider;

    @MockBean
    VerificationTokenProvider tokenProvider;

    LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);


    ResendRegistrationConfirmation resendRegistrationConfirmation;

    @BeforeEach
    void setUp() {
        resendRegistrationConfirmation = new ResendRegistrationConfirmation(userProvider, userNotificationProvider, tokenProvider);
        when(tokenProvider.generate(any(),anyLong())).thenAnswer(invocationOnMock -> new VerificationToken(invocationOnMock.getArgument(0), "",expiryDate));
    }

    @Test
    void mustCheckUser_ThenNotify() throws UserNotFoundException {
        //ARRANGE
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(new User()));
        doNothing().when(userNotificationProvider).notify(any());

        //ACT
        resendRegistrationConfirmation.execute("email", "appUrl", Locale.FRANCE);

        //ASSERT
        verify(userProvider, times(1)).userOfEmail(any());
        verify(userNotificationProvider, times(1)).notify(any());

    }

    @Test
    void onUserNotFound_ThrowUserNotFoundException() {
        //ARRANGE
        when(userProvider.userOfEmail(any())).thenReturn(Optional.empty());

        //ACT & ASSERT
        assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(() -> resendRegistrationConfirmation.execute("email", "appUrl", Locale.FRANCE));

    }
}