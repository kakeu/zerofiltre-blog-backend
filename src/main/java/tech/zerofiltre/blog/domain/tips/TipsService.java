package tech.zerofiltre.blog.domain.tips;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class TipsService {

    private final TipsProvider tipsProvider;

    public String generateTip(User user, Locale locale) throws ZerofiltreException {
        String profession = "";

        if(user != null) {
            profession = user.getProfession();
            if(profession == null || profession.isBlank()) profession = "";

            locale = Locale.forLanguageTag(user.getLanguage());
        }

        return tipsProvider.getTip(profession, locale);
    }
}
