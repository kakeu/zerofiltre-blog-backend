package tech.zerofiltre.blog.domain.user.features;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tech.zerofiltre.blog.domain.user.UserNotificationProvider;
import tech.zerofiltre.blog.domain.user.VerificationTokenProvider;
import tech.zerofiltre.blog.domain.user.model.Action;
import tech.zerofiltre.blog.domain.user.model.User;
import tech.zerofiltre.blog.domain.user.model.UserActionEvent;

import java.util.Locale;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class NotifyRegistrationCompleteTest {

    public static final String APP_URL = "appUrl";
    public static final String TOKEN = "token";
    NotifyRegistrationComplete notifyRegistrationComplete;

    @MockBean
    UserNotificationProvider userNotificationProvider;

    @MockBean
    VerificationTokenProvider tokenProvider;


    @BeforeEach
    void setUp() {
        notifyRegistrationComplete = new NotifyRegistrationComplete(userNotificationProvider);
    }

    @Test
    void mustBuildTheNotificationEvent_ThenNotify() {
        //ARRANGE
        doNothing().when(userNotificationProvider).notify(any());

        //ACT
        notifyRegistrationComplete.execute(new User(),TOKEN, Locale.FRANCE, APP_URL);

        //ASSERT
        ArgumentCaptor<UserActionEvent> captor = ArgumentCaptor.forClass(UserActionEvent.class);

        verify(userNotificationProvider, times(1)).notify(captor.capture());
        UserActionEvent event = captor.getValue();

        assertThat(event.getAppUrl()).isEqualTo(APP_URL);
        assertThat(event.getCurrentToken()).isEqualTo(TOKEN);
        assertThat(event.getLocale()).isEqualTo(Locale.FRANCE);
        assertThat(event.getAction()).isEqualTo(Action.REGISTRATION_COMPLETE);
    }
}