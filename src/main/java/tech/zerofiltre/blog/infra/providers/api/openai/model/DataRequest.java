package tech.zerofiltre.blog.infra.providers.api.openai.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class DataRequest {

    private final String role;
    private final String content;
}
