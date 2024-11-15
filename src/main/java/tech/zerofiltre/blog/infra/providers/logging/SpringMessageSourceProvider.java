package tech.zerofiltre.blog.infra.providers.logging;

import org.springframework.context.MessageSource;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.domain.logging.MessageSourceProvider;

import java.util.Locale;

@Component
public class SpringMessageSourceProvider implements MessageSourceProvider {

    private final MessageSource messageSource;

    public SpringMessageSourceProvider(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public String getMessage(String code, @Nullable Object[] args, Locale locale) {
        return messageSource.getMessage(code, args, locale);
    }
}
