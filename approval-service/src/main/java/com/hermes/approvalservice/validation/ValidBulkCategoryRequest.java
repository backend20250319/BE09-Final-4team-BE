package com.hermes.approvalservice.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BulkCategoryRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBulkCategoryRequest {
    
    String message() default "벌크 요청에서 중복된 ID가 발견되었습니다";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}