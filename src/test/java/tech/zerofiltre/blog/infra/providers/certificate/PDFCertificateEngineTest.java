package tech.zerofiltre.blog.infra.providers.certificate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.thymeleaf.ITemplateEngine;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;

import java.io.IOException;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PDFCertificateEngineTest {

    @Mock
    private ITemplateEngine templateEngine;

    private PDFCertificateEngine certificateEngine;

    @BeforeEach
    void init() {
        certificateEngine = new PDFCertificateEngine(templateEngine);

    }

    @Test
    void mustCall_TemplateEngine() throws IOException, ZerofiltreException {
        //given
        when(templateEngine.process(anyString(), any())).thenReturn("content");


        //when
        certificateEngine.process(Locale.FRANCE, "name", "title", "fileName");


        //then
        verify(templateEngine, times(1)).process(anyString(), any());
    }

}