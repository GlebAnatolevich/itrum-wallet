package ru.itrum.wallet.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ValidOperationTypeValidator implements ConstraintValidator<ValidOperationType, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return "DEPOSIT".equals(value) || "WITHDRAW".equals(value);
    }
}
