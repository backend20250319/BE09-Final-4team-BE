package com.hermes.userservice.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isEmpty()) {
            return false;
        }

        // 최소 8자 이상
        if (password.length() < 8) {
            return false;
        }

        // 영문 대문자 포함
        if (!password.matches(".*[A-Z].*")) {
            return false;
        }

        // 영문 소문자 포함
        if (!password.matches(".*[a-z].*")) {
            return false;
        }

        // 숫자 포함
        if (!password.matches(".*[0-9].*")) {
            return false;
        }

        // 특수문자 포함 (!@#$%^&*()_+-=[]{}|;':\",./<>?)
        if (!password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;':\",./<>?].*")) {
            return false;
        }

        return true;
    }
}
