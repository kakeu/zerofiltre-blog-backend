package tech.zerofiltre.blog.infra.providers.database.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class UserEmail {
    private String email;
    private String paymentEmail;
}
