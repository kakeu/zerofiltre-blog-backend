package tech.zerofiltre.blog.infra.providers.api.notchpay;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tech.zerofiltre.blog.domain.Product;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.metrics.MetricsProvider;
import tech.zerofiltre.blog.domain.metrics.model.CounterSpecs;
import tech.zerofiltre.blog.domain.payment.PaymentException;
import tech.zerofiltre.blog.domain.payment.PaymentProvider;
import tech.zerofiltre.blog.domain.payment.model.ChargeRequest;
import tech.zerofiltre.blog.domain.payment.model.Payment;
import tech.zerofiltre.blog.domain.user.UserNotificationProvider;
import tech.zerofiltre.blog.domain.user.UserProvider;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.UserActionEvent;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.api.notchpay.model.NotchPaymentPaylod;
import tech.zerofiltre.blog.infra.providers.api.notchpay.model.WebhookPayload;
import tech.zerofiltre.blog.infra.providers.database.payment.DBNotchPayProvider;
import tech.zerofiltre.blog.infra.providers.notification.user.ZerofiltreEmailSender;
import tech.zerofiltre.blog.infra.providers.notification.user.model.Email;
import tech.zerofiltre.blog.infra.security.config.EmailValidator;
import tech.zerofiltre.blog.util.ZerofiltreUtils;

import java.time.LocalDateTime;
import java.util.*;

import static tech.zerofiltre.blog.domain.payment.model.Payment.MONTH;
import static tech.zerofiltre.blog.domain.payment.model.Payment.YEAR;

@Slf4j
@Component
@Qualifier("notchPayProvider")
public class NotchPayProvider implements PaymentProvider {
    public static final String NOTCHPAY_LABEL_VALUE = "notchpay";
    public static final String FALSE_LABEL_VALUE = "false";
    public static final String SUCCESS_LABEL = "success";
    public static final String PROVIDER_LABEL = "provider";
    public static final String TRUE_LABEL_VALUE = "true";

    private final RestTemplate restTemplate;
    private final InfraProperties infraProperties;
    private final RetryTemplate retryTemplate;
    private final UserProvider userProvider;
    private final DBNotchPayProvider dbNotchPayProvider;
    private final ZerofiltreEmailSender emailSender;
    private final MetricsProvider metricsProvider;
    private final UserNotificationProvider userNotificationProvider;

    public NotchPayProvider(RestTemplate restTemplate, InfraProperties infraProperties, RetryTemplate retryTemplate, UserProvider userProvider, DBNotchPayProvider dbNotchPayProvider, ZerofiltreEmailSender emailSender, MetricsProvider metricsProvider, UserNotificationProvider userNotificationProvider) {
        this.restTemplate = restTemplate;
        this.infraProperties = infraProperties;
        this.retryTemplate = retryTemplate;
        this.userProvider = userProvider;
        this.dbNotchPayProvider = dbNotchPayProvider;
        this.emailSender = emailSender;
        this.metricsProvider = metricsProvider;
        this.userNotificationProvider = userNotificationProvider;
    }

