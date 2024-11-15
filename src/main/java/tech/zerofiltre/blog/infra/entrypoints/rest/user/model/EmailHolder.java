package tech.zerofiltre.blog.infra.entrypoints.rest.user.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import tech.zerofiltre.blog.infra.security.config.EmailMatches;
import tech.zerofiltre.blog.infra.security.config.ValidEmail;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EmailMatches
public class EmailHolder {

    @NotNull(message = "The email must not be null")
    @NotEmpty(message = "The email must not be empty")
    @ValidEmail
    private String email;
    private String matchingEmail;
}
