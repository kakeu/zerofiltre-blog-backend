package tech.zerofiltre.blog.domain.user.features;

import tech.zerofiltre.blog.domain.error.ZerofiltreException;

public class InvalidTokenException extends ZerofiltreException {
    public InvalidTokenException(String message) {
        super(message);
    }
}
