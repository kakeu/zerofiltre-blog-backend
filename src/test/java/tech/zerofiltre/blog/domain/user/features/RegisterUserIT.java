package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.error.ResourceAlreadyExistException;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.user.AvatarProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.doubles.DummyMetricsProvider;
import tech.zerofiltre.blog.infra.providers.GravatarProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


@DataJpaTest
@Import({DBUserProvider.class, GravatarProvider.class})
class RegisterUserIT {

    private RegisterUser registerUser;

    @Autowired
    UserProvider userProvider;

    @Autowired
    AvatarProvider profilePictureGenerator;

    private final MetricsProvider metricsProvider = new DummyMetricsProvider();


    User toRegister = new User();

    @BeforeEach
    void init() {
        registerUser = new RegisterUser(userProvider, profilePictureGenerator, metricsProvider);
        toRegister.setPassword("pass");
        toRegister.setFullName("first");

    }


    @Test
    @DisplayName("Must register the user properly")
    void mustSaveProperly() throws ResourceAlreadyExistException {
        //ARRANGE
        toRegister.setEmail("email");

        //ACT
        User registeredUser = registerUser.execute(toRegister);

        //ASSERT
        assertThat(registeredUser.getFullName()).isEqualTo(toRegister.getFullName());
        assertThat(registeredUser.getPassword()).isEqualTo(toRegister.getPassword());
        assertThat(registeredUser.getRoles()).contains("ROLE_USER");
        assertThat(registeredUser.getId()).isNotZero();
        assertThat(registeredUser.getProfilePicture()).isNotNull();
        assertThat(registeredUser.getProfilePicture()).isNotEmpty();

    }

    @Test
    @DisplayName("Must throw UserAlreadyExistException if another user with the same email exist already")
    void mustCheckAlreadyExistingUsers() {
        //ARRANGE
        toRegister.setEmail("email1");

        userProvider.save(toRegister);

        //ACT && ASSERT
        assertThatExceptionOfType(ResourceAlreadyExistException.class).isThrownBy(() -> registerUser.execute(toRegister));


    }
}