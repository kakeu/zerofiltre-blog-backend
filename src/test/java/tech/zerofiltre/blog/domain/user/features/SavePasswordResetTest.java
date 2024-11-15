package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.VerificationToken;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class SavePasswordResetTest {

    public static final String TOKEN = "token";
    public static final String PASSWORD = "password";
    private SavePasswordReset savePasswordReset;

    @MockBean
    private VerificationTokenProvider verificationTokenProvider;

    @MockBean
    private UserProvider userProvider;

    LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);


    @BeforeEach
    void setUp() {
        savePasswordReset = new SavePasswordReset(verificationTokenProvider, userProvider);
    }

    @Test
    void mustCheckToken_ThenSaveUser() throws InvalidTokenException {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        //ARRANGE
        User user = new User();
        when(verificationTokenProvider.ofToken(TOKEN)).thenReturn(java.util.Optional.of(new VerificationToken(user, TOKEN,expiryDate)));
        when(userProvider.save(any())).thenReturn(user);

        //ACT
        savePasswordReset.execute(passwordEncoder.encode(PASSWORD), TOKEN);

        //ASSERT
        verify(verificationTokenProvider, times(1)).ofToken(TOKEN);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userProvider, times(1)).save(captor.capture());
        User updatedUser = captor.getValue();
        assertThat(passwordEncoder.matches(PASSWORD, updatedUser.getPassword())).isTrue();
        verify(verificationTokenProvider, times(1)).delete(any());
    }
}