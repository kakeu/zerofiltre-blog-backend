package tech.zerofiltre.blog.domain.logging;

import org.springframework.lang.Nullable;

import java.util.Locale;

public interface MessageSourceProvider {
    String getMessage(String code, @Nullable Object[] args, Locale locale);
}
