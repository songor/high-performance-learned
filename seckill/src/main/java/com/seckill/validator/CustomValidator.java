package com.seckill.validator;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import java.util.Set;

@Component
public class CustomValidator implements InitializingBean {

    private Validator validator;

    public ValidationResult validate(Object object) {
        ValidationResult result = new ValidationResult();
        Set<ConstraintViolation<Object>> constraintViolationSet = validator.validate(object);
        constraintViolationSet.forEach(constraintViolation -> {
            String propertyPath = constraintViolation.getPropertyPath().toString();
            String message = constraintViolation.getMessage();
            result.setHasErrors(true);
            result.getErrorMessageMap().put(propertyPath, message);
        });
        return result;
    }

    @Override
    public void afterPropertiesSet() {
        this.validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

}
