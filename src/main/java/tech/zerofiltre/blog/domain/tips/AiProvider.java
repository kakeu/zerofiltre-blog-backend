package tech.zerofiltre.blog.domain.tips;

import tech.zerofiltre.blog.domain.error.ZerofiltreException;

public interface AiProvider {

    String answer(String question) throws ZerofiltreException;
}
