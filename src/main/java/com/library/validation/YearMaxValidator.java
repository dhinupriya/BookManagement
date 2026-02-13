package com.library.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class YearMaxValidator implements ConstraintValidator<YearMax, Integer> {

    @Override
    public boolean isValid(Integer year, ConstraintValidatorContext context) {
        if (year == null) {
            return true; // @NotNull will handle null validation
        }
        int currentYear = LocalDate.now().getYear();
        return year <= currentYear;
    }
}

