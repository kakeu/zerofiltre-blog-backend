package tech.zerofiltre.blog.domain.article.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;
import tech.zerofiltre.blog.domain.user.SocialLoginProvider;
import tech.zerofiltre.blog.domain.user.features.RetrieveSocialToken;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class RetrieveSocialTokenTest {

    public static final String TOKEN = "token";
    private RetrieveSocialToken retrieveSocialToken;

    @MockBean
    private SocialLoginProvider socialLoginProvider;

    @BeforeEach
    void init() {
        retrieveSocialToken = new RetrieveSocialToken(socialLoginProvider);

    }

    @Test
    void execute_returnsTokenProperly() throws ResourceNotFoundException {
        //ARRANGE
        when(socialLoginProvider.tokenFromCode(any())).thenReturn(TOKEN);


        //ACT
        String token = retrieveSocialToken.execute("");

        //ASSERT
        assertThat(token).isNotNull();
        assertThat(token).isEqualTo(TOKEN);
    }

    @Test
    void execute_ThrowsNotFoundExceptionIfTokenIsNotFound() {
        //ARRANGE
        when(socialLoginProvider.tokenFromCode(any())).thenReturn(null);

        //ACT & ASSERT
        assertThatExceptionOfType(ResourceNotFoundException.class)
                .isThrownBy(() -> retrieveSocialToken.execute(""));


    }


}
