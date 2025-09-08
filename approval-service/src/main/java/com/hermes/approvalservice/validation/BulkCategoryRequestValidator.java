package com.hermes.approvalservice.validation;

import com.hermes.approvalservice.dto.request.BulkCategoryRequest;
import com.hermes.approvalservice.dto.request.BulkCategoryOperation;
import com.hermes.approvalservice.enums.CategoryOperationType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.HashSet;
import java.util.Set;

public class BulkCategoryRequestValidator implements ConstraintValidator<ValidBulkCategoryRequest, BulkCategoryRequest> {
    
    @Override
    public boolean isValid(BulkCategoryRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getOperations() == null) {
            return true; // null 체크는 다른 어노테이션에서 처리
        }
        
        Set<Long> processedIds = new HashSet<>();
        context.disableDefaultConstraintViolation();
        
        for (int i = 0; i < request.getOperations().size(); i++) {
            BulkCategoryOperation operation = request.getOperations().get(i);
            
            if (operation.getType() == CategoryOperationType.UPDATE || 
                operation.getType() == CategoryOperationType.DELETE) {
                
                if (operation.getId() != null) {
                    if (processedIds.contains(operation.getId())) {
                        context.buildConstraintViolationWithTemplate(
                                String.format("카테고리 ID %d에 대한 중복 작업이 발견되었습니다", operation.getId()))
                                .addPropertyNode("operations")
                                .addPropertyNode("[" + i + "]")
                                .addPropertyNode("id")
                                .addConstraintViolation();
                        return false;
                    }
                    processedIds.add(operation.getId());
                }
            }
        }
        
        return true;
    }
}