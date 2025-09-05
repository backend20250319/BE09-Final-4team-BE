package com.hermes.approvalservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CategoryOperationValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCategoryOperation {
    
    String message() default "작업 타입에 따른 필수 필드가 누락되었습니다";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}