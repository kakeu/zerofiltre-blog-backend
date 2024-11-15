package tech.zerofiltre.blog.infra.providers.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.context.MessageSource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.logging.MessageSourceProvider;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class SpringMessageSourceProviderTest {

    @Mock
    private MessageSource messageSource;

    MessageSourceProvider messageSourceProvider;

    @BeforeEach
    void setUp() {
        messageSourceProvider = new SpringMessageSourceProvider(messageSource);
    }

    @Test
    void getMessage() {

        String ESPECTED_MESSAGE = "Renouvellement impossible car il s'agit d'un compte github/stackoverflow. \n Merci de faire la demande avec une autre adresse email. ";

        when(messageSource.getMessage("ZBLOG_013", null, Locale.getDefault())).thenReturn(ESPECTED_MESSAGE);

        String message = messageSourceProvider.getMessage("ZBLOG_013", null, Locale.getDefault());

        assertEquals(ESPECTED_MESSAGE, message);
    }
}