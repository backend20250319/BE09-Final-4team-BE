package com.hermes.approvalservice.validation;

import com.hermes.approvalservice.dto.request.BulkCategoryOperation;
import com.hermes.approvalservice.enums.CategoryOperationType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CategoryOperationValidator implements ConstraintValidator<ValidCategoryOperation, BulkCategoryOperation> {
    
    @Override
    public boolean isValid(BulkCategoryOperation operation, ConstraintValidatorContext context) {
        if (operation == null || operation.getType() == null) {
            return false;
        }
        
        context.disableDefaultConstraintViolation();
        
        switch (operation.getType()) {
            case CREATE:
                if (operation.getCreateRequest() == null) {
                    context.buildConstraintViolationWithTemplate("CREATE 작업에는 createRequest가 필수입니다")
                            .addPropertyNode("createRequest")
                            .addConstraintViolation();
                    return false;
                }
                if (operation.getId() != null) {
                    context.buildConstraintViolationWithTemplate("CREATE 작업에는 id를 지정할 수 없습니다")
                            .addPropertyNode("id")
                            .addConstraintViolation();
                    return false;
                }
                break;
                
            case UPDATE:
                if (operation.getId() == null) {
                    context.buildConstraintViolationWithTemplate("UPDATE 작업에는 id가 필수입니다")
                            .addPropertyNode("id")
                            .addConstraintViolation();
                    return false;
                }
                if (operation.getUpdateRequest() == null) {
                    context.buildConstraintViolationWithTemplate("UPDATE 작업에는 updateRequest가 필수입니다")
                            .addPropertyNode("updateRequest")
                            .addConstraintViolation();
                    return false;
                }
                break;
                
            case DELETE:
                if (operation.getId() == null) {
                    context.buildConstraintViolationWithTemplate("DELETE 작업에는 id가 필수입니다")
                            .addPropertyNode("id")
                            .addConstraintViolation();
                    return false;
                }
                break;
        }
        
        return true;
    }
}