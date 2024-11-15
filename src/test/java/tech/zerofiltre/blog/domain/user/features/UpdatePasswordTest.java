package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.user.PasswordVerifierProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.doubles.DummyMetricsProvider;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class UpdatePasswordTest {

    private UpdatePassword updatePassword;

    @MockBean
    private UserProvider userProvider;

    @MockBean
    private PasswordVerifierProvider passwordVerifierProvider;

    private final MetricsProvider metricsProvider = new DummyMetricsProvider();


    @BeforeEach
    void setUp() {

        updatePassword = new UpdatePassword(userProvider, passwordVerifierProvider, metricsProvider);
    }

    @Test
    void updatePassword_isOkOnValidInput() {
        //ARRANGE
        User user = new User();
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(user));
        when(passwordVerifierProvider.isValid(any(), any())).thenReturn(true);

        //ACT
        assertThatNoException().isThrownBy(() -> updatePassword.execute("", "", ""));


        //ASSERT
        verify(passwordVerifierProvider, times(1)).isValid(user, "");
    }

    @Test
    void updatePassword_throws_UserNotFoundException_OnMissingUser() {
        //ARRANGE
        when(userProvider.userOfEmail(any())).thenReturn(Optional.empty());

        //ACT & ASSERT
        assertThatExceptionOfType(UserNotFoundException.class).isThrownBy(() -> updatePassword.execute("", "", ""));

    }

    @Test
    void updatePassword_throws_InvalidPasswordException_OnIncorrectPassword() {
        //ARRANGE
        User user = new User();
        when(userProvider.userOfEmail(any())).thenReturn(Optional.of(user));
        when(passwordVerifierProvider.isValid(any(), any())).thenReturn(false);

        //ACT & ASSERT
        assertThatExceptionOfType(InvalidPasswordException.class).isThrownBy(() -> updatePassword.execute("", "", ""));

    }


}