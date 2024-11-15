package tech.zerofiltre.blog.infra.providers.api.stripe;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tech.zerofiltre.blog.infra.InfraProperties;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventChecker {

    public static final String INVALID_PAYLOAD = "Invalid payload";

    private final InfraProperties infraProperties;

    public Event checkAndProvideEvent(String payload, String sigHeader) {

        log.debug("Payload: {}", payload.replace("\n", " "));

        Event event = checkSignature(payload, sigHeader);

        log.debug("Handling Event: id = {},type = {}", event.getId(), event.getType());
        return event;
    }

    private Event checkSignature(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, infraProperties.getStripeWebhookSecret());
        } catch (SignatureVerificationException e) {
            throw new IllegalArgumentException("Invalid signature", e);
        } catch (Exception e) {
            throw new IllegalArgumentException(INVALID_PAYLOAD, e);
        }
        return event;
    }

}
