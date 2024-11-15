package tech.zerofiltre.blog.infra.providers.certificate;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.layout.SharedContext;
import org.xhtmlrenderer.pdf.ITextRenderer;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Component
public class PDFCertificateEngine {

    private final ITemplateEngine templateEngine;

    public PDFCertificateEngine(ITemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }


    public byte[] process(Locale locale, String fullName, String courseTitle, String pdfFileName) throws IOException, ZerofiltreException {
        Resource attachment = new ClassPathResource("mail_header_image.png");

        Map<String, Object> templateModel = new HashMap<>();
        templateModel.put("attachement", attachment.getURL().toString());
        templateModel.put("fullName", fullName);
        templateModel.put("courseTitle", courseTitle);
        templateModel.put("today", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        Context thymeleafContext = new Context();
        thymeleafContext.setVariables(templateModel);
        thymeleafContext.setLocale(locale);

        String certificateHtml = templateEngine.process("certificate_course_completed.html", thymeleafContext);

        log.trace("certificateHtml = {}", certificateHtml);

        if (certificateHtml == null) throw new ZerofiltreException("Error during certificate generation.");

        return convertHtmlToPdf(certificateHtml, pdfFileName);
    }

    private byte[] convertHtmlToPdf(String certificateHtml, String pdfFileName) throws ZerofiltreException {
        org.jsoup.nodes.Document documentJsoup = Jsoup.parse(certificateHtml);
        documentJsoup.outputSettings().syntax(org.jsoup.nodes.Document.OutputSettings.Syntax.xml);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            ITextRenderer renderer = new ITextRenderer();
            SharedContext sharedContext = renderer.getSharedContext();
            sharedContext.setPrint(true);
            sharedContext.setInteractive(false);
            renderer.setDocumentFromString(documentJsoup.html());
            renderer.layout();
            renderer.createPDF(outputStream);
            outputStream.close();
        } catch (IOException e) {
            throw new ZerofiltreException("Error during certificate generation.", e);
        }

        log.info("Course completion certificate for file: {} converted to pdf completed.", pdfFileName);

        return outputStream.toByteArray();
    }
}