    @Override
    public String createSession(User user, Product product, ChargeRequest chargeRequest) throws PaymentException {
        CounterSpecs counterSpecs = new CounterSpecs();
        counterSpecs.setName(CounterSpecs.ZEROFILTRE_CHECKOUT_CREATIONS);
        String appUrl = ZerofiltreUtils.getOriginUrl(infraProperties.getEnv());

        NotchPaymentPaylod body = new NotchPaymentPaylod();
        body.setEmail(chargeRequest.getPaymentEmail());
        boolean monthly = "month".equals(chargeRequest.getRecurringInterval());
        int amount = monthly ? 6500 : 72000;
        body.setAmount(amount);
        body.setCurrency(chargeRequest.getCurrency().getValue());

        body.setDescription("Abonnement PRO" + (monthly ? "" : " annuel") + " à la plateforme Zerofiltre");
        String paymentReference = UUID.randomUUID().toString();
        body.setReference(paymentReference);
        body.getCustomerMeta().put("userId", user.getId());
        body.setCallback("https://zerofiltre.tech/cours");

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
        headers.add("Authorization", infraProperties.getNotchPayPublicKey());

        try {
            return retryTemplate.execute(retryContext -> {
                HttpEntity<NotchPaymentPaylod> requestEntity = new HttpEntity<>(body, headers);
                ResponseEntity<String> response = restTemplate.exchange(infraProperties.getNotchPayUrl() + "/payments/initialize", HttpMethod.POST, requestEntity, String.class);
                String responseBody = response.getBody();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode rootNode;
                rootNode = mapper.readTree(responseBody);
                JsonNode code = rootNode.get("code");
                JsonNode authorizationUrl = rootNode.get("authorization_url");
                if (code != null && code.asText().equals("201")) {
                    Payment payment = new Payment();
                    payment.setRecurringInterval(monthly ? MONTH : YEAR);
                    payment.setReference(paymentReference);
                    payment.setUser(user);
                    save(payment);
                    user.setPaymentEmail(chargeRequest.getPaymentEmail());
                    userProvider.save(user);
                    String session = authorizationUrl.asText();
                    userNotificationProvider.notify(new UserActionEvent(appUrl, Locale.forLanguageTag(user.getLanguage()), user, null, null, Action.CHECKOUT_STARTED));
                    counterSpecs.setTags(PROVIDER_LABEL, NOTCHPAY_LABEL_VALUE, SUCCESS_LABEL, TRUE_LABEL_VALUE);
                    metricsProvider.incrementCounter(counterSpecs);
                    return session;
                } else {
                    counterSpecs.setTags(PROVIDER_LABEL, NOTCHPAY_LABEL_VALUE, SUCCESS_LABEL, FALSE_LABEL_VALUE);
                    metricsProvider.incrementCounter(counterSpecs);
                    throw new PaymentException("We couldn't initialize the payment due to : " + responseBody);
                }
            });
        } catch (Exception e) {
            log.error("We couldn't initialize the payment", e);
            counterSpecs.setTags(PROVIDER_LABEL, NOTCHPAY_LABEL_VALUE, SUCCESS_LABEL, FALSE_LABEL_VALUE);
            metricsProvider.incrementCounter(counterSpecs);
            throw new PaymentException("We couldn't initialize the payment", e);
        }
    }

    @Override
    public String handleWebhook(String payload, String signature) throws PaymentException {
        try {
            String computedHash = new HmacUtils("HmacSHA256", infraProperties.getNotchPayHash()).hmacHex(payload);
            if (!computedHash.equals(signature)) throw new PaymentException("Invalid notchpay signature");
            WebhookPayload webhookPayload = new ObjectMapper().readValue(payload, WebhookPayload.class);
            if (!"payment.complete".equals(webhookPayload.getEvent()))
                return "Nothing was done as payment was not completed";
            String paymentReference = webhookPayload.getData().getMerchantReference();
            Optional<Payment> payment = paymentOf(paymentReference);
            if (payment.isEmpty()) throw new PaymentException("Initialized payment not found in db");
            Payment foundPayment = payment.get();
            foundPayment.setStatus(Payment.COMPLETED);
            foundPayment.setAt(LocalDateTime.now());
            save(foundPayment);
            User user = userProvider.userOfId(foundPayment.getUser().getId()).orElseThrow(() -> new PaymentException("Unable to find the user of the payment"));
            user.setPlan(User.Plan.PRO);
            userProvider.save(user);
            String originURL = ZerofiltreUtils.getOriginUrl(infraProperties.getEnv());

            notifyUser(user,
                    "Votre paiement chez Zerofiltre",
                    "Merci de faire confiance à Zerofiltre.tech, vous pouvez dès à présent bénéficier de nos contenus dans leur entièreté: " +
                            originURL +
                            ". Vous recevrez un rappel 5 jours avant la date de renouvellement de votre abonnement afin d'éviter toute coupure.");
            
            return "OK";
        } catch (Exception e) {
            throw new PaymentException("Unable to process notchpay webhook", e);
        }
    }

    @Override
    public void cancelSubscription(String paymentCustomerId) throws PaymentException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Optional<tech.zerofiltre.blog.domain.payment.model.Payment> paymentOf(String reference) {
        return dbNotchPayProvider.paymentOf(reference);
    }

    public tech.zerofiltre.blog.domain.payment.model.Payment save(tech.zerofiltre.blog.domain.payment.model.Payment payment) throws ZerofiltreException {
        return dbNotchPayProvider.save(payment);
    }

    public List<Payment> payments() {
        return dbNotchPayProvider.payments();
    }

    void notifyUser(User user, String subject, String message) {
        boolean validEmail = user.getEmail() != null && EmailValidator.validateEmail(user.getEmail());
        String emailAddress = validEmail ? user.getEmail() : user.getPaymentEmail();
        if (emailAddress != null) {
            Email email = new Email();
            email.setRecipients(Collections.singletonList(emailAddress));
            email.setSubject(subject);
            email.setReplyTo("info@zerofiltre.tech");
            email.setContent(message);
            emailSender.send(email, false);
        }
    }

    public void delete(String status, long userId) {
        dbNotchPayProvider.delete(status, userId);
    }
}
