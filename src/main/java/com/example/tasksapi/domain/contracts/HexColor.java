package com.example.tasksapi.domain.contracts;

import com.example.tasksapi.domain.contracts.validator.HexColorValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Constraint(validatedBy = HexColorValidator.class)
public @interface HexColor {
    String message() default "Invalid hex color, use hexadecimal format (#RRGGBB or #RGB)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
