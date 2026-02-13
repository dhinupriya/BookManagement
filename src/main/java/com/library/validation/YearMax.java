package com.library.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = YearMaxValidator.class)
@Documented
public @interface YearMax {
    String message() default "Year must not exceed the current year";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

