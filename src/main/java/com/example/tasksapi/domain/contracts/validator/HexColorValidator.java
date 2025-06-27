package com.example.tasksapi.domain.contracts.validator;

import com.example.tasksapi.domain.contracts.HexColor;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class HexColorValidator implements ConstraintValidator<HexColor, String> {
    private static final String HEX_REGEX = "^#(?:[0-9a-fA-F]{3}){1,2}$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        return value != null && value.matches(HEX_REGEX);
    }
}
