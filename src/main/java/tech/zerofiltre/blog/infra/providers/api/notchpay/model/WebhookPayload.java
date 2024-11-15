package tech.zerofiltre.blog.infra.providers.api.notchpay.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WebhookPayload {
    private String event;
    private String id;
    private WebhookData data;
}

