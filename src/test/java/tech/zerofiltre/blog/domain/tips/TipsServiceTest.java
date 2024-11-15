package tech.zerofiltre.blog.domain.tips;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Locale;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TipsServiceTest {

    private TipsService tipsService;

    @Mock
    private TipsProvider tipsProvider;

    @BeforeEach
    void init() {
        tipsService = new TipsService(tipsProvider);
    }

    @Test
    void given_user_null_when_generateTip_then_use_default_profession_and_locale() throws ZerofiltreException {
        //ACT
        tipsService.generateTip(null, Locale.FRENCH);

        //ASSERT
        verify(tipsProvider, times(1)).getTip("", Locale.FRENCH);
    }

    @Test
    void given_user_with_profession_and_language_when_generateTip_then_use_profession_and_locale() throws ZerofiltreException {
        //ARRANGE
        User user = new User();
        user.setProfession("engineer");
        user.setLanguage("fr");

        //ACT
        tipsService.generateTip(user, Locale.ENGLISH);

        //ASSERT
        verify(tipsProvider, times(1)).getTip(user.getProfession(), Locale.forLanguageTag(user.getLanguage()));
    }

    @Test
    void given_user_with_language_and_profession_null_when_generateTip_then_use_locale_and_default_profession() throws ZerofiltreException {
        //ARRANGE
        User user = new User();
        user.setProfession(null);
        user.setLanguage("en");

        //ACT
        tipsService.generateTip(user, Locale.FRENCH);

        //ASSERT
        verify(tipsProvider, times(1)).getTip("", Locale.forLanguageTag(user.getLanguage()));
    }

    @Test
    void given_user_with_language_and_profession_blank_when_generateTip_then_use_locale_and_default_profession() throws ZerofiltreException {
        //ARRANGE
        User user = new User();
        user.setProfession("    ");
        user.setLanguage("en");

        //ACT
        tipsService.generateTip(user, Locale.FRENCH);

        //ASSERT
        verify(tipsProvider, times(1)).getTip("", Locale.forLanguageTag(user.getLanguage()));
    }
}
