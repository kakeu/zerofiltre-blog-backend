package tech.zerofiltre.blog.infra.providers.api.openai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import tech.zerofiltre.blog.domain.error.ZerofiltreException;
import tech.zerofiltre.blog.domain.tips.AiProvider;
import tech.zerofiltre.blog.infra.InfraProperties;
import tech.zerofiltre.blog.infra.providers.api.openai.model.DataRequest;
import tech.zerofiltre.blog.infra.providers.api.openai.model.OpenAiModel;
import tech.zerofiltre.blog.infra.providers.api.openai.model.OpenAiRequest;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiProvider implements AiProvider {

    private final RestTemplate restTemplate;
    private final InfraProperties infraProperties;

    @Override
    public String answer(String question) throws ZerofiltreException {
        log.info("Question: {}", question);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.add("Content-Type", "application/json");
        headers.add("Authorization", "Bearer " + infraProperties.getOpenaiApiKey());
        headers.add("OpenAI-Organization", infraProperties.getOpenaiOrganizationId());
        headers.add("OpenAI-Project", infraProperties.getOpenaiProjectId());

        DataRequest dataRequest = new DataRequest("user", question);
        OpenAiRequest openAiRequest = new OpenAiRequest(OpenAiModel.GPT_4O_MINI.getValue(), Collections.singletonList(dataRequest));

        HttpEntity<OpenAiRequest> requestEntity = new HttpEntity<>(openAiRequest, headers);

        ResponseEntity<String> jsonResponse = restTemplate.exchange(infraProperties.getOpenaiUrl(), HttpMethod.POST, requestEntity, String.class);

        if(jsonResponse.getStatusCode().is2xxSuccessful() && jsonResponse.hasBody()) return findTipFromJson(jsonResponse.getBody());

        throw new ZerofiltreException("The request to OpenAI returns an error or the body of the json response is empty");
    }

    String findTipFromJson(String jsonResponse) throws ZerofiltreException {
        ObjectMapper mapper = new ObjectMapper();

        JsonNode rootNode;
        try {
            rootNode = mapper.readTree(jsonResponse);
        } catch (JsonProcessingException e) {
            throw new ZerofiltreException("We could not parse the response.", e);
        }
        List<String> list = rootNode.findValuesAsText("content");

        if(!list.isEmpty()) {
            String tip = list.get(0);
            if(!tip.isEmpty()) return tip;
        }

        throw new ZerofiltreException("The json does not contain the searched key: 'content'");
    }

}
