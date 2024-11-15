package tech.zerofiltre.blog.infra.providers.api.openai.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class OpenAiRequest {

    private final String model;
    private final List<DataRequest> messages;
}
