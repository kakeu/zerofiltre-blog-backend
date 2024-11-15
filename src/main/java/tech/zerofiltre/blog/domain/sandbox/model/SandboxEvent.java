package tech.zerofiltre.blog.domain.sandbox.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.UserActionEvent;

import java.util.Locale;


@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class SandboxEvent extends UserActionEvent {
    private Sandbox sandbox;

    public SandboxEvent(String appUrl, Locale locale, User user, Action action, Sandbox sandbox) {
        super(appUrl, locale, user, null, null, action);
        this.sandbox = sandbox;
    }
}
