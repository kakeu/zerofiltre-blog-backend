package tech.zerofiltre.blog.infra.providers.notification.user.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tech.zerofiltre.blog.domain.sandbox.model.Sandbox;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Locale;

@Getter
@Setter
@ToString(callSuper = true)
public class SandboxCreatedApplicationEvent extends ActionApplicationEvent {

    private Sandbox sandbox;

    public SandboxCreatedApplicationEvent(User user, Locale locale, String appUrl, Sandbox sandbox) {
        super(user, locale, appUrl, null, Action.SANDBOX_CREATED);
        this.sandbox = sandbox;
    }


}
