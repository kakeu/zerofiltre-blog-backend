package tech.zerofiltre.blog.infra.providers.api.notchpay.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookData {
    private int amount;
    @JsonProperty("amount_total")
    private int amountTotal;
    private boolean sandbox;
    private int fee;
    @JsonProperty("payment_method")
    private String paymentMethod;
    @JsonProperty("merchant_reference")
    private String merchantReference;
}



