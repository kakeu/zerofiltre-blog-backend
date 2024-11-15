package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.error.ResourceAlreadyExistException;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.user.AvatarProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.doubles.DummyMetricsProvider;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class RegisterUserTest {

    public static final String EMAIL = "email";
    private RegisterUser registerUser;

    @MockBean
    UserProvider userProvider;

    @MockBean
    AvatarProvider profilePictureGenerator;

    private final MetricsProvider metricsProvider = new DummyMetricsProvider();


    LocalDateTime beforeRegistration = LocalDateTime.now();
    User toRegister = new User();


    @BeforeEach
    void init() {
        registerUser = new RegisterUser(userProvider, profilePictureGenerator, metricsProvider);
        toRegister.setPassword("pass");
        toRegister.setFullName("first");
        toRegister.setEmail("email");

    }


    @Test
    @DisplayName("Must register the user properly")
    void mustSaveProperly() throws ResourceAlreadyExistException {
        //ARRANGE
        when(userProvider.userOfEmail(EMAIL)).thenReturn(Optional.empty());
        when(userProvider.save(any())).thenAnswer(invocationOnMock -> {
            User result = invocationOnMock.getArgument(0);
            result.setId(12);
            return result;
        });
        //ACT
        User registeredUser = registerUser.execute(toRegister);

        //ASSERT
        verify(userProvider, times(1)).userOfEmail(any());
        verify(userProvider, times(1)).save(any());
        verify(profilePictureGenerator,times(1)).byEmail(EMAIL);
        assertThat(registeredUser.getFullName()).isEqualTo(toRegister.getFullName());
        assertThat(registeredUser.getPassword()).isEqualTo(toRegister.getPassword());
        assertThat(registeredUser.getRegisteredOn()).isNotNull();
        assertThat(registeredUser.getRoles()).contains("ROLE_USER");
        assertThat(registeredUser.getRegisteredOn()).isAfterOrEqualTo(beforeRegistration);
        assertThat(registeredUser.getId()).isNotZero();

    }

    @Test
    @DisplayName("Must throw UserAlreadyExistException if another user with the same email exist already")
    void mustCheckAlreadyExistingUsers() {
        //ARRANGE
        when(userProvider.userOfEmail("email")).thenReturn(Optional.of(toRegister));
        when(userProvider.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        //ACT && ASSERT
        assertThatExceptionOfType(ResourceAlreadyExistException.class).isThrownBy(() -> registerUser.execute(toRegister));


    }
}