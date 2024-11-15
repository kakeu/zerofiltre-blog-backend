package tech.zerofiltre.blog.infra.providers.api.openai;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.infra.InfraProperties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenAiProviderTest {

    private OpenAiProvider openAiProvider;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private InfraProperties infraProperties;

    private static String jsonResponse;

    @BeforeAll
    static void setUp() {
        jsonResponse = "{\n" +
                "  \"id\": \"chatcmpl-AAAAAAA\",\n" +
                "  \"object\": \"chat.completion\",\n" +
                "  \"created\": 111111,\n" +
                "  \"model\": \"gpt-4o-mini-2024-07-18\",\n" +
                "  \"choices\": [\n" +
                "    {\n" +
                "      \"index\": 0,\n" +
                "      \"message\": {\n" +
                "        \"role\": \"assistant\",\n" +
                "        \"content\": \"tip\",\n" +
                "        \"refusal\": null\n" +
                "      },\n" +
                "      \"logprobs\": null,\n" +
                "      \"finish_reason\": \"stop\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"usage\": {\n" +
                "    \"prompt_tokens\": 26,\n" +
                "    \"completion_tokens\": 34,\n" +
                "    \"total_tokens\": 60,\n" +
                "    \"completion_tokens_details\": {\n" +
                "      \"reasoning_tokens\": 0\n" +
                "    }\n" +
                "  },\n" +
                "  \"system_fingerprint\": \"fp_111111111111\"\n" +
                "}";
    }

    @BeforeEach
    void init() {
        openAiProvider = new OpenAiProvider(restTemplate, infraProperties);
    }

    @Test
    void given_json_when_findTipFromJson_then_return_tip() throws ZerofiltreException {
        //ACT
        String value = openAiProvider.findTipFromJson(jsonResponse);

        //ASSERT
        assertThat(value).isEqualTo("tip");
    }

    @Test
    void given_json_without_content_field_when_findTipFromJson_then_throw_ZerofiltreException() {
        //ASSERT
        assertThatExceptionOfType(ZerofiltreException.class)
                .isThrownBy(() -> openAiProvider.findTipFromJson("{\"error\":1}"));
    }

    @Test
    void given_question_when_answer_then_return_tip() throws ZerofiltreException {
        //ARRANGE
        when(infraProperties.getOpenaiApiKey()).thenReturn("key");
        when(infraProperties.getOpenaiOrganizationId()).thenReturn("orgId");
        when(infraProperties.getOpenaiProjectId()).thenReturn("projectId");
        when(infraProperties.getOpenaiUrl()).thenReturn("url");

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(ResponseEntity.ok(jsonResponse));

        //ACT
        String value = openAiProvider.answer("question");

        //ASSERT
        assertThat(value).isEqualTo("tip");
        verify(infraProperties, times(1)).getOpenaiApiKey();
        verify(infraProperties, times(1)).getOpenaiOrganizationId();
        verify(infraProperties, times(1)).getOpenaiProjectId();
        verify(infraProperties, times(1)).getOpenaiUrl();
        verify(restTemplate, times(1)).exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class));
    }

    @Test
    void given_question_when_answer_and_restTemplate_return_BadRequest_then_throw_ZerofiltreException() {
        //ARRANGE
        when(infraProperties.getOpenaiApiKey()).thenReturn("key");
        when(infraProperties.getOpenaiOrganizationId()).thenReturn("orgId");
        when(infraProperties.getOpenaiProjectId()).thenReturn("projectId");
        when(infraProperties.getOpenaiUrl()).thenReturn("url");

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        //ASSERT
        assertThatExceptionOfType(ZerofiltreException.class)
                .isThrownBy(() -> openAiProvider.answer("question"));
    }

    @Test
    void given_question_when_answer_and_restTemplate_not_return_body_then_throw_ZerofiltreException() {
        //ARRANGE
        when(infraProperties.getOpenaiApiKey()).thenReturn("key");
        when(infraProperties.getOpenaiOrganizationId()).thenReturn("orgId");
        when(infraProperties.getOpenaiProjectId()).thenReturn("projectId");
        when(infraProperties.getOpenaiUrl()).thenReturn("url");

        when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(HttpEntity.class), eq(String.class))).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        //ASSERT
        assertThatExceptionOfType(ZerofiltreException.class)
                .isThrownBy(() -> openAiProvider.answer("question"));
    }

}
