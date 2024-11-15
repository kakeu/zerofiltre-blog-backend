package tech.zerofiltre.blog.infra.providers.tips;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.logging.MessageSourceProvider;
import tech.zerofiltre.blog.domain.tips.AiProvider;
import tech.zerofiltre.blog.domain.tips.TipsProvider;
import tech.zerofiltre.blog.domain.tips.model.Tip;

import java.time.LocalDate;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class AiTipsProvider implements TipsProvider {

    private final MessageSourceProvider messageSourceProvider;
    private final AiProvider aiProvider;

    @Override
    public String getTip(String profession, Locale locale) throws ZerofiltreException {
        String request  = messageSourceProvider.getMessage("message.tip.ai.prompt", null, locale);

        if(profession.isEmpty()) {
            return findTip(request + messageSourceProvider.getMessage("message.tip.ai.prompt.profession", null, locale));
        }

        return aiProvider.answer(request + profession);
    }

    String findTip(String request) throws ZerofiltreException {
        if(Tip.date == null || !Tip.date.equals(LocalDate.now()) || Tip.tip == null || Tip.tip.isBlank()) {
            Tip.tip = aiProvider.answer(request);
            Tip.date = LocalDate.now();
        }
        return Tip.tip;
    }

}
