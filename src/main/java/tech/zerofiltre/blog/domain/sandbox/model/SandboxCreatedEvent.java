package tech.zerofiltre.blog.domain.sandbox.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Locale;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SandboxCreatedEvent extends SandboxEvent {
    public SandboxCreatedEvent(String appUrl, Locale locale, User user, Sandbox sandbox) {
        super(appUrl, locale, user, Action.SANDBOX_CREATED, sandbox);
    }
}
