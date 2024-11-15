package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.VerificationToken;
import tech.zerofiltre.blog.infra.providers.database.user.DBUserProvider;
import tech.zerofiltre.blog.infra.providers.database.user.DBVerificationTokenProvider;

import java.time.LocalDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
@Import({DBUserProvider.class, DBVerificationTokenProvider.class})
class ConfirmUserRegistrationIT {


    public static final String TOKEN = "token";

    @Autowired
    VerificationTokenProvider verificationTokenProvider;

    @Autowired
    UserProvider userProvider;

    LocalDateTime expiryDate = LocalDateTime.now().plusDays(1);

    ConfirmUserRegistration confirmUserRegistration;

    User user = new User();
    VerificationToken verificationToken;


    @BeforeEach
    void setUp() {
        user = userProvider.save(user);
        verificationToken = new VerificationToken(user, TOKEN,expiryDate);
        verificationTokenProvider.save(verificationToken);
        confirmUserRegistration = new ConfirmUserRegistration(verificationTokenProvider, userProvider);
    }

    @Test
    void onValidToken_setUserActive_thenSave() throws InvalidTokenException {
        //ARRANGE
        assertThat(user.isActive()).isFalse();

        //ACT
        user = confirmUserRegistration.execute(TOKEN);

        //ASSERT
        assertThat(user.isActive()).isTrue();

    }

}