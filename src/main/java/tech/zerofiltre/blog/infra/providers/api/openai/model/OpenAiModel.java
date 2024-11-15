package tech.zerofiltre.blog.infra.providers.api.openai.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OpenAiModel {
    GPT_4O_MINI("gpt-4o-mini");

    private final String value;

}
