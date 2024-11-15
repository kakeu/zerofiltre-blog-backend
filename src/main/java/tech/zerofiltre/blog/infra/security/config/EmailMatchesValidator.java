package tech.zerofiltre.blog.infra.security.config;

import tech.zerofiltre.blog.infra.entrypoints.rest.user.model.EmailHolder;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class EmailMatchesValidator
        implements ConstraintValidator<EmailMatches, Object> {

    @Override
    public void initialize(EmailMatches constraintAnnotation) {
        // No need to implement
    }

    @Override
    public boolean isValid(Object obj, ConstraintValidatorContext context) {
        EmailHolder holder = (EmailHolder) obj;
        return holder.getEmail().equals(holder.getMatchingEmail());
    }
}
