package tech.zerofiltre.blog.infra.providers.tips;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.logging.MessageSourceProvider;
import tech.zerofiltre.blog.domain.tips.AiProvider;
import tech.zerofiltre.blog.domain.tips.model.Tip;

import java.time.LocalDate;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AiTipsProviderTest {

    private AiTipsProvider aiTipsProvider;

    @Mock
    private MessageSourceProvider messageSourceProvider;

    @Mock
    private AiProvider aiProvider;

    @BeforeEach
    void setUp() {
        aiTipsProvider = new AiTipsProvider(messageSourceProvider, aiProvider);
    }

    @Test
    void given_Tip_date_is_null_when_findTip_then_return_tip_by_ai() throws ZerofiltreException {
        //ARRANGE
        Tip.date = null;

        when(aiProvider.answer(anyString())).thenReturn("tip");

        //ACT
        aiTipsProvider.findTip("request");

        //ASSERT
        assertThat(Tip.tip).isEqualTo("tip");
        assertThat(Tip.date).isBeforeOrEqualTo(LocalDate.now());
        verify(aiProvider, times(1)).answer(anyString());
    }

    @Test
    void given_Tip_date_is_yesterday_when_findTip_then_return_tip_by_ai() throws ZerofiltreException {
        //ARRANGE
        Tip.date = LocalDate.now().minusDays(1);

        when(aiProvider.answer(anyString())).thenReturn("tip");

        //ACT
        aiTipsProvider.findTip("request");

        //ASSERT
        assertThat(Tip.tip).isEqualTo("tip");
        assertThat(Tip.date).isBeforeOrEqualTo(LocalDate.now());
        verify(aiProvider, times(1)).answer(anyString());
    }

    @Test
    void given_Tip_tip_is_null_and_Tip_date_is_good_when_findTip_then_return_tip_by_ai() throws ZerofiltreException {
        //ARRANGE
        Tip.tip = null;
        Tip.date = LocalDate.now();

        when(aiProvider.answer(anyString())).thenReturn("tip");

        //ACT
        aiTipsProvider.findTip("request");

        //ASSERT
        assertThat(Tip.tip).isEqualTo("tip");
        assertThat(Tip.date).isBeforeOrEqualTo(LocalDate.now());
        verify(aiProvider, times(1)).answer(anyString());
    }

    @Test
    void given_Tip_tip_is_blank_and_Tip_date_is_good_when_findTip_then_return_tip_by_ai() throws ZerofiltreException {
        //ARRANGE
        Tip.tip = " ";
        Tip.date = LocalDate.now();

        when(aiProvider.answer(anyString())).thenReturn("tip");

        //ACT
        aiTipsProvider.findTip("request");

        //ASSERT
        assertThat(Tip.tip).isEqualTo("tip");
        assertThat(Tip.date).isBeforeOrEqualTo(LocalDate.now());
        verify(aiProvider, times(1)).answer(anyString());
    }

    @Test
    void given_Tip_tip_is_good_and_Tip_date_is_today_when_findTip_then_return_tip_by_Tip() throws ZerofiltreException {
        //ARRANGE
        Tip.tip = "tip";
        Tip.date = LocalDate.now();

        //ACT
        String tip = aiTipsProvider.findTip("request");

        //ASSERT
        assertThat(tip).isEqualTo(Tip.tip);
        verify(aiProvider, times(0)).answer(anyString());
    }

    @DisplayName("")
    @Test
    void given_profession_is_empty_and_locale_and_Tip_date_is_null_when_getTip_then_question_posed_to_AI_concerns_devops_profession() throws ZerofiltreException {
        //ARRANGE
        String profession = "";
        Locale locale = Locale.FRENCH;
        Tip.date = null;

        when(messageSourceProvider.getMessage(anyString(), eq(null), any(Locale.class))).thenReturn("question ").thenReturn("Dev ou DevOps");
        when(aiProvider.answer(anyString())).thenReturn("tip");

        //ACT
        String tip = aiTipsProvider.getTip(profession, locale);

        //ASSERT
        assertThat(tip).isEqualTo("tip");
        verify(messageSourceProvider, times(2)).getMessage(anyString(), eq(null), any(Locale.class));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(aiProvider, times(1)).answer(captor.capture());
        String questionCaptor = captor.getValue();
        assertThat(questionCaptor).isEqualTo("question Dev ou DevOps");
    }

    @Test
    void given_profession_and_locale_when_getTip_then_question_posed_to_AI_concerns_user_s_profession() throws ZerofiltreException {
        //ARRANGE
        String profession = "engineer";
        Locale locale = Locale.FRENCH;

        when(messageSourceProvider.getMessage(anyString(), eq(null), any(Locale.class))).thenReturn("question ");
        when(aiProvider.answer(anyString())).thenReturn("tip");

        //ACT
        String tip = aiTipsProvider.getTip(profession, locale);

        //ASSERT
        assertThat(tip).isEqualTo("tip");
        verify(messageSourceProvider, times(1)).getMessage(anyString(), eq(null), any(Locale.class));

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(aiProvider, times(1)).answer(captor.capture());
        String questionCaptor = captor.getValue();
        assertThat(questionCaptor).isEqualTo("question " + profession);
    }
}