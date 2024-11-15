package tech.zerofiltre.blog.infra.providers.notification.user.model;

import lombok.ToString;
import org.springframework.context.ApplicationEvent;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;

import java.util.Locale;
import java.util.UUID;

@ToString
public class ActionApplicationEvent extends ApplicationEvent {
    private final String id;
    private final String token;
    private String appUrl;
    private Locale locale;
    private User user;
    private Action action;

    public ActionApplicationEvent(User user, Locale locale, String appUrl, String token, Action action) {
        super(user);
        this.id = UUID.randomUUID().toString();
        this.user = user;
        this.locale = locale;
        this.appUrl = appUrl;
        this.token = token;
        this.action = action;
    }

    public String getAppUrl() {
        return appUrl;
    }

    public void setAppUrl(String appUrl) {
        this.appUrl = appUrl;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getToken() {
        return token;
    }

    public String getId() {
        return id;
    }

}
