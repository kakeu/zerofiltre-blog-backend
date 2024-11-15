package tech.zerofiltre.blog.infra.providers.notification.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.UserActionEvent;
import tech.zerofiltre.blog.infra.providers.notification.user.model.UserActionApplicationEvent;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class AppPublisherNotificationProviderTest {

    public static final String APP_URL = "appUrl";
    public static final String LAST_NAME = "last";
    public static final String TOKEN = "token";
    AppPublisherNotificationProvider appPublisherNotificationProvider;

    @MockBean
    ApplicationEventPublisher eventPublisher;

    User user;


    @BeforeEach
    void setUp() {
        user = new User();
        appPublisherNotificationProvider = new AppPublisherNotificationProvider(eventPublisher);
    }

    @Test
    void notifyRegistrationComplete_mustConstructEvent_ThenPublish_withProperAction() {
        //ARRANGE
        doNothing().when(eventPublisher).publishEvent(any());

        //ACT
        appPublisherNotificationProvider.notify(new UserActionEvent(
                APP_URL, Locale.FRANCE, user, TOKEN, null, Action.REGISTRATION_COMPLETE));

        //ASSERT
        ArgumentCaptor<UserActionApplicationEvent> captor = ArgumentCaptor.forClass(UserActionApplicationEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        UserActionApplicationEvent event = captor.getValue();

        assertThat(event.getAppUrl()).isEqualTo(APP_URL);
        assertThat(event.getToken()).isEqualTo(TOKEN);
        assertThat(event.getAction()).isEqualTo(Action.REGISTRATION_COMPLETE);
        assertThat(event.getLocale()).isEqualTo(Locale.FRANCE);
    }

    @Test
    void notifyResetPassword_mustConstructEvent_ThenPublish_withProperAction() {
        //ARRANGE
        doNothing().when(eventPublisher).publishEvent(any());

        //ACT
        appPublisherNotificationProvider.notify(new UserActionEvent(
                APP_URL, Locale.FRANCE, user, TOKEN, null, Action.PASSWORD_RESET));

        //ASSERT
        ArgumentCaptor<UserActionApplicationEvent> captor = ArgumentCaptor.forClass(UserActionApplicationEvent.class);
        verify(eventPublisher, times(1)).publishEvent(captor.capture());
        UserActionApplicationEvent event = captor.getValue();

        assertThat(event.getAppUrl()).isEqualTo(APP_URL);
        assertThat(event.getToken()).isEqualTo(TOKEN);
        assertThat(event.getAction()).isEqualTo(Action.PASSWORD_RESET);
        assertThat(event.getLocale()).isEqualTo(Locale.FRANCE);

    }
}