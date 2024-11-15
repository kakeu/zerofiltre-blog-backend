package tech.zerofiltre.blog.domain.user.features;

import tech.zerofiltre.blog.domain.error.ResourceNotFoundException;

public class UserNotFoundException extends ResourceNotFoundException {
    public UserNotFoundException(String message, String resourceId) {
        super(message, resourceId);
    }
}
